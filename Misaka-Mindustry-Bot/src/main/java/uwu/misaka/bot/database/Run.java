package uwu.misaka.bot.database;

import uwu.misaka.bot.DiscordServerConfig;

import java.sql.*;

public class Run {
    public static final String DB_URL = "jdbc:h2:./BotDatabase";
    public static final String DB_Driver = "org.h2.Driver";
    public Connection connection;

    public Run(){
        try {
            Class.forName(DB_Driver);
            connection = DriverManager.getConnection(DB_URL);
            System.out.print("Подключение к DATABASE прошло успешно");
        } catch (ClassNotFoundException e) {
            System.out.print("\ncnf");
        } catch (SQLException e) {
            System.out.print("\nsql");
        }
    }
    public void Loading(){
        try {
            Statement s = connection.createStatement();
            s.executeQuery("SELECT * FROM ServersConfig");
            System.out.println("Таблица ServerConfig существует");
        } catch (SQLException e) {
            createConfigTable();
            System.out.println("Таблица ServerConfig сгенерирована");
        }
    }

    private void createConfigTable() {
        try {
            Statement s = connection.createStatement();
            s.executeUpdate("CREATE TABLE ServerConfig (guildConfig LONG not NULL,botChannel LONG not NUll, schematicsChannel LONG not NUll,mapsChannel LONG not NUll,modsChannel LONG not NUll)");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    private void createConfigLine(DiscordServerConfig config){
        try{
            Statement s = connection.createStatement();
            s.executeUpdate("INSERT INTO ServerConfig VALUES ("+config.id+" "+config.botChannel+" "+config.schematicsChannel+" "+config.mapsChannel+" "+config.modsChannel+")");
        }catch(SQLException t){
            t.printStackTrace();
        }
    }
    public DiscordServerConfig getDiscordServerConfig(Long id){
        DiscordServerConfig cfg;
        try{
        Statement s = connection.createStatement();
        ResultSet set = s.executeQuery("SELECT * FROM ServerConfig where id ="+id);
        while(set.next()){
            return new DiscordServerConfig(id,
            set.getLong("botChannel"),
            set.getLong("schematicsChannel"),
            set.getLong("mapsChannel"),
            set.getLong("modsChannel"));
        }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        cfg = new DiscordServerConfig(id,0,0,0,0);
        createConfigLine(cfg);
        return cfg;
    }
    public void updateDiscordServerConfig(){

    }
}
