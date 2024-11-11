package codes.shiftmc.shiftEconomy.commands.admin;

import dev.jorel.commandapi.CommandAPICommand;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class AdminCommand {

    private final JavaPlugin plugin;

    public CommandAPICommand get() {
        return new CommandAPICommand( "admin")
                .withSubcommands(new ReloadCommand(plugin).get());
    }
}