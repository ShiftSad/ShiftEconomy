package codes.shiftmc.shiftEconomy.commands;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.wrappers.IntegerRange;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import reactor.core.publisher.Flux;

import java.util.UUID;

public class MoneyTopCommand {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    private static final Component HEADER = mm.deserialize("""
            
            <green>TOP 10 jogadores(as) mais ricos(as)
            
            """
    );
    private static final String ELEMENTS = "<green>. <player_name> - <balance>";

    public static CommandAPICommand get(UserService userService) {
        return new CommandAPICommand("top")
                .withOptionalArguments(new IntegerRangeArgument("range"))
                .executes((sender, context) -> {
                    IntegerRange range = (IntegerRange) context.getOptional("range")
                            .orElse(new IntegerRange(0, 10));

                    Flux<UserData> usersFlux = userService.findTopUsers(range.getLowerBound(), range.getUpperBound());
                    usersFlux
                            .collectList()
                            .doOnNext(users -> {
                                sender.sendMessage(HEADER);

                                for (int i = 0; i < (range.getUpperBound() - range.getLowerBound()); i++) {
                                    UserData user;
                                    if (i < users.size()) user = users.get(i);
                                    else user = new UserData(UUID.randomUUID(), "vazio", 0);

                                    mm.deserialize(ELEMENTS,
                                            Placeholder.unparsed("player_name", user.getUsername()),
                                            Placeholder.unparsed("balance", NumberFormatter.format(user.getBalance()))
                                    );
                                }
                            })
                            .subscribe();
                });
    }
}
