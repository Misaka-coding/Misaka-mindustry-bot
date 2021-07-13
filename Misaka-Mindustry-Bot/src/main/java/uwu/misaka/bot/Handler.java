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
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Handler {
    public static void read(MessageCreateEvent msg) {
        if (msg.getMessage().getContent().equalsIgnoreCase("ня") || msg.getMessage().getContent().equalsIgnoreCase("nya")) {
            msg.getChannel().sendMessage("ня");
        }
    }

    public static void handle(MessageCreateEvent msg) {
        TextChannel channel = msg.getChannel();
        if (msg.getMessage().getContent().startsWith("+канал ")) {
            String m = msg.getMessage().getContent();
            if (m.length() < 8) return;
            switch (m.substring(7)) {
                case "бот" -> {
                    setBotChannel(msg.getMessage());
                    return;
                }
                case "карты" -> {
                    setMapChannel(msg.getMessage());
                    return;
                }
                case "схемы" -> {
                    setSchematicChannel(msg.getMessage());
                    return;
                }
                case "моды" -> {
                    setModsChannel(msg.getMessage());
                    return;
                }
                default -> channel.sendMessage(new EmbedBuilder().setTitle("Канал данного типа не поддерживается.")
                        .setFooter("Доступные каналы:\nбот\nкарты\nсхемы\nмоды", "")
                        .setColor(new Color(255, 0, 0)));
            }
        }

        if (DiscordServerConfig.get(msg.getServerTextChannel().get().getId()).botChannel != 0 &&
                msg.getChannel().getId() != DiscordServerConfig.get(msg.getChannel().getId()).botChannel) {
            return;
        }
        // TODO: 13.07.2021 prefix
        if (DiscordServerConfig.get(msg.getServerTextChannel().get().getId()).botChannel == 0 && msg.getMessage().getContent().startsWith("+")) {
            channel.sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.")
                    .setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды")
                    .setColor(new Color(255, 0, 0)));
            return;
        }
        if (msg.getMessage().getContent().equalsIgnoreCase("+помощь") ||
                msg.getMessage().getContent().equalsIgnoreCase("+памагити") ||
                msg.getMessage().getContent().equalsIgnoreCase("+хелп") ||
                msg.getMessage().getContent().equalsIgnoreCase("+help")) {
            channel.sendMessage(new EmbedBuilder()
                    .setColor(new Color(0, 255, 0))
                    .addField("+канал <тип канала>", "установка каналов для контента. если канал не установлен, вывод в бот канал\nДоступно для: \nРоль с правами на управление каналами\nАдминистраторы бота(только бот канал)", false)
                    .addField("+помощь", "помощь", false)
                    .addField("Преобразование контента", "Моды, плагины, карты, схемы преобразуются при отправке их в бот канал. Если канал типа контента существует, отправка туда, инначе в бот канал", false)
                    .addField("+ава <упоменание/айди>", "Совлерские фокусы с аватарками", false)
                    .addField("+хентай", "Совлерские фокусы с хентаем | Только для администраторов сервера и администраторов бота | только в каналах с меткой nsfw | не работает", false)
            )
            ;
            return;
        }
        if (msg.getMessage().getContent().startsWith("+ава")) {
            if (msg.getMessage().getMentionedUsers().size() > 0) {
                channel.sendMessage(msg.getMessage().getMentionedUsers().get(0).getAvatar().getUrl().toString());
                return;
            } else {
                try {
                    if (msg.getMessage().getContent().length() > 5) {
                        channel.sendMessage(Ohayo.api.getUserById(Long.parseLong(msg.getMessage().getContent().substring(5))).get().getAvatar().getUrl().toString());
                        return;
                    }
                } catch (Exception e) {
                    channel.sendMessage(new EmbedBuilder().setTitle("Участник не найден.").setColor(new Color(255, 0, 0)));
                    return;
                }
            }
            channel.sendMessage(new EmbedBuilder().setTitle("Участник не найден.").setColor(new Color(255, 0, 0)));
            return;
        }

        if (msg.getMessage().getAttachments().size() != 1) {
            return;
        }

        MessageAttachment a = msg.getMessage().getAttachments().get(0);

        if (a.getFileName().endsWith(".msch")) {
            if (DiscordServerConfig.get(msg.getServer().get().getId()).botChannel == 0) {
                channel.sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.")
                        .setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды")
                        .setColor(new Color(255, 0, 0)));
                return;
            }
            parseSchematic(msg.getMessage());
            return;
        }
        if (a.getFileName().endsWith(".zip") || a.getFileName().endsWith(".jar")) {
            if (DiscordServerConfig.get(msg.getServer().get().getId()).botChannel == 0) {
                channel.sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.")
                        .setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды")
                        .setColor(new Color(255, 0, 0)));
                return;
            }
            parseModPlugin(msg.getMessage());
            return;
        }
        if (a.getFileName().endsWith(".msav")) {
            if (DiscordServerConfig.get(msg.getServer().get().getId()).botChannel == 0) {
                channel.sendMessage(new EmbedBuilder().setTitle("Бот канал не установлен.")
                        .setFooter("Установите его с помощью команды +канал\nдоступные каналы:\nбот\nкарты\nсхемы\nмоды")
                        .setColor(new Color(255, 0, 0)));
                return;
            }
            parseMap(msg.getMessage());
        }
    }

    public static void parseMap(Message msg) {
        try {
            MessageAttachment a = msg.getAttachments().get(0);
            ContentParser.Map map = Ohayo.parser.readMap(ContentParser.download(a.getUrl().toString()));
            File mapFile = new File(map.name.replaceAll(" ", "_") + ".msav");
            File imageFile = new File("image_output.png");
            Streams.copy(ContentParser.download(a.getUrl().toString()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(new Color(0, 255, 0));
            builder.setImage("attachment://" + imageFile.getName());
            builder.setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatar().getUrl().toString(), msg.getAuthor().getAvatar().toString());
            builder.setTitle(map.name == null ? a.getFileName().replace(".msav", "") : map.name);
            if (map.author.length() > 3) builder.addField("Автор: ", map.author, true);
            if (map.description.length() > 3) builder.addField("Описание: ", map.description, true);
            DiscordServerConfig c = DiscordServerConfig.get(msg.getServer().get().getId());

            FileInputStream inputStream1;
            try {
                inputStream1 = new FileInputStream(mapFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            FileInputStream inputStream2;
            try{
                inputStream2 = new FileInputStream(imageFile);
            }catch(FileNotFoundException e){
                throw new RuntimeException(e);
            }

            if (c.schematicsChannel == 0) {
                msg.getChannel().sendMessage(builder, mapFile, imageFile);
            } else {
                msg.getServer().get().getChannelById(c.mapsChannel).get().asTextChannel().get().sendMessage(builder, mapFile, imageFile);
                ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseSchematic(Message msg) {
        try {
            Schematic schema = Ohayo.parser.parseSchematic(msg.getAttachments().get(0).getUrl().toString());
            BufferedImage preview = Ohayo.parser.previewSchematic(schema);
            Fi previewFile = Fi.get("img_shema.png");
            Fi schemaFile = Fi.get(schema.name().replaceAll(" ", "_") + ".msch");
            Schematics.write(schema, schemaFile);
            ImageIO.write(preview, "png", previewFile.file());
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(new Color(0, 255, 0));
            builder.setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatar().getUrl().toString(), msg.getAuthor().getAvatar().getUrl().toString());
            builder.setTitle(schema.name());
            builder.setImage("attachment://" + previewFile.name());
            if (msg.getContent().length() > 3) {
                builder.addField("От " + msg.getAuthor().getName(), msg.getContent(), false);
            }
            StringBuilder field = new StringBuilder();
            for (ItemStack stack : schema.requirements()) {
                List<Emoji> emotes = new ArrayList<>();
                emotes.addAll(msg.getServer().get().getCustomEmojisByNameIgnoreCase(stack.item.name.replace("-", "")));
                Emoji result = null;
                try {
                    List<Emoji> ohnos = new ArrayList<>();
                    ohnos.addAll(msg.getServer().get().getCustomEmojisByNameIgnoreCase("oh-no"));
                    result = emotes.isEmpty() ? ohnos.get(0) : emotes.get(0);
                } catch (Exception e) {
                    emotes.addAll(Ohayo.api.getServerById(Ohayo.botBaseServer).get().getCustomEmojisByNameIgnoreCase(stack.item.name.replace("-", "")));
                    if (!emotes.isEmpty())
                        result = emotes.get(0);
                }
                if (result == null) {
                    field.append(stack.item.name.replace("-", "")).append(stack.amount).append("  ");
                } else {
                    field.append(result.getMentionTag()).append(stack.amount).append("  ");
                }
            }
            builder.addField("Требуемые Ресурсы", field.toString(), false);
            if (schema.description().length() > 3) {
                builder.addField("Описание", schema.description(), false);
            }
            builder.addField("Потребление энергии: ", (int) schema.powerConsumption() * 40 + "", true);
            builder.addField("Производство энергии: ", (int) schema.powerProduction() * 40 + "", true);

            DiscordServerConfig c = DiscordServerConfig.get(msg.getServer().get().getId());

            if (c.schematicsChannel == 0) {
                msg.getChannel().sendMessage(builder, schemaFile.file(), previewFile.file());
            } else {
                msg.getServer().get().getChannelById(c.schematicsChannel).get().asTextChannel().get().sendMessage(builder, schemaFile.file(), previewFile.file());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseModPlugin(Message msg) {
        MessageAuthor user = msg.getAuthor();
        if (user == null){
            return;
        }

        if (new File("mods/").mkdir()) {
            System.out.print(" ");
        }
        File modFile = new File("mods/" + (msg.getAttachments().get(0)).getFileName());
        try {
            Streams.copy(ContentParser.download(msg.getAttachments().get(0).getUrl().toString()), new FileOutputStream(modFile));
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
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Мета данные не найдены.")
                    .setColor(new Color(255, 0, 0)));
            return;
        }
        boolean isPlugin = metaf.name().startsWith("plugin");
        Mods.ModMeta meta = Ohayo.gson.fromJson(Jval.read(metaf.readString()).toString(Jval.Jformat.plain),Mods.ModMeta.class);
        meta.cleanup();
        BufferedImage image = null;
        if (zip.child("icon.png").exists()) {
            try {
                image = ImageIO.read(zip.child("icon.png").read());
                File imageF = new File("mods/icon.png");
                ImageIO.write(image, "png", imageF);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(0, 255, 0));
        if (image != null) {
            builder.setImage("attachment://icon.png");
        }
        builder.setTitle(meta.displayName());
        builder.addField("Автор:", Strings.stripColors(meta.author), true);
        builder.addField("Описание:", Strings.stripColors(meta.description), true);
        builder.addField("Минимальная версия:", Strings.stripColors(meta.minGameVersion), true);
        if (isPlugin) {
            builder.addField("Тип модификации:", "плагин", true);
        } else {
            builder.addField("Тип модификации:", "мод", true);
            if (msg.getAttachments().get(0).getFileName().endsWith(".jar")) {
                builder.addField("Внимание!", "Java модификации не всегда безопасны", false);
            }
        }
        if (msg.getContent().length() > 3) {
            builder.addField("От " + user.getName(), msg.getContent(), false);
        }
        builder.setAuthor(user.getName(), user.getAvatar().getUrl().toString(), user.getAvatar().getUrl().toString());
        DiscordServerConfig c = DiscordServerConfig.get(msg.getServer().get().getId());
        FileInputStream inputStream1;
        try {
            inputStream1 = new FileInputStream(modFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (zip.child("icon.png").exists()) {
            FileInputStream inputStream2;
            try {
                inputStream2 = new FileInputStream("mods/icon.png");
            }catch(FileNotFoundException e){
                throw new RuntimeException(e);
            }
            if (c.schematicsChannel == 0) {
                msg.getChannel().sendMessage(builder, modFile, new File("mods/icon.png"));
            } else {
                msg.getServer().get().getChannelById(c.modsChannel).get().asTextChannel().get().sendMessage(builder, modFile, new File("mods/icon.png"));
            }
        }else{
            if (c.schematicsChannel == 0) {
                msg.getChannel().sendMessage(builder, modFile);
            } else {
                msg.getServer().get().getChannelById(c.modsChannel).get().asTextChannel().get()
                        .sendMessage(builder, modFile);
            }
        }
    }

    public static void setBotChannel(Message msg) {
        if (msg.getAuthor().canCreateChannelsOnServer() ||
                msg.getAuthor().isBotUser()) {
            DiscordServerConfig.get(msg.getServer().get().getId()).botChannel = msg.getChannel().getId();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.")
                    .setColor(new Color(0, 0, 255)));
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.")
                .setColor(new Color(255, 0, 0)));

    }

    public static void setMapChannel(Message msg) {
        if (msg.getAuthor().canCreateChannelsOnServer() ||
                msg.getAuthor().isBotUser()) {
            DiscordServerConfig.get(msg.getServer().get().getId()).mapsChannel = msg.getChannel().getId();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.")
                    .setColor(new Color(0, 0, 255)));
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.")
                .setColor(new Color(255, 0, 0)));

    }

    public static void setSchematicChannel(Message msg) {
        if (msg.getAuthor().canCreateChannelsOnServer() ||
                msg.getAuthor().isBotUser()) {
            DiscordServerConfig.get(msg.getServer().get().getId()).schematicsChannel = msg.getChannel().getId();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.")
                    .setColor(new Color(0, 0, 255)));
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.")
                .setColor(new Color(255, 0, 0)));

    }

    public static void setModsChannel(Message msg) {
        if (msg.getAuthor().canCreateChannelsOnServer() ||
                msg.getAuthor().isBotUser()) {
            DiscordServerConfig.get(msg.getServer().get().getId()).modsChannel = msg.getChannel().getId();
            msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Канал успешно установлен.")
                    .setColor(new Color(0, 0, 255)));
            DiscordServerConfig.save();
            return;
        }
        msg.getChannel().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.")
                .setColor(new Color(255, 0, 0)));
    }
}
