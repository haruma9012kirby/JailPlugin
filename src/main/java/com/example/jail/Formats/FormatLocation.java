package com.example.jail.Formats;

import org.bukkit.Location;

// 座標のフォーマット
public class FormatLocation {
    public String formatLocation(Location location) {
        if (location == null) {
            return "無効な座標";
        }
        int x = (int)Math.round(location.getX());
        int y = location.getBlockY();
        int z = (int)Math.round(location.getZ());
        return String.format("(%d, %d, %d)", x, y, z);
    }
}