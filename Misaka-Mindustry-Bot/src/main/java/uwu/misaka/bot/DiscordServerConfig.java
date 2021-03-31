package uwu.misaka.bot;

import java.io.*;
import java.util.ArrayList;

import static uwu.misaka.bot.Ichi.gson;

public class DiscordServerConfig {
    public long id;
    public long botChannel=0;
    public long schematicsChannel=0;
    public long mapsChannel=0;
    public long modsChannel=0;

    public static ArrayList<DiscordServerConfig> servers = new ArrayList<>();

    public DiscordServerConfig(Long id){
        this.id=id;
        servers.add(this);
    }

    public static void load() throws IOException {
      File storage=new File("Storage.txt");
      if(storage.exists()){
        storage.createNewFile();
      }
      BufferedReader r = new BufferedReader(new FileReader(storage));
      while(true){
        String s = r.readLine();
        if(s!=null){
          servers.add(fromJson(s));
        }else{
          break;
        }
      }
    }
    public static void save(){
        try{
        File storage=new File("Storage.txt");
        if(storage.exists()){
            storage.createNewFile();
        }
        FileWriter w = new FileWriter(storage,false);
        for(DiscordServerConfig c:servers){
            w.append(c.toJson());
        }
        w.flush();} catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DiscordServerConfig fromJson(String s){
        return gson.fromJson(s,DiscordServerConfig.class);
    }
    public String toJson(){
        return gson.toJson(DiscordServerConfig.class);
    }
    public static DiscordServerConfig get(Long id){
        for(DiscordServerConfig c:servers){
            if(c.id==id){return c;}
        }
        return new DiscordServerConfig(id);
    }
}
