package codes.shiftmc.common.messaging.packet;

import java.util.UUID;

public record PaymentPacket(
    UUID senderUUID,
    UUID receiverUUID,
    Double amount,
    UUID serverUUID,
    PaymentType type
) implements ShiftPacket {
    public enum PaymentType {
        PAY, SET
    }
}