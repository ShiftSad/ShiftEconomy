package codes.shiftmc.shiftEconomy.commands;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class MoneyCommand {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    private final UserService userService;
    private final TransactionService transactionService;

    public void register() {
        var playerArg = new OfflinePlayerArgument("player");
        var amountArg = new DoubleArgument("amount");

        var pay = new CommandAPICommand("pay")
                .withArguments(
                        playerArg,
                        amountArg
                )
                .executesPlayer((player, arguments) -> {
                    var target = (OfflinePlayer) arguments.get("player");
                    var amount = (Double) arguments.get("amount");

                    if (target == null || amount == null || amount <= 0) {
                        player.sendMessage(mm.deserialize("<Red>Invalid arguments"));
                        return;
                    }
                    if (target == player) {
                        player.sendMessage(mm.deserialize("<red>You can't pay yourself"));
                        return;
                    }
                    pay(player.getUniqueId(), target.getUniqueId(), target.getName(), amount, false).subscribe(player::sendMessage);
                });
        pay.register();

        var set = new CommandAPICommand("give")
                .withArguments(
                        playerArg,
                        amountArg
                )
                .executes((player, arguments) -> {
                    var target = (OfflinePlayer) arguments.get("player");
                    var amount = (Double) arguments.get("amount");

                    if (target == null || amount == null || amount <= 0) {
                        player.sendMessage(mm.deserialize("<Red>Invalid arguments"));
                        return;
                    }

                    pay(null, target.getUniqueId(), target.getName(), amount, true).subscribe(player::sendMessage);
                });

        new CommandAPICommand("money")
                .withOptionalArguments(new OfflinePlayerArgument("player"))
                .withSubcommands(set, pay, MoneyTopCommand.get(userService), MoneyTransactionsCommand.get(userService, transactionService))
                .executesPlayer((player, arguments) -> {
                    var target = arguments.getOptionalByClass("player", OfflinePlayer.class).orElse(player);
                    checkMoney(target).subscribe(player::sendMessage);
                })
                .executes((sender, arguments) -> {
                    var target = arguments.getOptionalByClass("player", OfflinePlayer.class);
                    if (target.isPresent()) {
                        checkMoney(target.get()).subscribe(sender::sendMessage);
                        return;
                    }
                    sender.sendMessage(mm.deserialize("<red>You need to provide a player"));
                })
                .register();
    }

    private Mono<Component> pay(UUID senderUUID, UUID targetUUID, String target, double amount, boolean force) {
        if (amount <= 0) {
            return Mono.just(mm.deserialize("<red>Amount must be positive.</red>"));
        }

        // If senderUUID is null, skip the balance check and update for the sender
        if (senderUUID == null || force) {
            return userService.findByUuid(targetUUID)
                    .flatMap(targetData -> userService.updateBalance(targetUUID, targetData.getBalance() + amount))
                    .then(transactionService.createTransaction(
                            new Transaction(UUID.randomUUID(),null, targetUUID, amount, System.currentTimeMillis()))
                    )
                    .then(Mono.just(mm.deserialize("<green>Transaction successful! " + NumberFormatter.format(amount) + " transferred to " + target + ".</green>")))
                    .onErrorResume(e -> Mono.just(mm.deserialize("<red>Error processing transaction.</red> " + e.getMessage())));
        }

        // Check sender's balance and transfer if sufficient
        return userService.findByUuid(senderUUID)
                .flatMap(userData -> {
                    var balance = userData.getBalance();
                    if (balance < amount) {
                        return Mono.just(mm.deserialize("<red>Insufficient funds.</red>"));
                    }
                    // Deduct amount from sender's balance and add to target's balance
                    return userService.updateBalance(senderUUID, balance - amount)
                            .then(userService.findByUuid(targetUUID)
                                    .flatMap(targetData -> userService.updateBalance(targetUUID, targetData.getBalance() + amount)))
                            .then(transactionService.createTransaction(
                                    new Transaction(UUID.randomUUID(),senderUUID, targetUUID, amount, System.currentTimeMillis()))
                            )
                            .then(Mono.just(mm.deserialize("<green>Transaction successful! " + NumberFormatter.format(amount) + " transferred to " + target + ".</green>")));
                })
                .onErrorResume(e -> Mono.just(mm.deserialize("<red>Error processing transaction.</red> " + e.getMessage())));
    }

    private Mono<Component> checkMoney(OfflinePlayer player) {
        return userService.findByUuid(player.getUniqueId())
                .map(userData -> {
                    double balance = userData.getBalance();
                    return mm.deserialize("<yellow><player_name></yellow>'s balance: <green>$<balance></green>",
                            Placeholder.unparsed("player_name", player.getName() != null ? player.getName() : "Unknown"),
                            Placeholder.unparsed("balance", NumberFormatter.format(balance)));
                })
                .switchIfEmpty(Mono.just(mm.deserialize("<red>No balance found for <player_name>.</red>",
                        Placeholder.unparsed("player_name", player.getName() != null ? player.getName() : "Unknown"))));
    }
}
