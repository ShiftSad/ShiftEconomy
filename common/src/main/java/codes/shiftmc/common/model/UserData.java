package codes.shiftmc.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserData {
    private final UUID UUID;
    private String username;
    private double balance;
}
