package uwu.misaka.bot;

import arc.util.serialization.Json;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.internal.entities.EntityBuilder;

import com.google.gson.*;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Ichi {
    public static JDA botCore;
    public static Listener listener;
    public static Gson gson = new Gson();
    public static ContentParser parser = new ContentParser();
    public static Json json = new Json();

    public static Long botBaseServer =  826541082534871061L;

    public static void main(String[] s) throws LoginException, IOException {
        listener=new Listener();
        DiscordServerConfig.load();
        botCore=runBot(s[0]);
    }
    
    public static JDA runBot(String token) throws LoginException {
            JDABuilder builder = new JDABuilder();
            builder.setToken(token);
            System.out.println("Token");
            builder.setActivity(EntityBuilder.createActivity("+помощь", null, Activity.ActivityType.CUSTOM_STATUS));
            System.out.println("Table");
            builder.addEventListeners(new Listener());
            System.out.println("Listener");
            return builder.build();
    }
}
