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
    public void unjailPlayer(Player player, String[] args) {
        if (args == null || args.length < 1) {
            player.sendMessage(ChatColor.RED + "使用法: /unjail <プレイヤー名> [仮釈放時間]");
            return;
        }
        // プレイヤー名を取得
        String playerName = args[0];
        // プレイヤーの刑務所名を取得
        String jailName = plugin.getJailNameByPlayer.getJailNameByPlayer(playerName);

        // プレイヤーが収監されていない場合
        if (jailName == null) {
            player.sendMessage(ChatColor.RED + "プレイヤーは収監されていません。");
            return;
        }

        // 刑務所を取得
        Jail foundJail = plugin.getJails().get(jailName);

        // 刑務所が見つからない場合
        if (foundJail == null) {
            player.sendMessage(ChatColor.RED + "刑務所が見つかりません。");
            return;
        }

        // 仮釈放時間が指定されている場合
        if (args.length > 1) {
            handleParole(player, playerName, foundJail, args[1]);
        } else {
            handleRelease(player, playerName, foundJail);
        }
    }

    // 仮釈放の処理
    private void handleParole(Player player, String playerName, Jail jail, String durationString) {
        long paroleDuration;
        try {
            paroleDuration = plugin.parseDuration.parseDuration(durationString);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "無効な仮釈放期間です。正しいフォーマットを使用してください (例: 1h30m)。");
            return;
        }

        releasePlayer(player, playerName, jail);

        player.sendMessage(ChatColor.GREEN + "あなたは仮釈放されました。仮釈放期間: " + plugin.formatDuration.formatDuration(paroleDuration));

        long paroleTicks = paroleDuration * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                jail.addPlayer(playerName);
                player.teleport(jail.getLocation());
                player.setRespawnLocation(jail.getLocation(), true);
                player.setGameMode(GameMode.ADVENTURE);
                player.sendMessage(ChatColor.RED + "仮釈放期間が終了し、再び収監されました。");
            } else {
                plugin.playerJoinListener.handleOfflinePlayerRejail(playerName, jail);
            }
            plugin.dataBaseMGR.saveJailedPlayerToDatabase(playerName, jail.getName(), -1L, -1L, true);
        }, paroleTicks);
    }

    // 釈放の処理
    private void handleRelease(Player player, String playerName, Jail jail) {
        releasePlayer(player, playerName, jail);
        player.sendMessage(ChatColor.GREEN + "あなたは釈放されました。");
    }

    // プレイヤーの釈放
    private void releasePlayer(Player player, String playerName, Jail jail) {
        jail.removePlayer(playerName);
        plugin.dataBaseMGR.removeJailedPlayerFromDatabase(playerName);

        Location unjailLocation = jail.getUnjailLocation();
        if (unjailLocation != null) {
            player.teleport(unjailLocation);
            player.setRespawnLocation(unjailLocation, true);
            player.setGameMode(GameMode.SURVIVAL);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }, 6L);
        } else {
            player.sendMessage(ChatColor.RED + "釈放地点が設定されていません。");
        }
    }
}
