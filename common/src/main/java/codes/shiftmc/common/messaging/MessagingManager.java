package codes.shiftmc.common.messaging;

import codes.shiftmc.common.messaging.packet.ShiftPacket;

public abstract class MessagingManager {

    /**
     * Sends a packet to the messaging system.
     * @param packet The packet to send.
     */
    public abstract void sendPacket(ShiftPacket packet);

    /**
     * Adds a listener for incoming packets.
     * @param listener The listener to add.
     */
    public abstract void addListener(PacketListener listener);

    /**
     * Removes a listener.
     * @param listener The listener to remove.
     */
    public abstract void removeListener(PacketListener listener);

    /**
     * Interface for packet listeners.
     */
    public interface PacketListener {
        void onPacketReceived(ShiftPacket packet);
    }
}