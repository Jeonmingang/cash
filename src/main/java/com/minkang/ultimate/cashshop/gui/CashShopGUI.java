
package com.minkang.ultimate.cashshop.gui;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.store.ShopStore;
import com.minkang.ultimate.cashshop.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CashShopGUI {

    public static String title(Main plugin) {
        FileConfiguration c = plugin.getConfig();
        return ItemUtil.color(c.getString("gui.title", "&8후원 상점 &7(&f캐시&7)"));
    }

    public static int size(Main plugin) {
        int s = plugin.getConfig().getInt("gui.size", 54);
        if (s < 9) s = 9;
        if (s % 9 != 0) s = 54;
        if (s > 54) s = 54;
        return s;
    }

    public static List<String> overlay(Main plugin) {
        List<String> raw = plugin.getConfig().getStringList("lore.overlay");
        if (raw == null || raw.isEmpty()) {
            raw = new ArrayList<String>();
            raw.add("&7-------------------------");
            raw.add("&a가격: &f{price}&7 캐시");
            raw.add("&a수량: &f{amount}&7 개");
            raw.add("&e클릭하여 구매");
            raw.add("&7-------------------------");
        }
        return raw;
    }

    public static void open(Player p, Main plugin) {
        Inventory inv = Bukkit.createInventory(null, size(plugin), title(plugin));
        List<String> overlayRaw = overlay(plugin);
        for (Map.Entry<Integer, ShopStore.Listing> e : plugin.getShopStore().all().entrySet()) {
            int slot = e.getKey();
            if (slot < 0 || slot >= inv.getSize()) continue;
            ShopStore.Listing l = e.getValue();

            Map<String, String> repl = new HashMap<String, String>();
            repl.put("price", Long.toString(l.price));
            repl.put("amount", Integer.toString(l.amount));

            List<String> overlay = new ArrayList<String>();
            for (String line : overlayRaw) {
                overlay.add(ItemUtil.fmt(line, repl));
            }
            ItemStack display = ItemUtil.withOverlay(l.item, overlay);
            inv.setItem(slot, display);
        }
        p.openInventory(inv);
    }
}
