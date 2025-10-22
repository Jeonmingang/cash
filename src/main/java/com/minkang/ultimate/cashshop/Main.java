
package com.minkang.ultimate.cashshop;

import com.minkang.ultimate.cashshop.commands.CashCommand;
import com.minkang.ultimate.cashshop.commands.ShopCommand;
import com.minkang.ultimate.cashshop.listeners.GuiListener;
import com.minkang.ultimate.cashshop.store.BalanceStore;
import com.minkang.ultimate.cashshop.store.LinkStore;
import com.minkang.ultimate.cashshop.store.ShopStore;
import com.minkang.ultimate.cashshop.util.CitizenHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    private BalanceStore balanceStore;
    private ShopStore shopStore;
    private LinkStore linkStore;

    public static Main getInstance() {
        return instance;
    }

    public BalanceStore getBalanceStore() {
        return balanceStore;
    }

    public ShopStore getShopStore() {
        return shopStore;
    }

    public LinkStore getLinkStore() {
        return linkStore;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        balanceStore = new BalanceStore(this);
        shopStore = new ShopStore(this);
        linkStore = new LinkStore(this);

        // Register commands
        getCommand("캐시").setExecutor(new CashCommand(this));
        getCommand("캐시상점").setExecutor(new ShopCommand(this));

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);

        // Optional Citizens hook (no hard dep)
        CitizenHook.tryRegister(this);

        getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (balanceStore != null) balanceStore.save();
        if (shopStore != null) shopStore.save();
        if (linkStore != null) linkStore.save();
        getLogger().info(getDescription().getName() + " disabled.");
    }
}
