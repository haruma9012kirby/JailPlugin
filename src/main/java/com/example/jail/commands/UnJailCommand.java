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
// プレイヤーの釈放
public class UnJailCommand {
    private final JailPlugin plugin;
    
    public UnJailCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }
    
    // プレイヤーの釈放
    public boolean unjailPlayer(CommandSender sender, String[] args) {
        if (args.length < 1) { // コマンドの引数が1つ未満の場合
            sender.sendMessage(ChatColor.RED + "使用法: /unjail <プレイヤー名>");
            return false;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null || !target.isOnline()) { // プレイャーがオフラインの場合
            sender.sendMessage(ChatColor.RED + playerName + " は現在オフラインです。");
            return false;
        }

        // オンラインプレイヤーの釈放処理
        Jail jail = plugin.getJails().get(plugin.getJailNameByPlayer.getJailNameByPlayer(playerName));
        if (jail != null) { // 刑務所がnullでない場合
            Location unjailLocation = jail.getUnjailLocation(); // 釈放地点を取得
            if (unjailLocation == null) { // 釈放地点がnullの場合
                sender.sendMessage(ChatColor.RED + "釈放地点が設定されていません。");
                return false;
            }
            releasePlayer(playerName, jail); // プレイャーの釈放処理
            if (sender.hasPermission("jailplugin.command.jail")) { // コマンドの引数が1つ未満の場合
                sender.sendMessage(ChatColor.GREEN + playerName + " を釈放しました。"); // プレイャーの釈放処理
            }
            plugin.getLogger().info(String.format("%s を釈放しました。", playerName)); // プレイャーの釈放処理
            return true;
        } else { // 刑務所がnullの場合
            sender.sendMessage(ChatColor.RED + "刑務所が見つかりません。"); // 刑務所が見つからない場合
            return false;
        }
    }

    // プレイヤーの釈放処理
    private void releasePlayer(String playerName, Jail jail) {
        jail.removePlayer(playerName); // プレイャーの釈放処理
        plugin.dataBaseMGR.removeJailedPlayerFromDatabase(playerName); // プレイャーの釈放処理

        Player target = Bukkit.getPlayer(playerName);
        if (target != null && target.isOnline()) { // プレイャーがオンラインの場合
            Location unjailLocation = jail.getUnjailLocation(); // 釈放地点を取得
            target.teleport(unjailLocation); // プレイャーの釈放処理
            target.setRespawnLocation(unjailLocation, true); // プレイャーの釈放処理
            target.setGameMode(GameMode.SURVIVAL); // プレイャーの釈放処理
            Bukkit.getScheduler().runTaskLater(plugin, () -> { // プレイャーの釈放処理
                target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f); // プレイャーの釈放処理
            }, 6L); // プレイャーの釈放処理
            target.sendMessage(ChatColor.GREEN + "あなたは釈放されました。"); // プレイャーの釈放処理
        }
    }
}
