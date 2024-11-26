package codes.shiftmc.common.messaging;

import codes.shiftmc.common.adapter.ShiftPacketTypeAdapter;
import codes.shiftmc.common.messaging.packet.ShiftPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.ChannelMessage;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class RedisMessagingManager extends MessagingManager {

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final RedisPubSubReactiveCommands<String, String> reactiveCommands;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ShiftPacket.class, new ShiftPacketTypeAdapter())
            .create();
    private static final String CHANNEL_NAME = "shiftpackets";

    public RedisMessagingManager(String redisURI) {
        this.redisClient = RedisClient.create(redisURI);
        this.connection = redisClient.connect();
        this.pubSubConnection = redisClient.connectPubSub();
        this.reactiveCommands = pubSubConnection.reactive();

        subscribeToChannel();
    }

    @Override
    public void sendPacket(ShiftPacket packet) {
        JsonObject jsonObject = gson.toJsonTree(packet).getAsJsonObject();
        jsonObject.addProperty("packetType", packet.getClass().getSimpleName()); // Add a packet type
        String json = gson.toJson(jsonObject);
        connection.reactive().publish(CHANNEL_NAME, json).subscribe();
    }

    private void subscribeToChannel() {
        reactiveCommands.subscribe(CHANNEL_NAME).subscribe();

        Flux<String> messages = reactiveCommands.observeChannels()
            .filter(msg -> CHANNEL_NAME.equals(msg.getChannel()))
            .map(ChannelMessage::getMessage);

        messages.subscribe(this::handleIncomingMessage);
    }

    private void handleIncomingMessage(String message) {
        try {
            ShiftPacket packet = gson.fromJson(message, ShiftPacket.class);
            if (packet != null) handleIncomingPacket(packet);
        } catch (Exception e) {
            log.error("Failed to process incoming packet: {}", message, e);
        }
    }

    public void shutdown() {
        connection.close();
        pubSubConnection.close();
        redisClient.shutdown();
    }
}