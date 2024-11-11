package codes.shiftmc.common.repository.impl;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.repository.TransactionRepository;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class MySQLTransactionRepository implements TransactionRepository {

    private final ConnectionFactory connectionFactory;

    public MySQLTransactionRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Mono<Transaction> save(Transaction transaction) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("INSERT INTO transactions (id, senderUUID, receiverUUID, amount, timestamp) VALUES (?, ?, ?, ?, ?)")
                                        .bind(0, transaction.transactionID().toString())
                                        .bind(1, transaction.senderUUID().toString())
                                        .bind(2, transaction.receiverUUID().toString())
                                        .bind(3, transaction.amount())
                                        .bind(4, transaction.timestamp())
                                        .execute())
                                .doFinally(signal -> connection.close())
                ).thenReturn(transaction);
    }

    public Flux<Transaction> findByUser(UUID uuid) {
        return Mono.from(connectionFactory.create())
                .flatMapMany(connection ->
                        Flux.from(connection.createStatement("SELECT * FROM transactions WHERE senderUUID = ? OR receiverUUID = ?")
                                        .bind(0, uuid.toString())
                                        .bind(1, uuid.toString())
                                        .execute())
                                .flatMap(this::mapRowToTransaction)
                                .doFinally(signal -> connection.close())
                );
    }

    public Flux<Transaction> findByReceiverUuid(UUID receiverUuid) {
        return Mono.from(connectionFactory.create())
                .flatMapMany(connection ->
                        Flux.from(connection.createStatement("SELECT * FROM transactions WHERE receiverUUID = ?")
                                        .bind(0, receiverUuid.toString())
                                        .execute())
                                .flatMap(this::mapRowToTransaction)
                                .doFinally(signal -> connection.close())
                );
    }

    public Flux<Transaction> findBySenderUuid(UUID senderUuid) {
        return Mono.from(connectionFactory.create())
                .flatMapMany(connection ->
                        Flux.from(connection.createStatement("SELECT * FROM transactions WHERE senderUUID = ?")
                                        .bind(0, senderUuid.toString())
                                        .execute())
                                .flatMap(this::mapRowToTransaction)
                                .doFinally(signal -> connection.close())
                );
    }

    private Mono<Transaction> mapRowToTransaction(Result result) {
        return Mono.from(result.map((row, metadata) ->
                new Transaction(
                        UUID.fromString(row.get("id", String.class)), UUID.fromString(row.get("senderUUID", String.class)),
                        UUID.fromString(row.get("receiverUUID", String.class)), row.get("amount", Double.class),
                        row.get("timestamp", Long.class))
                )
        );
    }

}
