package uwu.misaka.bot;

import com.google.gson.Gson;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;

import static uwu.misaka.bot.events.MessageListener.listen;

public class Ohayo {
    public static Gson gson = new Gson();
    public static ArrayList<DiscordServerConfig> servers = new ArrayList<>();
    public static ContentParser parser = new ContentParser();
    public static Long botBaseServer =  826541082534871061L;
    public static GatewayDiscordClient gateway;

    public static void main(String[] args) throws IOException {
        DiscordClient client = DiscordClient.create(args[0]);
        gateway = client.login().block();
        DiscordServerConfig.load();
        gateway.on(MessageCreateEvent.class).flatMap(event -> Mono.fromRunnable(() -> listen(event.getMessage()))).subscribe();
        gateway.onDisconnect().block();
    }
}
