package com.example.jail.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

public class SetUnJailCommand {
    private final JailPlugin plugin;

    public SetUnJailCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }

    // 釈放地点の設定
    public boolean handleSetUnjail(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "使用法:");
            player.sendMessage(ChatColor.YELLOW + "- /setunjail <刑務所>");
            return false;
        }

        String jailName = args[0];
        if (this.plugin.jails == null) {
            player.sendMessage(ChatColor.RED + "刑務所リストが初期化されていません。");
            return false;
        }

        Jail jail = this.plugin.jails.get(jailName);
        if (jail == null) {
            player.sendMessage(ChatColor.RED + "刑務所が見つかりません。");
            return false;
        }

        Location location = player.getLocation();
        jail.setUnjailLocation(location);

        if (this.plugin.dataBaseMGR != null) {
            this.plugin.dataBaseMGR.updateUnjailLocationInDatabase(jailName, location); // 釈放地点をデータベースに更新
        } else {
            player.sendMessage(ChatColor.RED + "データベースマネージャーが初期化されていません。");
            return false;
        }

        player.sendMessage(ChatColor.GREEN + jailName + " の釈放地点が設定されました。");
        return true;
    }
}
