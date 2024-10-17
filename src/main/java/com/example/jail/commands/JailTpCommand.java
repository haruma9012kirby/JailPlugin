package com.example.jail.commands;

import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

public class JailTpCommand {
    private final JailPlugin plugin;

    public JailTpCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean handleJailTp(Player player, String jailName) {
        if (plugin.jails == null || !plugin.jails.containsKey(jailName)) {
            player.sendMessage("監獄が見つかりません。");
            return false;
        }
        
        Jail jail = (Jail) plugin.jails.get(jailName);
        if (jail == null || jail.getLocation() == null) {
            player.sendMessage("監獄の位置情報が無効です。");
            return false;
        }
        
        player.teleport(jail.getLocation());
        player.sendMessage(jailName + " にテレポートしました。");
        return true;
    }
}
