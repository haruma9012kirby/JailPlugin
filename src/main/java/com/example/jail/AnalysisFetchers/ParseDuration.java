package com.example.jail.AnalysisFetchers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseDuration {
    // 期間の解析
    public long parseDuration(String input) throws IllegalArgumentException {
        if (input != null && !input.equalsIgnoreCase("infinity")) {
            Pattern pattern = Pattern.compile("(\\d+)([wdhms])");
            Matcher matcher = pattern.matcher(input);
            long totalDuration = 0L;
            boolean matched = false;

            while (matcher.find()) {
                matched = true;
                int value = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);
                switch (unit) {
                    case "w":
                        totalDuration += (long) value * 604800;
                        break;
                    case "d":
                        totalDuration += (long) value * 86400;
                        break;
                    case "h":
                        totalDuration += (long) value * 3600;
                        break;
                    case "m":
                        totalDuration += (long) value * 60;
                        break;
                    case "s":
                        totalDuration += value;
                        break;
                }
            }

            if (!matched) {
                throw new IllegalArgumentException("無効な期間のフォーマットです。");
            }

            return totalDuration;
        } else {
            return -1L;
        }
    }
}
