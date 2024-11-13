package codes.shiftmc.common.messaging.packet;

public interface ShiftPacket {
    default String getPacketType() {
        return this.getClass().getSimpleName();
    }
}