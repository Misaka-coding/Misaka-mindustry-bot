package uwu.misaka.bot;

import com.google.gson.Gson;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.ArrayList;

import static uwu.misaka.bot.events.MessageListener.listen;

public class Ohayo {
    public static Gson gson = new Gson();
    public static ArrayList<DiscordServerConfig> servers = new ArrayList<>();
    public static ContentParser parser = new ContentParser();
    public static Long botBaseServer =  826541082534871061L;
    public static GatewayDiscordClient gateway;

    public static void main(String[] args){
        final String token = args[0];
        final DiscordClient client = DiscordClient.create(token);
        gateway = client.login().block();
        gateway.on(MessageCreateEvent.class).subscribe(event->{
            listen(event.getMessage());
        });
        gateway.onDisconnect().block();
    }
}
