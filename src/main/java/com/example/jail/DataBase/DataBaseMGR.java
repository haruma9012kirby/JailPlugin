package com.example.jail.DataBase;

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

// データベース管理
public class DataBaseMGR {
   private static final Logger LOGGER = Logger.getLogger(DataBaseMGR.class.getName()); // ログを出力
   private final boolean mysqlEnabled; // MySQLが有効か
   private final String mysqlHost; // MySQLのホスト
   private final int mysqlPort; // MySQLのポート
   private final String mysqlDatabase; // MySQLのデータベース
   private final String mysqlUsername; // MySQLのユーザー名
   private final String mysqlPassword; // MySQLのパスワード
   private Connection connection; // データベース接続
   private final Map<String, Jail> jails; // 刑務所
   private final Map<Jail, Map<String, Long>> jailedPlayers; // 刑務所の収容プレイヤー
   // データベース管理
   public DataBaseMGR(boolean mysqlEnabled, String mysqlHost, int mysqlPort, String mysqlDatabase, String mysqlUsername, String mysqlPassword) {
      this.mysqlEnabled = mysqlEnabled; // MySQLが有効か
      this.mysqlHost = mysqlHost; // MySQLのホスト
      this.mysqlPort = mysqlPort; // MySQLのポート
      this.mysqlDatabase = mysqlDatabase; // MySQLのデータベース
      this.mysqlUsername = mysqlUsername; // MySQLのユーザー名
      this.mysqlPassword = mysqlPassword; // MySQLのパスワード
      this.jails = new HashMap<>(); // 刑務所
      this.jailedPlayers = new HashMap<>(); // 刑務所の収容プレイヤー
   }

   public void initialize() {
      initializeDatabase(); // データベースを初期化
      loadJailsFromDatabase(); // 刑務所をロード
   }

   public void initializeDatabase() {
      if (mysqlEnabled) { // MySQLが有効か
         try {
            String url = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase; // MySQLのURL
            connection = DriverManager.getConnection(url, mysqlUsername, mysqlPassword); // データベース接続
            LOGGER.info("MySQLデータベースに接続しました。"); // ログを出力
         } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "MySQLデータベースへの接続に失敗しました: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "ホスト: {0}, ポート: {1}, データベース: {2}, ユーザー: {3}", new Object[]{mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername});
            LOGGER.log(Level.SEVERE, "エラー詳細: ", e);
            LOGGER.info("SQLiteにフォールバックします。");
            initializeSQLite(); // SQLiteを初期化
         }
      } else {
         LOGGER.info("MySQLは無効化されています。SQLiteを使用します。"); // ログを出力
         initializeSQLite(); // SQLiteを初期化
      }
   }

   private void initializeSQLite() {
      try {
         String url = "jdbc:sqlite:plugins/JailPlugin/jails.db"; // SQLiteのurl
         connection = DriverManager.getConnection(url); // データベース接続
         LOGGER.info("SQLiteデータベースに接続しました。"); // ログを出力
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLiteデータベースへの接続に失敗しました: {0}", e.getMessage());
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e);
         connection = null; // データベース接続をnullに設定
      }
   }
   // 刑務所テーブルを作成
   public void createJailsTable() throws SQLException {
      String createTableSQL = "CREATE TABLE IF NOT EXISTS jails (" // 刑務所テーブルを作成
                            + "name TEXT PRIMARY KEY, " // 刑務所名
                            + "location TEXT, " // 位置
                            + "capacity INTEGER, " // 容量
                            + "unjail_location TEXT)"; // 釈放地点
      
      try (Statement statement = connection.createStatement()) { // データベース接続を作成
         statement.executeUpdate(createTableSQL); // テーブルを作成
      }
   }
   // 刑務所の収容プレイヤーテーブルを作成
   public void createJailedPlayersTable() throws SQLException {
      String createTableSQL = "CREATE TABLE IF NOT EXISTS jailed_players (" // 刑務所の収容プレイヤーテーブルを作成
                              + "player TEXT PRIMARY KEY, " // プレイヤー
                              + "jail_name TEXT, " // 刑務所名
                              + "start_time INTEGER, " // 開始時間
                              + "duration INTEGER, " // 刑期
                              + "parole_until INTEGER, " // 刑期終了時間
                              + "adventure_mode BOOLEAN)"; // アドベンチャーモード
      
      try (Statement statement = connection.createStatement()) { // データベース接続を作成
         statement.executeUpdate(createTableSQL); // テーブルを作成
      }
   }
   // 刑務所をロード
   public void loadJailsFromDatabase() {
      if (connection == null) { // データベース接続がnullの場合
         LOGGER.log(Level.SEVERE, "データベース接続が確立されていません。"); // ログを出力
         return; // ログを出力
      }
      String query = "SELECT name, location, capacity, unjail_location FROM jails";
      try (Statement statement = connection.createStatement();
           ResultSet rs = statement.executeQuery(query)) {

         while (rs.next()) {
            String name = rs.getString("name"); // 刑務所名
            Location location = stringToLocation(rs.getString("location")); // 位置
            int capacity = rs.getInt("capacity"); // 容量
            String unjailLocationStr = rs.getString("unjail_location"); // 釈放地点
            Location unjailLocation = unjailLocationStr != null ? stringToLocation(unjailLocationStr) : null; // 釈放地点を設定
            jails.put(name, new Jail(name, location, capacity, unjailLocation)); // 刑務所を追加
         }
         JailPlugin plugin = JavaPlugin.getPlugin(JailPlugin.class); // プラグインを取得
         plugin.jails = this.jails; // 刑務所を設定
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "刑務所のロード中にSQLエラーが発生しました: {0}", e.getMessage());
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e);
         if (e.getMessage().contains("no such table")) {
            LOGGER.info("jailsテーブルが存在しません。テーブルを作成します。");
            createTables(); // テーブルを作成
            loadJailsFromDatabase(); // 刑務所をロード
         }
      } catch (Exception e) { // 予期しないエラーが発生した場合
         LOGGER.log(Level.SEVERE, "予期しないエラーが発生しました: {0}", e.getMessage());
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e);
      }
   }
   // 刑務所を保存
   public void saveJailToDatabase(String jailName, Location location, int capacity, Location unjailLocation) {
      String query = "INSERT OR REPLACE INTO jails (name, location, capacity, unjail_location) VALUES (?, ?, ?, ?)"; // 刑務所を保存
      try (PreparedStatement ps = this.connection.prepareStatement(query)) { // データベース接続を作成
         ps.setString(1, jailName); // 刑務所名
         ps.setString(2, locationToString(location)); // 位置
         ps.setInt(3, capacity); // 容量
         ps.setString(4, unjailLocation != null ? locationToString(unjailLocation) : null); // 釈放地点
         ps.executeUpdate(); // 刑務所を保存
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e); // ログを出力
      }
   }
   // 釈放地点を更新
   public void updateUnjailLocationInDatabase(String jailName, Location unjailLocation) {
      String query = "UPDATE jails SET unjail_location = ? WHERE name = ?"; // 釈放地点を更新
      try (PreparedStatement ps = this.connection.prepareStatement(query)) { // データベース接続を作成
         ps.setString(1, locationToString(unjailLocation)); // 釈放地点
         ps.setString(2, jailName); // 刑務所名
         ps.executeUpdate(); // 釈放地点を更新
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e); // ログを出力
      }
   }
   // 刑務所の収容プレイヤーを保存
   public void saveJailedPlayerToDatabase(String playerName, String jailName, long duration, boolean adventureMode) {
      long startTime = System.currentTimeMillis() / 1000L; // 開始時間
      long paroleUntil = duration > 0 ? startTime + duration : -1L; // 刑期終了時間を計算
      String query = "INSERT OR REPLACE INTO jailed_players (player, jail_name, start_time, duration, parole_until, adventure_mode) VALUES (?, ?, ?, ?, ?, ?)"; // 刑務所の収容プレイャーを保存

      try (PreparedStatement ps = this.connection.prepareStatement(query)) { // データベース接続を作成
         ps.setString(1, playerName); // プレイャー
         ps.setString(2, jailName); // 刑務所名
         ps.setLong(3, startTime); // 開始時間
         ps.setLong(4, duration); // 刑期
         ps.setLong(5, paroleUntil); // 刑期終了時間
         ps.setBoolean(6, adventureMode); // アドベンチャーモード
         ps.executeUpdate();
      } catch (SQLException e) {
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage());
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e);
      }
   }
   // 位置を文字列に変換
   public String locationToString(Location location) {
      return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
   }
   // 位置を文字列に変換
   public Location stringToLocation(String locationStr) {
      String[] parts = locationStr.split(",");
      return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
   }
   // 刑務所の収容プレイャーを削除
   public void removeJailedPlayerFromDatabase(String playerName) {
      String query = "DELETE FROM jailed_players WHERE player = ?"; // 刑務所の収容プレイャーを削除
      try (PreparedStatement ps = this.connection.prepareStatement(query)) { // データベース接続を作成
         ps.setString(1, playerName); // プレイャー
         ps.executeUpdate(); // 刑務所の収容プレイャーを削除
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e); // ログを出力
      }
   }
   // 刑務所を削除
   public void removeJailFromDatabase(String jailName) {
      String query = "DELETE FROM jails WHERE name = ?"; // 刑務所を削除
      try (PreparedStatement ps = this.connection.prepareStatement(query)) { // データベース接続を作成
         ps.setString(1, jailName); // 刑務所名
         ps.executeUpdate(); // 刑務所を削除
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e);
      }
   }

   public Map<String, Jail> getJails() {
      return jails; // 刑務所を取得
   }
   // 刑務所の収容プレイャーをロード
   public void loadJailedPlayersFromDatabase() {
      if (connection == null) { // データベース接続がnullの場合
         LOGGER.log(Level.SEVERE, "データベース接続が確立されていません。");
         return;
      }
      String query = "SELECT player, jail_name, parole_until FROM jailed_players"; // 刑務所の収容プレイャーをロード
      try (Statement statement = connection.createStatement();
           ResultSet rs = statement.executeQuery(query)) { // データベース接続を作成

         while (rs.next()) {
            String playerName = rs.getString("player"); // プレイャー
            String jailName = rs.getString("jail_name"); // 刑務所名
            long paroleUntil = rs.getLong("parole_until"); // 刑期終了時間
            Jail jail = jails.get(jailName); // 刑務所を取得
            if (jail != null) { // 刑務所がnullでない場合
               jail.addPlayer(playerName); // プレイャーを追加
               if (paroleUntil > System.currentTimeMillis() / 1000L) { // 刑期終了時間が現在時刻より大きい場合
                  Map<String, Long> playerParoleMap = jailedPlayers.getOrDefault(jail, new HashMap<>()); // プレイャーの刑期終了時間を取得
                  playerParoleMap.put(playerName, paroleUntil); // プレイャーの刑期終了時間を設定
                  jailedPlayers.put(jail, playerParoleMap); // プレイャーの刑期終了時間を設定
               }
            }
         }
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "プレイヤーのロード中にSQLエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e); // ログを出力
      }
   }

   public void createTables() {
      if (connection == null) { // データベース接続がnullの場合
         LOGGER.log(Level.SEVERE, "データベース接続が確立されていません。テーブルを作成できません。"); // ログを出力
         return; // ログを出力
      }
      try {
         createJailsTable(); // 刑務所テーブルを作成
         createJailedPlayersTable(); // 刑務所の収容プレイャーテーブルを作成
         LOGGER.info("必要なテーブルを作成しました。"); // ログを出力
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "テーブルの作成中にエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e); // ログを出力
      }
   }
   // 刑務所名を取得
   public String getJailNameFromDatabase(String playerName) {
      String jailName = null; // 刑務所名
      String query = "SELECT jail_name FROM jailed_players WHERE player = ?"; // 刑務所名を取得
      
      try (PreparedStatement ps = this.connection.prepareStatement(query)) { // データベース接続を作成
         ps.setString(1, playerName); // プレイャー
         try (ResultSet rs = ps.executeQuery()) { // データベース接続を作成
            if (rs.next()) { // 刑務所名を取得
               jailName = rs.getString("jail_name"); // 刑務所名を取得
            }
         }
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e); // ログを出力
      }
      
      return jailName;
   }
   // オフラインプレイヤーの釈放時間を取得
   public long getParoleUntil(String playerName) {
      long paroleUntil = -1; // 刑期終了時間
      String query = "SELECT parole_until FROM jailed_players WHERE player = ?"; // 刑期終了時間を取得

      try (PreparedStatement ps = this.connection.prepareStatement(query)) { // データベース接続を作成
         ps.setString(1, playerName); // プレイャー
         try (ResultSet rs = ps.executeQuery()) { // データベース接続を作成
            if (rs.next()) { // 刑期終了時間を取得
               paroleUntil = rs.getLong("parole_until"); // 刑期終了時間を取得
            }
         }
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e); // ログを出力
      }

      return paroleUntil;
   }

   public long getStartTime(String playerName) {
       long startTime = -1; // 開始時間
       String query = "SELECT start_time FROM jailed_players WHERE player = ?"; // 開始時間を取得

       try (PreparedStatement ps = this.connection.prepareStatement(query)) { // データベース接続を作成
           ps.setString(1, playerName);
           try (ResultSet rs = ps.executeQuery()) { // データベース接続を作成
               if (rs.next()) { // 開始時間を取得
                   startTime = rs.getLong("start_time"); // 開始時間を取得
            }
         }
      } catch (SQLException e) { // SQLエラーが発生した場合
         LOGGER.log(Level.SEVERE, "SQLエラーが発生しました: {0}", e.getMessage()); // ログを出力
         LOGGER.log(Level.SEVERE, "エラー詳細: ", e); // ログを出力
      }
      return startTime; // 開始時間を取得
   }

}
