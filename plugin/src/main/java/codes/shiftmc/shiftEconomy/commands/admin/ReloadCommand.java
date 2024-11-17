package codes.shiftmc.shiftEconomy.commands.admin;

import codes.shiftmc.shiftEconomy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class ReloadCommand {

    private final JavaPlugin plugin;

    public CommandAPICommand get() {
        return new CommandAPICommand("reload")
                .withPermission("shifteconomy.money.admin.reload")
                .withSubcommand(language())
                .executes((sender, args) -> {
                    reloadLanguage(sender);
                });
    }

    public CommandAPICommand language() {
        return new CommandAPICommand("language")
                .executes((sender, args) -> {
                    reloadLanguage(sender);
                });
    }

    private void reloadLanguage(CommandSender sender) {
        var lang = LanguageManager.instance(plugin);
        lang = lang.reload();

        lang.sendMessage(sender, "admin.reload.language.sucess");
    }
}