package codes.shiftmc.common.repository.impl;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.repository.TransactionRepository;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class MongoTransactionRepository implements TransactionRepository {

    private final MongoCollection<Transaction> transactionCollection;

    public MongoTransactionRepository(MongoDatabase database) {
        this.transactionCollection = database.getCollection("transactions", Transaction.class);
    }

    @Override
    public Mono<Transaction> save(Transaction transaction) {
        return Mono.from(transactionCollection.insertOne(transaction))
                .thenReturn(transaction);
    }

    @Override
    public Flux<Transaction> findByUser(UUID uuid) {
        return Flux.from(transactionCollection.find(
                Filters.or(
                        Filters.eq("senderUUID", uuid),
                        Filters.eq("receiverUUID", uuid)
                )));
    }

    @Override
    public Flux<Transaction> findByReceiverUuid(UUID receiverUuid) {
        return Flux.from(transactionCollection.find(Filters.eq("receiverUUID", receiverUuid)));
    }

    @Override
    public Flux<Transaction> findBySenderUuid(UUID senderUuid) {
        return Flux.from(transactionCollection.find(Filters.eq("senderUUID", senderUuid)));
    }
}