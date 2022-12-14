package cc.mcac.mirai.plugin;

import java.sql.*;
import java.util.HashMap;

public class SQLManager {
    private Connection connectionMedal;
    private Connection connectionInfo;
    private HashMap<String, String> medalsMap;
    private static SQLManager instance = null;

    public static SQLManager getInstance() throws ClassNotFoundException {
        return instance == null ? instance = new SQLManager() : instance;
    }


    private SQLManager() throws ClassNotFoundException {
        try {
            String host = PluginMain.Config.INSTANCE.getHost();
            String username = PluginMain.Config.INSTANCE.getUsername_medal();
            String password = PluginMain.Config.INSTANCE.getPassword_medal();
            Class.forName("com.mysql.cj.jdbc.Driver");
            // connect to medal database
            connectionMedal = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":3306/" + username + "?autoReconnect=true&useSSL=false",
                    username, password
            );
            PluginMain.INSTANCE.getLogger().info("SQL-medal connected");
            PreparedStatement ps = connectionMedal.prepareStatement(
                    "SELECT * FROM medal"
            );
            ResultSet rs = ps.executeQuery();
            medalsMap = new HashMap<>();
            while (rs.next()) {
                medalsMap.put(rs.getString("medal_id"), rs.getString("medal_name").substring(2));
            }
            PluginMain.INSTANCE.getLogger().info("SQL-medalsMap initialized");
            // connect to info database
            username = PluginMain.Config.INSTANCE.getUsername_info();
            password = PluginMain.Config.INSTANCE.getPassword_info();
            connectionInfo = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":3306/" + username + "?autoReconnect=true&useSSL=false",
                    username, password
            );
            PluginMain.INSTANCE.getLogger().info("SQL-info connected");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerMedals(String playerName) {
        StringBuilder medals = new StringBuilder();
        try {
            PreparedStatement ps = connectionMedal.prepareStatement(
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

    public String getPlayerList() {
        int playerNumber = 0;
        StringBuilder playerListMsg = new StringBuilder();
        try {
            PreparedStatement ps = connectionInfo.prepareStatement(
                    "SELECT * FROM server_player_list"
            );
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("player_number") > 0) {
                    playerNumber += rs.getInt("player_number");
                    playerListMsg.append(rs.getString("player_list"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "当前在线人数: " + playerNumber + " 人, " + "在线玩家: " + playerListMsg;
    }


}
