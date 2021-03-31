package uwu.misaka.bot;

import arc.files.Fi;
import arc.util.io.Streams;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.type.ItemStack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Handler {
    public static void handle(Message msg) {
        if(DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel!=0&&msg.getChannel().getIdLong()!=DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel){return;}
        if (DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel == 0 && msg.getContentRaw().startsWith("+") && !msg.getContentRaw().startsWith("+канал")) {
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды").setColor(Color.decode("#FF0000")).build()).queue();
            return;
        }
        if (msg.getContentRaw().equalsIgnoreCase("+помощь")) {

            return;
        }

        if(msg.getContentRaw().startsWith("+канал ")){
            String m = msg.getContentRaw();
            if(m.length()<8)return;
            switch (m.substring(7)){
                case "бот":
                    setBotChannel(msg);
                    return;
                case "карты":
                    setMapChannel(msg);
                    return;
                case "схемы":
                    setSchematicChannel(msg);
                    return;
                case "моды":
                    setModsChannel(msg);
                    return;
                default:
                    msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал данного типа не поддерживается.").setFooter("Доступные каналы:\nбот\nкарты\nсхемы\nмоды").setColor(Color.decode("#FF0000")).build()).queue();
            }
        }

        if (msg.getAttachments().size() != 1) {
            return;
        }

        Message.Attachment a = msg.getAttachments().get(0);

        if (a.getFileName().endsWith(".msch")) {
            if (DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel == 0) {
                msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды").setColor(Color.decode("#FF0000")).build()).queue();
                return;
            }
            parseSchematic(msg);
            return;
        }
        if (a.getFileName().endsWith(".zip") || a.getFileName().endsWith(".jar")) {
            if (DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel == 0) {
                msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды").setColor(Color.decode("#FF0000")).build()).queue();
                return;
            }
            parseModPlugin(msg);
            return;
        }
        if (a.getFileName().endsWith(".msav")) {
            if (DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel == 0) {
                msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды").setColor(Color.decode("#FF0000")).build()).queue();
                return;
            }
            parseMap(msg);
            return;
        }
    }

    public static void parseMap(Message msg) {
        try {
            Message.Attachment a = msg.getAttachments().get(0);
            ContentParser.Map map = Ichi.parser.readMap(Ichi.parser.download(a.getUrl()));
            File mapFile = new File(map.name.replaceAll(" ","_")+".msav");
            File imageFile = new File("image_output.png");
            Streams.copy(Ichi.parser.download(a.getUrl()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.decode("#00FF00"));
            builder.setImage("attachment://" + imageFile.getName());
            builder.setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatarUrl(), msg.getAuthor().getAvatarUrl());
            builder.setTitle(map.name == null ? a.getFileName().replace(".msav", "") : map.name);
            if (map.author.length()>3) builder.addField("Автор: ",map.author,true);
            if (map.description.length()>3) builder.addField("Описание: ",map.description,true);
            DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().getIdLong());
            if(c.schematicsChannel==0){
                msg.getChannel().sendFile(mapFile).addFile(imageFile).embed(builder.build()).queue();
            }else{
                Ichi.botCore.getTextChannelById(c.schematicsChannel).sendFile(mapFile).addFile(imageFile).embed(builder.build()).queue();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseSchematic(Message msg) {
        try {
            Schematic schema = Ichi.parser.parseSchematic(msg.getAttachments().get(0).getUrl());
            BufferedImage preview = Ichi.parser.previewSchematic(schema);
            File previewFile = new File("img_" + "shema" + ".png");
            File schemaFile = new File(schema.name().replaceAll(" ","_") + "." + "msch");
            Schematics.write(schema, new Fi(schemaFile));
            ImageIO.write(preview, "png", previewFile);
            EmbedBuilder builder = new EmbedBuilder().setColor(Color.decode("#00FF00"));
            builder.setAuthor(msg.getAuthor().getName(), msg.getJumpUrl(), msg.getAuthor().getAvatarUrl());
            builder.setTitle(schema.name());
            builder.setImage("attachment://" + previewFile.getName());
            if (msg.getContentRaw().length() > 3) {
                builder.addField("От " + msg.getAuthor().getName(), msg.getContentRaw(), false);
            }
            StringBuilder field = new StringBuilder();
            for(ItemStack stack : schema.requirements()){
                java.util.List<Emote> emotes = Ichi.botCore.getGuildById(Ichi.botBaseServer).getEmotesByName(stack.item.name.replace("-", ""), true);
                Emote result = emotes.isEmpty() ? msg.getGuild().getEmotesByName("ohno", true).get(0) : emotes.get(0);
                field.append(result.getAsMention()).append(stack.amount).append("  ");
            }
            builder.addField("ребуемые Ресурсы", field.toString(), false);
            if(schema.description().length()>3){
            builder.addField("Описание", schema.description(), false);
            }
            builder.addField("Потребление энергии: ", schema.powerConsumption()+"", true);
            builder.addField("Производство энергии: ", schema.powerProduction()+"", true);

            DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().getIdLong());
            if(c.schematicsChannel==0){
                msg.getChannel().sendFile(schemaFile).addFile(previewFile).embed(builder.build()).queue();
            }else{
                Ichi.botCore.getTextChannelById(c.schematicsChannel).sendFile(schemaFile).embed(builder.build()).queue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseModPlugin(Message msg) {

    }

    public static void setBotChannel(Message msg){
        if(msg.getGuild().getMember(msg.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL)){
            DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel=msg.getChannel().getIdLong();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.").setColor(Color.decode("#0000FF")).build()).queue();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(Color.decode("#FF0000")).build()).queue();

    }
    public static void setMapChannel(Message msg){
        if(msg.getGuild().getMember(msg.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL)){
            DiscordServerConfig.get(msg.getGuild().getIdLong()).mapsChannel=msg.getChannel().getIdLong();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.").setColor(Color.decode("#0000FF")).build()).queue();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(Color.decode("#FF0000")).build()).queue();

    }
    public static void setSchematicChannel(Message msg){
        if(msg.getGuild().getMember(msg.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL)){
            DiscordServerConfig.get(msg.getGuild().getIdLong()).schematicsChannel=msg.getChannel().getIdLong();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.").setColor(Color.decode("#0000FF")).build()).queue();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(Color.decode("#FF0000")).build()).queue();

    }
    public static void setModsChannel(Message msg){
        if(msg.getGuild().getMember(msg.getAuthor()).hasPermission(Permission.MANAGE_CHANNEL)){
            DiscordServerConfig.get(msg.getGuild().getIdLong()).modsChannel=msg.getChannel().getIdLong();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.").setColor(Color.decode("#0000FF")).build()).queue();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(Color.decode("#FF0000")).build()).queue();
    }
}
