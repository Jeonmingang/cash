
package com.minkang.ultimate.cashshop.util;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemUtil {
    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> color(List<String> list) {
        List<String> out = new ArrayList<>();
        for (String s : list) out.add(color(s));
        return out;
    }

    public static String fmt(String raw, Map<String, String> repl) {
        String out = raw;
        for (Map.Entry<String, String> e : repl.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        return color(out);
    }

    public static ItemStack withOverlay(ItemStack base, List<String> lore) {
        ItemStack item = base.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> outLore = new ArrayList<>();
            // Preserve original lore if present
            ItemMeta baseMeta = base.getItemMeta();
            if (baseMeta != null && baseMeta.hasLore()) {
                for (String sLine : baseMeta.getLore()) {
                    outLore.add(sLine);
                }
                if (!lore.isEmpty()) {
                    // Spacer between original lore and overlay
                    outLore.add("");
                }
            }
            for (String sLine : lore) {
                outLore.add(color(sLine));
            }
            meta.setLore(outLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}

