package com.example.jail.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;

// 刑務所情報の表示
public class JailInfoCommand {
    private final JailPlugin plugin;
    private static final long INFINITY_DURATION = -1L; // 無期限の期間を定義

    public JailInfoCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }
    // 刑務所情報の表示
    public boolean handleJailInfo(Player player, String jailName) {
        Jail jail = this.plugin.jails.get(jailName); // 刑務所を取得
        if (jail == null) { // 刑務所がnullの場合
            player.sendMessage(ChatColor.RED + "監獄が見つかりません。");
            return false;
        } else { // 刑務所がnullでない場合
            player.sendMessage(ChatColor.YELLOW + "========== " + ChatColor.RED + "Jail info" + ChatColor.YELLOW + " =========="); // タイトルの表示
            player.sendMessage(ChatColor.BLUE + "監獄名: " + ChatColor.YELLOW + jail.getName()); // 刑務所名の表示
            player.sendMessage(ChatColor.BLUE + "座標: " + ChatColor.YELLOW + this.plugin.formatLocation.formatLocation(jail.getLocation())); // 刑務所の座標の表示
            player.sendMessage(ChatColor.BLUE + "収容人数: " + ChatColor.YELLOW + jail.getCapacity()); // 刑務所の収容人数の表示
            StringBuilder jailedPlayersInfo = new StringBuilder(ChatColor.BLUE + "収容しているプレイヤー: "); // 収容しているプレイヤーの表示

            for (String jailedPlayer : jail.getJailedPlayers()) { // 収容しているプレイヤーを取得
                jailedPlayersInfo.append(jailedPlayer).append(", "); // 収容しているプレイヤーの表示
            }

            if (jailedPlayersInfo.length() > 2) { // 収容しているプレイヤーの表示
                jailedPlayersInfo.setLength(jailedPlayersInfo.length() - 2); // 収容しているプレイヤーの表示
            }

            player.sendMessage(jailedPlayersInfo.toString()); // 収容しているプレイヤーの表示

            // 収容されているプレイヤーの刑期情報を表示
            for (String jailedPlayer : jail.getJailedPlayers()) { // 収容しているプレイヤーを取得
                long paroleUntil = this.plugin.dataBaseMGR.getParoleUntil(jailedPlayer); // 刑期終了時間を取得
                long currentTime = System.currentTimeMillis() / 1000L; // 現在時間を取得
                if (paroleUntil == INFINITY_DURATION) { // 刑期終了時間が無期限の場合
                    player.sendMessage(ChatColor.BLUE + jailedPlayer + "の刑期: " + ChatColor.RED + "無期懲役");
                } else { // 刑期終了時間が無期限でない場合
                    long remainingTime = paroleUntil - currentTime; // 残刑期間を取得
                    String remainingTimeStr = this.plugin.formatDuration.formatDuration(remainingTime); // 残刑期間を表示
                    long totalDuration = paroleUntil - this.plugin.dataBaseMGR.getStartTime(jailedPlayer); // 刑期間を取得
                    String totalDurationStr = this.plugin.formatDuration.formatDuration(totalDuration); // 刑期間を表示
                    player.sendMessage(ChatColor.BLUE + jailedPlayer + "の刑期: " + ChatColor.YELLOW + remainingTimeStr + " / " + totalDurationStr); // 刑期の表示
                }
            }

            // 釈放地点の情報を表示
            Location unjailLocation = jail.getUnjailLocation(); // 釈放地点を取得
            if (unjailLocation != null) { // 釈放地点がnullでない場合
                player.sendMessage(ChatColor.BLUE + "釈放地点:"); // 釈放地点の表示
                player.sendMessage(ChatColor.GOLD + " - ディメンション: " + ChatColor.YELLOW + unjailLocation.getWorld().getName()); // 釈放地点のディメンションの表示
                player.sendMessage(ChatColor.GOLD + " - 座標: " + ChatColor.YELLOW + this.plugin.formatLocation.formatLocation(unjailLocation)); // 釈放地点の座標の表示
            } else { // 釈放地点がnullの場合
                player.sendMessage(ChatColor.BLUE + "釈放地点: 未設定"); // 釈放地点が未設定の場合
            }
            return true;
        }
    }
}
