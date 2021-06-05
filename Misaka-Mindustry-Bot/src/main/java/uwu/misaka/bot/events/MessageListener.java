package uwu.misaka.bot.events;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import uwu.misaka.bot.Handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MessageListener {
    public static final SimpleDateFormat loggerFormat = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");

    public static void listen(Message message){
        if(message.getAuthor()==null||message==null){return;}
        log(message);
        try{
            Handler.handle(message);
        }catch (Exception e){
            return;
        }
    }
    public static void log(Message message){
        //System.out.println(loggerFormat.format(new Date()) + "│" + formatText(message.getGuild().block().getName(), 13) + "│" + formatText(Objects.requireNonNull(message.getChannel().ofType(GuildChannel.class).map(GuildChannel::getName).block()),15) + "│" + formatText(message.getAuthor().get().getUsername(),10) + ": " + message.getContent());
        try{
        parseMessageToPGUI(message.getGuild().block().getName(),Objects.requireNonNull(message.getChannel().ofType(GuildChannel.class).map(GuildChannel::getName).block()),message.getAuthor().get().getUsername(),message.getContent());}catch (Exception e){
            try{
            parseMessageToPGUI(message.getGuild().block().getName(),"АнонимуС",message.getAuthor().get().getUsername(),message.getContent());}catch(Exception ignored){}
        }
    }
    private static void parseMessageToPGUI(String guild,String channel,String author,String text){
        ArrayList<String> strings = new ArrayList<>();
        for(String s : text.split("\n")){
        while(s.length()>40){
            strings.add(s.substring(0,39));
            s=s.substring(39);
        }
        strings.add(s);
        }
        System.out.println("│"+loggerFormat.format(new Date()) + "│" + formatText(guild, 15) + "│" + formatText(channel,15) + "│" + formatText(author,13) + "│" + formatText(strings.get(0),40)+"│");
        strings.remove(0);
        for(String s:strings){
            System.out.println("│                   │               │               │             │"+formatText(s,40)+"│");
        }
        System.out.println("├───────────────────┼───────────────┼───────────────┼─────────────┼────────────────────────────────────────┤");
    }
    private static String formatText(String s,int index){
        if(s.length()>index){
            s=s.substring(0,index-3);
            s+="...";
        }
        else{
            while(s.length()<index){
                s+=" ";
            }
        }
        return s;
    }
}
