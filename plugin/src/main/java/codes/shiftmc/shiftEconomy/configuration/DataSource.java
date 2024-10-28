package codes.shiftmc.shiftEconomy.configuration;

import codes.shiftmc.common.model.enums.StorageMethod;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("DataSource")
@AllArgsConstructor
public class DataSource implements ConfigurationSerializable {

    private final StorageMethod storageMethod;

    private final String address;
    private final String database;
    private final String username;
    private final String password;

    private final String mongodbConnectionUri;

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("storageMethod", storageMethod.name());
        map.put("address", address);
        map.put("database", database);
        map.put("username", username);
        map.put("password", password);
        map.put("mongodbConnectionUri", mongodbConnectionUri);
        return map;
    }

    public static DataSource deserialize(final Map<String, Object> map) {
        String storageMethodStr = (String) map.get("storageMethod");
        StorageMethod storageMethod = StorageMethod.valueOf(storageMethodStr);

        String address = (String) map.get("address");
        String database = (String) map.get("database");
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String mongodbConnectionUri = (String) map.get("mongodbConnectionUri");

        return new DataSource(storageMethod, address, database, username, password, mongodbConnectionUri);
    }
}
