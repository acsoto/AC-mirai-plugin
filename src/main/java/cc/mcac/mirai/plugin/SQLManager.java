package cc.mcac.mirai.plugin;

import java.sql.*;
import java.util.HashMap;

public class SQLManager {
    private Connection connection;
    private HashMap<String, String> medalsMap;
    private static SQLManager instance = null;

    public static SQLManager getInstance() throws ClassNotFoundException {
        return instance == null ? instance = new SQLManager() : instance;
    }


    private SQLManager() throws ClassNotFoundException {
        String ip = PluginMain.Config.INSTANCE.getHost();
        String databaseName = PluginMain.Config.INSTANCE.getDatabase();
        String userName = PluginMain.Config.INSTANCE.getUsername();
        String userPassword = PluginMain.Config.INSTANCE.getPassword();
        int port = 3306;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + ip + ":" + port + "/" + databaseName + "?autoReconnect=true&useSSL=false",
                    userName, userPassword
            );
            PluginMain.INSTANCE.getLogger().info("SQL-medal connected");
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM medal"
            );
            ResultSet rs = ps.executeQuery();
            medalsMap = new HashMap<>();
            while (rs.next()) {
                medalsMap.put(rs.getString("medal_id"), rs.getString("medal_name").substring(2));
            }
            PluginMain.INSTANCE.getLogger().info("SQL-medalsMap initialized");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerMedals(String playerName) {
        StringBuilder medals = new StringBuilder();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM player_medal WHERE player_id = ?"
            );
            ps.setString(1, playerName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String medal_id = rs.getString("medal_id");
                medals.append("[").append(medalsMap.get(medal_id)).append("]");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medals.toString();
    }


}
