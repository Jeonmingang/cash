# UltimateCashVoucher

OP-only command **/캐시권 <금액>** that creates a *cash voucher* item.
Redeem **only by right-clicking AIR with the item in the main hand** (off-hand & block-click do not trigger).
On redeem, the player's cash is credited via `UltimateCashShop`'s BalanceStore (reflection).

## Requirements
- Paper/Spigot **1.16.5** (Java 11)
- The plugin **UltimateCashShop** must be installed and enabled (same one you sent: `name: UltimateCashShop`)
- This plugin is independent and keeps all existing features intact.

## Permissions
- `ultimate.cashshop.voucher` (default: OP)

## Build
- GitHub Actions workflow included (`.github/workflows/main.yml`)
- Or locally: `mvn package`

## Install
1. Drop `UltimateCashVoucher-x.y.z.jar` into `plugins/` alongside `UltimateCashShop.jar`
2. Restart server.
3. OP runs `/캐시권 <금액>` to get a voucher.
4. Main-hand + right-click on **air** to redeem. Off-hand right-click will NOT work.
