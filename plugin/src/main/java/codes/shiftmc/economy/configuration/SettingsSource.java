package codes.shiftmc.economy.configuration;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("CacheSource")
public record SettingsSource(
        String defaultLanguage,
        double startMoney
) implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("default-language", defaultLanguage);
        map.put("start-money", startMoney);
        return map;
    }

    public static SettingsSource deserialize(final Map<String, Object> map) {
        final String defaultLanguage = (String) map.get("default-language");
        final double startMoney = (double) map.get("start-money");

        return new SettingsSource(defaultLanguage, startMoney);
    }
}
