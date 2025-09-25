
package com.minkang.ultimate.cashshop.commands;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CashCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                long bal = Main.getInstance().getBalanceStore().get(p.getUniqueId());
                String tpl = Main.getInstance().getConfig().getString("messages.balance", "&a보유 캐시: &f{balance}");
                Map<String, String> repl = new HashMap<>();
                repl.put("balance", Long.toString(bal));
                p.sendMessage(ItemUtil.fmt(tpl, repl));
                return true;
            } else {
                sender.sendMessage("플레이어만 사용할 수 있습니다.");
                return true;
            }
        }

        // /캐시 지급 <플레이어> <수량>
        if ("지급".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시 지급 <플레이어> <수량>"));
                return true;
            }
            String name = args[1];
            String amtStr = args[2];
            long amount;
            try {
                amount = Long.parseLong(amtStr);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c수량은 숫자여야 합니다.");
                return true;
            }
            if (amount == 0L) {
                sender.sendMessage("§c0원은 지급할 수 없습니다.");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            UUID uuid = target.getUniqueId();
            Main.getInstance().getBalanceStore().add(uuid, amount);
            Main.getInstance().getBalanceStore().save();

            long bal = Main.getInstance().getBalanceStore().get(uuid);

            Map<String, String> repl = new HashMap<>();
            repl.put("player", target.getName() == null ? name : target.getName());
            repl.put("amount", Long.toString(amount));
            repl.put("balance", Long.toString(bal));

            String given = Main.getInstance().getConfig().getString("messages.given");
            String received = Main.getInstance().getConfig().getString("messages.received");
            if (given != null) sender.sendMessage(ItemUtil.fmt(given, repl));

            if (target.isOnline()) {
                Player tp = target.getPlayer();
                if (tp != null && received != null) {
                    tp.sendMessage(ItemUtil.fmt(received, repl));
                }
            }
            return true;
        }

        // /캐시 차감 <플레이어> <수량>
        if ("차감".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시 차감 <플레이어> <수량>"));
                return true;
            }
            String name = args[1];
            String amtStr = args[2];
            long amount;
            try {
                amount = Long.parseLong(amtStr);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c수량은 숫자여야 합니다.");
                return true;
            }
            if (amount <= 0L) {
                sender.sendMessage("§c0 이하로는 차감할 수 없습니다.");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            UUID uuid = target.getUniqueId();
            Main.getInstance().getBalanceStore().add(uuid, -amount);
            Main.getInstance().getBalanceStore().save();

            long bal = Main.getInstance().getBalanceStore().get(uuid);
            Map<String, String> repl = new HashMap<>();
            repl.put("player", target.getName() == null ? name : target.getName());
            repl.put("amount", Long.toString(amount));
            repl.put("balance", Long.toString(bal));

            String took = Main.getInstance().getConfig().getString("messages.took");
            String deducted = Main.getInstance().getConfig().getString("messages.deducted");

            if (took != null) sender.sendMessage(ItemUtil.fmt(took, repl));
            if (target.isOnline()) {
                Player tp = target.getPlayer();
                if (tp != null && deducted != null) {
                    tp.sendMessage(ItemUtil.fmt(deducted, repl));
                }
            }
            return true;
        }

        // Fallback
        
        // /캐시 보내기 <닉네임> <수량>  -- player to player transfer
        boolean isSend = false;
            if (args[0].equalsIgnoreCase("보내기")) { isSend = true; }
            else if (args[0].equalsIgnoreCase("send")) { isSend = true; }
            if (args.length >= 3 && isSend) {
            if (!(sender instanceof Player)) {
                String onlyPlayer = Main.getInstance().getConfig().getString("messages.only_player");
                sender.sendMessage(ItemUtil.color(onlyPlayer != null ? onlyPlayer : "&c이 명령어는 플레이어만 사용할 수 있습니다."));
                return true;
            }
            Player from = (Player) sender;
            String targetName = args[1];
            long amount;
            try {
                amount = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                String invalid = Main.getInstance().getConfig().getString("messages.invalid_amount");
                from.sendMessage(ItemUtil.color(invalid != null ? invalid : "&c수량은 1 이상의 정수여야 합니다."));
                return true;
            }
            if (amount <= 0L) {
                String invalid = Main.getInstance().getConfig().getString("messages.invalid_amount");
                from.sendMessage(ItemUtil.color(invalid != null ? invalid : "&c수량은 1 이상의 정수여야 합니다."));
                return true;
            }
            if (from.getName().equalsIgnoreCase(targetName)) {
                String cannot = Main.getInstance().getConfig().getString("messages.cannot_self");
                from.sendMessage(ItemUtil.color(cannot != null ? cannot : "&c자기 자신에게는 보낼 수 없습니다."));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (target == null || (target.getName() == null && !target.isOnline())) {
                String pn = Main.getInstance().getConfig().getString("messages.player_not_found");
                Map<String, String> repl = new HashMap<>();
                repl.put("player", targetName);
                from.sendMessage(ItemUtil.fmt(pn != null ? pn : "&c해당 플레이어를 찾을 수 없습니다: {player}", repl));
                return true;
            }

            UUID fromId = from.getUniqueId();
            UUID toId = target.getUniqueId();

            long now = Main.getInstance().getBalanceStore().get(fromId);
            if (now < amount) {
                Map<String, String> repl = new HashMap<>();
                repl.put("amount", Long.toString(amount));
                repl.put("balance", Long.toString(now));
                String ne = Main.getInstance().getConfig().getString("messages.not_enough");
                from.sendMessage(ItemUtil.fmt(ne != null ? ne : "&c보유 캐시가 부족합니다. 필요: {amount}, 보유: {balance}", repl));
                return true;
            }

            Main.getInstance().getBalanceStore().add(fromId, -amount);
            Main.getInstance().getBalanceStore().add(toId, amount);
            Main.getInstance().getBalanceStore().save();

            Map<String, String> repl = new HashMap<>();
            repl.put("from", from.getName());
            repl.put("to", target.getName() == null ? targetName : target.getName());
            repl.put("amount", Long.toString(amount));
            repl.put("sender_balance", Long.toString(Main.getInstance().getBalanceStore().get(fromId)));
            repl.put("balance", Long.toString(Main.getInstance().getBalanceStore().get(toId)));

            String sentMsg = Main.getInstance().getConfig().getString("messages.sent");
            String recvMsg = Main.getInstance().getConfig().getString("messages.incoming");

            if (sentMsg != null) from.sendMessage(ItemUtil.fmt(sentMsg, repl));
            if (target.isOnline() && recvMsg != null) {
                Player to = target.getPlayer();
                if (to != null) to.sendMessage(ItemUtil.fmt(recvMsg, repl));
            }
            return true;
        }

        sender.sendMessage(ItemUtil.color("&c사용법: /캐시 | /캐시 지급 <플레이어> <수량> | /캐시 차감 <플레이어> <수량> | /캐시 보내기 <닉네임> <수량>"));
        return true;
    }
}
