package codes.shiftmc.shiftEconomy.vault;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import lombok.AllArgsConstructor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
public class VaultEconomyHook extends EconomyWrapper {

    private final UserService userService;
    private final TransactionService transactionService;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "ShiftEconomy";
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double v) {
        return NumberFormatter.format(v);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return userService.findByUuid(offlinePlayer.getUniqueId())
                .blockOptional()
                .isPresent();
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return userService.getBalance(offlinePlayer.getUniqueId())
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Could not get balance"));
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        // Check if user has enough balance and withdraw if possible
        Mono<UserData> userMono = userService.findByUuid(offlinePlayer.getUniqueId());
        return userMono.flatMap(user -> {
            if (user.getBalance() < amount)
                return Mono.just(new EconomyResponse(0, user.getBalance(), EconomyResponse.ResponseType.FAILURE, "Insufficient funds"));
            else {
                return userService.updateBalance(user.getUUID(), user.getBalance() - amount)
                        .publishOn(Schedulers.boundedElastic())
                        .map(updatedUser -> {
                            // Record transaction
                            transactionService.createTransaction(new Transaction(null, user.getUUID(), amount, System.currentTimeMillis())).subscribe();
                            return new EconomyResponse(amount, updatedUser.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
                        });
            }
        }).block();
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        // Deposit amount to user balance
        return userService.findByUuid(offlinePlayer.getUniqueId())
                .flatMap(user -> userService.updateBalance(user.getUUID(), user.getBalance() + amount)
                        .publishOn(Schedulers.boundedElastic())
                        .map(updatedUser -> {
                            // Record transaction
                            transactionService.createTransaction(new Transaction(null, user.getUUID(), amount, System.currentTimeMillis())).subscribe();
                            return new EconomyResponse(amount, updatedUser.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
                        }))
                .defaultIfEmpty(new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account not found"))
                .block();
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        // Create new user account if it does not exist
        boolean present = userService.findByUuid(offlinePlayer.getUniqueId()).blockOptional().isPresent();
        if (!present) {
            userService.save(new UserData(offlinePlayer.getUniqueId(), offlinePlayer.getName(), 0.0)).subscribe();
            present = true;
        }

        return present;
    }
}
