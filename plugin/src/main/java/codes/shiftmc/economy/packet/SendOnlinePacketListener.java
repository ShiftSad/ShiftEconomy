package codes.shiftmc.economy.packet;

import codes.shiftmc.common.messaging.PacketListener;
import codes.shiftmc.common.messaging.packet.SendOnlinePacket;
import codes.shiftmc.common.messaging.packet.objects.SimplePlayer;
import codes.shiftmc.economy.ShiftEconomy;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class SendOnlinePacketListener implements PacketListener<SendOnlinePacket> {

    private static final HashMap<String, SimplePlayer[]> playerCache = new HashMap<>();

    @Override
    public Class<SendOnlinePacket> getPacketType() {
        return SendOnlinePacket.class;
    }

    @Override
    public void onPacketReceived(SendOnlinePacket packet) {
        var uuid = packet.serverUUID();
        var players = packet.players();
        if (uuid.equals(ShiftEconomy.serverUUID.toString())) return;

        playerCache.put(uuid, players);
    }

    // Returns all cached players + online ones
    public static SimplePlayer[] getPlayers() {
        Collection<SimplePlayer> allPlayers = new ArrayList<>();

        for (SimplePlayer[] cachedPlayers : playerCache.values()) {
            allPlayers.addAll(Arrays.asList(cachedPlayers));
        }

        Bukkit.getOnlinePlayers().stream()
                .map(player -> new SimplePlayer(
                        ShiftEconomy.serverUUID.toString(),
                        player.getUniqueId().toString(),
                        player.getName()
                ))
                .forEach(allPlayers::add);

        return allPlayers.toArray(SimplePlayer[]::new);
    }

    public static String[] getPlayerNames() {
        return Arrays.stream(getPlayers()).map(SimplePlayer::username).toArray(String[]::new);
    }
}