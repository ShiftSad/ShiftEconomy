package codes.shiftmc.common.repository.impl;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.repository.UserRepository;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
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
        return Mono.from(userCollection.find(Filters.eq("uUID", uuid)).first());
    }

    @Override
    public Mono<UserData> findByUsername(String username) {
        return Mono.from(userCollection.find(Filters.eq("username", username)).first());
    }

    @Override
    public Mono<UserData> save(UserData userData) {
        return Mono.from(userCollection.replaceOne(
                        Filters.eq("uUID", userData.getUUID()),
                        userData,
                        new ReplaceOptions().upsert(true)
                ))
                .thenReturn(userData);
    }

    @Override
    public Mono<UserData> updateBalance(UUID uuid, double newBalance) {
        return Mono.from(userCollection.findOneAndUpdate(
                Filters.eq("uUID", uuid),
                Updates.set("balance", newBalance)
        ));
    }
}
