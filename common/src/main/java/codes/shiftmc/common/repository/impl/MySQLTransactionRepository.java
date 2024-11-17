package codes.shiftmc.common.repository.impl;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.repository.TransactionRepository;
import io.r2dbc.spi.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class MySQLTransactionRepository implements TransactionRepository {

    private final ConnectionFactory connectionFactory;

    public MySQLTransactionRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Mono<Transaction> save(Transaction transaction) {
        String query = "INSERT INTO transactions (id, senderUUID, receiverUUID, amount, timestamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(query)
                                .bind(0, transaction.transactionID().toString())
                                .bind(1, transaction.senderUUID().toString())
                                .bind(2, transaction.receiverUUID().toString())
                                .bind(3, transaction.amount())
                                .bind(4, transaction.timestamp())
                                .execute())
                        .doFinally(signal -> connection.close()))
                .thenReturn(transaction);
    }

    @Override
    public Flux<Transaction> findByUser(UUID uuid) {
        String query = "SELECT * FROM transactions WHERE senderUUID = ? OR receiverUUID = ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(query)
                                .bind(0, uuid.toString())
                                .bind(1, uuid.toString())
                                .execute())
                        .flatMap(result -> result.map((row, rowMetadata) -> mapRowToTransaction(row))),
                Connection::close);
    }

    @Override
    public Flux<Transaction> findByUserWithAmountBounds(UUID uuid, int lowerBound, int upperBound) {
        String query = "SELECT * FROM transactions WHERE (senderUUID = ? OR receiverUUID = ?) " +
                "ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        int limit = upperBound - lowerBound;
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(query)
                                .bind(0, uuid.toString())
                                .bind(1, uuid.toString())
                                .bind(2, limit)
                                .bind(3, lowerBound)
                                .execute())
                        .flatMap(result -> result.map((row, rowMetadata) -> mapRowToTransaction(row))),
                Connection::close);
    }

    @Override
    public Flux<Transaction> findByReceiverUuid(UUID receiverUuid) {
        String query = "SELECT * FROM transactions WHERE receiverUUID = ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(query)
                                .bind(0, receiverUuid.toString())
                                .execute())
                        .flatMap(result -> result.map((row, rowMetadata) -> mapRowToTransaction(row))),
                Connection::close);
    }

    @Override
    public Flux<Transaction> findBySenderUuid(UUID senderUuid) {
        String query = "SELECT * FROM transactions WHERE senderUUID = ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(query)
                                .bind(0, senderUuid.toString())
                                .execute())
                        .flatMap(result -> result.map((row, rowMetadata) -> mapRowToTransaction(row))),
                Connection::close);
    }

    private Transaction mapRowToTransaction(Row row) {
        UUID transactionID = UUID.fromString(row.get("id", String.class));
        UUID senderUUID = UUID.fromString(row.get("senderUUID", String.class));
        UUID receiverUUID = UUID.fromString(row.get("receiverUUID", String.class));
        double amount = row.get("amount", Double.class);
        long timestamp = row.get("timestamp", Long.class);
        return new Transaction(transactionID, senderUUID, receiverUUID, amount, timestamp);
    }
}
