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
   public static final long INFINITY_DURATION = -1L; // 無期限の期間を定義

   public JailCommand(JailPlugin plugin) {
      this.plugin = plugin;
      this.jails = plugin.getJails();
   }

   // コマンドの実行
   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player) { // コマンドを実行したプレイヤーが存在するか
         Player player = (Player) sender;
         return handleJailCommand(player, args); // プレイヤーの収監
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
      if (args.length >= 3 && args[2].equalsIgnoreCase("infinity")) {
         duration = INFINITY_DURATION;
      } else {
         duration = args.length >= 3 ? plugin.parseDuration.parseDuration(args[2]) : INFINITY_DURATION;
      }

      boolean adventureMode = args.length >= 4 && Boolean.parseBoolean(args[3]);

      Player target = Bukkit.getPlayer(playerName);
      if (target == null || !target.isOnline()) {
         player.sendMessage(ChatColor.RED + playerName + " は現在オフラインです。次回参加時に収監されます。");
         plugin.dataBaseMGR.offlinePlayerNextJoin(playerName, jailName, duration, adventureMode);
         return true;
      }

      // オンラインプレイヤーの収監処理
      jailPlayerOnline(target, jail, duration, adventureMode, player);
      return true;
   }

   // プレイヤーを収監
   public boolean jailPlayer(CommandSender sender, String[] args) {
      if (args.length < 2) { // コマンドの引数が2つ未満の場合
         sender.sendMessage(ChatColor.RED + "使用法: /jail <プレイヤー名> <監獄名> [期間] [アドベンチャーモード(true/false)]");
         return false;
      }
      
      String playerName = args[0];
      String jailName = args[1];

      Player target = Bukkit.getPlayer(playerName);

      if (target == null || !target.isOnline()) { // プレイヤーがオフラインの場合
         sender.sendMessage(ChatColor.RED + playerName + " は現在オフラインです。");
         return false;
      }

      // オンラインプレイヤーの収監処理
      Jail jail = plugin.getJails().get(jailName); // 刑務所を取得
      if (jail == null) { // 刑務所がnullの場合
         sender.sendMessage(ChatColor.RED + "指定された監獄が見つかりません。");
         return false;
      }
      // 収監処理を実行
      return handleJailCommand((Player) sender, args); // プレイヤーの収監
   }

   // オンラインプレイヤーの収監処理
   public void jailPlayerOnline(Player target, Jail jail, long duration, boolean adventureMode, Player player) {
      jail.addPlayer(target.getName()); // プレイヤーを刑務所に収監
      plugin.dataBaseMGR.saveJailedPlayerToDatabase(target.getName(), jail.getName(), duration, adventureMode); // プレイヤーをデータベースに保存

      Location jailLocation = jail.getLocation(); // 刑務所の位置を取得
      if (jailLocation == null) { // 刑務所の位置がnullの場合
         player.sendMessage(ChatColor.RED + "エラー: 刑務所の位置が設定されていません。");
         return;
      }
      // プレイヤーのリスポーン地点を設定
      target.setRespawnLocation(jailLocation, true);
      // プレイヤーを刑務所の位置にテレポート
      target.teleport(jailLocation);
      // プレイヤーの位置を取得
      Location targetLocation = target.getLocation();
      // プレイヤーの位置が取得できた場合、金床の使用音を再生
      if (adventureMode) {
         target.setGameMode(GameMode.ADVENTURE);
      }
      if (targetLocation != null) { // プレイヤーの位置が取得できた場合
         Bukkit.getScheduler().runTaskLater(plugin, () -> { // 金床の使用音を再生
            target.playSound(targetLocation, Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
         }, 6L);
      } else { // プレイヤーの位置が取得できなかった場合
         player.sendMessage(ChatColor.RED + "エラー: プレイヤーの位置を取得できませんでした。");
      }
      // 刑期が無期の場合
      if (duration == INFINITY_DURATION) {
         target.sendMessage(ChatColor.RED + "あなたは無期懲役で監獄に収監されました。");
      } else { // 刑期が無期でない場合
         String formattedDuration = plugin.formatDuration.formatDuration(duration);
         target.sendMessage(ChatColor.RED + "あなたは " + formattedDuration + " の間、監獄に収監されました。");
         player.sendMessage(ChatColor.GREEN + target.getName() + " が " + jail.getName() + " に " + formattedDuration + " の間、収監されました。");
      }
   }

   // オフラインプレイヤーの次回参加時の収監処理
   public void offlinePlayerNextJoin(String playerName, String jailName, long duration, boolean adventureMode) {
      plugin.dataBaseMGR.offlinePlayerNextJoin(playerName, jailName, duration, adventureMode); // プレイヤーをデータベースに保存
   }
}
