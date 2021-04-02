package uwu.misaka.bot;

import arc.files.Fi;
import arc.files.ZipFi;
import arc.util.Strings;
import arc.util.io.Streams;
import arc.util.serialization.Jval;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.mod.Mods;
import mindustry.type.ItemStack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class Handler {
    public static void handle(Message msg) {
        if (msg.getContentRaw().startsWith("+канал ")) {
            String m = msg.getContentRaw();
            if (m.length() < 8) return;
            switch (m.substring(7)) {
                case "бот" -> {
                    setBotChannel(msg);
                    return;
                }
                case "карты" -> {
                    setMapChannel(msg);
                    return;
                }
                case "схемы" -> {
                    setSchematicChannel(msg);
                    return;
                }
                case "моды" -> {
                    setModsChannel(msg);
                    return;
                }
                default -> msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал данного типа не поддерживается.").setFooter("Доступные каналы:\nбот\nкарты\nсхемы\nмоды").setColor(Color.decode("#FF0000")).build()).queue();
            }
        }

        if (DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel != 0 && msg.getChannel().getIdLong() != DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel) {
            return;
        }
        if (DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel == 0 && msg.getContentRaw().startsWith("+")) {
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды").setColor(Color.decode("#FF0000")).build()).queue();
            return;
        }
        if (msg.getContentRaw().equalsIgnoreCase("+помощь")||msg.getContentRaw().equalsIgnoreCase("+памагити")||msg.getContentRaw().equalsIgnoreCase("+хелп")||msg.getContentRaw().equalsIgnoreCase("+help")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("ПОМОЩЬ");
            embedBuilder.setColor(Color.decode("#00FF00"));
            embedBuilder.addField("+канал <тип канала>","установка каналов для контента. если канал не установлен, вывод в бот канал\nДоступно для: \nРоль с правами на управление каналами\nАдминистраторы бота(только бот канал)",false);
            embedBuilder.addField("+помощь","помощь",false);
            embedBuilder.addField("Преобразование контента","Моды, плагины, карты, схемы преобразуются при отправке их в бот канал. Если канал типа контента существует, отправка туда, инначе в бот канал",false);
            embedBuilder.addField("+ава <упоменание/айди>","Совлерские фокусы с аватарками",false);
            embedBuilder.addField("+хентай","Совлерские фокусы с хентаем | Только для администраторов сервера и администраторов бота | только в каналах с меткой nsfw | не работает",false);
            msg.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }
        if(msg.getContentRaw().startsWith("+ава")){
            if(msg.getMentions().size()>0){
                msg.getChannel().sendMessage(Ichi.botCore.getUserById(msg.getMentions().get(0).getIdLong()).getAvatarUrl()).queue();
                return;
            }else{
                if(msg.getContentRaw().length()>5){
                msg.getChannel().sendMessage(Ichi.botCore.getUserById(msg.getContentRaw().substring(5)).getAvatarUrl()).queue();
                return;
                }
            }
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Участник не найден.").setColor(Color.decode("#FF0000")).build()).queue();
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
        }
    }

    public static void parseMap(Message msg) {
        try {
            Message.Attachment a = msg.getAttachments().get(0);
            ContentParser.Map map = Ichi.parser.readMap(ContentParser.download(a.getUrl()));
            File mapFile = new File(map.name.replaceAll(" ", "_") + ".msav");
            File imageFile = new File("image_output.png");
            Streams.copy(ContentParser.download(a.getUrl()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.decode("#00FF00"));
            builder.setImage("attachment://" + imageFile.getName());
            builder.setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatarUrl(), msg.getAuthor().getAvatarUrl());
            builder.setTitle(map.name == null ? a.getFileName().replace(".msav", "") : map.name);
            if (map.author.length() > 3) builder.addField("Автор: ", map.author, true);
            if (map.description.length() > 3) builder.addField("Описание: ", map.description, true);
            DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().getIdLong());
            if (c.schematicsChannel == 0) {
                msg.getChannel().sendFile(mapFile).addFile(imageFile).embed(builder.build()).queue();
            } else {
                Objects.requireNonNull(Ichi.botCore.getTextChannelById(c.schematicsChannel)).sendFile(mapFile).addFile(imageFile).embed(builder.build()).queue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseSchematic(Message msg) {
        try {
            Schematic schema = Ichi.parser.parseSchematic(msg.getAttachments().get(0).getUrl());
            BufferedImage preview = Ichi.parser.previewSchematic(schema);
            File previewFile = new File("img_" + "shema" + ".png");
            File schemaFile = new File(schema.name().replaceAll(" ", "_") + "." + "msch");
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
            for (ItemStack stack : schema.requirements()) {
                java.util.List<Emote> emotes = Objects.requireNonNull(Ichi.botCore.getGuildById(Ichi.botBaseServer)).getEmotesByName(stack.item.name.replace("-", ""), true);
                Emote result = emotes.isEmpty() ? msg.getGuild().getEmotesByName("ohno", true).get(0) : emotes.get(0);
                field.append(result.getAsMention()).append(stack.amount).append("  ");
            }
            builder.addField("ребуемые Ресурсы", field.toString(), false);
            if (schema.description().length() > 3) {
                builder.addField("Описание", schema.description(), false);
            }
            builder.addField("Потребление энергии: ", schema.powerConsumption() + "", true);
            builder.addField("Производство энергии: ", schema.powerProduction() + "", true);

            DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().getIdLong());
            if (c.schematicsChannel == 0) {
                msg.getChannel().sendFile(schemaFile).addFile(previewFile).embed(builder.build()).queue();
            } else {
                Objects.requireNonNull(Ichi.botCore.getTextChannelById(c.schematicsChannel)).sendFile(schemaFile).addFile(previewFile).embed(builder.build()).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseModPlugin(Message msg) {
        if (new File("mods/").mkdir()) {
            System.out.print(" ");
        }
        File modFile = new File("mods/" + msg.getAttachments().get(0).getFileName());
        try {
            Streams.copy(ContentParser.download(msg.getAttachments().get(0).getUrl()), new FileOutputStream(modFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Fi zip = modFile.isDirectory() ? new Fi(modFile) : new ZipFi(new Fi(modFile));
        if (zip.list().length == 1 && zip.list()[0].isDirectory()) {
            zip = zip.list()[0];
        }
        Fi metaf = zip.child("mod.json").exists() ? zip.child("mod.json") :
                zip.child("mod.hjson").exists() ? zip.child("mod.hjson") :
                        zip.child("plugin.json").exists() ? zip.child("plugin.json") :
                                zip.child("plugin.hjson");
        if (!metaf.exists()) {
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Мета данные не найдены.").setColor(Color.decode("#FF0000")).build()).queue();
            return;
        }
        boolean isPlugin = metaf.name().startsWith("plugin");
        Mods.ModMeta meta = Ichi.json.fromJson(Mods.ModMeta.class, Jval.read(metaf.readString()).toString(Jval.Jformat.plain));
        meta.cleanup();
        BufferedImage image = null;
        if(zip.child("icon.png").exists()){
            try {
                image = ImageIO.read(zip.child("icon.png").read());
                File imageF = new File("mods/icon.png");
                ImageIO.write(image, "png", imageF);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#00FF00"));
        if(image!=null){
            builder.setImage("attachment://icon.png");
        }
        builder.setTitle(meta.displayName());
        builder.addField("Автор:", Strings.stripColors(meta.author),true);
        builder.addField("Описание:", Strings.stripColors(meta.description),true);
        builder.addField("Минимальная версия:", Strings.stripColors(meta.minGameVersion),true);
        if(isPlugin){
            builder.addField("Тип модификации:","плагин",true);
        }else{
            builder.addField("Тип модификации:","мод",true);
            if(meta.java){
                builder.addField("Внимание!","Java модификации не всегда безопасны",false);
            }
        }
        if (msg.getContentRaw().length() > 3) {
            builder.addField("От " + msg.getAuthor().getName(), msg.getContentRaw(), false);
        }
        builder.setAuthor(msg.getAuthor().getName(), msg.getJumpUrl(), msg.getAuthor().getAvatarUrl());
        DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().getIdLong());
        if(zip.child("icon.png").exists()){
        if (c.schematicsChannel == 0) {
            msg.getChannel().sendFile(modFile).addFile(new File("mods/icon.png")).embed(builder.build()).queue();
        } else {
            Objects.requireNonNull(Ichi.botCore.getTextChannelById(c.modsChannel)).sendFile(modFile).addFile(new File("mods/icon.png")).embed(builder.build()).queue();
        }
        }else{
            if (c.schematicsChannel == 0) {
                msg.getChannel().sendFile(modFile).embed(builder.build()).queue();
            } else {
                Objects.requireNonNull(Ichi.botCore.getTextChannelById(c.modsChannel)).sendFile(modFile).embed(builder.build()).queue();
            }
        }
    }

    public static void setBotChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().getMember(msg.getAuthor())).hasPermission(Permission.MANAGE_CHANNEL) || msg.getAuthor().getIdLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().getIdLong()).botChannel = msg.getChannel().getIdLong();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.").setColor(Color.decode("#0000FF")).build()).queue();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(Color.decode("#FF0000")).build()).queue();

    }

    public static void setMapChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().getMember(msg.getAuthor())).hasPermission(Permission.MANAGE_CHANNEL)) {
            DiscordServerConfig.get(msg.getGuild().getIdLong()).mapsChannel = msg.getChannel().getIdLong();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.").setColor(Color.decode("#0000FF")).build()).queue();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(Color.decode("#FF0000")).build()).queue();

    }

    public static void setSchematicChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().getMember(msg.getAuthor())).hasPermission(Permission.MANAGE_CHANNEL)) {
            DiscordServerConfig.get(msg.getGuild().getIdLong()).schematicsChannel = msg.getChannel().getIdLong();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.").setColor(Color.decode("#0000FF")).build()).queue();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(Color.decode("#FF0000")).build()).queue();

    }

    public static void setModsChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().getMember(msg.getAuthor())).hasPermission(Permission.MANAGE_CHANNEL)) {
            DiscordServerConfig.get(msg.getGuild().getIdLong()).modsChannel = msg.getChannel().getIdLong();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.").setColor(Color.decode("#0000FF")).build()).queue();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(Color.decode("#FF0000")).build()).queue();
    }
}
