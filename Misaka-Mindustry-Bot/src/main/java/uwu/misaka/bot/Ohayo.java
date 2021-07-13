package uwu.misaka.bot;

import com.google.gson.Gson;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import uwu.misaka.bot.events.MessageListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Ohayo {
    public static Gson gson = new Gson();
    public static ArrayList<DiscordServerConfig> servers = new ArrayList<>();
    public static ContentParser parser = new ContentParser();
    public static Long botBaseServer = 826541082534871061L;
    public static DiscordApi api;

    public static void main(String[] args) throws IOException {
        api = new DiscordApiBuilder().setToken(args[0]).login().join();
        DiscordServerConfig.load();
        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                MessageListener.consoleListen(scanner.nextLine());
            }
        });
        thread.setDaemon(true);
        thread.setName("console lister");
        thread.start();

        api.addMessageCreateListener(MessageListener::listen);
    }
}
