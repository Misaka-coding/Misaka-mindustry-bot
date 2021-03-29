package uwu.misaka.bot;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.internal.entities.EntityBuilder;

import javax.security.auth.login.LoginException;

public class Ichi {
    public static JDA botCore;
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
