package codes.shiftmc.shiftEconomy.listeners;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.UserService;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

@AllArgsConstructor
public class AsyncPlayerPreLoginListener implements Listener {

    private final UserService userService;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerHandshake(AsyncPlayerPreLoginEvent event) {
        var uuid = event.getUniqueId();
        var name = event.getName();

        userService.save(new UserData(
                uuid,
                name,
                0.0
        )).subscribe();
    }
}
