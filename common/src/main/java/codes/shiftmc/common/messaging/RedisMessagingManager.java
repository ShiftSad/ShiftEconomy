package codes.shiftmc.common.messaging;

import codes.shiftmc.common.adapter.ShiftPacketTypeAdapter;
import codes.shiftmc.common.messaging.packet.ShiftPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.ChannelMessage;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class RedisMessagingManager extends MessagingManager {

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final RedisPubSubReactiveCommands<String, String> reactiveCommands;
    private final Set<PacketListener> listeners = new CopyOnWriteArraySet<>();
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
        String json = gson.toJson(packet);
        connection.reactive().publish(CHANNEL_NAME, json).subscribe();
    }

    @Override
    public void addListener(PacketListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(PacketListener listener) {
        listeners.remove(listener);
    }

    private void subscribeToChannel() {
        reactiveCommands.subscribe(CHANNEL_NAME).subscribe();

        Flux<String> messages = reactiveCommands.observeChannels()
            .filter(msg -> CHANNEL_NAME.equals(msg.getChannel()))
            .map(ChannelMessage::getMessage);

        messages.subscribe(this::handleIncomingMessage);
    }

    private void handleIncomingMessage(String message) {
        // Deserialize the JSON to ShiftPacket
        ShiftPacket packet = gson.fromJson(message, ShiftPacket.class);
        log.info(packet.toString());

        // Notify all listeners
        for (PacketListener listener : listeners) {
            listener.onPacketReceived(packet);
        }
    }

    public void shutdown() {
        connection.close();
        pubSubConnection.close();
        redisClient.shutdown();
    }
}