package codes.shiftmc.shiftEconomy.configuration;


import codes.shiftmc.common.model.enums.CachingMethod;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("CacheSource")
@AllArgsConstructor
public class CacheSource implements ConfigurationSerializable {

    private final CachingMethod cachingMethod;

    private final String address;
    private final int port;
    private final String password;

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("cachingMethod", cachingMethod.name());
        map.put("address", address);
        map.put("port", port);
        map.put("password", password);
        return map;
    }

    public static CacheSource deserialize(final Map<String, Object> map) {
        String cachingMethodStr = (String) map.get("cachingMethod");
        CachingMethod cachingMethod = CachingMethod.valueOf(cachingMethodStr);

        String address = (String) map.get("address");
        int port = (int) map.get("port");
        String password = (String) map.get("password");

        return new CacheSource(cachingMethod, address, port, password);
    }
}