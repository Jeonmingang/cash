package com.minkang.ultimate.cashvoucher;

import com.minkang.ultimate.cashvoucher.commands.VoucherCommand;
import com.minkang.ultimate.cashvoucher.listener.VoucherListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VoucherPlugin extends JavaPlugin {

    private static VoucherPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getCommand("캐시권").setExecutor(new VoucherCommand(this));
        getCommand("캐시권").setTabCompleter(new VoucherCommand(this));
        Bukkit.getPluginManager().registerEvents(new VoucherListener(this), this);
        getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " disabled.");
    }

    public static VoucherPlugin getInstance() {
        return instance;
    }
}
