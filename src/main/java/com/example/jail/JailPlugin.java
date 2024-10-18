package com.example.jail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.example.jail.AnalysisFetchers.GetJailNameByPlayer;
import com.example.jail.AnalysisFetchers.ParseDuration;
import com.example.jail.DataBase.DataBaseMGR;
import com.example.jail.EventHolders.PlayerStatusChecker;
import com.example.jail.EventListeners.PlayerJoinListener;
import com.example.jail.Formats.FormatDuration;
import com.example.jail.Formats.FormatLocation;
import com.example.jail.Tabs.TabComplete;
import com.example.jail.commands.JailCommand;
import com.example.jail.commands.JailInfoCommand;
import com.example.jail.commands.JailListCommand;
import com.example.jail.commands.JailTpCommand;
import com.example.jail.commands.JailTpRpCommand;
import com.example.jail.commands.RemoveJailCommand;
import com.example.jail.commands.SetJailCommand;
import com.example.jail.commands.SetUnJailCommand;
import com.example.jail.commands.UnJailCommand;

// プラグインのメインクラス
public class JailPlugin extends JavaPlugin implements Listener {
   public static final Logger LOGGER = Logger.getLogger(JailPlugin.class.getName());
   public Connection connection;
   public Map<String, Jail> jails = new HashMap<>();
   public SetJailCommand setJailCommand;
   public PlayerStatusChecker playerStatusChecker;
   public JailCommand jailCommand;
   public UnJailCommand unJailCommand;
   public SetUnJailCommand setUnJailCommand;
   public JailListCommand jailListCommand;
   public DataBaseMGR dataBaseMGR;
   public PlayerJoinListener playerJoinListener;
   public JailInfoCommand jailInfoCommand;
   public RemoveJailCommand removeJailCommand;
   public JailTpCommand jailTpCommand;
   public JailTpRpCommand jailTpRpCommand;
   public FormatDuration formatDuration;
   public FormatLocation formatLocation;
   public TabComplete tabComplete;
   public GetJailNameByPlayer getJailNameByPlayer;
   public ParseDuration parseDuration;
   
   // プラグイン有効化時の処理
   @Override
   public void onEnable() {
      saveDefaultConfig();

      // プラグインのデータフォルダを作成
      if (!getDataFolder().exists()) {
         getDataFolder().mkdirs();
      }

      // MySQL設定を読み込む
      boolean mysqlEnabled = getConfig().getBoolean("mysql.enabled"); // MySQLが有効かどうか
      String mysqlHost = getConfig().getString("mysql.host"); // MySQLのホスト名
      int mysqlPort = getConfig().getInt("mysql.port"); // MySQLのポート番号
      String mysqlDatabase = getConfig().getString("mysql.database"); // MySQLのデータベース名
      String mysqlUsername = getConfig().getString("mysql.username"); // MySQLのユーザー名
      String mysqlPassword = getConfig().getString("mysql.password"); // MySQLのパスワード
      
      dataBaseMGR = new DataBaseMGR(mysqlEnabled, mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername, mysqlPassword); // DataBaseMGRのインスタンスを作成
      dataBaseMGR.initialize(); // データベースを初期化
      dataBaseMGR.createTables(); // テーブルを作成

      // データベースから情報をロード
      dataBaseMGR.loadJailsFromDatabase(); // 刑務所の情報をロード
      dataBaseMGR.loadJailedPlayersFromDatabase(); // 収監されたプレイヤーの情報をロード

      playerJoinListener = new PlayerJoinListener(this, jailCommand); // PlayerJoinListenerのインスタンスを作成
      getServer().getPluginManager().registerEvents(playerJoinListener, this); // PlayerJoinListenerを登録

      String[] commands = {"setjail", "jail", "setunjail", "jaillist", "unjail", "removejail", "jailinfo", "jailtp", "jailtp-rp"};
      for (String cmd : commands) {
         PluginCommand pluginCommand = this.getCommand(cmd);
         if (pluginCommand != null) {
            pluginCommand.setExecutor(this); // コマンドを実行
         }
      }

      String[] tabCommands = {"jail", "unjail", "jailtp", "jailtp-rp", "setunjail", "removejail", "jailinfo"};
      for (String cmd : tabCommands) {
         PluginCommand pluginCommand = this.getCommand(cmd);
         if (pluginCommand != null) {
            pluginCommand.setTabCompleter(this); // コマンドのタブ補完を設定
         }
      }

      Bukkit.getScheduler().runTaskTimer(this, () -> {
         for (Player player : Bukkit.getOnlinePlayers()) {
            this.playerStatusChecker.checkPlayerStatus(player); // プレイヤーのステータスをチェック
         }
      }, 0L, 20L); // 20秒ごとに実行

      this.setJailCommand = new SetJailCommand(this); // SetJailCommandのインスタンスを作成
      this.tabComplete = new TabComplete(this.jails); // TabCompleteのインスタンスを作成
      this.formatDuration = new FormatDuration(); // FormatDurationのインスタンスを作成
      this.formatLocation = new FormatLocation(); // FormatLocationのインスタンスを作成
      this.unJailCommand = new UnJailCommand(this); // UnJailCommandのインスタンスを作成
      this.setUnJailCommand = new SetUnJailCommand(this); // SetUnJailCommandのインスタンスを作成
      this.playerStatusChecker = new PlayerStatusChecker(this, this.unJailCommand); // PlayerStatusCheckerのインスタンスを作成
      this.getJailNameByPlayer = new GetJailNameByPlayer(this.jails); // GetJailNameByPlayerのインスタンスを作成
      this.jailInfoCommand = new JailInfoCommand(this); // JailInfoCommandのインスタンスを作成
      this.removeJailCommand = new RemoveJailCommand(this); // RemoveJailCommandのインスタンスを作成
      this.jailListCommand = new JailListCommand(this); // JailListCommandのインスタンスを作成
      this.jailTpCommand = new JailTpCommand(this); // JailTpCommandのインスタンスを作成
      this.jailTpRpCommand = new JailTpRpCommand(this); // JailTpRpCommandのインスタンスを作成
      this.parseDuration = new ParseDuration(); // ParseDurationのインスタンスを作成
      this.jailCommand = new JailCommand(this); // JailCommandのインスタンスを作成

      PluginCommand jailCmd = getCommand("jail");
      if (jailCmd != null) {
         jailCmd.setTabCompleter(tabComplete); // jailコマンドのタブ補完を設定
      }

      PluginCommand unjailCommand = getCommand("unjail");
      if (unjailCommand != null) {
         unjailCommand.setTabCompleter(tabComplete); // unjailコマンドのタブ補完を設定
      }

      PluginCommand setUnjailCommand = getCommand("setunjail");
      if (setUnjailCommand != null) {
         setUnjailCommand.setTabCompleter(tabComplete); // setunjailコマンドのタブ補完を設定
      }

      PluginCommand jailTpCmd = getCommand("jailtp");
      if (jailTpCmd != null) {
         jailTpCmd.setTabCompleter(tabComplete); // jailtpコマンドのタブ補完を設定
      }

      PluginCommand jailTpRpCmd = getCommand("jailtp-rp");
      if (jailTpRpCmd != null) {
         jailTpRpCmd.setTabCompleter(tabComplete); // jailtp-rpコマンドのタブ補完を設定
      }

      PluginCommand removeJailCmd = getCommand("removejail");
      if (removeJailCmd != null) {
         removeJailCmd.setTabCompleter(tabComplete); // removejailコマンドのタブ補完を設定
      }

      PluginCommand jailInfoCmd = getCommand("jailinfo");
      if (jailInfoCmd != null) {
         jailInfoCmd.setTabCompleter(tabComplete); // jailinfoコマンドのタブ補完を設定
      }
   }
   // プラグイン無効化時の処理
   @Override
   public void onDisable() {
      if (connection != null) {
         try {
            connection.close();
         } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "データベース接続のクローズに失敗しました: {0}", e.getMessage());
         }
      }
   }

   // コマンドの実行 
   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (sender instanceof Player) { // コマンドを実行したプレイヤーが存在する場合
         Player player = (Player)sender;
         playerStatusChecker.checkPlayerStatus(player); // プレイヤーのステータスをチェック
         String commandName = command.getName().toLowerCase(); // コマンドの名前を取得
         
         switch(commandName) {
            case "setjail":
               return setJailCommand.execute(player, args); // setjailコマンドを実行
            case "jail":
               return jailCommand.handleJailCommand(player, args); // jailコマンドを実行
            case "setunjail":
               return setUnJailCommand.handleSetUnjail(player, args); // setunjailコマンドを実行
            case "jaillist":
               return jailListCommand.handleJailList(player, args); // jaillistコマンドを実行
            case "unjail":
               return unJailCommand.unjailPlayer(player, args); // unjailコマンドを実行
            case "jailinfo":
               jailInfoCommand.handleJailInfo(player, args.length > 0 ? args[0] : null); // jailinfoコマンドを実行
               return true;
            case "jailtp":
               return jailTpCommand.handleJailTp(player, args.length > 0 ? args[0] : null); // jailtpコマンドを実行
            case "jailtp-rp":
               return jailTpRpCommand.handleJailTpRp(player, args.length > 0 ? args[0] : null); // jailtp-rpコマンドを実行
            case "removejail":
               return removeJailCommand.handleRemoveJail(player, args); // removejailコマンドを実行
            default:
               return false;
         }
      }
      return false; // コマンドが実行できない場合はfalseを返す
   }

   // 刑務所の取得
   public Map<String, Jail> getJails() {
      return jails; // 刑務所の情報を取得
   }
}
