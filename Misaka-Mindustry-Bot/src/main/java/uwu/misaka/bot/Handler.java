package uwu.misaka.bot;

import arc.files.Fi;
import arc.files.ZipFi;
import arc.util.Strings;
import arc.util.io.Streams;
import arc.util.serialization.Jval;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.*;
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Handler {
    public static void handle(Message msg) {
        MessageChannel channel = msg.getChannel().block();
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
                default -> channel.createEmbed(e-> e.setTitle("Канал данного типа не поддерживается.")
                        .setFooter("Доступные каналы:\nбот\nкарты\nсхемы\nмоды","")
                        .setColor(Color.of(255,0,0))).block();
            }
        }

        if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel != 0 &&
                msg.getChannel().block().getId().asLong() != DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel) {
            return;
        }
        if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel == 0 && msg.getContent().startsWith("+")) {
            channel.createEmbed(e->e.setTitle("Бот канал не установлен.")
                    .setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды", null)
                    .setColor(Color.of(255,0,0))).block();
            return;
        }
        if (msg.getContent().equalsIgnoreCase("+помощь") ||
                msg.getContent().equalsIgnoreCase("+памагити") ||
                msg.getContent().equalsIgnoreCase("+хелп") ||
                msg.getContent().equalsIgnoreCase("+help")) {
            channel.createEmbed(e-> e.setTitle("ПОМОЩЬ")
                    .setColor(Color.of(0,255,0))
                    .addField("+канал <тип канала>", "установка каналов для контента. если канал не установлен, вывод в бот канал\nДоступно для: \nРоль с правами на управление каналами\nАдминистраторы бота(только бот канал)",false)
                    .addField("+помощь","помощь",false)
                    .addField("Преобразование контента","Моды, плагины, карты, схемы преобразуются при отправке их в бот канал. Если канал типа контента существует, отправка туда, инначе в бот канал",false)
                    .addField("+ава <упоменание/айди>","Совлерские фокусы с аватарками",false)
                    .addField("+хентай","Совлерские фокусы с хентаем | Только для администраторов сервера и администраторов бота | только в каналах с меткой nsfw | не работает",false))
                    .block();
            return;
        }
        if(msg.getContent().startsWith("+ава")){
            if(msg.getUserMentionIds().size()>0){
                channel.createMessage(msg.getGuild().block().getMemberById(((Snowflake)msg.getUserMentionIds().toArray()[0])).block().getAvatarUrl()).block();
                return;
            }else{
                if(msg.getContent().length()>5){
                    channel.createMessage(msg.getGuild().block().getMemberById(Snowflake.of(msg.getContent().substring(5))).block().getAvatarUrl()).block();
                    return;
                }
            }
            channel.createEmbed(e->e.setTitle("Участник не найден.").setColor(Color.of(255,0,0))).block();
        }

        if (msg.getAttachments().size() != 1) {
            return;
        }

        Attachment a = (Attachment) msg.getAttachments().toArray()[0];

        if (a.getFilename().endsWith(".msch")) {
            if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel == 0) {
                channel.createEmbed(e->e.setTitle("Бот канал не установлен.")
                        .setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды",null)
                        .setColor(Color.of(255,0,0))).block();
                return;
            }
            parseSchematic(msg);
            return;
        }
        if (a.getFilename().endsWith(".zip") || a.getFilename().endsWith(".jar")) {
            if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel == 0) {
                channel.createEmbed(e->e.setTitle("Бот канал не установлен.")
                        .setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды",null)
                        .setColor(Color.of(255,0,0))).block();
                return;
            }
            parseModPlugin(msg);
            return;
        }
        if (a.getFilename().endsWith(".msav")) {
            if (DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel == 0) {
                channel.createEmbed(e->e.setTitle("Бот канал не установлен.")
                        .setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды",null)
                        .setColor(Color.of(255,0,0))).block();
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

            FileInputStream inputStream1;
            try{
                inputStream1 = new FileInputStream(mapFile);
            }catch(FileNotFoundException e){
                throw new RuntimeException(e);
            }

            FileInputStream inputStream2;
            try{
                inputStream2 = new FileInputStream(imageFile);
            }catch(FileNotFoundException e){
                throw new RuntimeException(e);
            }

            if (c.schematicsChannel == 0) {
                msg.getChannel().block().createMessage(spec -> spec.addFile(mapFile.getName(), inputStream1)
                        .addFile(imageFile.getName(), inputStream2)
                        .setEmbed(embed -> embed.from(builder.asRequest())))
                        .block();
            } else {
                msg.getGuild().block().getChannelById(Snowflake.of(c.mapsChannel)).cast(GuildMessageChannel.class).block()
                        .createMessage(spec -> spec.addFile(mapFile.getName(), inputStream1)
                                .addFile(imageFile.getName(), inputStream2)
                                .setEmbed(embed -> embed.from(builder.asRequest())))
                        .block();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseSchematic(Message msg) {
        try {
            Schematic schema =  Ohayo.parser.parseSchematic(((Attachment)msg.getAttachments().toArray()[0]).getUrl());
            BufferedImage preview =  Ohayo.parser.previewSchematic(schema);
            Fi previewFile = Fi.get("img_shema.png");
            Fi schemaFile = Fi.get(schema.name().replaceAll(" ", "_") + ".msch");
            Schematics.write(schema, schemaFile);
            ImageIO.write(preview, "png", previewFile.file());
            EmbedCreateSpec builder = new EmbedCreateSpec();
            builder.setColor(Color.of(0,255,0));
            builder.setAuthor(msg.getAuthor().get().getUsername(), msg.getAuthor().get().getAvatarUrl(), msg.getAuthor().get().getAvatarUrl());
            builder.setTitle(schema.name());
            builder.setImage("attachment://" + previewFile.name());
            if (msg.getContent().length() > 3) {
                builder.addField("От " + msg.getAuthor().get().getUsername(), msg.getContent(), false);
            }
            StringBuilder field = new StringBuilder();
            for (ItemStack stack : schema.requirements()) {
                List<GuildEmoji> emotes = new ArrayList<>();
                msg.getGuild().block().getEmojis().filter(a -> a.getName().equals(stack.item.name.replace("-", "")))
                        .doOnNext(emotes::add).then()
                        .block();
                GuildEmoji result;
                try {
                    result = emotes.isEmpty() ? msg.getGuild().block().getEmojis().filter(a->a.getName().startsWith("oh")).blockFirst() : emotes.get(0);
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
                msg.getChannel().block().createMessage(spec -> spec.addFile(schemaFile.name(), schemaFile.read())
                        .addFile(previewFile.name(), previewFile.read())
                        .setEmbed(embed -> embed.from(builder.asRequest())))
                        .block();
            } else {
                msg.getGuild().block().getChannelById(Snowflake.of(c.schematicsChannel)).cast(GuildMessageChannel.class).block()
                        .createMessage(spec -> spec.addFile(schemaFile.name(), schemaFile.read())
                                .addFile(previewFile.name(), previewFile.read())
                                .setEmbed(embed -> embed.from(builder.asRequest())))
                        .block();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseModPlugin(Message msg) {
        User user = msg.getAuthor().orElse(null);
        if (user == null){
            return;
        }

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
            msg.getChannel().block().createEmbed(e->e.setTitle("Мета данные не найдены.")
                    .setColor(Color.of(255,0,0))).block();
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
            builder.addField("От " + user.getUsername(), msg.getContent(), false);
        }
        builder.setAuthor(user.getUsername(), user.getAvatarUrl(), user.getAvatarUrl());
        DiscordServerConfig c = DiscordServerConfig.get(msg.getGuild().block().getId().asLong());
        FileInputStream inputStream1;
        try{
            inputStream1 = new FileInputStream(modFile);
        }catch(FileNotFoundException e){
            throw new RuntimeException(e);
        }
        if(zip.child("icon.png").exists()){
            FileInputStream inputStream2;
            try{
                inputStream2 = new FileInputStream("mods/icon.png");
            }catch(FileNotFoundException e){
                throw new RuntimeException(e);
            }
            if (c.schematicsChannel == 0) {
                msg.getChannel().block().createMessage(spec -> spec.addFile(modFile.getName(), inputStream1)
                        .addFile("mods/icon.png", inputStream2)
                        .setEmbed(embed -> embed.from(builder.asRequest())))
                        .block();
            } else {
                msg.getGuild().block().getChannelById(Snowflake.of(c.modsChannel)).cast(GuildMessageChannel.class).block()
                        .createMessage(spec -> spec.addFile(modFile.getName(), inputStream1)
                                .addFile("mods/icon.png", inputStream2)
                                .setEmbed(embed -> embed.from(builder.asRequest())))
                        .block();
            }
        }else{
            if (c.schematicsChannel == 0) {
                msg.getChannel().block().createMessage(spec -> spec.addFile(modFile.getName(), inputStream1)
                        .setEmbed(embed -> embed.from(embed.asRequest())))
                        .block();
            } else {
                msg.getGuild().block().getChannelById(Snowflake.of(c.modsChannel)).cast(GuildMessageChannel.class).block()
                        .createMessage(spec -> spec.addFile(modFile.getName(), inputStream1)
                                .setEmbed(embed -> embed.from(embed.asRequest())))
                        .block();
            }
        }
    }

    public static void setBotChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().block().getMemberById(msg.getAuthor().get().getId())).block().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS) ||
                msg.getAuthor().get().getId().asLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).botChannel = msg.getChannel().block().getId().asLong();
            msg.getChannel().block().createEmbed(e->e.setTitle("Канал успешно установлен.")
                    .setColor(Color.of(0,0,255))).block();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().block().createEmbed(e->e.setTitle("Недостаточно полномочий.")
                .setColor(Color.of(255,0,0))).block();

    }

    public static void setMapChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().block().getMemberById(msg.getAuthor().get().getId())).block().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS) ||
                msg.getAuthor().get().getId().asLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).mapsChannel = msg.getChannel().block().getId().asLong();
            msg.getChannel().block().createEmbed(e->e.setTitle("Канал успешно установлен.")
                    .setColor(Color.of(0,0,255))).block();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().block().createEmbed(e->e.setTitle("Недостаточно полномочий.")
                .setColor(Color.of(255,0,0))).block();

    }

    public static void setSchematicChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().block().getMemberById(msg.getAuthor().get().getId())).block().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS) ||
                msg.getAuthor().get().getId().asLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).schematicsChannel = msg.getChannel().block().getId().asLong();
            msg.getChannel().block().createEmbed(e->e.setTitle("Канал успешно установлен.")
                    .setColor(Color.of(0,0,255))).block();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().block().createEmbed(e->e.setTitle("Недостаточно полномочий.")
                .setColor(Color.of(255,0,0))).block();

    }

    public static void setModsChannel(Message msg) {
        if (Objects.requireNonNull(msg.getGuild().block().getMemberById(msg.getAuthor().get().getId())).block().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS) ||
                msg.getAuthor().get().getId().asLong() == 826128001145765935L) {
            DiscordServerConfig.get(msg.getGuild().block().getId().asLong()).modsChannel = msg.getChannel().block().getId().asLong();
            msg.getChannel().block().createEmbed(e->e.setTitle("Канал успешно установлен.")
                    .setColor(Color.of(0,0,255))).block();
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().block().createEmbed(e->e.setTitle("Недостаточно полномочий.")
                .setColor(Color.of(255,0,0))).block();
    }
}
