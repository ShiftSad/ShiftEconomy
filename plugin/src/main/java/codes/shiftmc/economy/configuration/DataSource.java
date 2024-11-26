package codes.shiftmc.economy.configuration;

import codes.shiftmc.common.model.enums.StorageMethod;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("DataSource")
public record DataSource(StorageMethod storageMethod, String database, String mongodbConnectionUri, String jdbcUrl) implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("storageMethod", storageMethod.name());
        map.put("database", database);
        map.put("mongodbConnectionUri", mongodbConnectionUri);
        map.put("jdbcUrl", jdbcUrl);
        return map;
    }

    public static DataSource deserialize(final Map<String, Object> map) {
        String storageMethodStr = (String) map.get("storageMethod");
        StorageMethod storageMethod = StorageMethod.valueOf(storageMethodStr);
        String database = (String) map.get("database");
        String mongodbConnectionUri = (String) map.get("mongodbConnectionUri");
        String jdbcUrl = (String) map.get("jdbcUrl");

        return new DataSource(storageMethod, database, mongodbConnectionUri, jdbcUrl);
    }
}
