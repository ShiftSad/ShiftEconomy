package codes.shiftmc.common.messaging;

import codes.shiftmc.common.messaging.packet.ShiftPacket;

public interface PacketListener<T extends ShiftPacket> {
    /**
     * Specifies the type of packet this listener is interested in.
     * @return The class type of the packet.
     */
    Class<T> getPacketType();

    /**
     * Called when a packet of the specified type is received.
     * @param packet The packet received.
     */
    void onPacketReceived(T packet);
}