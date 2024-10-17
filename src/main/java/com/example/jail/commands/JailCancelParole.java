package com.example.jail.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

// 仮釈放の取りやめ
public class JailCancelParole {
   private final JailPlugin plugin;
   public JailCancelParole(JailPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean handleJailCancelParole(Player player, String[] args) {
      if (args.length < 1) {
         player.sendMessage(ChatColor.RED + "使用法: /jailcancelparole <プレイヤー>");
         return false;
      }

      String targetName = args[0];
      String jailName = this.plugin.getJailNameByPlayer.getJailNameByPlayer(targetName);
      if (jailName == null) {
         player.sendMessage(ChatColor.RED + "プレイヤーは収監されていません。");
         return false;
      }

      this.plugin.dataBaseMGR.updateParoleUntilInDatabase(targetName, -1L);
      player.sendMessage(ChatColor.GREEN + targetName + " の仮釈放が取りやめられました。");
      Player target = Bukkit.getPlayer(targetName);
      if (target != null) {
         target.sendMessage(ChatColor.RED + "あなたの仮釈放は取りやめられました。");
      }
      return true;
   }
}
