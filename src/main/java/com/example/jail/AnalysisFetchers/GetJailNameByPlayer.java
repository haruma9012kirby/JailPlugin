package com.example.jail.AnalysisFetchers;

import java.util.Map;
import java.util.Objects;

import com.example.jail.Jail;

// プレイヤーの刑務所名取得
public class GetJailNameByPlayer {
    private final Map<String, Jail> jails;

    public GetJailNameByPlayer(Map<String, Jail> jails) {
        this.jails = jails;
    }
    // プレイヤーの刑務所名取得
    public String getJailNameByPlayer(String playerName) {
        if (this.jails == null || playerName == null) { // 刑務所がnullの場合
            return null;
        }
        // プレイヤーの刑務所名取得
        return this.jails.entrySet().stream()
            .filter(entry -> Objects.nonNull(entry.getValue()) && // 刑務所がnullでないか
                             Objects.nonNull(entry.getValue().getJailedPlayers()) && // 刑務所の収容プレイヤーがnullでないか
                             entry.getValue().getJailedPlayers().contains(playerName)) // 刑務所の収容プレイヤーにプレイヤーが含まれているか
            .findFirst()
            .map(Map.Entry::getKey) // 刑務所名を取得
            .orElse(null); // 刑務所名がnullの場合はnullを返す
    }
}