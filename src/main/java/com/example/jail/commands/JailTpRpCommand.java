package com.example.jail.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

public class JailTpRpCommand {
    private final JailPlugin plugin;
    // 
    public JailTpRpCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }

    // 釈放地点へのテレポート
    public boolean handleJailTpRp(Player player, String jailName) {
        if (plugin.jails == null || !plugin.jails.containsKey(jailName)) {
            player.sendMessage(ChatColor.RED + "監獄が見つかりません。");
            return false;
        }

        Jail jail = plugin.jails.get(jailName);
        if (jail == null) {
            player.sendMessage(ChatColor.RED + "監獄が見つかりません。");
            return false;
        }

        Location unjailLocation = jail.getUnjailLocation();
        if (unjailLocation == null) {
            player.sendMessage(ChatColor.RED + "釈放地点が設定されていません。");
            return false;
        }

        player.teleport(unjailLocation);
        player.sendMessage(ChatColor.GREEN + jailName + " の釈放地点にテレポートしました。");
        return true;
    }
}