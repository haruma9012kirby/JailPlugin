package com.example.jail.AnalysisFetchers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseDuration {
    // 期間の解析
    public long parseDuration(String durationStr) {
        if ("infinity".equalsIgnoreCase(durationStr)) {
            return -1L; // 無期限を表す特別な値
        }

        try {  // 期間の解析
            Pattern pattern = Pattern.compile("(\\d+)([wdhms])");
            Matcher matcher = pattern.matcher(durationStr);
            long totalDuration = 0L;
            boolean matched = false;

            while (matcher.find()) {
                matched = true;
                int value = Integer.parseInt(matcher.group(1)); // 期間の値を取得
                String unit = matcher.group(2); // 期間の単位を取得
                switch (unit) {
                    case "w":
                        totalDuration += (long) value * 604800; // 期間の値を週に変換
                        break;
                    case "d":
                        totalDuration += (long) value * 86400; // 期間の値を日に変換
                        break;
                    case "h":
                        totalDuration += (long) value * 3600; // 期間の値を時間に変換
                        break;
                    case "m":
                        totalDuration += (long) value * 60; // 期間の値を分に変換
                        break;
                    case "s":
                        totalDuration += value; // 期間の値を秒に変換
                        break;
                }
            }

            if (!matched) { // 期間の解析が成功したか
                throw new IllegalArgumentException("無効な期間のフォーマットです。"); // 期間の解析が失敗した場合
            }

            return totalDuration;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("無効な期間のフォーマットです。");
        }
    }
}
