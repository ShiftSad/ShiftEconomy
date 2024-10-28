package codes.shiftmc.common.model;

import lombok.Data;

import java.util.UUID;

@Data
public class UserData {
    private final UUID uuid;
    private String username;
    private Double balance;
}
