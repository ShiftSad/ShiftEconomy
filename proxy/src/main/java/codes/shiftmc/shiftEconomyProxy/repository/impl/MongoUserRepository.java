package codes.shiftmc.shiftEconomyProxy.repository.impl;

import codes.shiftmc.shiftEconomyProxy.model.UserData;
import codes.shiftmc.shiftEconomyProxy.repository.UserRepository;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class MongoUserRepository implements UserRepository {

    private final MongoCollection<UserData> userCollection;

    public MongoUserRepository(MongoDatabase database) {
        this.userCollection = database.getCollection("users", UserData.class);
    }

    @Override
    public Mono<UserData> findByUuid(UUID uuid) {
        return Mono.from(userCollection.find(Filters.eq("uuid", uuid)).first());
    }

    @Override
    public Mono<UserData> findByUsername(String username) {
        return Mono.from(userCollection.find(Filters.eq("username", username)).first());
    }

    @Override
    public Mono<UserData> save(UserData userData) {
        return Mono.from(userCollection.insertOne(userData))
                .thenReturn(userData);
    }

    @Override
    public Mono<UserData> updateBalance(UUID uuid, double newBalance) {
        return Mono.from(userCollection.findOneAndUpdate(
                Filters.eq("uuid", uuid),
                Updates.set("balance", newBalance)
        ));
    }
}
