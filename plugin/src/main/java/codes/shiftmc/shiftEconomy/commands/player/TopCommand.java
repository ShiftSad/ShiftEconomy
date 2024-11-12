package codes.shiftmc.shiftEconomy.commands.player;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.shiftEconomy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.wrappers.IntegerRange;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class TopCommand {

    private final UserService userService;

    private static final MiniMessage mm = MiniMessage.builder().build();

    public CommandAPICommand get() {
        return new CommandAPICommand( "top")
                .withOptionalArguments(new IntegerRangeArgument("range"))
                .executes((sender, args) -> {
                    var lang = LanguageManager.instance();

                    IntegerRange range = (IntegerRange) args.getOptional("range")
                            .orElse(new IntegerRange(0, 10));
                    if (!checkArgument(range, lang, sender)) return;

                    var userFlux = userService.findTopUsers(range.getLowerBound(), range.getUpperBound());
                    userFlux.collectList()
                            .doOnNext(users -> {
                                lang.sendMessage(sender, "player.top.header");
                                var location = lang.getRawMessage("player.top.position");

                                for (int i = range.getLowerBound(); i < range.getUpperBound(); i++) {
                                    UserData data;
                                    if (i >= users.size()) data = new UserData(null, lang.getRawMessage(sender, "player.top.missing"), 0.0);
                                    else data = users.get(i);

                                    lang.sendMessage(sender, "player.top.elements",
                                            Placeholder.unparsed("player_name", data.getUsername()),
                                            Placeholder.unparsed("balance", NumberFormatter.format(data.getBalance())),
                                            Placeholder.parsed("position", location.replace("<number>", i + 1 + "")) // Easier and faster than using MiniMessage
                                    );
                                }

                                var footer = lang.getRawMessage(sender, "player.top.footer")
                                        .replace("<back_lower>", Math.max(range.getUpperBound() - 10, 10) + "")
                                        .replace("<back_upper>", Math.max(range.getLowerBound() - 10, 0) + "")
                                        .replace("<forward_lower>", range.getLowerBound() + 10 + "")
                                        .replace("<forward_upper>", range.getUpperBound() + 10 + "");

                                sender.sendMessage(mm.deserialize(footer));
                            })
                            .subscribe();
                });
    }

    public static boolean checkArgument(IntegerRange range, LanguageManager lang, CommandSender sender) {
        if (range.getUpperBound() - range.getLowerBound() > 15) {
            lang.sendMessage(sender,  "player.range.error.big");
            return false;
        }

        if (range.getLowerBound() < 0 || range.getUpperBound() < 0) {
            lang.sendMessage(sender,  "player.range.error.negative");
            return false;
        }

        if (range.getUpperBound() > 1000) {
            lang.sendMessage(sender, "player.range.error.numberbig");
            return false;
        }

        return true;
    }
}