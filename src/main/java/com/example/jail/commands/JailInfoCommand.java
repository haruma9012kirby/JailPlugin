package com.example.jail.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

// 刑務所情報の表示
public class JailInfoCommand {
    private final JailPlugin plugin;
    
    public JailInfoCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean handleJailInfo(Player player, String jailName) {
        Jail jail = this.plugin.jails.get(jailName);
        if (jail == null) {
            player.sendMessage(ChatColor.RED + "監獄が見つかりません。");
            return false;
        } else {
            player.sendMessage(ChatColor.YELLOW + "========== " + ChatColor.RED + "Jail info" + ChatColor.YELLOW + " ==========");
            player.sendMessage(ChatColor.BLUE + "監獄名: " + ChatColor.YELLOW + jail.getName());
            player.sendMessage(ChatColor.BLUE + "座標: " + ChatColor.YELLOW + this.plugin.formatLocation.formatLocation(jail.getLocation()));
            player.sendMessage(ChatColor.BLUE + "収容人数: " + ChatColor.YELLOW + jail.getCapacity());
            StringBuilder jailedPlayersInfo = new StringBuilder(ChatColor.BLUE + "収容しているプレイヤー: ");

            for (String jailedPlayer : jail.getJailedPlayers()) {
                jailedPlayersInfo.append(jailedPlayer).append(", ");
            }

            if (jailedPlayersInfo.length() > 2) {
                jailedPlayersInfo.setLength(jailedPlayersInfo.length() - 2);
            }

            player.sendMessage(jailedPlayersInfo.toString());
            
            // 釈放地点の情報を表示
            Location unjailLocation = jail.getUnjailLocation();
            if (unjailLocation != null) {
                player.sendMessage(ChatColor.BLUE + "釈放地点:");
                player.sendMessage(ChatColor.GOLD + " - ディメンション: " + ChatColor.YELLOW + unjailLocation.getWorld().getName());
                player.sendMessage(ChatColor.GOLD + " - 座標: " + ChatColor.YELLOW + this.plugin.formatLocation.formatLocation(unjailLocation));
            } else {
                player.sendMessage(ChatColor.BLUE + "釈放地点: 未設定");
            }

            return true;
        }
    }
}
