
package com.minkang.ultimate.cashshop.store;

import com.minkang.ultimate.cashshop.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LinkStore {
    private final Main plugin;
    private final File file;
    private FileConfiguration conf;
    private final Set<Integer> linked = new HashSet<>();

    public LinkStore(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "links.yml");
        load();
    }

    public synchronized boolean isLinked(int npcId) {
        return linked.contains(npcId);
    }

    public synchronized void link(int npcId) {
        linked.add(npcId);
        save();
    }

    public synchronized void unlink(int npcId) {
        linked.remove(npcId);
        save();
    }

    private void load() {
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException ignored) {}
        }
        conf = YamlConfiguration.loadConfiguration(file);
        linked.clear();
        for (String k : conf.getStringList("linked")) {
            try { linked.add(Integer.parseInt(k)); } catch (NumberFormatException ignored) {}
        }
    }

    public synchronized void save() {
        YamlConfiguration out = new YamlConfiguration();
        out.set("linked", linked.stream().map(String::valueOf).toArray(String[]::new));
        try { out.save(file); } catch (IOException ignored) {}
    }
}
