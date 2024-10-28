package codes.shiftmc.common.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Transaction(UUID senderUUID, UUID receiverUUID, double amount, long timestamp) { }
