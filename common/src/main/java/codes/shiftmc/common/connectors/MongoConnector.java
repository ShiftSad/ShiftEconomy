package codes.shiftmc.common.connectors;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.Getter;
import org.bson.BsonDocument;
import org.bson.BsonInt64;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MongoConnector {

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final MongoClient mongoClient;
    @Getter private final MongoDatabase mongoDatabase;

    public MongoConnector(String connectionString, String database) {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        mongoClient = MongoClients.create(settings);
        mongoDatabase = mongoClient.getDatabase(database);

        var command = new BsonDocument("ping", new BsonInt64(1));
        scheduler.scheduleAtFixedRate(() -> mongoDatabase.runCommand(command), 0, 1, TimeUnit.MINUTES);
    }
}
