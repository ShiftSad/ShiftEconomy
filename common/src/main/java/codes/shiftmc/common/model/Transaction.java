package codes.shiftmc.common.model;

import java.util.UUID;

public record Transaction(UUID transactionID, UUID senderUUID, UUID receiverUUID, double amount, long timestamp) { }
