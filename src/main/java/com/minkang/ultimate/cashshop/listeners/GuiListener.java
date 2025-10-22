
package com.minkang.ultimate.cashshop.listeners;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.gui.CashShopGUI;
import com.minkang.ultimate.cashshop.store.ShopStore;
import com.minkang.ultimate.cashshop.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuiListener implements Listener {
    private final Main plugin;

    public GuiListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv == null) return;
        String title = e.getView().getTitle();
        if (!title.equals(CashShopGUI.title(plugin))) return;

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= inv.getSize()) return;

        Optional<ShopStore.Listing> opt = plugin.getShopStore().get(slot);
        if (!opt.isPresent()) return;
        ShopStore.Listing listing = opt.get();

        long bal = plugin.getBalanceStore().get(p.getUniqueId());
        if (bal < listing.price) {
            Map<String, String> repl = new HashMap<String, String>();
            repl.put("balance", String.valueOf(bal));
            String msg = plugin.getConfig().getString("messages.not_enough", "&c보유 캐시가 부족합니다. 현재 잔액: &f{balance}");
            p.sendMessage(ItemUtil.fmt(msg, repl));
            return;
        }

        // Deduct and give
        plugin.getBalanceStore().add(p.getUniqueId(), -listing.price);

        ItemStack give = listing.item.clone();
        give.setAmount(listing.amount);
        p.getInventory().addItem(give);

        Map<String, String> repl = new HashMap<String, String>();
        repl.put("price", String.valueOf(listing.price));
        repl.put("amount", String.valueOf(listing.amount));
        repl.put("balance", String.valueOf(plugin.getBalanceStore().get(p.getUniqueId())));
        String msg = plugin.getConfig().getString("messages.buy_success", "&a구매 완료! &f{amount}&7개를 &f{price}&7 캐시에 구매했습니다. 현재 잔액: &f{balance}");
        p.sendMessage(ItemUtil.fmt(msg, repl));
    }
}
