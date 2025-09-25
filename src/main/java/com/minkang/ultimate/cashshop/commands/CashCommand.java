
package com.minkang.ultimate.cashshop.commands;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.store.BalanceStore;
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

    private final Main plugin;
    private final BalanceStore balance;

    public CashCommand(Main plugin) {
        this.plugin = plugin;
        this.balance = plugin.getBalanceStore();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /캐시 : 본인 잔액 확인
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                String msg = plugin.getConfig().getString("messages.only_player", "&c이 명령어는 플레이어만 사용할 수 있습니다.");
                sender.sendMessage(ItemUtil.color(msg));
                return true;
            }
            Player p = (Player) sender;
            long bal = balance.get(p.getUniqueId());
            p.sendMessage(ItemUtil.color("&a현재 캐시: &f" + bal));
            return true;
        }

        String sub = args[0];

        // 관리자 지급/차감
        if (sub.equalsIgnoreCase("지급") || sub.equalsIgnoreCase("차감")) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage(ItemUtil.color("&c권한이 없습니다."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시 " + sub + " <플레이어> <수량>"));
                return true;
            }
            String targetName = args[1];
            Long amt = parsePositiveLong(args[2]);
            if (amt == null || amt <= 0) {
                sender.sendMessage(ItemUtil.color("&c수량은 1 이상의 정수여야 합니다."));
                return true;
            }

            OfflinePlayer off = Bukkit.getOfflinePlayer(targetName);
            if (off == null || (off.getName() == null && !off.isOnline())) {
                Map<String, String> r = new HashMap<String, String>();
                r.put("player", targetName);
                sender.sendMessage(ItemUtil.fmt(plugin.getConfig().getString("messages.player_not_found", "&c플레이어 {player}를 찾을 수 없습니다."), r));
                return true;
            }

            UUID uuid = off.getUniqueId();
            if (sub.equalsIgnoreCase("지급")) {
                balance.add(uuid, amt);
                sender.sendMessage(ItemUtil.color("&a" + targetName + "에게 &f" + amt + "&7 캐시를 지급했습니다."));
            } else {
                // 차감
                long cur = balance.get(uuid);
                long next = Math.max(0L, cur - amt);
                balance.set(uuid, next);
                sender.sendMessage(ItemUtil.color("&e" + targetName + "의 캐시에서 &f" + amt + "&7 차감했습니다. 현재: &f" + next));
            }
            return true;
        }

        // /캐시 보내기 <플레이어> <수량>
        if (sub.equalsIgnoreCase("보내기")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ItemUtil.color("&c이 명령어는 플레이어만 사용할 수 있습니다."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시 보내기 <플레이어> <수량>"));
                return true;
            }

            Player from = (Player) sender;
            String targetName = args[1];

            // 금액 파싱
            Long amt = parsePositiveLong(args[2]);
            if (amt == null || amt <= 0) {
                String msg = plugin.getConfig().getString("messages.invalid_amount", "&c수량은 1 이상의 정수여야 합니다.");
                from.sendMessage(ItemUtil.color(msg));
                return true;
            }

            // 자기 자신 체크
            if (from.getName().equalsIgnoreCase(targetName)) {
                String msg = plugin.getConfig().getString("messages.cannot_self", "&c본인에게는 보낼 수 없습니다.");
                from.sendMessage(ItemUtil.color(msg));
                return true;
            }

            // 대상 검색
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (target == null || (target.getName() == null && !target.isOnline())) {
                Map<String, String> r = new HashMap<String, String>();
                r.put("player", targetName);
                String msg = plugin.getConfig().getString("messages.player_not_found", "&c플레이어 {player}를 찾을 수 없습니다.");
                from.sendMessage(ItemUtil.fmt(msg, r));
                return true;
            }

            UUID fromId = from.getUniqueId();
            UUID toId = target.getUniqueId();

            long fromBal = balance.get(fromId);
            if (fromBal < amt) {
                Map<String, String> r = new HashMap<String, String>();
                r.put("balance", String.valueOf(fromBal));
                String msg = plugin.getConfig().getString("messages.not_enough", "&c보유 캐시가 부족합니다. 현재 잔액: &f{balance}");
                from.sendMessage(ItemUtil.fmt(msg, r));
                return true;
            }

            // 이체 수행
            balance.add(fromId, -amt);
            balance.add(toId, amt);

            Map<String, String> repl = new HashMap<String, String>();
            repl.put("from", from.getName());
            repl.put("to", target.getName() != null ? target.getName() : targetName);
            repl.put("amount", String.valueOf(amt));
            repl.put("balance", String.valueOf(balance.get(toId)));
            repl.put("sender_balance", String.valueOf(balance.get(fromId)));

            String sentMsg = plugin.getConfig().getString("messages.sent", "&a{to}&7에게 &f{amount}&7 캐시를 보냈습니다. 현재 잔액: &f{sender_balance}");
            String recvMsg = plugin.getConfig().getString("messages.incoming", "&a{from}&7님이 &f{amount}&7 캐시를 보냈습니다. 현재 잔액: &f{balance}");

            from.sendMessage(ItemUtil.fmt(sentMsg, repl));

            if (target.isOnline()) {
                Player toPlayer = target.getPlayer();
                if (toPlayer != null) toPlayer.sendMessage(ItemUtil.fmt(recvMsg, repl));
            }
            return true;
        }

        // 도움말
        sender.sendMessage(ItemUtil.color("&c사용법: /캐시 | /캐시 지급 <플레이어> <수량> | /캐시 차감 <플레이어> <수량> | /캐시 보내기 <플레이어> <수량>"));
        return true;
    }

    private Long parsePositiveLong(String s) {
        try {
            long v = Long.parseLong(s);
            if (v <= 0) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
