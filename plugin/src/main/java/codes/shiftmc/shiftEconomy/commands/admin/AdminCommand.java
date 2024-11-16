package codes.shiftmc.shiftEconomy.commands.admin;

import codes.shiftmc.common.messaging.MessagingManager;
import codes.shiftmc.common.service.UserService;
import dev.jorel.commandapi.CommandAPICommand;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class AdminCommand {

    private final UserService userService;
    private final MessagingManager messagingManager;
    private final JavaPlugin plugin;

    public CommandAPICommand get() {
        return new CommandAPICommand( "admin")
                .withSubcommands(
                        new ReloadCommand(plugin).get(),
                        new SetCommand(userService, messagingManager).get()
        );
    }
}