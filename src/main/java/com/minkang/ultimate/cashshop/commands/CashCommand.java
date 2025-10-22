
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
        // ===== /캐시 <플레이어> : 지정 플레이어 잔액 확인 =====
        if (args.length == 1
                && !sub.equalsIgnoreCase("랭킹")
                && !sub.equalsIgnoreCase("지급")
                && !sub.equalsIgnoreCase("차감")
                && !sub.equalsIgnoreCase("보내기")) {
            org.bukkit.OfflinePlayer off = org.bukkit.Bukkit.getOfflinePlayer(sub);
            if (off == null || ((off.getName() == null) && !off.isOnline() && !off.hasPlayedBefore())) {
                java.util.Map<String,String> r = new java.util.HashMap<>();
                r.put("player", sub);
                String msg = plugin.getConfig().getString("messages.player_not_found", "&c플레이어 {player}를 찾을 수 없습니다.");
                sender.sendMessage(com.minkang.ultimate.cashshop.util.ItemUtil.fmt(msg, r));
                return true;
            }
            long other = balance.get(off.getUniqueId());
            java.util.Map<String,String> r = new java.util.HashMap<>();
            r.put("player", off.isOnline() ? off.getPlayer().getName() : off.getName());
            r.put("amount", String.valueOf(other));
            String fmt = plugin.getConfig().getString("messages.balance_other", "&f{player}&7의 보유 캐시: &a{amount}");
            sender.sendMessage(com.minkang.ultimate.cashshop.util.ItemUtil.fmt(fmt, r));
            return true;
        }

        // ===== 신규기능: /캐시 랭킹 =====
        if (sub.equalsIgnoreCase("랭킹")) {
            java.util.Map<java.util.UUID, Long> all = balance.getAllBalances();
            java.util.List<java.util.Map.Entry<java.util.UUID, Long>> list = new java.util.ArrayList<>(all.entrySet());
            list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

            int total = list.size();
            int limit = Math.min(10, total);
            sender.sendMessage(com.minkang.ultimate.cashshop.util.ItemUtil.color("&6[캐시 랭킹] &f(총 " + total + "명, 상위 " + limit + "명)"));
            for (int idx = 0; idx < limit; idx++) {
                java.util.Map.Entry<java.util.UUID, Long> e = list.get(idx);
                org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(e.getKey());
                String name = (op != null && op.getName() != null) ? op.getName() : e.getKey().toString().substring(0, 8);
                sender.sendMessage(com.minkang.ultimate.cashshop.util.ItemUtil.color("&e" + (idx+1) + "위 &f" + name + " &7- &a" + e.getValue() + " 캐시"));
            }
            if (limit == 0) {
                sender.sendMessage(com.minkang.ultimate.cashshop.util.ItemUtil.color("&7랭킹에 표시할 데이터가 없습니다."));
            }
            return true;
        }


        // 관리자 지급/차감
        if (sub.equalsIgnoreCase("지급") || sub.equalsIgnoreCase("차감")) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage(ItemUtil.color("&c권한이 없습니다."));
                return true;
            }
            if (args.length < 3) {
                
        // ===== 신규기능: /캐시 <플레이어> 잔액 확인 =====
        if (args.length == 1) {
            String targetName = args[0];
            if (!(targetName.equalsIgnoreCase("지급") || targetName.equalsIgnoreCase("차감") || targetName.equalsIgnoreCase("보내기") || targetName.equalsIgnoreCase("랭킹"))) {
                org.bukkit.OfflinePlayer off = org.bukkit.Bukkit.getOfflinePlayer(targetName);
                if (off == null || (off.getName() == null && !off.isOnline())) {
                    java.util.Map<String, String> r = new java.util.HashMap<String, String>();
                    r.put("player", targetName);
                    String msg = plugin.getConfig().getString("messages.player_not_found", "&c플레이어 {player}를 찾을 수 없습니다.");
                    sender.sendMessage(com.minkang.ultimate.cashshop.util.ItemUtil.fmt(msg, r));
                    return true;
                }
                long balOther = balance.get(off.getUniqueId());
                sender.sendMessage(com.minkang.ultimate.cashshop.util.ItemUtil.color("&f" + off.getName() + "&7의 보유 캐시: &a" + balOther));
                return true;
            }
        }

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
        sender.sendMessage(ItemUtil.color("&c사용법: /캐시 | /캐시 <플레이어> | /캐시 랭킹 | /캐시 지급 <플레이어> <수량> | /캐시 차감 <플레이어> <수량> | /캐시 보내기 <플레이어> <수량>"));
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
