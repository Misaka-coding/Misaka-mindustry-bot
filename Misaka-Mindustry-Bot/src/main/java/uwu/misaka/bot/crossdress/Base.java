package uwu.misaka.bot.crossdress;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import uwu.misaka.bot.Ohayo;

import java.awt.*;
import java.io.File;
import java.net.URL;

public class Base {

    public static boolean checkPerms(Long server, Long user) {
        return false;
    }

    public static URL getAvatarURL(Long user) {
        return null;
    }

    public static void sendMessage(Long channel, String message) {
        Ohayo.api.getChannelById(channel).get().asTextChannel().get().sendMessage(message);
    }

    public static void sendMessageWithFiles(Long channel, String message, File... files) {
        Ohayo.api.getChannelById(channel).get().asTextChannel().get().sendMessage(message, files);
    }

    public static void sendNotEnoughPerms(Long channel) {
        Ohayo.api.getChannelById(channel).get().asTextChannel().get().sendMessage(new EmbedBuilder().setTitle("Недостаточно полномочий.").setColor(new Color(255, 0, 0)));
    }

    public static void sendEmbed(Object embed, Long channelId) {

    }

    public static void sendEmbedWithFiles(Object embed, Long channelId, File... files) {

    }

    public static class CDAttachment {
        public URL fileUrl;
        public String filename;
    }
}
