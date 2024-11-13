package codes.shiftmc.common.messaging;

import codes.shiftmc.common.messaging.packet.ShiftPacket;

import java.util.HashSet;
import java.util.Set;

public abstract class MessagingManager {

    private final Set<PacketListener<? extends ShiftPacket>> listeners = new HashSet<>();

    /**
     * Sends a packet to the messaging system.
     * @param packet The packet to send.
     */
    public abstract void sendPacket(ShiftPacket packet);

    /**
     * Adds a listener for a specific packet type.
     * @param listener The listener to add.
     */
    public <T extends ShiftPacket> void addListener(PacketListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     * @param listener The listener to remove.
     */
    public void removeListener(PacketListener<? extends ShiftPacket> listener) {
        listeners.remove(listener);
    }

    /**
     * Processes incoming packets and dispatches them to the correct listeners.
     * @param packet The received packet.
     */
    protected void handleIncomingPacket(ShiftPacket packet) {
        for (PacketListener<? extends ShiftPacket> listener : listeners) {
            if (listener.getPacketType().isInstance(packet)) {
                @SuppressWarnings("unchecked")
                PacketListener<ShiftPacket> typedListener = (PacketListener<ShiftPacket>) listener;
                typedListener.onPacketReceived(packet);
            }
        }
    }
}