
package com.minkang.ultimate.cashshop.util;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.gui.CashShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

public class CitizenHook {
    public static void tryRegister(final Main plugin) {
        try {
            if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
                plugin.getLogger().info("Citizens not found; NPC hook disabled.");
                return;
            }
            final Class<?> eventClass = Class.forName("net.citizensnpcs.api.event.NPCRightClickEvent");
            final Method getNPC = eventClass.getMethod("getNPC");
            final Method getId = getNPC.getReturnType().getMethod("getId");
            final Method getClicker = eventClass.getMethod("getClicker");

            PluginManager pm = Bukkit.getPluginManager();
            pm.registerEvent((Class<? extends Event>) eventClass, new Listener() {}, EventPriority.NORMAL, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event event) {
                    try {
                        if (!eventClass.isInstance(event)) return;
                        Object npc = getNPC.invoke(event);
                        int id = (Integer) getId.invoke(npc);
                        Player clicker = (Player) getClicker.invoke(event);
                        if (plugin.getLinkStore().isLinked(id)) {
                            CashShopGUI.open(clicker, plugin);
                        }
                    } catch (Throwable ignore) {}
                }
            }, plugin);
            plugin.getLogger().info("Citizens hook registered.");
        } catch (Throwable t) {
            plugin.getLogger().info("Citizens not present/compatible; NPC hook disabled.");
        }
    }
}
