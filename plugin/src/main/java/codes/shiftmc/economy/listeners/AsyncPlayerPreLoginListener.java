package codes.shiftmc.economy.listeners;

import codes.shiftmc.common.messaging.MessagingManager;
import codes.shiftmc.common.messaging.packet.SendOnlinePacket;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.economy.configuration.SettingsSource;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import static codes.shiftmc.economy.ShiftEconomy.getSimplePlayers;
import static codes.shiftmc.economy.ShiftEconomy.serverUUID;

@AllArgsConstructor
public class AsyncPlayerPreLoginListener implements Listener {

    private final UserService userService;
    private final MessagingManager messagingManager;
    private final SettingsSource settingsSource;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerHandshake(AsyncPlayerPreLoginEvent event) {
        var uuid = event.getUniqueId();
        var name = event.getName();

        userService.save(new UserData(
                uuid,
                name,
                settingsSource.startMoney()
        )).subscribe();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var packet = new SendOnlinePacket(serverUUID.toString(), getSimplePlayers());
        messagingManager.sendPacket(packet);
    }
}
