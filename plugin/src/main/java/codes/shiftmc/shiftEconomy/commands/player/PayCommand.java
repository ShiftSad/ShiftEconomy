package codes.shiftmc.shiftEconomy.commands.player;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.shiftEconomy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import lombok.AllArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

import java.util.UUID;

@AllArgsConstructor
public class PayCommand {

    private final TransactionService transactionService;
    private final UserService userService;

    public CommandAPICommand get() {
        return new CommandAPICommand("pay")
                .withArguments(
                        new OfflinePlayerArgument("target"),
                        new DoubleArgument("amount")
                )
                .executes((sender, args) -> {
                    var receiver = (OfflinePlayer) args.get("target");
                    var amount = (Double) args.get("amount");
                    executePayCommand(sender, "server", receiver, amount);
                })
                .executesPlayer((player, args) -> {
                    var lang = LanguageManager.instance();
                    var receiver = (OfflinePlayer) args.get("target");
                    var amount = (Double) args.get("amount");
                    checkBalance(player.getUniqueId(), amount)
                        .doOnNext(enough -> {
                            if (enough) executePayCommand(player, player.getUniqueId().toString(), receiver, amount);
                            else lang.sendMessage(player, "player.balance.error.not-enough");
                        })
                        .subscribe();
                });
    }

    private void executePayCommand(CommandSender sender, String senderUUID, OfflinePlayer receiver, Double amount) {
        var lang = LanguageManager.instance();

        if (receiver == null || amount == null) {
            lang.sendMessage(sender, "player.command.error.invalid-arguments");
            return;
        }

        pay(UUID.fromString(senderUUID), receiver.getUniqueId(), amount)
                .doOnNext((a) -> {
                    lang.sendMessage(sender, "player.pay.sent");
                    if (receiver.isOnline()) lang.sendMessage((Player) receiver, "player.pay.received");
                })
                .subscribe();
    }

    private Mono<Boolean> checkBalance(UUID uuid, Double amount) {
        return userService.getBalance(uuid)
            .map(balance -> balance >= amount);
    }

    /*
     * This method assumes you already checked the balance
     */
    private Mono<Void> pay(UUID senderUUID, UUID receiverUUID, double amount) {
        Mono<UserData> debitOperation = senderUUID != null
            ? userService.updateBalance(senderUUID, -amount)
            : Mono.empty();

        var creditOperation = userService.updateBalance(receiverUUID, +amount);

        var transaction = new Transaction(
                UUID.randomUUID(), senderUUID, receiverUUID, amount, System.currentTimeMillis()
        );

        var transactionOperation = transactionService.createTransaction(transaction);

        return debitOperation
                .then(creditOperation)
                .then(transactionOperation)
                .then();
    }
}