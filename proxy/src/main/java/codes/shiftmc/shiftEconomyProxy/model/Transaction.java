package codes.shiftmc.shiftEconomyProxy.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Transaction(@NotNull UUID senderUUID, @NotNull UUID receiverUUID, double amount, long timestamp) { }
