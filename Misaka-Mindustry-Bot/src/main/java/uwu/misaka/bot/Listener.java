package uwu.misaka.bot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Date;

public class Listener extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        User author = message.getAuthor();
        if (author.isBot()) {
            return;
        }
        System.out.println(new Date().toString() + " " + message.getGuild().getName() + " " + message.getChannel().getName() + " " + message.getAuthor().getName() + " : " + message.getContentRaw());
        Handler.handle(message);
    }
}
