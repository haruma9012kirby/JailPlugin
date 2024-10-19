package com.example.jail.EventListeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.example.jail.JailPlugin;
import com.example.jail.commands.JailCommand;
import com.example.jail.Jail;
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
        String jailName = plugin.dataBaseMGR.getJailNameFromDatabase(player.getName()); // プレイヤーの刑務所の名前を取得
        long paroleUntil = plugin.dataBaseMGR.getParoleUntil(player.getName()); // 刑期終了時間を取得
        boolean adventureMode = plugin.dataBaseMGR.getAdventureMode(player.getName()); // アドベンチャーモードの有無を取得

        if (jailName != null) {
            long currentTime = System.currentTimeMillis() / 1000L;
            long duration = paroleUntil > currentTime ? paroleUntil - currentTime : JailCommand.INFINITY_DURATION; // 残りの刑期を計算
            Jail jail = plugin.getJails().get(jailName);

            if (jail != null) {
                // オンラインプレイヤーの収監処理
                jailCommand.jailPlayerOnline(player, jail, duration, adventureMode, player);
            }
        }
    }
}
