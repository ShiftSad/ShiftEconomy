package codes.shiftmc.shiftEconomy.configuration;


import codes.shiftmc.common.model.enums.MessagingMethod;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("CacheSource")
public record MessagingSource(MessagingMethod messagingMethod) implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("messagingMethod", messagingMethod.name());
        return map;
    }

    public static MessagingSource deserialize(final Map<String, Object> map) {
        String messagingMethodStr = (String) map.get("messagingMethod");
        MessagingMethod messagingMethod = MessagingMethod.valueOf(messagingMethodStr);

        return new MessagingSource(messagingMethod);
    }
}