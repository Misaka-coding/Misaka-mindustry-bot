package uwu.misaka.bot;

import arc.files.Fi;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Handler {
    public static void handle(Message msg) {
        if (msg.getContentRaw().equalsIgnoreCase("+помощь")) {

            return;
        }
        if (msg.getAttachments().size() != 1) {
            msg.delete();
            return;
        }

        Message.Attachment a = msg.getAttachments().get(0);

        if (a.getFileName().endsWith(".msch")) {
            parseSchematic(msg);
            return;
        }
        if (a.getFileName().endsWith(".zip") || a.getFileName().endsWith(".jar")) {
            parseModPlugin(msg);
            return;
        }
        if (a.getFileName().endsWith(".msav")) {
            parseMap(msg);
            return;
        }
    }

    public static void parseMap(Message msg) {

    }

    public static void parseSchematic(Message msg) {
        try {
            Schematic schema = Ichi.parser.parseSchematic(msg.getAttachments().get(0).getUrl());
            BufferedImage preview = Ichi.parser.previewSchematic(schema);
            File previewFile = new File("img_" + schema.name() + ".png");
            File schemFile = new File(schema.name() + "." + ".msch");
            Schematics.write(schema, new Fi(schemFile));
            ImageIO.write(preview, "png", previewFile);
            EmbedBuilder builder = new EmbedBuilder().setColor(Color.decode("#00FF00"));
            builder.setAuthor(msg.getAuthor().getName(), msg.getJumpUrl(), msg.getAuthor().getAvatarUrl());
            builder.setTitle(schema.name());
            builder.setImage("attachment://" + previewFile.getName());
            if (msg.getContentRaw().length() > 3) {
                builder.addField("От " + msg.getAuthor().getAsMention(), msg.getContentRaw(), false);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseModPlugin(Message msg) {

    }
}
