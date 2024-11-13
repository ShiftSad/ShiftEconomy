package codes.shiftmc.common.messaging.packet;

import java.util.UUID;

public record PaymentPacket(
    UUID senderUUID,
    UUID receiverUUID,
    Double amount,
    UUID serverUUID
) implements ShiftPacket { }