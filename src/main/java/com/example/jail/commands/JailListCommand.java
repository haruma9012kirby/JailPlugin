package com.example.jail.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;

import com.example.jail.Jail;
import com.example.jail.JailPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class JailListCommand {
    private final JailPlugin plugin;
   
    public JailListCommand(JailPlugin plugin) {
        this.plugin = plugin;
    }
    // 刑務所の一覧表示
    public boolean handleJailList(Player player, String[] args) {
        if (plugin.jails == null || plugin.jails.isEmpty()) {
            player.sendMessage(ChatColor.RED + "登録されている刑務所はありません。");
            return true;
        } else {
            int page = 1;
       if (args.length > 0) {
          try {
             page = Integer.parseInt(args[0]);
          } catch (NumberFormatException e) {
             player.sendMessage(ChatColor.RED + "無効なページ番号です。");
             return false;
          }
       }

       int itemsPerPage = 8;
       int totalItems = plugin.jails.size();
       int totalPages = (int)Math.ceil((double)totalItems / (double)itemsPerPage);
       if (page >= 1 && page <= totalPages) {
          player.sendMessage(ChatColor.YELLOW + "============= " + ChatColor.RED + "Jail list" + ChatColor.YELLOW + " =============");
          List<String> sortedJailNames = new ArrayList<>(plugin.jails.keySet());
          Collections.sort(sortedJailNames, (a, b) -> {
             int numA = Integer.parseInt(a.replaceAll("\\D", "0"));
             int numB = Integer.parseInt(b.replaceAll("\\D", "0"));
             return Integer.compare(numA, numB);
          });
          int startIndex = (page - 1) * itemsPerPage;
          int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
          int index = 0;

          for(Iterator<String> var11 = sortedJailNames.iterator(); var11.hasNext(); ++index) {
             String jailName = var11.next();
             if (index >= startIndex && index < endIndex) {
                Jail jail = plugin.jails.get(jailName);
                TextComponent jailLine = new TextComponent(index + 1 + ". ");
                jailLine.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                TextComponent jailNameComponent = new TextComponent(jail.getName());
                jailNameComponent.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                TextComponent infoButton = new TextComponent(" [Info]");
                infoButton.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                infoButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jailinfo " + jail.getName()));
                StringBuilder jailedPlayersInfo = new StringBuilder("収容しているプレイヤー: ");

                for (String jailedPlayer : jail.getJailedPlayers()) {
                    long paroleUntil = this.plugin.dataBaseMGR.getParoleUntilFromDatabase(jailedPlayer);
                    TextComponent playerNameComponent = new TextComponent(jailedPlayer);
                    
                    if (paroleUntil > System.currentTimeMillis() / 1000L) {
                        playerNameComponent.addExtra(" " + net.md_5.bungee.api.ChatColor.RED + "(仮釈放中)");
                    }
                    
                    jailedPlayersInfo.append(playerNameComponent.toLegacyText()).append(", ");
                }

                if (jailedPlayersInfo.length() > 0) {
                    jailedPlayersInfo.setLength(jailedPlayersInfo.length() - 2);
                }

                jailLine.addExtra(jailNameComponent);
                TextComponent tpButton = new TextComponent(" [TP]");
                tpButton.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                tpButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jailtp " + jail.getName()));
                TextComponent tpRpButton = new TextComponent(" [TP-RP]");
                tpRpButton.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                tpRpButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jailtp-rp " + jail.getName()));
                TextComponent removeButton = new TextComponent(" [Remove]");
                removeButton.setColor(net.md_5.bungee.api.ChatColor.RED);
                removeButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/removejail " + jail.getName()));
                jailLine.addExtra(infoButton);
                jailLine.addExtra(tpButton);
                jailLine.addExtra(tpRpButton);
                jailLine.addExtra(removeButton);
                player.spigot().sendMessage(jailLine); // エラー解決不可
             }
          }

          TextComponent pageInfo = new TextComponent(ChatColor.YELLOW + "========== ");
          TextComponent nextPage;
          if (page > 1) {
             nextPage = new TextComponent("<<< ");
             nextPage.setColor(net.md_5.bungee.api.ChatColor.GOLD);
             nextPage.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jaillist " + (page - 1)));
             pageInfo.addExtra(nextPage);
          }

          pageInfo.addExtra(new TextComponent(ChatColor.YELLOW + "Page " + ChatColor.GOLD + page + ChatColor.YELLOW + " of " + ChatColor.GOLD + totalPages + ChatColor.YELLOW + " "));
          if (page < totalPages) {
             nextPage = new TextComponent(">>> ");
             nextPage.setColor(net.md_5.bungee.api.ChatColor.GOLD);
             nextPage.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jaillist " + (page + 1)));
             pageInfo.addExtra(nextPage);
          }
          pageInfo.addExtra(new TextComponent(ChatColor.YELLOW + "=========="));
          player.spigot().sendMessage(pageInfo); // エラー解決不可
          return true;
       } else {
          player.sendMessage(ChatColor.RED + "ページ番号が範囲外です。");
          return false;
       }
    }
}
}
