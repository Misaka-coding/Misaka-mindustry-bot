package uwu.misaka.bot;

import java.io.*;

import static uwu.misaka.bot.Ichi.gson;

public class DiscordServerConfig {
    public long id = 0;
    public long botChannel = 0;
    public long schematicsChannel = 0;
    public long mapsChannel = 0;
    public long modsChannel = 0;

    public DiscordServerConfig(Long id) {
        this.id = id;
        Ichi.servers.add(this);
    }

    public DiscordServerConfig() {
    }

    public static void load() throws IOException {
        File storage = new File("Storage.txt");
        if (!storage.exists()) {
            if (!storage.createNewFile()) {
                return;
            }
        }
        BufferedReader r = new BufferedReader(new FileReader(storage));
        while (true) {
            String s = r.readLine();
            if (s != null) {
          Ichi.servers.add(fromJson(s));
        }else{
          break;
        }
      }
    }
    public static void save(){
        try{
        File storage=new File("Storage.txt");
        if (!storage.exists()) {
            if (!storage.createNewFile()) {
                return;
            }
        }
        FileWriter w = new FileWriter(storage,false);
        for(DiscordServerConfig c:Ichi.servers){
            w.append(c.toJson()).append("\n");
        }
        w.flush();} catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DiscordServerConfig fromJson(String s){
        return gson.fromJson(s,DiscordServerConfig.class);
    }
    public String toJson(){
        return gson.toJson(this);
    }
    public static DiscordServerConfig get(Long id){
        for(DiscordServerConfig c:Ichi.servers){
            if(c.id==id){return c;}
        }
        return new DiscordServerConfig(id);
    }
}
