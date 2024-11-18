package codes.shiftmc.common.repository.impl;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.repository.UserRepository;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class MySQLUserRepository implements UserRepository {

    private final ConnectionFactory connectionFactory;

    public MySQLUserRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Mono<UserData> findByUuid(UUID uuid) {
        String query = "SELECT * FROM users WHERE uUID = ?";
        return Mono.from(connectionFactory.create())
                .flatMapMany(connection -> Mono.from(connection.createStatement(query)
                                .bind(0, uuid.toString())
                                .execute())
                        .flatMapMany(result -> result.map((row, rowMetadata) -> mapRowToUserData(row)))
                        .doFinally(signal -> connection.close()))
                .singleOrEmpty();
    }

    @Override
    public Mono<UserData> findByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        return Mono.from(connectionFactory.create())
                .flatMapMany(connection -> Mono.from(connection.createStatement(query)
                                .bind(0, username)
                                .execute())
                        .flatMapMany(result -> result.map((row, rowMetadata) -> mapRowToUserData(row)))
                        .doFinally(signal -> connection.close()))
                .singleOrEmpty();
    }

    @Override
    public Mono<UserData> save(UserData userData) {
        String query = "INSERT INTO users (uUID, username, balance) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE username = VALUES(username), balance = VALUES(balance)";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(query)
                                .bind(0, userData.getUUID().toString())
                                .bind(1, userData.getUsername())
                                .bind(2, userData.getBalance())
                                .execute())
                        .doFinally(signal -> connection.close()))
                .thenReturn(userData);
    }

    @Override
    public Mono<UserData> updateBalance(UUID uuid, double newBalance) {
        String query = "UPDATE users SET balance = ? WHERE uUID = ?";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(query)
                                .bind(0, newBalance)
                                .bind(1, uuid.toString())
                                .execute())
                        .then(Mono.from(connection.close())))
                .then(findByUuid(uuid));
    }

    @Override
    public Flux<UserData> findTopUsers(int from, int to) {
        String query = "SELECT * FROM users ORDER BY balance DESC LIMIT ? OFFSET ?";
        int limit = to - from;
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(query)
                                .bind(0, limit)
                                .bind(1, from)
                                .execute())
                        .flatMap(result -> result.map((row, rowMetadata) -> mapRowToUserData(row))),
                Connection::close);
    }

    private UserData mapRowToUserData(Row row) {
        UUID uuid = UUID.fromString(row.get("uUID", String.class));
        String username = row.get("username", String.class);
        double balance = row.get("balance", Double.class);
        return new UserData(uuid, username, balance);
    }
}