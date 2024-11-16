package codes.shiftmc.common.messaging.packet;

import codes.shiftmc.common.messaging.packet.objects.SimplePlayer;

public record SendOnlinePacket(
    String serverUUID,
    SimplePlayer[] players
) implements ShiftPacket {}