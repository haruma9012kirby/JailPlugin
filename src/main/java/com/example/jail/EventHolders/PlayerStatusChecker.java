package com.example.jail.EventHolders;

import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;
import com.example.jail.commands.UnJailCommand;

public class PlayerStatusChecker {
   private final JailPlugin plugin;
   private final UnJailCommand unJailCommand;

   public PlayerStatusChecker(JailPlugin plugin, UnJailCommand unJailCommand) {
      this.plugin = plugin;
      this.unJailCommand = unJailCommand;
   }

   public void checkPlayerStatus(Player player) {
      if (player == null || plugin == null || plugin.getJailNameByPlayer == null || plugin.dataBaseMGR == null || plugin.getJails() == null || unJailCommand == null) {
         return;
      }

      String playerName = player.getName();
      String jailName = this.plugin.getJailNameByPlayer.getJailNameByPlayer(playerName);
      if (jailName != null) {
         long paroleUntil = this.plugin.dataBaseMGR.getParoleUntilFromDatabase(playerName);
         if (paroleUntil > 0L && System.currentTimeMillis() / 1000L >= paroleUntil) {
            Jail jail = this.plugin.getJails().get(jailName);
            if (jail != null) {
               unJailCommand.unjailPlayer(player, new String[]{playerName}); // プレイヤーを釈放
            }
         }
      }
   }
}
