package codes.shiftmc.economy.commands.player;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.economy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.wrappers.IntegerRange;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class TransactionsCommand {

    private final TransactionService transactionService;
    private final UserService userService;

    private static final MiniMessage mm = MiniMessage.builder().build();

    public CommandAPICommand get() {
        return new CommandAPICommand("transactions")
                .withPermission("shifteconomy.money.player.transactions")
                .withOptionalArguments(new IntegerRangeArgument("range"))
                .executesPlayer((sender, args) -> {
                    var lang = LanguageManager.instance();

                    IntegerRange range = (IntegerRange) args.getOptional("range")
                            .orElse(new IntegerRange(0, 10));
                    if (!TopCommand.checkArgument(range, lang, sender)) return;

                    lang.sendMessage(sender,  "player.transactions.header");
                    var transactionsFlux = transactionService.getTransactionsByUser(sender.getUniqueId(), range.getLowerBound(), range.getUpperBound());
                    transactionsFlux.collectList()
                            .defaultIfEmpty(Collections.emptyList())
                            .flatMap(transactions -> {
                                if (transactions.isEmpty()) {
                                    lang.sendMessage(sender, "player.transactions.empty");
                                    return Mono.empty();
                                }
                                return Mono.just(transactions);
                            })
                            .doOnNext(transactions -> {
                                List<Mono<Void>> transactionMessages = new ArrayList<>();

                                for (Transaction transaction : transactions) {
                                    UUID targetUUID = transaction.receiverUUID().equals(sender.getUniqueId())
                                            ? transaction.senderUUID()
                                            : transaction.receiverUUID();

                                    // Collect each transaction message operation into a Mono<Void> list
                                    Mono<Void> transactionMessage = nameFromUUID(targetUUID, userService)
                                        .flatMap(name -> {
                                            if (transaction.receiverUUID().equals(sender.getUniqueId())) {
                                                // Receive
                                                lang.sendMessage(sender, "player.transactions.receive",
                                                        Placeholder.unparsed("amount", NumberFormatter.format(transaction.amount())),
                                                        Placeholder.unparsed("sender", name)
                                                );
                                            } else {
                                                // Sent
                                                lang.sendMessage(sender, "player.transactions.send",
                                                        Placeholder.unparsed("amount", NumberFormatter.format(transaction.amount())),
                                                        Placeholder.unparsed("receiver", name)
                                                );
                                            }
                                            return Mono.empty();
                                        });

                                    transactionMessages.add(transactionMessage);
                                }

                                // After all transaction messages are processed, send the footer
                                Mono.when(transactionMessages)
                                    .doOnTerminate(() -> {
                                        var footer = lang.getRawMessage(sender, "player.transactions.footer")
                                                .replace("<back_lower>", Math.max(range.getUpperBound() - 10, 10) + "")
                                                .replace("<back_upper>", Math.max(range.getLowerBound() - 10, 0) + "")
                                                .replace("<forward_lower>", range.getLowerBound() + 10 + "")
                                                .replace("<forward_upper>", range.getUpperBound() + 10 + "");

                                        sender.sendMessage(mm.deserialize(footer));
                                    })
                                    .subscribe();
                            })
                            .subscribe();
                });
    }

    public static Mono<String> nameFromUUID(UUID uuid, UserService userService) {
        if (uuid == null) return Mono.just("server");
        if (uuid.equals(UUID.nameUUIDFromBytes("server".getBytes(StandardCharsets.UTF_8)))) return Mono.just("server");
        return userService.findByUuid(uuid)
                .flatMap(userData -> Mono.justOrEmpty(userData.getUsername()));
    }
}