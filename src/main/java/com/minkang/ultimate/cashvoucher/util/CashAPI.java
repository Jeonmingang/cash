package com.minkang.ultimate.cashvoucher.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Reflection bridge to UltimateCashShop's BalanceStore
 */
public class CashAPI {

    private static Object mainInstance;
    private static Method mGetInstance;
    private static Method mGetBalanceStore;
    private static Method mAdd;

    private static boolean ensureLinked() {
        if (mainInstance != null) return true;
        Plugin other = Bukkit.getPluginManager().getPlugin("UltimateCashShop");
        if (other == null || !other.isEnabled()) return false;
        try {
            Class<?> mainClazz = Class.forName("com.minkang.ultimate.cashshop.Main");
            mGetInstance = mainClazz.getMethod("getInstance");
            mainInstance = mGetInstance.invoke(null);
            mGetBalanceStore = mainClazz.getMethod("getBalanceStore");
            Object store = mGetBalanceStore.invoke(mainInstance);
            Class<?> storeClazz = store.getClass();
            mAdd = storeClazz.getMethod("add", java.util.UUID.class, long.class);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addCash(UUID uuid, long amount) {
        if (!ensureLinked()) return false;
        try {
            Object store = mGetBalanceStore.invoke(mainInstance);
            mAdd.invoke(store, uuid, amount);
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
