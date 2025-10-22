package com.minkang.ultimate.cashvoucher.commands;

import com.minkang.ultimate.cashvoucher.util.ItemFactory;
import com.minkang.ultimate.cashvoucher.VoucherPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoucherCommand implements CommandExecutor, TabCompleter {
    private final VoucherPlugin plugin;
    public VoucherCommand(VoucherPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.cashshop.voucher")) {
            sender.sendMessage(color("&c권한이 없습니다."));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&c플레이어만 사용할 수 있습니다."));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(color("&e사용법: /캐시권 <금액>"));
            return true;
        }
        Long amount = parsePositiveLong(args[0]);
        if (amount == null) {
            sender.sendMessage(color("&c금액은 1 이상의 정수로 입력하세요."));
            return true;
        }
        Player p = (Player) sender;
        ItemStack voucher = ItemFactory.createVoucher(plugin, amount, p.getName());
        // add to inventory
        p.getInventory().addItem(voucher);
        p.sendMessage(color("&a캐시 수표를 발행했습니다: &f" + amount + "&a 캐시"));
        return true;
    }

    private Long parsePositiveLong(String s) {
        try {
            long v = Long.parseLong(s.replaceAll("[,_]", ""));
            if (v <= 0) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(java.util.Arrays.asList("1000","10000","50000","100000"));
        }
        return Collections.emptyList();
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
