package codes.shiftmc.shiftEconomy.packet;

import codes.shiftmc.common.messaging.PacketListener;
import codes.shiftmc.common.messaging.packet.PaymentPacket;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.shiftEconomy.language.LanguageManager;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;

import static codes.shiftmc.shiftEconomy.commands.player.TransactionsCommand.nameFromUUID;

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
                .doOnNext(name -> lang.sendMessage(receiver, "player.pay.received",
                        Placeholder.unparsed("sender", name),
                        Placeholder.unparsed("amount", NumberFormatter.format(packet.amount()))))
                .subscribe();
    }
}