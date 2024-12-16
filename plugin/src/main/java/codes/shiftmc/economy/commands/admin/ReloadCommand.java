package codes.shiftmc.economy.commands.admin;

import codes.shiftmc.economy.configuration.SettingsSource;
import codes.shiftmc.economy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class ReloadCommand {

    private final JavaPlugin plugin;
    private final SettingsSource settings;

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
        var lang = LanguageManager.instance(plugin, settings);
        lang = lang.reload(settings);

        lang.sendMessage(sender, "admin.reload.language.sucess");
    }
}