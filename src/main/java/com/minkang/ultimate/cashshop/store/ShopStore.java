
package com.minkang.ultimate.cashshop.store;

import com.minkang.ultimate.cashshop.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShopStore {
    public static class Listing {
        public ItemStack item;
        public long price;
        public int amount;
        public Listing(ItemStack item, long price, int amount) {
            this.item = item; this.price = price; this.amount = amount;
        }
    }

    private final Main plugin;
    private final File file;
    private FileConfiguration conf;
    private final Map<Integer, Listing> listings = new HashMap<>();

    public ShopStore(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shop.yml");
        load();
    }

    public synchronized Optional<Listing> get(int slot) {
        return Optional.ofNullable(listings.get(slot));
    }

    public synchronized Map<Integer, Listing> all() {
        return new HashMap<>(listings);
    }

    public synchronized void register(int slot, ItemStack item, long price, int amount) {
        listings.put(slot, new Listing(item.clone(), price, amount));
        save();
    }

    public synchronized void unregister(int slot) {
        listings.remove(slot);
        save();
    }

    public synchronized void load() {
        listings.clear();
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException ignored) {}
        }
        conf = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = conf.getConfigurationSection("items");
        if (sec == null) return;
        for (String k : sec.getKeys(false)) {
            try {
                int slot = Integer.parseInt(k);
                ConfigurationSection s = sec.getConfigurationSection(k);
                if (s == null) continue;
                ItemStack item = s.getItemStack("item");
                long price = s.getLong("price", 0L);
                int amount = s.getInt("amount", 1);
                if (item != null) listings.put(slot, new Listing(item, price, amount));
            } catch (NumberFormatException ignored) {}
        }
    }

    public synchronized void save() {
        YamlConfiguration out = new YamlConfiguration();
        for (Map.Entry<Integer, Listing> e : listings.entrySet()) {
            String path = "items." + e.getKey();
            out.set(path + ".item", e.getValue().item);
            out.set(path + ".price", e.getValue().price);
            out.set(path + ".amount", e.getValue().amount);
        }
        try { out.save(file); } catch (IOException ignored) {}
    }
}
