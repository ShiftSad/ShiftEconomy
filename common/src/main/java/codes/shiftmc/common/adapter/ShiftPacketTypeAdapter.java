package codes.shiftmc.common.adapter;

import codes.shiftmc.common.messaging.packet.PaymentPacket;
import codes.shiftmc.common.messaging.packet.ShiftPacket;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ShiftPacketTypeAdapter implements JsonSerializer<ShiftPacket>, JsonDeserializer<ShiftPacket> {

    private static final Map<String, Class<? extends ShiftPacket>> PACKET_TYPES = new HashMap<>();

    static {
        PACKET_TYPES.put("PaymentPacket", PaymentPacket.class);
        // Register other packet types here
    }

    @Override
    public JsonElement serialize(ShiftPacket src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObj = context.serialize(src).getAsJsonObject();
        jsonObj.addProperty("packetType", src.getPacketType());
        return jsonObj;
    }

    @Override
    public ShiftPacket deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObj = json.getAsJsonObject();
        String packetType = jsonObj.get("packetType").getAsString();
        Class<? extends ShiftPacket> packetClass = PACKET_TYPES.get(packetType);
        if (packetClass == null) {
            throw new JsonParseException("Unknown packet type: " + packetType);
        }
        return context.deserialize(jsonObj, packetClass);
    }
}