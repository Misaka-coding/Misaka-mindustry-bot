package uwu.misaka.bot;

import uwu.misaka.bot.events.MessageListener;

public class CallBackModule {
    public static long lastChannel=0;

    public static void sendToThis(String text){
        if(lastChannel==0){
            lastChannel= MessageListener.last;
        }
        Ohayo.api.getChannelById(lastChannel).get().asTextChannel().get().sendMessage(text);
    }

    ;

    public static void getThis() {
        try {
            if (lastChannel == 0) {
                lastChannel = MessageListener.last;
            }
            System.out.println("Выбран канал " + Ohayo.api.getChannelById(lastChannel).get().asServerChannel().get().getName() + " на сервере " + Ohayo.api.getChannelById(lastChannel).get().asServerChannel().get().getServer().getName());
        } catch (Exception ignored) {
        }
    }

    ;

    public static void changeChannel(String id) {
        try {
            long a = Ohayo.api.getChannelById(id).get().getId();
            lastChannel = a;
            getThis();
        } catch (Exception e) {
            System.out.println("Ошибка");
        }
    }

    public static void getServerChannels() {
        Ohayo.api.getChannelById(lastChannel).get().asServerChannel().get().getServer().getChannels().forEach(
                c -> System.out.println(c.getName() + " " + c.getId())
        );
    }

    public static void getServerChannelsById(String id) {
        Ohayo.api.getChannelById(Long.parseLong(id)).get().asServerChannel().get().getServer().getChannels().forEach(
                c -> System.out.println(c.getName() + " " + c.getId())
        );
    }

    public static void getServers() {
        Ohayo.api.getServers().forEach(s -> System.out.println(s.getName() + " " + s.getId()));
    }
}