
package com.minkang.ultimate.cashshop.store;

import com.minkang.ultimate.cashshop.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BalanceStore {
    private final Main plugin;
    private final File file;
    private FileConfiguration conf;
    private final Map<UUID, Long> balances = new HashMap<>();

    public BalanceStore(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "balances.yml");
        load();
    }

    public synchronized long get(UUID uuid) {
        Long v = balances.get(uuid);
        return v == null ? 0L : v;
    }

    public synchronized void set(UUID uuid, long value) {
        balances.put(uuid, value);
        save();
    }

    public synchronized void add(UUID uuid, long delta) {
        long cur = get(uuid);
        long next = cur + delta;
        if (next < 0) next = 0;
        set(uuid, next);
    }

    private void load() {
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException ignored) {}
        }
        conf = YamlConfiguration.loadConfiguration(file);
        for (String k : conf.getKeys(false)) {
            try {
                UUID id = UUID.fromString(k);
                long v = conf.getLong(k, 0L);
                balances.put(id, v);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public synchronized void save() {
        YamlConfiguration out = new YamlConfiguration();
        for (Map.Entry<UUID, Long> e : balances.entrySet()) {
            out.set(e.getKey().toString(), e.getValue());
        }
        try { out.save(file); } catch (IOException ignored) {}
    }


    public synchronized java.util.Map<java.util.UUID, Long> getAllBalances() {
        return new java.util.HashMap<java.util.UUID, Long>(balances);
    }
}
