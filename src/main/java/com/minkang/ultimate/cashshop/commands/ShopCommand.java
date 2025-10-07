
package com.minkang.ultimate.cashshop.commands;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.gui.CashShopGUI;
import com.minkang.ultimate.cashshop.store.ShopStore;
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
    private final Main plugin;

    public ShopCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ItemUtil.color("&c이 명령어는 플레이어만 사용할 수 있습니다."));
                return true;
            }
            CashShopGUI.open((Player) sender, plugin);
            return true;
        }

        String sub = args[0];

        if (sub.equalsIgnoreCase("등록")) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage(ItemUtil.color("&c권한이 없습니다."));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ItemUtil.color("&c플레이어만 사용 가능합니다."));
                return true;
            }
            if (args.length < 4) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시상점 등록 <슬롯> <가격> <수량> (손에 든 아이템 등록)"));
                return true;
            }
            Integer slot = parseInt(args[1]);
            Long price = parseLong(args[2]);
            Integer amount = parseInt(args[3]);
            if (slot == null || slot < 0 || slot > 53) {
                sender.sendMessage(ItemUtil.color("&c슬롯은 0~53 사이여야 합니다."));
                return true;
            }
            if (price == null || price <= 0) {
                sender.sendMessage(ItemUtil.color("&c가격은 1 이상의 정수여야 합니다."));
                return true;
            }
            if (amount == null || amount <= 0) {
                sender.sendMessage(ItemUtil.color("&c수량은 1 이상의 정수여야 합니다."));
                return true;
            }
            Player p = (Player) sender;
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                sender.sendMessage(ItemUtil.color("&c손에 들고 있는 아이템이 없습니다."));
                return true;
            }
            ItemStack toSave = hand.clone();
            toSave.setAmount(1);
            plugin.getShopStore().register(slot, toSave, price, amount);
            Map<String, String> r = new HashMap<String, String>();
            r.put("slot", String.valueOf(slot));
            sender.sendMessage(ItemUtil.fmt("&a{slot}번 슬롯에 아이템을 등록했습니다.", r));
            return true;
        }

        if (sub.equalsIgnoreCase("취소") || sub.equalsIgnoreCase("삭제")) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage(ItemUtil.color("&c권한이 없습니다."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시상점 취소 <슬롯>"));
                return true;
            }
            Integer slot = parseInt(args[1]);
            if (slot == null || slot < 0 || slot > 53) {
                sender.sendMessage(ItemUtil.color("&c슬롯은 0~53 사이여야 합니다."));
                return true;
            }
            plugin.getShopStore().unregister(slot);
            Map<String, String> r = new HashMap<String, String>();
            r.put("slot", String.valueOf(slot));
            sender.sendMessage(ItemUtil.fmt("&a{slot}번 슬롯의 등록을 취소했습니다.", r));
            return true;
        }

        if (sub.equalsIgnoreCase("링크") || sub.equalsIgnoreCase("연동")) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage(ItemUtil.color("&c권한이 없습니다."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시상점 링크(연동) <NPC_ID>"));
                return true;
            }
            Integer id = parseInt(args[1]);
            if (id == null || id < 0) {
                sender.sendMessage(ItemUtil.color("&cNPC ID가 올바르지 않습니다."));
                return true;
            }
            plugin.getLinkStore().link(id);
            Map<String, String> r = new HashMap<String, String>();
            r.put("npcId", String.valueOf(id));
            sender.sendMessage(ItemUtil.fmt("&aNPC(ID: {npcId})와 캐시상점을 연동했습니다.", r));
            return true;
        }

        if (sub.equalsIgnoreCase("링크해제") || sub.equalsIgnoreCase("연동해제")) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage(ItemUtil.color("&c권한이 없습니다."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시상점 링크해제(연동해제) <NPC_ID>"));
                return true;
            }
            Integer id = parseInt(args[1]);
            if (id == null || id < 0) {
                sender.sendMessage(ItemUtil.color("&cNPC ID가 올바르지 않습니다."));
                return true;
            }
            plugin.getLinkStore().unlink(id);
            Map<String, String> r = new HashMap<String, String>();
            r.put("npcId", String.valueOf(id));
            sender.sendMessage(ItemUtil.fmt("&aNPC(ID: {npcId}) 연동을 해제했습니다.", r));
            return true;
        }

        // Fallback
        sender.sendMessage(ItemUtil.color("&c사용법: /캐시상점 | /캐시상점 등록 <슬롯> <가격> <수량> | /캐시상점 취소 <슬롯> | /캐시상점 링크(연동) <NPC_ID> | /캐시상점 링크해제(연동해제) <NPC_ID>"));
        return true;
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }
    private Long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}
