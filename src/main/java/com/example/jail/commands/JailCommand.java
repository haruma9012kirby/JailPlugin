package com.example.jail.commands;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

public class JailCommand implements CommandExecutor {
   private final JailPlugin plugin;
   private final Map<String, Jail> jails;

   public JailCommand(JailPlugin plugin) {
      this.plugin = plugin;
      this.jails = plugin.getJails();
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player) {
         Player player = (Player) sender;
         return handleJailCommand(player, args);
      }
      return false;
   }

   // プレイヤーの収監
   public boolean handleJailCommand(Player player, String[] args) {
      if (args.length < 2) {
         player.sendMessage(ChatColor.RED + "使用法:");
         player.sendMessage(ChatColor.YELLOW + "- /jail <プレイヤー> <刑務所> [期間] [アドベンチャーモード(true/false)]");
         return false;
      }

      String playerName = args[0];
      String jailName = args[1];
      Jail jail = this.jails.get(jailName);
      
      if (jail == null) {
         player.sendMessage(ChatColor.RED + "刑務所が見つかりません");
         return false;
      }

      if (jail.isFull()) {
         player.sendMessage(ChatColor.RED + "刑務所が満員です。");
         return false;
      }

      long duration;
      if (args.length >= 3 && "infiniti".equalsIgnoreCase(args[2])) {
         duration = -1L; // 無期限を示す
      } else {
         duration = plugin.parseDuration.parseDuration(args.length >= 3 ? args[2] : null);
      }
      boolean adventureMode = args.length >= 4 && Boolean.parseBoolean(args[3]);

      Player target = Bukkit.getPlayer(playerName);
      if (target == null || !target.isOnline()) {
         // オフラインプレイヤーの場合
         long paroleUntil = duration > 0L ? System.currentTimeMillis() / 1000L + duration : -1L;
         plugin.dataBaseMGR.saveJailedPlayerToDatabase(playerName, jailName, duration, paroleUntil, adventureMode);
         player.sendMessage(ChatColor.YELLOW + playerName + " は現在オフラインです。次回オンライン時に収監されます。");
         return true;
      }

      jail.addPlayer(target.getName());
      plugin.dataBaseMGR.saveJailedPlayerToDatabase(target.getName(), jailName, duration, -1L, adventureMode);

      if (duration > 0L) {
         long paroleUntil = System.currentTimeMillis() / 1000L + duration;
         plugin.dataBaseMGR.updateParoleUntilInDatabase(target.getName(), paroleUntil);
      }

      Location jailLocation = jail.getLocation();
      if (jailLocation == null) {
         player.sendMessage(ChatColor.RED + "エラー: 刑務所の位置が設定されていません。");
         return false;
      }
      // プレイヤーのリスポーン地点を設定
      target.setRespawnLocation(jailLocation, true);
      // プレイヤーを刑務所の位置にテレポート
      target.teleport(jailLocation);
      // プレイヤーの位置を取得
      Location targetLocation = target.getLocation();
      // プレイヤーの位置が取得できた場合、金床の使用音を再生
      if (targetLocation != null) {
         Bukkit.getScheduler().runTaskLater(plugin, () -> {
            target.playSound(targetLocation, Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
         }, 6L);
      } else {
         player.sendMessage(ChatColor.RED + "エラー: プレイヤーの位置を取得できませんでした。");
      }
      // 刑期が無期の場合
      if (duration == -1L) {
         target.sendMessage(ChatColor.RED + "あなたは無期懲役で監獄に収監されました。");
         player.sendMessage(ChatColor.GREEN + target.getName() + " が " + jailName + " に無期懲役で収監されました。");
      } else {
         String formattedDuration = plugin.formatDuration.formatDuration(duration);
         target.sendMessage(ChatColor.RED + "あなたは " + formattedDuration + " の間、監獄に収監されました。");
         player.sendMessage(ChatColor.GREEN + target.getName() + " が " + jailName + " に " + formattedDuration + " の間、収監されました。");
      }

      // アドベンチャーモードを有効にする
      if (adventureMode) {
         target.setGameMode(GameMode.ADVENTURE);
      }

      return true;
   }

   // オフラインプレイヤーの収監データを保存
   private void saveOfflineJailData(String playerName, String jailName, long duration, long paroleUntil, boolean adventureMode) {
      plugin.dataBaseMGR.saveJailedPlayerToDatabase(playerName, jailName, duration, paroleUntil, adventureMode);
   }

   // オフラインプレイヤーを収監
   public void jailOfflinePlayer(CommandSender sender, String playerName, String jailName) {
      plugin.dataBaseMGR.saveJailedPlayerToDatabase(playerName, jailName, -1L, -1L, false);
      sender.sendMessage(ChatColor.YELLOW + playerName + " は次回参加時に収監されます。");
   }

   // プレイヤーを収監
   public boolean jailPlayer(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(ChatColor.RED + "使用法: /jail <プレイヤー名> <監獄名> [期間] [アドベンチャーモード(true/false)]");
         return false;
      }
      
      String playerName = args[0];
      String jailName = args[1];
      long duration = args.length >= 3 ? plugin.parseDuration.parseDuration(args[2]) : -1L;
      boolean adventureMode = args.length >= 4 && Boolean.parseBoolean(args[3]);

      Player target = Bukkit.getPlayer(playerName);

      if (target == null || !target.isOnline()) {
         saveOfflineJailData(playerName, jailName, duration, -1L, adventureMode);
         sender.sendMessage(ChatColor.YELLOW + playerName + " は現在オフラインです。次回オンライン時に収監されます。");
         return true;
      }

      // オンラインプレイヤーの収監処理
      Jail jail = plugin.getJails().get(jailName);
      if (jail == null) {
         sender.sendMessage(ChatColor.RED + "指定された監獄が見つかりません。");
         return false;
      }
      // 収監処理を実行
      return handleJailCommand((Player) sender, args);
   }
}
