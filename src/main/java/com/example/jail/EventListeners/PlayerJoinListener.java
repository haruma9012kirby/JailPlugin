package com.example.jail.EventListeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.example.jail.JailPlugin;
import com.example.jail.commands.JailCommand;

// プレイヤーがゲームに参加したときの処理
public class PlayerJoinListener implements Listener {
    private final JailPlugin plugin;
    private final JailCommand jailCommand;

    public PlayerJoinListener(JailPlugin plugin, JailCommand jailCommand) {
        this.plugin = plugin;
        this.jailCommand = jailCommand;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // プレイヤーの監獄情報を取得
        String jailName = plugin.dataBaseMGR.getJailNameFromDatabase(player.getName());

        if (jailName != null) {
            jailCommand.handleJailCommand(player, new String[]{player.getName(), jailName}); // 収監処理をJailCommandに委譲
        }
    }
}
