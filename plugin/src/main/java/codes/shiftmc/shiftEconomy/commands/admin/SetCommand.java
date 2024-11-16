package codes.shiftmc.shiftEconomy.commands.admin;

import codes.shiftmc.common.messaging.MessagingManager;
import codes.shiftmc.common.messaging.packet.PaymentPacket;
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

@AllArgsConstructor
public class SetCommand {

    private final UserService userService;
    private final MessagingManager messagingManager;

    public CommandAPICommand get() {
        return new CommandAPICommand("set")
                .withArguments(
                        new OfflinePlayerArgument("target"),
                        new DoubleArgument("amount")
                )
                .withPermission("shifteconomy.admin.set")
                .executes((sender, args) -> {
                    var target = (OfflinePlayer) args.get("target");
                    var amount = (Double) args.get("amount");
                    executeSetCommand(sender, target, amount);
                });
    }

    private void executeSetCommand(CommandSender sender, OfflinePlayer target, Double amount) {
        var lang = LanguageManager.instance();

        if (target == null || amount == null) {
            lang.sendMessage(sender, "admin.command.error.invalid-arguments");
            return;
        }

        if (amount < 0) {
            lang.sendMessage(sender, "admin.set.error.negative");
            return;
        }

        userService.updateBalance(target.getUniqueId(), amount)
                .doOnSuccess(unused -> {
                    lang.sendMessage(sender, "admin.set.success",
                            Placeholder.unparsed("target", Objects.requireNonNull(target.getName())),
                            Placeholder.unparsed("amount", NumberFormatter.format(amount)));
                    if (target.isOnline()) {
                        lang.sendMessage((Player) target, "player.balance.set",
                                Placeholder.unparsed("amount", NumberFormatter.format(amount)));
                    } else {
                        messagingManager.sendPacket(new PaymentPacket(
                                UUID.nameUUIDFromBytes("server".getBytes(StandardCharsets.UTF_8)),
                                target.getUniqueId(),
                                amount,
                                ShiftEconomy.serverUUID
                        ));
                    }
                })
                .onErrorResume(error -> {
                    lang.sendMessage(sender, "admin.set.error.failed");
                    return Mono.empty();
                })
                .subscribe();
    }
}