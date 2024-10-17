package com.example.jail.commands;

import org.bukkit.entity.Player;

import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

// 刑務所の削除
public class RemoveJailCommand {
    private final JailPlugin plugin;

    public RemoveJailCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean handleRemoveJail(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "使用法: /removejail <刑務所>");
            return false;
        } else {
            String jailName = args[0];
            if (this.plugin.jails != null && this.plugin.jails.remove(jailName) != null) {
                if (this.plugin.dataBaseMGR != null) {
                    this.plugin.dataBaseMGR.removeJailFromDatabase(jailName); // 刑務情報をデータベースから削除
                }
                player.sendMessage(ChatColor.GREEN + "刑務所 " + jailName + " が削除されました。");
                if (this.plugin.jailListCommand != null) {
                    this.plugin.jailListCommand.handleJailList(player, new String[0]);
                }
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "刑務所が見つかりません。");
                return false;
            }
        }
    }
}
