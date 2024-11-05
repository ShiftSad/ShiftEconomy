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

public class MoneyTopCommand {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    private static final Component HEADER = mm.deserialize("""
            
            <green>TOP 10 jogadores(as) mais ricos(as)
            
            """
    );
    private static final String FOOTER = """

            <green><click:run_command:'/money top <back_upper>..<back_lower>'><<</click> | <click:run_command:'/money top <forward_lower>..<forward_upper>'>>></click>
            """;
    private static final Component ERROR_BIG = mm.deserialize("""
            <red>ERROR: Você não pode verificar mais de 15 jogadores de uma vez.
            """
    );
    private static final Component ERROR_NEGATIVE = mm.deserialize("""
            <red>ERROR: Você não pode olhar um top negativo.
            """
    );
    private static final String ELEMENTS = "<green><position>. <player_name> - <balance>";

    public static CommandAPICommand get(UserService userService) {
        return new CommandAPICommand("top")
                .withOptionalArguments(new IntegerRangeArgument("range"))
                .executes((sender, context) -> {
                    IntegerRange range = (IntegerRange) context.getOptional("range")
                            .orElse(new IntegerRange(0, 10));
                    if (range.getUpperBound() - range.getLowerBound() > 15) {
                        sender.sendMessage(ERROR_BIG);
                        return;
                    }

                    if (range.getLowerBound() < 0 || range.getUpperBound() < 0) {
                        sender.sendMessage(ERROR_NEGATIVE);
                        return;
                    }

                    Flux<UserData> usersFlux = userService.findTopUsers(range.getLowerBound(), range.getUpperBound());
                    usersFlux
                            .collectList()
                            .doOnNext(users -> {
                                sender.sendMessage(HEADER);

                                for (int i = range.getLowerBound(); i < range.getUpperBound(); i++) {
                                    UserData data;
                                    if (i >= users.size()) data = new UserData(null, "vazio", 0);
                                    else data = users.get(i);

                                    sender.sendMessage(mm.deserialize(ELEMENTS,
                                            Placeholder.unparsed("player_name", data.getUsername()),
                                            Placeholder.unparsed("balance", NumberFormatter.format(data.getBalance())),
                                            Placeholder.unparsed("position", i + 1 + ""))
                                    );
                                }

                                sender.sendMessage(mm.deserialize(FOOTER
                                        .replace("<back_lower>", Math.max(range.getUpperBound() - 10, 10) + "")
                                        .replace("<back_upper>", Math.max(range.getLowerBound() - 10, 0) + "")
                                        .replace("<forward_lower>", range.getLowerBound() + 10 + "")
                                        .replace("<forward_upper>", range.getUpperBound() + 10 + "")
                                ));
                            })
                            .subscribe();
                });
    }
}
