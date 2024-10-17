package com.example.jail.Formats;

// 期間のフォーマット
public class FormatDuration {
    public String formatDuration(Long duration) {
        if (duration == -1L) {
            return "無期限";
        } else {
            StringBuilder result = new StringBuilder();
            long weeks = duration / 604800L;
            if (weeks > 0L) {
                result.append(weeks).append("週間 ");
                duration %= 604800L;
            }

            long days = duration / 86400L;
            if (days > 0L) {
                result.append(days).append("日 ");
                duration %= 86400L;
            }

            long hours = duration / 3600L;
            if (hours > 0L) {
                result.append(hours).append("時間 ");
                duration %= 3600L;
            }

            long minutes = duration / 60L;
            if (minutes > 0L) {
                result.append(minutes).append("分 ");
                duration %= 60L;
            }

            if (duration > 0L) {
                result.append(duration).append("秒");
            }

            String formattedDuration = result.toString().trim();
            return formattedDuration.isEmpty() ? "0秒" : formattedDuration;
        }
    }
}