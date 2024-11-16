package codes.shiftmc.common.messaging.packet.objects;

public record SimplePlayer(
            String serverUUID,
            String uuid,
            String username
) {}