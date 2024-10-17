package com.example.jail.EventListeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

public class PlayerJoinListener implements Listener {
   private final JailPlugin plugin;

   public PlayerJoinListener(JailPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      String playerName = player.getName();
      String jailName = this.plugin.getJailNameByPlayer.getJailNameByPlayer(playerName);
      if (jailName != null) { // 仮釈放期間が設定されている場合
         long paroleUntil = this.plugin.dataBaseMGR.getParoleUntilFromDatabase(playerName);
         if (paroleUntil > 0L && System.currentTimeMillis() / 1000L >= paroleUntil) { // 仮釈放期間が終了した場合
            Jail jail = this.plugin.getJails().get(jailName);
            if (jail != null) {
               this.plugin.unJailCommand.unjailPlayer(player, new String[]{playerName}); // プレイヤーを釈放
            }
         }
      }
   }
// オフラインプレイヤーの再収監
   public void handleOfflinePlayerRejail(String playerName, Jail jail) {
      Bukkit.getScheduler().runTaskLater(plugin, () -> { // 遅延実行
         Player onlineTarget = Bukkit.getPlayer(playerName); // オンラインプレイヤーを取得
         if (onlineTarget != null) {
            onlineTarget.teleport(jail.getLocation()); // プレイヤーを刑務所の位置にテレポート
            try {
               onlineTarget.setRespawnLocation(jail.getLocation(), true); // プレイヤーのリスポーン地点を設定
            } catch (IllegalArgumentException e) {
               // ベッドスポーン設定に失敗した場合のエラー処理
               plugin.getLogger().warning(String.format("プレイヤー %s のベッドスポーン設定に失敗しました: %s", playerName, e.getMessage()));
            }
            onlineTarget.setGameMode(GameMode.ADVENTURE); // アドベンチャーモードを有効にする
            onlineTarget.sendMessage(ChatColor.RED + "あなたの仮釈放期間が終了し、再び収監されました。"); // プレイヤーにメッセージを送信
         }
      }, 20L); // 20ティック = 1秒
   }
}
