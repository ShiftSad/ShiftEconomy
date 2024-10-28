package codes.shiftmc.shiftEconomy.commands;

import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class MoneyCommand {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    private final UserService userService;
    private final TransactionService transactionService;

    public void register() {
        new CommandAPICommand("money")
                .withOptionalArguments(new OfflinePlayerArgument("player"))
                .executesPlayer((player, arguments) -> {
                    var target = arguments.getOptionalByClass("player", OfflinePlayer.class).orElse(player);
                    checkMoney(target).subscribe(player::sendMessage);
                })
                .register();
    }

    public Mono<Component> checkMoney(OfflinePlayer player) {
        return userService.findByUuid(player.getUniqueId())
                .map(userData -> {
                    double balance = userData.getBalance();
                    return mm.deserialize("<yellow><player_name></yellow>'s balance: <green>$<balance></green>",
                            Placeholder.unparsed("player_name", player.getName() != null ? player.getName() : "Unknown"),
                            Placeholder.unparsed("balance", String.format("%.2f", balance)));
                })
                .switchIfEmpty(Mono.just(mm.deserialize("<red>No balance found for <player_name>.</red>",
                        Placeholder.unparsed("player_name", player.getName() != null ? player.getName() : "Unknown"))));
    }
}
