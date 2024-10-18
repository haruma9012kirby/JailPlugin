package com.example.jail.Tabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.example.jail.Jail;

// コマンドのタブ補完
public class TabComplete implements TabCompleter {
    private final Map<String, Jail> jails;

    public TabComplete(Map<String, Jail> jails) {
        this.jails = jails;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "jail":
                return completeJailCommand(args); // jailコマンドのタブ補完
            case "unjail":
                return completeUnjailCommand(args); // unjailコマンドのタブ補完
            case "setunjail":
                return completeSetUnjailCommand(args); // setunjailコマンドのタブ補完
            case "jailtp":
                return completeJailTpCommand(args); // jailtpコマンドのタブ補完
            case "jailtp-rp":
                return completeJailTpRpCommand(args); // jailtp-rpコマンドのタブ補完
            case "removejail":
                return completeJailNameCommand(args); // removejailコマンドのタブ補完
            case "jailinfo":
                return completeJailInfoCommand(args); // jailinfoコマンドのタブ補完
            default:
                return null;
        }
    }

    // 全プレイヤー名を取得
    private List<String> getAllPlayerNames() {
        List<String> onlinePlayers = getOnlinePlayerNames();
        List<String> allPlayers = new ArrayList<>(onlinePlayers);

        Arrays.stream(Bukkit.getOfflinePlayers())
                .map(offlinePlayer -> offlinePlayer.getName())
                .filter(name -> name != null && !onlinePlayers.contains(name))
                .forEach(allPlayers::add);

        return allPlayers;
    }

    // jailコマンドのタブ補完
    private List<String> completeJailCommand(String[] args) {
        switch (args.length) {
            case 1:
                // プレイヤー名の順序を保持して取得し、順序を維持したまま返す
                List<String> allPlayers = getAllPlayerNames(); // 全プレイヤー名を取得
                return filterByPrefix(allPlayers, args[0]); // プレイヤー名を補完
            case 2:
                return filterByPrefix(new ArrayList<>(this.jails.keySet()), args[1]); // 監獄名を補完
            case 3:
                return filterByPrefix(Arrays.asList("infinity", "1w", "1d", "1h", "1m", "1s"), args[2]); // 期間を補完
            case 4:
                return filterByPrefix(Arrays.asList("true", "false"), args[3]); // アドベンチャーモードの真偽値を補完
            default:
                return null;
        }
    }
    
    // オンラインプレイヤー名を取得
    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()); // オンラインプレイヤー名を取得
    }
    
    // プレイヤー名を補完
    private List<String> filterByPrefix(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return options;
        }
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    // jailtpコマンドのタブ補完
    private List<String> completeJailTpCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getAllPlayerNames(), args[0]); // プレイヤー名を補完
            default:
                return Collections.emptyList();
        }
    }

    // jailtp-rpコマンドのタブ補完
    private List<String> completeJailTpRpCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getAllPlayerNames(), args[0]); // プレイヤー名を補完
            default:
                return Collections.emptyList();
        }
    }

    // jailinfoコマンドのタブ補完
    private List<String> completeJailInfoCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getAllPlayerNames(), args[0]); // プレイヤー名を補完
            default:
                return Collections.emptyList();
        }
    }

    // unjailコマンドのタブ補完
    private List<String> completeUnjailCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getAllPlayerNames(), args[0]); // プレイヤー名を補完
            default:
                return Collections.emptyList();
        }
    }

    // setunjailコマンドのタブ補完
    private List<String> completeSetUnjailCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(new ArrayList<>(this.jails.keySet()), args[0]); // 監獄名を補完
            default:
                return Collections.emptyList();
        }
    }

    // removejailコマンドのタブ補完
    private List<String> completeJailNameCommand(String[] args) {
        if (args.length == 1) {
            return filterByPrefix(getAllPlayerNames(), args[0]); // プレイヤー名を補完
        }
        return Collections.emptyList();
    }
}
