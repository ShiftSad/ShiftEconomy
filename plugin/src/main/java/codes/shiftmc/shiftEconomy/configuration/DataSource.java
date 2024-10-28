package codes.shiftmc.shiftEconomy.configuration;

import codes.shiftmc.common.model.enums.StorageMethod;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("DataSource")
public record DataSource(StorageMethod storageMethod, String address, String database, String username, String password,
                         String mongodbConnectionUri) implements ConfigurationSerializable {

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
