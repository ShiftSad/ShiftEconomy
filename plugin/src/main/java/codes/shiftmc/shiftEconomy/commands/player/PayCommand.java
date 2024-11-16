package codes.shiftmc.shiftEconomy.commands.player;

import codes.shiftmc.common.messaging.MessagingManager;
import codes.shiftmc.common.messaging.packet.PaymentPacket;
import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.shiftEconomy.ShiftEconomy;
import codes.shiftmc.shiftEconomy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
public class PayCommand {

    private final TransactionService transactionService;
    private final UserService userService;
    private final MessagingManager messagingManager;

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

        if (senderUUID.equals(receiver.getUniqueId().toString())) {
            lang.sendMessage(sender, "player.pay.error.cant-send-to-self");
            return;
        }

        if (amount < 0) {
            lang.sendMessage(sender, "player.pay.error.negative");
            return;
        }

        UUID a;
        if (Objects.equals(senderUUID, "server")) a = UUID.nameUUIDFromBytes(senderUUID.getBytes(StandardCharsets.UTF_8));
        else a = UUID.fromString(senderUUID);

        pay(a, receiver.getUniqueId(), amount)
                .doOnSuccess((unused) -> {
                    lang.sendMessage(sender, "player.pay.sent",
                                     Placeholder.unparsed("receiver", Objects.requireNonNull(receiver.getName())),
                                     Placeholder.unparsed("amount", NumberFormatter.format(amount)));
                    if (receiver.isOnline()) lang.sendMessage((Player) receiver, "player.pay.received",
                                                              Placeholder.unparsed("sender", sender.getName()),
                                                              Placeholder.unparsed("amount", NumberFormatter.format(amount)));
                    else {
                        messagingManager.sendPacket(new PaymentPacket(
                                UUID.fromString(senderUUID),
                                receiver.getUniqueId(),
                                amount,
                                ShiftEconomy.serverUUID,
                                PaymentPacket.PaymentType.PAY
                        ));
                    }
                })
                .onErrorResume((error) -> {
                    lang.sendMessage(sender,  "player.pay.error.not-found");
                    return Mono.empty();
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
        Mono<Void> creditOperation = userService.getBalance(receiverUUID)
            .switchIfEmpty(Mono.error(new IllegalStateException("Receiver balance is null")))
            .flatMap(balance -> userService.updateBalance(receiverUUID, balance + amount)).then();

        Mono<Void> debitOperation = senderUUID != null
            ? userService.getBalance(senderUUID)
                .switchIfEmpty(Mono.error(new IllegalStateException("Sender balance is null")))
                .flatMap(balance -> userService.updateBalance(senderUUID, balance - amount)).then()
            : Mono.empty();

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