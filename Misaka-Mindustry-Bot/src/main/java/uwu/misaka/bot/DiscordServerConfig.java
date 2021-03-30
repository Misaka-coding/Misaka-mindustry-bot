package uwu.misaka.bot;

public class DiscordServerConfig {
    public long id;
    public long botChannel=0;
    public long schematicsChannel=0;
    public long mapsChannel=0;
    public long modsChannel=0;

    public static DiscordServerConfig servers;

    public DiscordServerConfig(Long id){
        this.id=id;
    }

    public static void load(){
      File storage=new File("Storage.txt");
      if(Storage.exists()){
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
    public static DiscordServerConfig fromJson(String s){
        return Ichi.gson.fromJson(s,DiscordServerConfig.class);
    }
}
