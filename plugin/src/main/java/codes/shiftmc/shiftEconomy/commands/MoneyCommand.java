package codes.shiftmc.shiftEconomy.commands;

import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.shiftEconomy.commands.admin.AdminCommand;
import codes.shiftmc.shiftEconomy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class MoneyCommand {

    private final UserService userService;
    private final TransactionService transactionService;
    private final JavaPlugin plugin;

    private static final LanguageManager lang = LanguageManager.instance();

    public void register() {
        var playerArg = new OfflinePlayerArgument("player").replaceSafeSuggestions(
                SafeSuggestions.suggest(info -> Bukkit.getOnlinePlayers().toArray(new Player[0]))
        );

        new CommandAPICommand("money")
                .withSubcommand(new AdminCommand(plugin).get())
                .withOptionalArguments(new OfflinePlayerArgument("player"))
                .executesPlayer((player, arguments) -> {
                    var target = arguments.getOptionalByArgument(playerArg).orElse(player);
                    displayBalance(player, player.locale().toLanguageTag(), target.getUniqueId());
                })
                .executes((sender, arguments) -> {
                    var target = arguments.getOptionalByArgument(playerArg);
                    target.ifPresentOrElse(
                        offlinePlayer -> displayBalance(sender, "en_US", offlinePlayer.getUniqueId()),
                        () -> sender.sendMessage(lang.getMessage("player.balance.missing"))
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
    private void displayBalance(Audience viewer, String language, UUID targetUuid) {
        userService.findByUuid(targetUuid)
                .map(userData -> lang.getMessage(language, "player.balance",
                    Placeholder.unparsed("balance", NumberFormatter.format(userData.getBalance())),
                    Placeholder.unparsed("name", userData.getUsername())
                ))
                .defaultIfEmpty(lang.getMessage(language, "player.balance.missing"))
                .doOnError(error -> lang.getMessage(language, "generic.error.stacktrace",
                    Placeholder.unparsed("stacktrace", Arrays.toString(error.getStackTrace()))
                ))
                .subscribe(viewer::sendMessage);
    }
}
