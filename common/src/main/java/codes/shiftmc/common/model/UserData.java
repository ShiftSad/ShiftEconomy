package codes.shiftmc.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    private UUID UUID;
    private String username;
    private double balance;
}
