package codes.shiftmc.shiftEconomy.commands.player;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class MoneyTransactionsCommand {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    private static final String RECEIVE_ELEMENT = "<green> + Você recebeu <amount> de <sender>";
    private static final String SEND_ELEMENT = "<red> - Você pagou <amount> para <receiver>";

    public static CommandAPICommand get(UserService userService, TransactionService transactionService) {
        return new CommandAPICommand("transactions")
                .executesPlayer((sender, context) -> {
                    Flux<Transaction> transactionsFlux = transactionService.getTransactionsByUser(sender.getUniqueId());
                    transactionsFlux
                            .collectList()
                            .doOnNext(transactions -> {
                                for (Transaction transaction : transactions) {
                                    UUID targetUUID = transaction.receiverUUID().equals(sender.getUniqueId())
                                            ? transaction.senderUUID()
                                            : transaction.receiverUUID();

                                    nameFromUUID(targetUUID, userService).subscribe(name -> {
                                        if (transaction.receiverUUID().equals(sender.getUniqueId())) {
                                            sender.sendMessage(mm.deserialize(RECEIVE_ELEMENT,
                                                    Placeholder.unparsed("amount", NumberFormatter.format(transaction.amount())),
                                                    Placeholder.unparsed("sender", name)
                                            ));
                                        } else {
                                            sender.sendMessage(mm.deserialize(SEND_ELEMENT,
                                                    Placeholder.unparsed("amount", NumberFormatter.format(transaction.amount())),
                                                    Placeholder.unparsed("receiver", name)
                                            ));
                                        }
                                    });
                                }
                            })
                            .subscribe();
                });
    }

    private static Mono<String> nameFromUUID(UUID uuid, UserService userService) {
        if (uuid == null) return Mono.just("server");
        return userService.findByUuid(uuid)
                .flatMap(userData -> Mono.justOrEmpty(userData.getUsername()));
    }
}
