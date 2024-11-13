package codes.shiftmc.shiftEconomy.packet;

import codes.shiftmc.common.messaging.PacketListener;
import codes.shiftmc.common.messaging.packet.PaymentPacket;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PaymentPacketListener implements PacketListener<PaymentPacket> {
    
    @Override
    public Class<PaymentPacket> getPacketType() {
        return PaymentPacket.class;
    }

    @Override
    public void onPacketReceived(PaymentPacket packet) {
        // Handle the payment logic here
        System.out.println("Received payment from " + packet.senderUUID() + " to " + packet.receiverUUID() + " of amount " + packet.amount());
    }
}