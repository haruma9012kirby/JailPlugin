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
import com.example.jail.commands.JailCancelParole;
import com.example.jail.commands.JailCommand;
import com.example.jail.commands.JailInfoCommand;
import com.example.jail.commands.JailListCommand;
import com.example.jail.commands.JailTpCommand;
import com.example.jail.commands.JailTpRpCommand;
import com.example.jail.commands.RemoveJailCommand;
import com.example.jail.commands.SetJailCommand;
import com.example.jail.commands.SetUnJailCommand;
import com.example.jail.commands.UnJailCommand;

import net.md_5.bungee.api.ChatColor;

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
   public JailCancelParole jailCancelParole;
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
      dataBaseMGR = new DataBaseMGR();
      dataBaseMGR.initializeDatabase();

      // データベースから情報をロード
      dataBaseMGR.loadJailsFromDatabase();
      dataBaseMGR.loadJailedPlayersFromDatabase();

      playerJoinListener = new PlayerJoinListener(this);
      getServer().getPluginManager().registerEvents(playerJoinListener, this);

      String[] commands = {"setjail", "jail", "setunjail", "jaillist", "unjail", "removejail", "jailcancelparole", "jailinfo", "jailtp", "jailtp-rp"};
      for (String cmd : commands) {
         PluginCommand pluginCommand = this.getCommand(cmd);
         if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
         }
      }

      String[] tabCommands = {"jail", "unjail", "jailcancelparole", "jailtp", "jailtp-rp", "setunjail", "removejail", "jailinfo"};
      for (String cmd : tabCommands) {
         PluginCommand pluginCommand = this.getCommand(cmd);
         if (pluginCommand != null) {
            pluginCommand.setTabCompleter(this);
         }
      }

      Bukkit.getScheduler().runTaskTimer(this, () -> {
         for (Player player : Bukkit.getOnlinePlayers()) {
            this.playerStatusChecker.checkPlayerStatus(player);
         }
      }, 0L, 20L);

      this.setJailCommand = new SetJailCommand(this);
      this.tabComplete = new TabComplete(this.jails);
      this.formatDuration = new FormatDuration();
      this.formatLocation = new FormatLocation();
      this.unJailCommand = new UnJailCommand(this);
      this.setUnJailCommand = new SetUnJailCommand(this);
      this.playerStatusChecker = new PlayerStatusChecker(this, this.unJailCommand);
      this.getJailNameByPlayer = new GetJailNameByPlayer(this.jails);
      this.jailInfoCommand = new JailInfoCommand(this);
      this.jailCancelParole = new JailCancelParole(this);
      this.removeJailCommand = new RemoveJailCommand(this);
      this.jailListCommand = new JailListCommand(this);
      this.jailTpCommand = new JailTpCommand(this);
      this.jailTpRpCommand = new JailTpRpCommand(this);
      this.parseDuration = new ParseDuration();
      this.jailCommand = new JailCommand(this);

      PluginCommand jailCmd = getCommand("jail");
      if (jailCmd != null) {
         jailCmd.setTabCompleter(tabComplete);
      }

      PluginCommand unjailCommand = getCommand("unjail");
      if (unjailCommand != null) {
         unjailCommand.setTabCompleter(tabComplete);
      }

      PluginCommand setUnjailCommand = getCommand("setunjail");
      if (setUnjailCommand != null) {
         setUnjailCommand.setTabCompleter(tabComplete);
      }

      PluginCommand jailTpCmd = getCommand("jailtp");
      if (jailTpCmd != null) {
         jailTpCmd.setTabCompleter(tabComplete);
      }

      PluginCommand jailTpRpCmd = getCommand("jailtp-rp");
      if (jailTpRpCmd != null) {
         jailTpRpCmd.setTabCompleter(tabComplete);
      }

      PluginCommand removeJailCmd = getCommand("removejail");
      if (removeJailCmd != null) {
         removeJailCmd.setTabCompleter(tabComplete);
      }

      PluginCommand jailInfoCmd = getCommand("jailinfo");
      if (jailInfoCmd != null) {
         jailInfoCmd.setTabCompleter(tabComplete);
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
      if (sender instanceof Player) {
         Player player = (Player)sender;
         playerStatusChecker.checkPlayerStatus(player);
         String commandName = command.getName().toLowerCase();
         
         switch(commandName) {
            case "setjail":
               return setJailCommand.execute(player, args);
            case "jail":
               return jailCommand.handleJailCommand(player, args);
            case "setunjail":
               return setUnJailCommand.handleSetUnjail(player, args);
            case "jaillist":
               return jailListCommand.handleJailList(player, args);
            case "unjail":
               if (args.length < 1) {
                  player.sendMessage(ChatColor.RED + "使用法: /unjail <プレイヤー名>");
                  return false;
               }
               unJailCommand.unjailPlayer(player, args);
               return true;
            case "jailinfo":
               jailInfoCommand.handleJailInfo(player, args.length > 0 ? args[0] : null);
               return true;
            case "jailtp":
               return jailTpCommand.handleJailTp(player, args.length > 0 ? args[0] : null);
            case "jailtp-rp":
               return jailTpRpCommand.handleJailTpRp(player, args.length > 0 ? args[0] : null);
            case "removejail":
               return removeJailCommand.handleRemoveJail(player, args);
            case "jailcancelparole":
               return jailCancelParole.handleJailCancelParole(player, args);
            default:
               return false;
         }
      }
      return false;
   }

   // 刑務所の取得
   public Map<String, Jail> getJails() {
      return jails;
   }
}
