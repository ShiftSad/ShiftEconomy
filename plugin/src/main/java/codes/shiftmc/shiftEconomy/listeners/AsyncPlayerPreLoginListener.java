package codes.shiftmc.shiftEconomy.listeners;

import codes.shiftmc.common.messaging.MessagingManager;
import codes.shiftmc.common.messaging.packet.SendOnlinePacket;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.UserService;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import static codes.shiftmc.shiftEconomy.ShiftEconomy.getSimplePlayers;
import static codes.shiftmc.shiftEconomy.ShiftEconomy.serverUUID;

@AllArgsConstructor
public class AsyncPlayerPreLoginListener implements Listener {

    private final UserService userService;
    private final MessagingManager messagingManager;

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var packet = new SendOnlinePacket(serverUUID.toString(), getSimplePlayers());
        messagingManager.sendPacket(packet);
    }
}
