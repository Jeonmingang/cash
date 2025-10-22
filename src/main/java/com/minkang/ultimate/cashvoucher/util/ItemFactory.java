package com.minkang.ultimate.cashvoucher.util;

import com.minkang.ultimate.cashvoucher.VoucherPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemFactory {

    public static NamespacedKey keyAmount(VoucherPlugin plugin) { return new NamespacedKey(plugin, "voucher_amount"); }
    public static NamespacedKey keyMark(VoucherPlugin plugin) { return new NamespacedKey(plugin, "voucher_mark"); }

    public static ItemStack createVoucher(VoucherPlugin plugin, long amount, String issuer) {
        String name = color(plugin.getConfig().getString("item.name", "&b캐시 수표 &7(클릭 사용)"));
        List<String> loreTpl = plugin.getConfig().getStringList("item.lore");
        if (loreTpl == null || loreTpl.isEmpty()) {
            loreTpl = new ArrayList<>();
            loreTpl.add("&7금액: &f{amount}");
            loreTpl.add("&7발행: &f{issuer}");
            loreTpl.add("&7일시: &f{date}");
            loreTpl.add("&8오른손 허공 우클릭 시 사용");
        }
        String formatted = String.format("%,d", amount);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        for (String s : loreTpl) {
            lore.add(color(s.replace("{amount}", formatted).replace("{issuer}", issuer).replace("{date}", date)));
        }
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // PDC marks
        meta.getPersistentDataContainer().set(keyAmount(plugin), PersistentDataType.LONG, amount);
        meta.getPersistentDataContainer().set(keyMark(plugin), PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);
        return item;
    }

    private static String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
