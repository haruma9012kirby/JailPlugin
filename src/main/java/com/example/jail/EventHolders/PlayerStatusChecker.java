package com.example.jail.EventHolders;

import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;
import com.example.jail.commands.UnJailCommand;

// プレイヤーのステータスをチェックするクラス
public class PlayerStatusChecker {
   private final JailPlugin plugin;
   private final UnJailCommand unJailCommand;
   public PlayerStatusChecker(JailPlugin plugin, UnJailCommand unJailCommand) {
      this.plugin = plugin;
      this.unJailCommand = unJailCommand;
   }

   // プレイヤーのステータスをチェックするメソッド
   public void checkPlayerStatus(Player player) {
      if (player == null || plugin == null || plugin.getJailNameByPlayer == null || plugin.dataBaseMGR == null || plugin.getJails() == null || unJailCommand == null) {
         return;
      }

      String playerName = player.getName(); // プレイヤーの名前を取得
      String jailName = this.plugin.getJailNameByPlayer.getJailNameByPlayer(playerName); // プレイヤーの刑務所の名前を取得
      if (jailName != null) { // 刑務所の名前がnullでない場合
         Jail jail = this.plugin.getJails().get(jailName); // 刑務所を取得
         if (jail != null) { // 刑務所がnullでない場合
            long paroleUntil = plugin.dataBaseMGR.getParoleUntil(playerName); // 刑期終了時間を取得
            if (paroleUntil > 0 && paroleUntil <= System.currentTimeMillis() / 1000L) { // 刑期終了時間が0以上で、現在時間以下の場合
               unJailCommand.unjailPlayer(player, new String[]{playerName}); // プレイヤーを釈放
            }
         }
      }
   }
}
