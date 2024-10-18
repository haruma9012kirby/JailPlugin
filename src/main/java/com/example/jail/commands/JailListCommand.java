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
        if (plugin.jails == null || plugin.jails.isEmpty()) { // 刑務所が空の場合はエラーメッセージを送信
            player.sendMessage(ChatColor.RED + "登録されている刑務所はありません。");
            return true;
        } else {
       int page = 1;
       if (args.length > 0) {
          try {
             page = Integer.parseInt(args[0]); // ページ番号を取得
          } catch (NumberFormatException e) {
             player.sendMessage(ChatColor.RED + "無効なページ番号です。"); // ページ番号が無効な場合はエラーメッセージを送信
             return false;
          }
       }

       int itemsPerPage = 8; // 1ページあたりの刑務所数  
       int totalItems = plugin.jails.size(); // 刑務所の総数
       int totalPages = (int)Math.ceil((double)totalItems / (double)itemsPerPage); // 総ページ数
       if (page >= 1 && page <= totalPages) { // ページ番号が有効な場合は刑務所の一覧を表示
          player.sendMessage(ChatColor.YELLOW + "============= " + ChatColor.RED + "Jail list" + ChatColor.YELLOW + " =============");
          List<String> sortedJailNames = new ArrayList<>(plugin.jails.keySet());
          Collections.sort(sortedJailNames, (a, b) -> {
             int numA = Integer.parseInt(a.replaceAll("\\D", "0")); // 刑務所名から数値を抽出
             int numB = Integer.parseInt(b.replaceAll("\\D", "0")); // 刑務所名から数値を抽出
             return Integer.compare(numA, numB); // 数値を比較
          });
          int startIndex = (page - 1) * itemsPerPage; // 開始インデックス
          int endIndex = Math.min(startIndex + itemsPerPage, totalItems); // 終了インデックス
          int index = 0; // インデックス

          for(Iterator<String> var11 = sortedJailNames.iterator(); var11.hasNext(); ++index) {
             String jailName = var11.next();
             if (index >= startIndex && index < endIndex) {
                Jail jail = plugin.jails.get(jailName); // 刑務所を取得
                TextComponent jailLine = new TextComponent(index + 1 + ". "); // 刑務所の行を作成
                jailLine.setColor(net.md_5.bungee.api.ChatColor.GOLD); // 刑務所の行の色を設定
                TextComponent jailNameComponent = new TextComponent(jail.getName()); // 刑務所名を作成
                jailNameComponent.setColor(net.md_5.bungee.api.ChatColor.GOLD); // 刑務所名の色を設定
                TextComponent infoButton = new TextComponent(" [Info]"); // 情報ボタンを作成
                infoButton.setColor(net.md_5.bungee.api.ChatColor.GRAY); // 情報ボタンの色を設定
                infoButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jailinfo " + jail.getName())); // 情報ボタンのクリックイベントを設定
                StringBuilder jailedPlayersInfo = new StringBuilder("収容しているプレイヤー: "); // 収容しているプレイヤーの情報を作成

                for (String jailedPlayer : jail.getJailedPlayers()) {
                    jailedPlayersInfo.append(jailedPlayer).append(", "); // 収容しているプレイヤーの情報を追加
                }

                if (jailedPlayersInfo.length() > 0) {
                    jailedPlayersInfo.setLength(jailedPlayersInfo.length() - 2); // 最後のカンマを削除
                }

                jailLine.addExtra(jailNameComponent); // 刑務所の行に刑務所名を追加
                TextComponent tpButton = new TextComponent(" [TP]"); // TPボタンを作成
                tpButton.setColor(net.md_5.bungee.api.ChatColor.GRAY); // TPボタンの色を設定
                tpButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jailtp " + jail.getName())); // TPボタンのクリックイベントを設定
                TextComponent tpRpButton = new TextComponent(" [TP-RP]"); // TP-RPボタンを作成
                tpRpButton.setColor(net.md_5.bungee.api.ChatColor.GRAY); // TP-RPボタンの色を設定
                tpRpButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jailtp-rp " + jail.getName())); // TP-RPボタンのクリックイベントを設定
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

          TextComponent pageInfo = new TextComponent(ChatColor.YELLOW + "========== "); // ページ情報を作成
          TextComponent nextPage;
          if (page > 1) {
             nextPage = new TextComponent("<<< "); // 前のページボタンを作成
             nextPage.setColor(net.md_5.bungee.api.ChatColor.GOLD); // 前のページボタンの色を設定
             nextPage.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jaillist " + (page - 1))); // 前のページボタンのクリックイベントを設定
             pageInfo.addExtra(nextPage); // ページ情報に前のページボタンを追加
          }

          pageInfo.addExtra(new TextComponent(ChatColor.YELLOW + "Page " + ChatColor.GOLD + page + ChatColor.YELLOW + " of " + ChatColor.GOLD + totalPages + ChatColor.YELLOW + " "));
          if (page < totalPages) {
             nextPage = new TextComponent(">>> "); // 次のページボタンを作成
             nextPage.setColor(net.md_5.bungee.api.ChatColor.GOLD); // 次のページボタンの色を設定
             nextPage.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/jaillist " + (page + 1))); // 次のページボタンのクリックイベントを設定
             pageInfo.addExtra(nextPage); // ページ情報に次のページボタンを追加
          }
          pageInfo.addExtra(new TextComponent(ChatColor.YELLOW + "=========="));
          player.spigot().sendMessage(pageInfo); // エラー解決不可
          return true;
       } else {
          player.sendMessage(ChatColor.RED + "ページ番号が範囲外です。"); // ページ番号が範囲外の場合はエラーメッセージを送信
          return false;
       }
    }
}
}
