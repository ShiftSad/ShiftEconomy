package codes.shiftmc.common.connectors;

import io.r2dbc.spi.*;
import lombok.Getter;
import lombok.extern.flogger.Flogger;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Slf4j
public class MySQLConnector {

    @Getter private final ConnectionFactory connectionFactory;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MySQLConnector(String host, int port, String database, String username, String password) {
        this.connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, "mysql")
                .option(HOST, host)
                .option(PORT, port)
                .option(USER, username)
                .option(PASSWORD, password)
                .option(DATABASE, database)
                .build());

        initializeDatabase();
        startDatabasePing();
    }

    private void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                uUID CHAR(36) PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                balance DOUBLE NOT NULL DEFAULT 0.0
            );
            """;

        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                id CHAR(36) PRIMARY KEY,
                senderUUID CHAR(36) NOT NULL,
                receiverUUID CHAR(36) NOT NULL,
                amount DOUBLE NOT NULL,
                timestamp BIGINT NOT NULL,
                FOREIGN KEY (senderUUID) REFERENCES users(uUID),
                FOREIGN KEY (receiverUUID) REFERENCES users(uUID)
            );
            """;

        // Execute the table creation commands in sequence
        Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement(createUsersTable).execute())
                                .then(Mono.from(connection.createStatement(createTransactionsTable).execute()))
                                .doFinally(signal -> connection.close())
                ).subscribe(
                        unused -> log.debug("Database tables initialized successfully"),
                        error -> log.debug("Error initializing database tables: {}", error.getMessage())
                );
    }

    private void startDatabasePing() {
        scheduler.scheduleAtFixedRate(() ->
                        Mono.from(connectionFactory.create())
                                .flatMap(connection -> Mono.from(connection.createStatement("SELECT 1").execute())
                                        .doFinally(signal -> connection.close()))
                                .subscribe(
                                        result -> log.debug("Database ping successful"),
                                        error -> log.debug("Database ping failed: {}", error.getMessage())
                                ),
                0, 1, TimeUnit.MINUTES
        );
    }
}
