package uwu.misaka.bot;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import uwu.misaka.bot.events.MessageListener;

public class CallBackModule {
    public static long lastChannel=0;

    public static void sendToThis(String text){
        if(lastChannel==0){
            lastChannel= MessageListener.last;
        }
        Ohayo.gateway.getChannelById(Snowflake.of(lastChannel)).cast(GuildMessageChannel.class).block().createMessage(text).block();
    };
    public static void getThis(){try{
        if(lastChannel==0){
            lastChannel= MessageListener.last;
        }
        System.out.println("Выбран канал "+Ohayo.gateway.getChannelById(Snowflake.of(lastChannel)).cast(GuildMessageChannel.class).block().getName());}catch(Exception ignored){}
    };
    public static boolean changeChannel(String id){
        try{
            long a = Ohayo.gateway.getChannelById(Snowflake.of(id)).block().getId().asLong();
            lastChannel = a;
            System.out.println("Выбран канал "+Ohayo.gateway.getChannelById(Snowflake.of(lastChannel)).cast(GuildMessageChannel.class).block().getName());
        }catch (Exception e){
            System.out.println("Ошибка");
            return false;
        }
        return true;
    }
}
