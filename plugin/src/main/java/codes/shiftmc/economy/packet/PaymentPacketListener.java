package codes.shiftmc.economy.packet;

import codes.shiftmc.common.messaging.PacketListener;
import codes.shiftmc.common.messaging.packet.PaymentPacket;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.economy.language.LanguageManager;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;

import static codes.shiftmc.economy.commands.player.TransactionsCommand.nameFromUUID;

@AllArgsConstructor
public class PaymentPacketListener implements PacketListener<PaymentPacket> {
    
    private final UserService userService;

    @Override
    public Class<PaymentPacket> getPacketType() {
        return PaymentPacket.class;
    }

    @Override
    public void onPacketReceived(PaymentPacket packet) {
        var lang = LanguageManager.instance();

        var receiver = Bukkit.getPlayer(packet.receiverUUID());
        if (receiver == null) return;

        nameFromUUID(packet.senderUUID(), userService)
                .doOnNext(name -> {
                    switch (packet.type()) {
                        case PaymentPacket.PaymentType.PAY ->
                                lang.sendMessage(receiver, "player.pay.received",
                                                 Placeholder.unparsed("sender", name),
                                                 Placeholder.unparsed("amount", NumberFormatter.format(packet.amount())));
                        case PaymentPacket.PaymentType.SET ->
                                lang.sendMessage(receiver, "player.set.balance.receive",
                                                 Placeholder.unparsed("sender", name),
                                                 Placeholder.unparsed("amount", NumberFormatter.format(packet.amount())));
                    }
                })
                .subscribe();
    }
}