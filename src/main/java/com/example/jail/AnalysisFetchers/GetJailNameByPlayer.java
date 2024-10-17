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

    public String getJailNameByPlayer(String playerName) {
        if (this.jails == null || playerName == null) {
            return null;
        }
        
        return this.jails.entrySet().stream()
            .filter(entry -> Objects.nonNull(entry.getValue()) && 
                             Objects.nonNull(entry.getValue().getJailedPlayers()) &&
                             entry.getValue().getJailedPlayers().contains(playerName))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}