package uwu.misaka.bot;

public class DiscordServerConfig {
    public long id;
    public long botChannel;
    public long schematicsChannel;
    public long mapsChannel;
    public long modsChannel;

    public DiscordServerConfig(Long id){
        this.id=id;
    }
}
