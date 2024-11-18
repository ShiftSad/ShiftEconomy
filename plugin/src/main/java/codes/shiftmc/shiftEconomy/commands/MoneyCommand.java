package codes.shiftmc.shiftEconomy.commands;

import codes.shiftmc.common.messaging.MessagingManager;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.shiftEconomy.commands.admin.AdminCommand;
import codes.shiftmc.shiftEconomy.commands.player.PayCommand;
import codes.shiftmc.shiftEconomy.commands.player.TopCommand;
import codes.shiftmc.shiftEconomy.commands.player.TransactionsCommand;
import codes.shiftmc.shiftEconomy.language.LanguageManager;
import codes.shiftmc.shiftEconomy.packet.SendOnlinePacketListener;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class MoneyCommand {

    private final UserService userService;
    private final TransactionService transactionService;
    private final MessagingManager messagingManager;
    private final JavaPlugin plugin;

    public void register() {
        var playerArg = new OfflinePlayerArgument("target")
                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                        SendOnlinePacketListener.getPlayerNames()
        ));

        new CommandAPICommand("money")
                .withPermission("shifteconomy.money.player")
                .withSubcommands(
                        new AdminCommand(userService, messagingManager, plugin).get(),
                        new TopCommand(userService).get(),
                        new TransactionsCommand(transactionService, userService).get(),
                        new PayCommand(transactionService, userService, messagingManager).get()
                )
                .withOptionalArguments(playerArg)
                .executesPlayer((player, arguments) -> {
                    LanguageManager lang = LanguageManager.instance();
                    var target = arguments.getOptionalByArgument(playerArg).orElse(player);
                    displayBalance(player, player.locale().toLanguageTag(), target.getUniqueId(), lang);
                })
                .executes((sender, arguments) -> {
                    LanguageManager lang = LanguageManager.instance();
                    var target = arguments.getOptionalByArgument(playerArg);
                    target.ifPresentOrElse(
                        offlinePlayer -> displayBalance(sender, LanguageManager.DEFAULT_LANGUAGE, offlinePlayer.getUniqueId(), lang),
                        () -> lang.sendMessage(sender, "player.balance.error.missing")
                    );
                })
                .register();
    }

    /**
     * Displays the balance of the specified user to the command sender.
     *
     * @param viewer The audience who issued the command.
     * @param language Representation of the language.
     * @param targetUuid The UUID of the target player whose balance is requested.
     */
    private void displayBalance(Audience viewer, String language, UUID targetUuid, LanguageManager lang) {
        userService.findByUuid(targetUuid)
                .map(userData -> lang.getMessage(language, "player.balance",
                    Placeholder.unparsed("balance", NumberFormatter.format(userData.getBalance())),
                    Placeholder.unparsed("name", userData.getUsername())
                ))
                .defaultIfEmpty(lang.getMessage(language, "player.balance.error.missing"))
                .doOnError(error -> lang.getMessage(language, "generic.error.stacktrace",
                    Placeholder.unparsed("stacktrace", Arrays.toString(error.getStackTrace()))
                ))
                .subscribe(viewer::sendMessage);
    }
}
