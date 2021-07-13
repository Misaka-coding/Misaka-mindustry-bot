package uwu.misaka.bot;

import uwu.misaka.bot.events.MessageListener;

public class CallBackModule {
    public static long lastChannel=0;

    public static void sendToThis(String text){
        if(lastChannel==0){
            lastChannel= MessageListener.last;
        }
        Ohayo.api.getChannelById(lastChannel).get().asTextChannel().get().sendMessage(text);
    };
    public static void getThis(){
        try {
            if (lastChannel == 0) {
                lastChannel = MessageListener.last;
            }
            System.out.println("Выбран канал " + Ohayo.api.getChannelById(lastChannel).get().asServerChannel().get().getName());
        } catch (Exception ignored) {
        }
    };
    public static boolean changeChannel(String id){
        try{
            long a = Ohayo.api.getChannelById(id).get().getId();
            lastChannel = a;
            System.out.println("Выбран канал " + Ohayo.api.getChannelById(lastChannel).get().asServerChannel().get().getName());
        }catch (Exception e){
            System.out.println("Ошибка");
            return false;
        }
        return true;
    }
}
