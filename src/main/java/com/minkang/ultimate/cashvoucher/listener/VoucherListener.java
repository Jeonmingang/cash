package com.minkang.ultimate.cashvoucher.listener;

import com.minkang.ultimate.cashvoucher.util.CashAPI;
import com.minkang.ultimate.cashvoucher.util.ItemFactory;
import com.minkang.ultimate.cashvoucher.VoucherPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class VoucherListener implements Listener {
    private final VoucherPlugin plugin;
    private final NamespacedKey keyAmount;
    private final NamespacedKey keyMark;

    public VoucherListener(VoucherPlugin plugin) {
        this.plugin = plugin;
        this.keyAmount = ItemFactory.keyAmount(plugin);
        this.keyMark = ItemFactory.keyMark(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUse(PlayerInteractEvent e) {
        // Only right-click air
        if (e.getAction() != Action.RIGHT_CLICK_AIR) return;
        // Only main hand
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player p = e.getPlayer();
        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) return;
        ItemMeta meta = inHand.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Is our voucher?
        if (!pdc.has(keyMark, PersistentDataType.BYTE)) return;
        if (!pdc.has(keyAmount, PersistentDataType.LONG)) return;

        long amount = pdc.getOrDefault(keyAmount, PersistentDataType.LONG, 0L);
        if (amount <= 0) return;

        // Redeem: credit cash via reflection into UltimateCashShop
        boolean ok = CashAPI.addCash(p.getUniqueId(), amount);
        if (!ok) {
            p.sendMessage(color("&cUltimateCashShop 플러그인을 찾을 수 없거나 연동에 실패했습니다."));
            return;
        }

        // consume one item
        inHand.setAmount(inHand.getAmount() - 1);
        p.getInventory().setItemInMainHand(inHand.getAmount() > 0 ? inHand : new ItemStack(Material.AIR));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f);
        p.sendMessage(color("&a캐시 수표 사용 완료! &f+" + amount + " 캐시 &7(오른손 허공 우클릭만 가능)"));
        e.setCancelled(true);
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
