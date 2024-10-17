package com.example.jail.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
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
    public boolean unjailPlayer(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "使用法: /unjail <プレイヤー名>");
            return false;
        }
        
        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);
        String jailName = plugin.getJailNameByPlayer.getJailNameByPlayer(playerName);

        if (jailName == null) {
            sender.sendMessage(ChatColor.RED + "プレイヤーは収監されていません。");
            return false;
        }

        if (target == null || !target.isOnline()) {
            unjailOfflinePlayer(sender, playerName, jailName);
            return true;
        }

        Jail foundJail = plugin.getJails().get(jailName);
        if (foundJail == null) {
            sender.sendMessage(ChatColor.RED + "刑務所が見つかりません。");
            return false;
        }

        releasePlayer(playerName, foundJail);
        sender.sendMessage(ChatColor.GREEN + playerName + " を釈放しました。");
        return true;
    }

    // プレイヤーの釈放処理
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
                target.sendMessage(ChatColor.GREEN + "あなたは釈放されました。");
            } else {
                target.sendMessage(ChatColor.RED + "釈放地点が設定されていません。");
            }
        }
    }

    // オフラインプレイヤーの釈放処理
    private void unjailOfflinePlayer(CommandSender sender, String playerName, String jailName) {
        Jail jail = plugin.getJails().get(jailName);
        if (jail != null) {
            jail.removePlayer(playerName);
            plugin.dataBaseMGR.removeJailedPlayerFromDatabase(playerName);
            sender.sendMessage(ChatColor.YELLOW + playerName + " は次回参加時に釈放されます。");
        } else {
            sender.sendMessage(ChatColor.RED + "刑務所が見つかりません。");
        }
    }
}
