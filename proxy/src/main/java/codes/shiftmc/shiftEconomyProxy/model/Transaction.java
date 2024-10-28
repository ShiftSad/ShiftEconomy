package codes.shiftmc.shiftEconomyProxy.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Transaction {
    private final UUID senderUUID, receiverUUID;
    private final double amount;
    private final long timestamp;
}
