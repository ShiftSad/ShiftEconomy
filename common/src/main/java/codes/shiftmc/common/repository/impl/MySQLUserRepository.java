package codes.shiftmc.common.repository.impl;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.repository.UserRepository;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class MySQLUserRepository implements UserRepository {

    private final ConnectionFactory connectionFactory;

    public MySQLUserRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Mono<UserData> findByUuid(UUID uuid) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("SELECT * FROM users WHERE uUID = ?")
                                        .bind(0, uuid.toString())
                                        .execute())
                                .flatMap(this::mapRowToUserData)
                                .doFinally(signal -> connection.close())
                );
    }

    public Mono<UserData> findByUsername(String username) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("SELECT * FROM users WHERE username = ?")
                                        .bind(0, username)
                                        .execute())
                                .flatMap(this::mapRowToUserData)
                                .doFinally(signal -> connection.close())
                );
    }

    public Mono<UserData> save(UserData userData) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("REPLACE INTO users (uUID, username, balance) VALUES (?, ?, ?)")
                                        .bind(0, userData.getUUID().toString())
                                        .bind(1, userData.getUsername())
                                        .bind(2, userData.getBalance())
                                        .execute())
                                .doFinally(signal -> connection.close())
                ).thenReturn(userData);
    }

    public Mono<UserData> updateBalance(UUID uuid, double newBalance) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("UPDATE users SET balance = ? WHERE uUID = ?")
                                        .bind(0, newBalance)
                                        .bind(1, uuid.toString())
                                        .execute())
                                .doFinally(signal -> connection.close())
                ).then(findByUuid(uuid));
    }

    public Flux<UserData> findTopUsers(int from, int to) {
        return Mono.from(connectionFactory.create())
                .flatMapMany(connection ->
                        Flux.from(connection.createStatement("SELECT * FROM users ORDER BY balance DESC LIMIT ?, ?")
                                        .bind(0, from)
                                        .bind(1, to - from)
                                        .execute())
                                .flatMap(result -> result.map((row, metadata) ->
                                        new UserData(row.get("uUID", UUID.class), row.get("username", String.class), row.get("balance", Double.class))))
                                .doFinally(signal -> connection.close())
                );
    }

    private Mono<UserData> mapRowToUserData(Result result) {
        return Mono.from(result.map((row, metadata) ->
                new UserData(
                        UUID.fromString(row.get("uUID", String.class)),
                        row.get("username", String.class),
                        row.get("balance", Double.class)
                )
        ));
    }
}
