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
                return completeJailCommand(args);
            case "unjail":
                return completeUnjailCommand(args);
            case "setunjail":
                return completeSetUnjailCommand(args);
            case "jailtp":
                return completeJailTpCommand(args);
            case "jailtp-rp":
                return completeJailTpRpCommand(args);
            case "removejail":
                return completeJailNameCommand(args);
            case "jailinfo":
                return completeJailInfoCommand(args);
            default:
                return Collections.emptyList();
        }
    }

   private List<String> completeJailCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getOnlinePlayerNames(), args[0]); // プレイヤー名を補完
            case 2:
                return filterByPrefix(new ArrayList<>(this.jails.keySet()), args[1]); // 監獄名を補完
            case 3:
                return filterByPrefix(Arrays.asList("infiniti", "1w", "1d", "1h", "1m", "1s"), args[2]); // 期間を補完
            case 4:
                return filterByPrefix(Arrays.asList("true", "false"), args[3]); // アドベンチャーモードの真偽値を補完
            default:
                return Collections.emptyList();
        }
    }
   private List<String> completeJailTpCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getOnlinePlayerNames(), args[0]); // プレイヤー名を補完
            default:
                return Collections.emptyList();
        }
    }

   private List<String> completeJailTpRpCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getOnlinePlayerNames(), args[0]); // プレイヤー名を補完
            default:
                return Collections.emptyList();
        }
    }

   private List<String> completeJailInfoCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getOnlinePlayerNames(), args[0]); // プレイヤー名を補完
            default:
                return Collections.emptyList();
        }
    } 
    
    private List<String> completeUnjailCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getOnlinePlayerNames(), args[0]); // プレイヤー名を補完
            case 2:
                return filterByPrefix(Arrays.asList("infiniti", "1w", "1d", "1h", "1m", "1s"), args[1]); // 期間を補完
            default:
                return Collections.emptyList();
        }
    }

    private List<String> completeSetUnjailCommand(String[] args) {
        switch (args.length) {
            case 1:
                return filterByPrefix(getOnlinePlayerNames(), args[0]); // プレイヤー名を補完
            default:
                return Collections.emptyList();
        }
    }


    private List<String> completeJailNameCommand(String[] args) {
        if (args.length == 1) {
            return filterByPrefix(getOnlinePlayerNames(), args[0]); // プレイヤー名を補完
        }
        return Collections.emptyList();
    }

    private List<String> filterByPrefix(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return options;
        }
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()); // オンラインプレイヤー名を取得
    }
}
