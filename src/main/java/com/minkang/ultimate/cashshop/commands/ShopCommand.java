
package com.minkang.ultimate.cashshop.commands;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.gui.CashShopGUI;
import com.minkang.ultimate.cashshop.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;

        // /캐시상점 or /캐시상점 열기
        if (args.length == 0 || (args.length >= 1 && "열기".equalsIgnoreCase(args[0]))) {
            CashShopGUI.open(p);
            return true;
        }

        // /캐시상점 등록 <갯수> <가격(캐시)> <슬롯>
        if ("등록".equalsIgnoreCase(args[0])) {
            if (!p.hasPermission("ultimate.cashshop.admin")) {
                p.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 4) {
                p.sendMessage(ItemUtil.color("&c사용법: /캐시상점 등록 <갯수> <가격(캐시)> <슬롯>"));
                return true;
            }
            int amount; long price; int slot;
            try {
                amount = Integer.parseInt(args[1]);
                price = Long.parseLong(args[2]);
                slot = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                p.sendMessage("§c숫자 형식이 올바르지 않습니다.");
                return true;
            }
            if (slot < 0 || slot > 53) {
                String msg = Main.getInstance().getConfig().getString("messages.invalid_slot");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
                return true;
            }
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                String msg = Main.getInstance().getConfig().getString("messages.hold_item");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
                return true;
            }
            Main.getInstance().getShopStore().put(slot, hand, amount, price);
            Map<String,String> repl = new HashMap<>();
            repl.put("slot", Integer.toString(slot));
            repl.put("amount", Integer.toString(amount));
            repl.put("price", Long.toString(price));
            String msg = Main.getInstance().getConfig().getString("messages.registered");
            if (msg != null) p.sendMessage(ItemUtil.fmt(msg, repl));
            return true;
        }

        // /캐시상점 등록삭제 <슬롯>
        if ("등록삭제".equalsIgnoreCase(args[0])) {
            if (!p.hasPermission("ultimate.cashshop.admin")) {
                p.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 2) {
                p.sendMessage(ItemUtil.color("&c사용법: /캐시상점 등록삭제 <슬롯>"));
                return true;
            }
            int slot;
            try {
                slot = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage("§c숫자 형식이 올바르지 않습니다.");
                return true;
            }
            if (slot < 0 || slot > 53) {
                String msg = Main.getInstance().getConfig().getString("messages.invalid_slot");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
                return true;
            }
            Main.getInstance().getShopStore().remove(slot);
            Map<String,String> repl = new HashMap<>();
            repl.put("slot", Integer.toString(slot));
            String msg = Main.getInstance().getConfig().getString("messages.unregistered");
            if (msg != null) p.sendMessage(ItemUtil.fmt(msg, repl));
            return true;
        }

        // /캐시상점 연동
        if ("연동".equalsIgnoreCase(args[0])) {
            if (!p.hasPermission("ultimate.cashshop.admin")) {
                p.sendMessage("§c권한이 없습니다.");
                return true;
            }
            // Citizens presence check
            try {
                Class.forName("net.citizensnpcs.api.CitizensAPI");
            } catch (ClassNotFoundException e) {
                String msg = Main.getInstance().getConfig().getString("messages.link_need_citizens");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
                return true;
            }
            org.bukkit.entity.Entity target = null;
            try {
                org.bukkit.Location eye = p.getEyeLocation();
                org.bukkit.util.Vector dir = eye.getDirection();
                org.bukkit.util.RayTraceResult rr = p.getWorld().rayTraceEntities(eye, dir, 5.0, 0.3, entity -> true);
                if (rr != null && rr.getHitEntity() != null) {
                    target = rr.getHitEntity();
                }
            } catch (Throwable t) {
                target = null;
            }
            if (target == null) {
                String msg = Main.getInstance().getConfig().getString("messages.link_need_npc");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
                return true;
            }
            try {
                Class<?> api = Class.forName("net.citizensnpcs.api.CitizensAPI");
                Object reg = api.getMethod("getNPCRegistry").invoke(null);
                java.lang.reflect.Method getNPC = reg.getClass().getMethod("getNPC", org.bukkit.entity.Entity.class);
                Object npc = getNPC.invoke(reg, target);
                if (npc == null) {
                    String msg = Main.getInstance().getConfig().getString("messages.link_need_npc");
                    if (msg != null) p.sendMessage(ItemUtil.color(msg));
                    return true;
                }
                int id = (int) npc.getClass().getMethod("getId").invoke(npc);
                if (Main.getInstance().getLinkStore().isLinked(id)) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("npcId", Integer.toString(id));
                    String msg = Main.getInstance().getConfig().getString("messages.link_already");
                    if (msg != null) p.sendMessage(ItemUtil.fmt(msg, repl));
                    return true;
                }
                Main.getInstance().getLinkStore().add(id);
                Map<String, String> repl = new HashMap<>();
                repl.put("npcId", Integer.toString(id));
                String msg = Main.getInstance().getConfig().getString("messages.link_success");
                if (msg != null) p.sendMessage(ItemUtil.fmt(msg, repl));
            } catch (Throwable t) {
                String msg = Main.getInstance().getConfig().getString("messages.link_error");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
            }
            return true;
        }

        // /캐시상점 연동해제
        if ("연동해제".equalsIgnoreCase(args[0])) {
            if (!p.hasPermission("ultimate.cashshop.admin")) {
                p.sendMessage("§c권한이 없습니다.");
                return true;
            }
            try {
                Class.forName("net.citizensnpcs.api.CitizensAPI");
            } catch (ClassNotFoundException e) {
                String msg = Main.getInstance().getConfig().getString("messages.link_need_citizens");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
                return true;
            }

            org.bukkit.entity.Entity target = null;
            try {
                org.bukkit.Location eye = p.getEyeLocation();
                org.bukkit.util.Vector dir = eye.getDirection();
                org.bukkit.util.RayTraceResult rr = p.getWorld().rayTraceEntities(eye, dir, 5.0, 0.3, entity -> true);
                if (rr != null && rr.getHitEntity() != null) {
                    target = rr.getHitEntity();
                }
            } catch (Throwable t) {
                target = null;
            }
            if (target == null) {
                String msg = Main.getInstance().getConfig().getString("messages.link_need_npc");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
                return true;
            }
            try {
                Class<?> api = Class.forName("net.citizensnpcs.api.CitizensAPI");
                Object reg = api.getMethod("getNPCRegistry").invoke(null);
                java.lang.reflect.Method getNPC = reg.getClass().getMethod("getNPC", org.bukkit.entity.Entity.class);
                Object npc = getNPC.invoke(reg, target);
                if (npc == null) {
                    String msg = Main.getInstance().getConfig().getString("messages.link_need_npc");
                    if (msg != null) p.sendMessage(ItemUtil.color(msg));
                    return true;
                }
                int id = (int) npc.getClass().getMethod("getId").invoke(npc);
                if (!Main.getInstance().getLinkStore().isLinked(id)) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("npcId", Integer.toString(id));
                    String msg = Main.getInstance().getConfig().getString("messages.link_not_linked");
                    if (msg != null) p.sendMessage(ItemUtil.fmt(msg, repl));
                    return true;
                }
                Main.getInstance().getLinkStore().remove(id);
                Map<String, String> repl = new HashMap<>();
                repl.put("npcId", Integer.toString(id));
                String msg = Main.getInstance().getConfig().getString("messages.link_unlinked");
                if (msg != null) p.sendMessage(ItemUtil.fmt(msg, repl));
            } catch (Throwable t) {
                String msg = Main.getInstance().getConfig().getString("messages.link_error");
                if (msg != null) p.sendMessage(ItemUtil.color(msg));
            }
            return true;
        }

        // fallback
        String usage = command.getUsage();
        p.sendMessage(ItemUtil.color("&c사용법: " + (usage == null ? "/캐시상점 열기 | /캐시상점 등록 <갯수> <가격(캐시)> <슬롯> | /캐시상점 등록삭제 <슬롯> | /캐시상점 연동 | /캐시상점 연동해제" : usage)));
        return true;
    }
}
