package uwu.misaka.bot;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.internal.entities.EntityBuilder;

import com.google.gson.*;

import javax.security.auth.login.LoginException;

public class Ichi {
    public static JDA botCore;
    public static Listener listener;
    public static Gson gson = new Gson();
    public static ContentParser parser = new ContentParser();

    public static Long botBaseServer =  826523297671938078L;

    public static void main(String[] s) throws LoginException {
        listener=new Listener();
        botCore=runBot(s[0]);
    }
    
    public static JDA runBot(String token) throws LoginException {
            JDABuilder builder = new JDABuilder(AccountType.BOT);
            builder.setToken(token); //
            System.out.println("Токен");
            builder.setActivity(EntityBuilder.createActivity("+помощь", null, Activity.ActivityType.DEFAULT));
            System.out.println("Табличка");
            builder.addEventListeners(new Listener());
            System.out.println("Обработчик");
            return builder.build();
    }
}
