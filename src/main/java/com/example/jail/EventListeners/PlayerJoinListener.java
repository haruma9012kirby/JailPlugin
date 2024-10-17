package com.example.jail.EventListeners;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
       String jailName = this.plugin.getJailNameByPlayer.getJailNameByPlayer(player.getName());
       
       if (jailName != null) {
           long paroleUntil = this.plugin.dataBaseMGR.getParoleUntilFromDatabase(player.getName());
           
           if (paroleUntil > 0L && System.currentTimeMillis() / 1000L >= paroleUntil) {
               Jail jail = this.plugin.getJails().get(jailName);
               
               if (jail != null) {
                   this.plugin.unJailCommand.unjailPlayer(player, new String[]{player.getName()});
                   player.sendMessage(ChatColor.GREEN + "あなたは釈放されました。");
                   player.setRespawnLocation(jail.getUnjailLocation(), true);
                   player.setGameMode(GameMode.SURVIVAL);
                   
                   new BukkitRunnable() {
                       @Override
                       public void run() {
                           if (player.isOnline()) {
                               player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                           }
                       }
                   }.runTaskLater(plugin, 6L);
               }
           }
       }
   }
}
