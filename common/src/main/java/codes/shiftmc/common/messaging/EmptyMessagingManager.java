package codes.shiftmc.common.messaging;

import codes.shiftmc.common.messaging.packet.ShiftPacket;

public class EmptyMessagingManager extends MessagingManager {

    @Override
    public void sendPacket(ShiftPacket packet) {
        // Does nothing
    }

    @Override
    public void addListener(PacketListener listener) {
        // Does nothing
    }

    @Override
    public void removeListener(PacketListener listener) {
        // Does nothing        
    }
}