package com.example.jail.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

public class SetJailCommand {
   private final JailPlugin plugin;

   public SetJailCommand(JailPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean execute(Player player, String[] args) {
      if (args.length < 2) {
         player.sendMessage(ChatColor.RED + "使用法:");
         player.sendMessage(ChatColor.YELLOW + "- /setjail <名前> <収容人数>");
         return false;
      } else {
         String jailName = args[0];
         int capacity;
         try {
            capacity = Integer.parseInt(args[1]);
         } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無効な収容人数です。");
            return false;
         }

         Location location = player.getLocation();
         if (plugin.getJails() != null) {
            plugin.getJails().put(jailName, new Jail(jailName, location, capacity, null));
         } else {
            player.sendMessage(ChatColor.RED + "エラー: 刑務所リストが初期化されていません。");
            return false;
         }
         
         if (plugin.dataBaseMGR != null) {
            plugin.dataBaseMGR.saveJailToDatabase(jailName, location, capacity, null);
         } else {
            player.sendMessage(ChatColor.RED + "エラー: データベースマネージャーが初期化されていません。");
            return false;
         }
         
         player.sendMessage(ChatColor.GREEN + "刑務所 " + jailName + " が設定されました。収容人数: " + capacity);
         return true;
      }
   }
}
