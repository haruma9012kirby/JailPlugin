package com.example.jail.Formats;

// 期間のフォーマット
public class FormatDuration {
    public String formatDuration(Long duration) {
        if (duration == null || duration == -1L) { // 刑期間がnullまたは-1の場合
            return "無期限"; // 刑期間が無期限の場合
        } else {
            StringBuilder result = new StringBuilder();
            long weeks = duration / 604800L; // 刑期間を週数に変換
            if (weeks > 0L) { // 刑期間が0以上の場合
                result.append(weeks).append("週間 "); // 刑期間を週数に変換
                duration %= 604800L; // 刑期間を週数に変換
            }

            long days = duration / 86400L; // 刑期間を日数に変換
            if (days > 0L) { // 刑期間が0以上の場合
                result.append(days).append("日 "); // 刑期間を日数に変換
                duration %= 86400L; // 刑期間を日数に変換
            }

            long hours = duration / 3600L; // 刑期間を時間に変換
            if (hours > 0L) { // 刑期間が0以上の場合
                result.append(hours).append("時間 "); // 刑期間を時間に変換
                duration %= 3600L; // 刑期間を時間に変換
            }

            long minutes = duration / 60L; // 刑期間を分に変換
            if (minutes > 0L) { // 刑期間が0以上の場合
                result.append(minutes).append("分 "); // 刑期間を分に変換
                duration %= 60L; // 刑期間を分に変換
            }

            if (duration > 0L) { // 刑期間が0以上の場合
                result.append(duration).append("秒"); // 刑期間を秒に変換
            }

            String formattedDuration = result.toString().trim();
            return formattedDuration.isEmpty() ? "0秒" : formattedDuration;
        }
    }
}