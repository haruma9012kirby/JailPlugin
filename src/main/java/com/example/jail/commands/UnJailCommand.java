package com.example.jail.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

public class UnJailCommand {
    private final JailPlugin plugin;
    
    public UnJailCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }
    
    // プレイヤーの釈放
    public boolean unjailPlayer(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "使用法: /unjail <プレイヤー名>");
            return false;
        }
        
        String playerName = args[0];
        String jailName = plugin.getJailNameByPlayer.getJailNameByPlayer(playerName);

        if (jailName == null) {
            player.sendMessage(ChatColor.RED + "プレイヤーは収監されていません。");
            return false;
        }

        Jail foundJail = plugin.getJails().get(jailName);
        if (foundJail == null) {
            player.sendMessage(ChatColor.RED + "刑務所が見つかりません。");
            return false;
        }
        releasePlayer(playerName, foundJail);
        return true;
    }

    // プレイヤーの釈放
    private void releasePlayer(String playerName, Jail jail) {
        jail.removePlayer(playerName);
        plugin.dataBaseMGR.removeJailedPlayerFromDatabase(playerName);

        Player target = Bukkit.getPlayer(playerName);
        if (target != null && target.isOnline()) {
            Location unjailLocation = jail.getUnjailLocation();
            if (unjailLocation != null) {
                target.teleport(unjailLocation);
                target.setRespawnLocation(unjailLocation, true);
                target.setGameMode(GameMode.SURVIVAL);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }, 6L);
            } else {
                target.sendMessage(ChatColor.RED + "釈放地点が設定されていません。");
            }
        }
    }
}
