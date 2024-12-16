package codes.shiftmc.economy.commands.admin;

import codes.shiftmc.common.messaging.MessagingManager;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.economy.configuration.SettingsSource;
import dev.jorel.commandapi.CommandAPICommand;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class AdminCommand {

    private final UserService userService;
    private final MessagingManager messagingManager;
    private final SettingsSource settingsSource;
    private final JavaPlugin plugin;

    public CommandAPICommand get() {
        return new CommandAPICommand( "admin")
                .withPermission("shifteconomy.money.admin")
                .withSubcommands(
                        new ReloadCommand(plugin, settingsSource).get(),
                        new SetCommand(userService, messagingManager).get()
        );
    }
}