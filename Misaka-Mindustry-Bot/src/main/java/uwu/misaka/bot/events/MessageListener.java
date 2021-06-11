package uwu.misaka.bot.events;

import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import uwu.misaka.bot.CallBackModule;
import uwu.misaka.bot.Handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MessageListener {
    public static long last;
    public static final SimpleDateFormat loggerFormat = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");

    public static void listen(Message message){
        if(message.getAuthor()==null||message==null){return;}
        log(message);
        try{
            if(message.getAuthor().get().isBot()){return;}
            Handler.read(message);
            Handler.handle(message);
        }catch (Exception e){
            return;
        }
    }

    public static void consoleListen(String message){
        if(message.startsWith("+канал ")){
            CallBackModule.changeChannel(message.substring(7));
            return;
        }
        if(message.startsWith("+канал")){
            CallBackModule.getThis();
            return;
        }
        CallBackModule.sendToThis(message);
    }

    public static void log(Message message){
        String msg = message.getContent();
        if(message.getEmbeds().size()>0){
            for(Embed e:message.getEmbeds()){
               msg+="\n"+e.toString();
            }
        }
        try{
        parseMessageToPGUI(message.getGuild().block().getName(),Objects.requireNonNull(message.getChannel().ofType(GuildChannel.class).map(GuildChannel::getName).block()),message.getAuthor().get().getUsername(),msg,message.getChannel().block().getId().asString());}catch (Exception e){
            try{
            parseMessageToPGUI(message.getGuild().block().getName(),"АнонимуС",message.getAuthor().get().getUsername(),msg,message.getChannel().block().getId().asString());}catch(Exception ignored){}
        }
        last=message.getChannel().block().getId().asLong();
    }
    private static void parseMessageToPGUI(String guild,String channel,String author,String text,String channelId){
        ArrayList<String> strings = new ArrayList<>();
        for(String s : text.split("\n")){
        while(s.length()>20){
            strings.add(s.substring(0,19));
            s=s.substring(19);
        }
        strings.add(s);
        }
        System.out.println("│"+loggerFormat.format(new Date()) + "│" + formatText(guild, 15) + "│" + formatText(channel,15) + "│"+formatText(channelId,18) + "│" + formatText(author,13) + "│" + formatText(strings.get(0),20)+"│");
        strings.remove(0);
        for(String s:strings){
            System.out.println("│                   │               │               │                  │             │"+formatText(s,20)+"│");
        }
        System.out.println("├───────────────────┼───────────────┼───────────────┼──────────────────┼─────────────┼────────────────────┤");
    }
    private static String parseMessageToLogTXT(String guild,String channel,String author,String text){
        StringBuilder rtn=new StringBuilder();
        ArrayList<String> strings = new ArrayList<>();
        for(String s : text.split("\n")){
            while(s.length()>60){
                strings.add(s.substring(0,59));
                s=s.substring(59);
            }
            strings.add(s);
        }
        rtn.append("│"+loggerFormat.format(new Date()) + "│" + formatText(guild, 30) + "│" + formatText(channel,30) + "│" + formatText(author,26) + "│" + formatText(strings.get(0),60)+"│");
        strings.remove(0);
        for(String s:strings){
            rtn.append("│                   │                              │                              │                          │"+formatText(s,60)+"\n│");
        }
        rtn.append("├───────────────────┼──────────────────────────────┼──────────────────────────────┼──────────────────────────┼────────────────────────────────────────────────────────────┤\n");
        return rtn.toString();
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
