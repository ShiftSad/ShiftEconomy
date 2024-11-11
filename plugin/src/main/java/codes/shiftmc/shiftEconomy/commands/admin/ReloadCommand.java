package codes.shiftmc.shiftEconomy.commands.admin;

import codes.shiftmc.shiftEconomy.language.LanguageManager;
import dev.jorel.commandapi.CommandAPICommand;
import lombok.AllArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class ReloadCommand {

    private final JavaPlugin plugin;

    public CommandAPICommand get() {
        return new CommandAPICommand("reload")
                .withSubcommand(language())
                .executes((sender, args) -> {
                    reloadLanguage(sender, null);
                })
                .executesPlayer(((sender, args) -> {
                    reloadLanguage(sender, sender);
                }));
    }

    public CommandAPICommand language() {
        return new CommandAPICommand("language")
                .executes((sender, args) -> {
                    reloadLanguage(sender, null);
                })
                .executesPlayer(((sender, args) -> {
                    reloadLanguage(sender, sender);
                }));
    }

    private void reloadLanguage(Audience audience, Player language) {
        var lang = LanguageManager.instance(plugin);
        lang = lang.reload();

        Component message;
        if (language == null) message = lang.getMessage("admin.reload.language.sucess");
        else message = lang.getMessage(language, "admin.reload.language.sucess");

        audience.sendMessage(message);
    }
}