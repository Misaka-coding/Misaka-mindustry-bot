package uwu.misaka.bot;

import arc.files.Fi;
import arc.files.ZipFi;
import arc.util.Strings;
import arc.util.io.Streams;
import arc.util.serialization.Jval;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedData;
import discord4j.rest.entity.RestEmoji;
import discord4j.rest.util.Permission;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.mod.Mods;
import mindustry.type.ItemStack;

import discord4j.rest.util.Color;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Handler {
    public static void handle(Message msg) {
        if (msg.getContent().startsWith("+канал ")) {
            String m = msg.getContent();
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
                default -> msg.getChannel().block().createEmbed(e->
                    e.setTitle("Канал данного типа не поддерживается.").setFooter("Доступные каналы:\nбот\nкарты\nсхемы\nмоды","").setColor(discord4j.rest.util.Color.of(255,0,0))).block();
            }
        }

        if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel != 0 && msg.getGuild().block().getId().asLong() != DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel) {
            return;
        }
        if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel == 0 && msg.getContent().startsWith("+")) {
            msg.getChannel().block().createEmbed(e->e.setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды","").setColor(discord4j.rest.util.Color.of(255,0,0))).block();
            return;
        }
        if (msg.getContent().equalsIgnoreCase("+помощь")||msg.getContent().equalsIgnoreCase("+памагити")||msg.getContent().equalsIgnoreCase("+хелп")||msg.getContent().equalsIgnoreCase("+help")) {
            msg.getChannel().block().createEmbed(e->
            e.setTitle("ПОМОЩЬ")
            .setColor(discord4j.rest.util.Color.of(0,255,0))
            .addField("+канал <тип канала>","установка каналов для контента. если канал не установлен, вывод в бот канал\nДоступно для: \nРоль с правами на управление каналами\nАдминистраторы бота(только бот канал)",false)
            .addField("+помощь","помощь",false)
            .addField("Преобразование контента","Моды, плагины, карты, схемы преобразуются при отправке их в бот канал. Если канал типа контента существует, отправка туда, инначе в бот канал",false)
            .addField("+ава <упоменание/айди>","Совлерские фокусы с аватарками",false)
            .addField("+хентай","Совлерские фокусы с хентаем | Только для администраторов сервера и администраторов бота | только в каналах с меткой nsfw | не работает",false)).block();
            return;
        }
        if(msg.getContent().startsWith("+ава")){
            if(msg.getUserMentionIds().size()>0){
                msg.getChannel().block().createMessage(((User)msg.getUserMentionIds().toArray()[0]).getAvatarUrl()).block();
                return;
            }else{
                if(msg.getContent().length()>5){
                msg.getChannel().block().createMessage(msg.getGuild().block().getMemberById(Snowflake.of(msg.getContent().substring(5))).block().getAvatarUrl()).block();
                return;
                }
            }
            msg.getChannel().block().createEmbed(e->e.setTitle("Участник не найден.").setColor(Color.of(255,0,0))).block();
        }

        if (msg.getAttachments().size() != 1) {
            return;
        }

        Attachment a = (Attachment) msg.getAttachments().toArray()[0];

        if (a.getFilename().endsWith(".msch")) {
            if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel == 0) {
                msg.getChannel().block().createEmbed(e->e.setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды","").setColor(discord4j.rest.util.Color.of(255,0,0))).block();
                return;
            }
            parseSchematic(msg);
            return;
        }
        if (a.getFilename().endsWith(".zip") || a.getFilename().endsWith(".jar")) {
            if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel == 0) {
                msg.getChannel().block().createEmbed(e->e.setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды","").setColor(discord4j.rest.util.Color.of(255,0,0))).block();
                return;
            }
            parseModPlugin(msg);
            return;
        }
        if (a.getFilename().endsWith(".msav")) {
            if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel == 0) {
                msg.getChannel().block().createEmbed(e->e.setTitle("Бот канал не установлен.").setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды","").setColor(discord4j.rest.util.Color.of(255,0,0))).block();
                return;
            }
            parseMap(msg);
        }
    }

    public static void parseMap(Message msg) {
        try {
            Attachment a = (Attachment) msg.getAttachments().toArray()[0];
            ContentParser.Map map =  Ohayo.parser.readMap(ContentParser.download(a.getUrl()));
            File mapFile = new File(map.name.replaceAll(" ", "_") + ".msav");
            File imageFile = new File("image_output.png");
            Streams.copy(ContentParser.download(a.getUrl()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile);
            EmbedCreateSpec builder = new EmbedCreateSpec();
            builder.setColor(Color.of(0,255,0));
            builder.setImage("attachment://" + imageFile.getName());
            builder.setAuthor(msg.getAuthor().get().getUsername(), msg.getAuthor().get().getAvatarUrl(), msg.getAuthor().get().getAvatarUrl());
            builder.setTitle(map.name == null ? a.getFilename().replace(".msav", "") : map.name);
            if (map.author.length() > 3) builder.addField("Автор: ", map.author, true);
            if (map.description.length() > 3) builder.addField("Описание: ", map.description, true);
            DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().block().getId().asLong());
            if (c.schematicsChannel == 0) {
                msg.getChannel().block().sendFile(mapFile).addFile(imageFile).embed(builder.build()).queue();
            } else {
                Objects.requireNonNull(msg.getGuild().block().getChannelById(Snowflake.of(c.mapsChannel)).block()).sendFile(mapFile).addFile(imageFile).embed(builder.build()).queue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseSchematic(Message msg) {
        try {
            Schematic schema =  Ohayo.parser.parseSchematic(((Attachment)msg.getAttachments().toArray()[0]).getUrl());
            BufferedImage preview =  Ohayo.parser.previewSchematic(schema);
            File previewFile = new File("img_" + "shema" + ".png");
            File schemaFile = new File(schema.name().replaceAll(" ", "_") + "." + "msch");
            Schematics.write(schema, new Fi(schemaFile));
            ImageIO.write(preview, "png", previewFile);
            EmbedCreateSpec builder = new EmbedCreateSpec();
            builder.setColor(Color.of(0,255,0)));
            builder.setAuthor(msg.getAuthor().get().getUsername(), msg.getAuthor().get().getAvatarUrl(), msg.getAuthor().get().getAvatarUrl());
            builder.setTitle(schema.name());
            builder.setImage("attachment://" + previewFile.getName());
            if (msg.getContent().length() > 3) {
                builder.addField("От " + msg.getAuthor().get().getUsername(), msg.getContent(), false);
            }
            StringBuilder field = new StringBuilder();
            for (ItemStack stack : schema.requirements()) {
                java.util.List<GuildEmoji> emotes = new ArrayList<>();
                java.util.List<GuildEmoji> finalEmotes = emotes;
                msg.getGuild().block().getEmojis().all(a -> {if(a.getName().equals(stack.item.name.replace("-", ""))){
                    finalEmotes.add(a);}return false;}
                );
                GuildEmoji result;
                try {
                    result = emotes.isEmpty() ? msg.getGuild().block().getEmojis().filter(a->a.getName().startsWith("ohno")).blockFirst() : emotes.get(0);
                } catch (Exception e) {
                    emotes.add(Objects.requireNonNull(Ohayo.gateway.getGuildById(Snowflake.of(Ohayo.botBaseServer)).block()).getEmojis().filter(a -> a.getName().equals(stack.item.name.replace("-", ""))).blockFirst());
                    result = emotes.isEmpty() ? Objects.requireNonNull(Ohayo.gateway.getGuildById(Snowflake.of(Ohayo.botBaseServer)).block()).getEmojis().filter(a->a.getName().startsWith("ohno")).blockFirst() : emotes.get(0);
                }
                field.append(result.asFormat()).append(stack.amount).append("  ");
            }
            builder.addField("Требуемые Ресурсы", field.toString(), false);
            if (schema.description().length() > 3) {
                builder.addField("Описание", schema.description(), false);
            }
            builder.addField("Потребление энергии: ", (int) schema.powerConsumption() * 40 + "", true);
            builder.addField("Производство энергии: ", (int) schema.powerProduction() * 40 + "", true);

            DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().block().getId().asLong());
            if (c.schematicsChannel == 0) {
                msg.getChannel().sendFile(schemaFile).addFile(previewFile).embed(builder.build()).queue();
            } else {
                Objects.requireNonNull(msg.getGuild().block().getChannelById(Snowflake.of(c.schematicsChannel)).block()).sendFile(schemaFile).addFile(previewFile).embed(builder.build()).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseModPlugin(Message msg) {
        if (new File("mods/").mkdir()) {
            System.out.print(" ");
        }
        File modFile = new File("mods/" + ((Attachment)msg.getAttachments().toArray()[0]).getFilename());
        try {
            Streams.copy(ContentParser.download(((Attachment)msg.getAttachments().toArray()[0]).getUrl()), new FileOutputStream(modFile));
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
            msg.getChannel().block().createEmbed(e->e.setTitle("Мета данные не найдены.").setColor(Color.of(255,0,0))).block();
            return;
        }
        boolean isPlugin = metaf.name().startsWith("plugin");
        Mods.ModMeta meta = Ohayo.gson.fromJson(Jval.read(metaf.readString()).toString(Jval.Jformat.plain),Mods.ModMeta.class);
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
        EmbedCreateSpec builder = new EmbedCreateSpec();
        builder.setColor(Color.of(0,255,0));
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
            if (((Attachment) msg.getAttachments().toArray()[0]).getFilename().endsWith(".jar")) {
                builder.addField("Внимание!", "Java модификации не всегда безопасны", false);
            }
        }
        if (msg.getContent().length() > 3) {
            builder.addField("От " + msg.getAuthor().get().getUsername(), msg.getContent(), false);
        }
        builder.setAuthor(msg.getAuthor().get().getUsername(), msg.getAuthor().get().getAvatarUrl(), msg.getAuthor().get().getAvatarUrl());
        DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().block().getId().asLong());
        if(zip.child("icon.png").exists()){
        if (c.schematicsChannel == 0) {
            msg.getChannel().sendFile(modFile).addFile(new File("mods/icon.png")).embed(builder.build()).queue();
        } else {
            Objects.requireNonNull(msg.getGuild().block().getChannelById(Snowflake.of(c.modsChannel)).sendFile(modFile).addFile(new File("mods/icon.png")).embed(builder.build()).queue();
        }
        }else{
            if (c.schematicsChannel == 0) {
                msg.getChannel().sendFile(modFile).embed(builder.build()).queue();
            } else {
                Objects.requireNonNull(msg.getGuild().block().getChannelById(Snowflake.of(c.modsChannel)).block()).sendFile(modFile).embed(builder.build()).queue();
            }
        }
    }

    public static void setBotChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().block().getMemberById(msg.getAuthor().get().getId())).block().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS) || msg.getAuthor().get().getId().asLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel = msg.getChannel().block().getId().asLong();
            msg.getChannel().block().createEmbed(e->e.setTitle("Канал успешно установлен.").setColor(Color.of(0,0,255))).block();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().block().createEmbed(e->e.setTitle("Недостаточно полномочий.").setColor(Color.of(255,0,0))).block();

    }

    public static void setMapChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().block().getMemberById(msg.getAuthor().get().getId())).block().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS) || msg.getAuthor().get().getId().asLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).mapsChannel = msg.getChannel().block().getId().asLong();
            msg.getChannel().block().createEmbed(e->e.setTitle("Канал успешно установлен.").setColor(Color.of(0,0,255))).block();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().block().createEmbed(e->e.setTitle("Недостаточно полномочий.").setColor(Color.of(255,0,0))).block();

    }

    public static void setSchematicChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().block().getMemberById(msg.getAuthor().get().getId())).block().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS) || msg.getAuthor().get().getId().asLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).schematicsChannel = msg.getChannel().block().getId().asLong();
            msg.getChannel().block().createEmbed(e->e.setTitle("Канал успешно установлен.").setColor(Color.of(0,0,255))).block();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().block().createEmbed(e->e.setTitle("Недостаточно полномочий.").setColor(Color.of(255,0,0))).block();

    }

    public static void setModsChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().block().getMemberById(msg.getAuthor().get().getId())).block().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS) || msg.getAuthor().get().getId().asLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).modsChannel = msg.getChannel().block().getId().asLong();
            msg.getChannel().block().createEmbed(e->e.setTitle("Канал успешно установлен.").setColor(Color.of(0,0,255))).block();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().block().createEmbed(e->e.setTitle("Недостаточно полномочий.").setColor(Color.of(255,0,0))).block();
    }
}
