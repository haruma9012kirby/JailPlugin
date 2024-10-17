package com.example.jail.DataBase;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

public class DataBaseMGR {
   private static final Logger LOGGER = Logger.getLogger(DataBaseMGR.class.getName());
   private Connection connection;
   private final Map<String, Jail> jails;
   private final Map<Jail, Map<String, Long>> jailedPlayers;

   public DataBaseMGR() {
      this.jails = new HashMap<>();
      this.jailedPlayers = new HashMap<>();
   }

   public void initialize() {
      initializeDatabase();
      loadJailsFromDatabase();
   }

   public void initializeDatabase() {
      JailPlugin plugin = JavaPlugin.getPlugin(JailPlugin.class);
      File dataFolder = plugin.getDataFolder();
      File dbFile = new File(dataFolder, "jails.db");
      
      try {
         if (!dataFolder.exists()) {
            dataFolder.mkdirs();
         }
         
         connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
         
         createJailsTable();
         createJailedPlayersTable();
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "データベースの初期化中にエラーが発生しました: {0}", e.getMessage());
      }
   }

   public void createJailsTable() throws SQLException {
      String createTableSQL = "CREATE TABLE IF NOT EXISTS jails ("
                            + "name TEXT PRIMARY KEY, "
                            + "location TEXT, "
                            + "capacity INTEGER, "
                            + "unjail_location TEXT)";
      
      try (Statement statement = connection.createStatement()) {
         statement.executeUpdate(createTableSQL);
      }
   }

   public void createJailedPlayersTable() throws SQLException {
      String createTableSQL = "CREATE TABLE IF NOT EXISTS jailed_players ("
                              + "player TEXT PRIMARY KEY, "
                              + "jail_name TEXT, "
                              + "start_time INTEGER, "
                              + "duration INTEGER, "
                              + "parole_until INTEGER, "
                              + "adventure_mode BOOLEAN)";
      
      try (Statement statement = connection.createStatement()) {
         statement.executeUpdate(createTableSQL);
      }
   }

   public void loadJailsFromDatabase() {
      String query = "SELECT name, location, capacity, unjail_location FROM jails";
      try (Statement statement = connection.createStatement();
           ResultSet rs = statement.executeQuery(query)) {

         while (rs.next()) {
            String name = rs.getString("name");
            Location location = stringToLocation(rs.getString("location"));
            int capacity = rs.getInt("capacity");
            String unjailLocationStr = rs.getString("unjail_location");
            Location unjailLocation = unjailLocationStr != null ? stringToLocation(unjailLocationStr) : null;
            jails.put(name, new Jail(name, location, capacity, unjailLocation));
         }
         // プラグインのjailsフィールドにロードしたデータを設定
         JailPlugin plugin = JavaPlugin.getPlugin(JailPlugin.class);
         plugin.jails = this.jails;
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "刑務所のロード中にSQLエラーが発生しました: {0}", e.getMessage());
      }
   }

   public void saveJailToDatabase(String jailName, Location location, int capacity, Location unjailLocation) {
      String query = "INSERT OR REPLACE INTO jails (name, location, capacity, unjail_location) VALUES (?, ?, ?, ?)";
      try (PreparedStatement ps = this.connection.prepareStatement(query)) {
         ps.setString(1, jailName);
         ps.setString(2, locationToString(location));
         ps.setInt(3, capacity);
         ps.setString(4, unjailLocation != null ? locationToString(unjailLocation) : null);
         ps.executeUpdate();
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage());
      }
   }

   public void updateUnjailLocationInDatabase(String jailName, Location unjailLocation) {
      String query = "UPDATE jails SET unjail_location = ? WHERE name = ?";
      try (PreparedStatement ps = this.connection.prepareStatement(query)) {
         ps.setString(1, locationToString(unjailLocation));
         ps.setString(2, jailName);
         ps.executeUpdate();
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage());
      }
   }

   public void saveJailedPlayerToDatabase(String playerName, String jailName, long duration, long paroleUntil, boolean adventureMode) {
      long startTime = System.currentTimeMillis() / 1000L;
      String query = "INSERT OR REPLACE INTO jailed_players (player, jail_name, start_time, duration, parole_until, adventure_mode) VALUES (?, ?, ?, ?, ?, ?)";

      try (PreparedStatement ps = this.connection.prepareStatement(query)) {
         ps.setString(1, playerName);
         ps.setString(2, jailName);
         ps.setLong(3, startTime);
         ps.setLong(4, duration);
         ps.setLong(5, paroleUntil);
         ps.setBoolean(6, adventureMode);
         ps.executeUpdate();
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage());
      }
   }

   public String locationToString(Location location) {
      return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
   }

   public Location stringToLocation(String locationStr) {
      String[] parts = locationStr.split(",");
      return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
   }

   public void updateParoleUntilInDatabase(String playerName, long paroleUntil) {
      String query = "UPDATE jailed_players SET parole_until = ? WHERE player = ?";
      try (PreparedStatement ps = this.connection.prepareStatement(query)) {
         ps.setLong(1, paroleUntil);
         ps.setString(2, playerName);
         ps.executeUpdate();
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage());
      }
   }

   public long getParoleUntilFromDatabase(String playerName) {
      long paroleUntil = -1L;
      String query = "SELECT parole_until FROM jailed_players WHERE player = ?";

      try (PreparedStatement ps = this.connection.prepareStatement(query)) {
         ps.setString(1, playerName);
         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               paroleUntil = rs.getLong("parole_until");
            }
         }
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage());
      }

      return paroleUntil;
   }

   public void removeJailedPlayerFromDatabase(String playerName) {
      String query = "DELETE FROM jailed_players WHERE player = ?";
      try (PreparedStatement ps = this.connection.prepareStatement(query)) {
         ps.setString(1, playerName);
         ps.executeUpdate();
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage());
      }
   }

   public void removeJailFromDatabase(String jailName) {
      String query = "DELETE FROM jails WHERE name = ?";
      try (PreparedStatement ps = this.connection.prepareStatement(query)) {
         ps.setString(1, jailName);
         ps.executeUpdate();
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage());
      }
   }

   public Map<String, Jail> getJails() {
      return jails;
   }

   public void loadJailedPlayersFromDatabase() {
      String query = "SELECT player, jail_name, parole_until FROM jailed_players";
      try (Statement statement = connection.createStatement();
           ResultSet rs = statement.executeQuery(query)) {

         while (rs.next()) {
            String playerName = rs.getString("player");
            String jailName = rs.getString("jail_name");
            long paroleUntil = rs.getLong("parole_until");
            // 仮釈放中のプレイヤーを適切に処理
            Jail jail = jails.get(jailName);
            if (jail != null) {
               jail.addPlayer(playerName);
               if (paroleUntil > System.currentTimeMillis() / 1000L) {
                  // 仮釈放中の処理
                  Map<String, Long> playerParoleMap = jailedPlayers.getOrDefault(jail, new HashMap<>());
                  playerParoleMap.put(playerName, paroleUntil);
                  jailedPlayers.put(jail, playerParoleMap);
               }
            }
         }
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "プレイヤーのロード中にSQLエラーが発生しました: {0}", e.getMessage());
      }
   }
}
