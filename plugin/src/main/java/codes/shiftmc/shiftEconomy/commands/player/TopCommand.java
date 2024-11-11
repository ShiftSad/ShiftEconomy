package codes.shiftmc.shiftEconomy.commands.player;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.shiftEconomy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.wrappers.IntegerRange;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class TopCommand {

    private final UserService userService;

    public CommandAPICommand get() {
        return new CommandAPICommand( "top")
                .withOptionalArguments(new IntegerRangeArgument("range"))
                .executes((sender, args) -> {
                    var lang = LanguageManager.instance();

                    IntegerRange range = (IntegerRange) args.getOptional("range")
                            .orElse(new IntegerRange(0, 10));

                    if (range.getUpperBound() - range.getLowerBound() > 15) {
                        lang.sendMessage(sender,  "player.top.error.big");
                        return;
                    }

                    if (range.getLowerBound() < 0 || range.getUpperBound() < 0) {
                        lang.sendMessage(sender,  "player.top.error.negative");
                        return;
                    }

                    if (range.getUpperBound() > 10000) {
                        lang.sendMessage(sender, "player.top.error.numberbig");
                        return;
                    }

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

                                lang.sendMessage(sender, "player.top.footer",
                                        Placeholder.unparsed("back_lower", Math.max(range.getUpperBound() - 10, 10) + ""),
                                        Placeholder.unparsed("back_upper", Math.max(range.getLowerBound() - 10, 0) + ""),
                                        Placeholder.unparsed("forward_lower", range.getLowerBound() + 10 + ""),
                                        Placeholder.unparsed("forward_upper", range.getUpperBound() + 10 + "")
                                );
                            })
                            .subscribe();
                });
    }
}