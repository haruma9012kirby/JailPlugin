package com.example.jail.EventListeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
        
        // プレイヤーの監獄情報を取得
        String jailName = plugin.dataBaseMGR.getJailNameFromDatabase(player.getName());

        if (jailName != null) {
            Jail jail = plugin.getJails().get(jailName);
            if (jail != null) {
                long paroleUntil = plugin.dataBaseMGR.getParoleUntilFromDatabase(player.getName());
                long currentTime = System.currentTimeMillis() / 1000L;

                // 釈放条件を満たしているか
                if (paroleUntil > 0L && currentTime >= paroleUntil) {
                    // プレイヤーを釈放
                    unjailPlayer(player, jail);
                } else {
                    // プレイヤーを収監
                    jailPlayer(player, jail);
                }
            }
        }
    }

    // プレイヤーを釈放する処理
    private void unjailPlayer(Player player, Jail jail) {
        plugin.unJailCommand.unjailPlayer(Bukkit.getConsoleSender(), new String[]{player.getName()});
        player.sendMessage(ChatColor.GREEN + "あなたは釈放されました。");

        // ゲームモードをサバイバルに変更
        player.setGameMode(GameMode.SURVIVAL);

        // 釈放後のテレポート処理
        Location unjailLocation = jail.getUnjailLocation();
        if (unjailLocation != null) {
            player.teleport(unjailLocation);
            player.setRespawnLocation(unjailLocation, true);
        }

        // 経験値レベルアップの音を再生
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            }
        }.runTaskLater(plugin, 6L);
    }

    // プレイヤーを収監する処理
    private void jailPlayer(Player player, Jail jail) {
        // 収監のためのテレポート処理
        Location jailLocation = jail.getLocation();
        if (jailLocation != null) {
            player.teleport(jailLocation);
            player.setRespawnLocation(jailLocation, true);
        }

        // ゲームモードをアドベンチャーモードに変更
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(ChatColor.RED + "あなたは監獄に収監されました。");

        // 金床の音を再生
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                }
            }
        }.runTaskLater(plugin, 20L);
    }
}
