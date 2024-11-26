package codes.shiftmc.common.connectors;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Slf4j
public class MySQLConnector {

    @Getter private final ConnectionFactory connectionFactory;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MySQLConnector(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:mysql://") || !jdbcUrl.startsWith("r2dbc:mysql://")) {
            throw new IllegalArgumentException("Invalid JDBC URL format. Expected format: jdbc:mysql://username:password@host:port/database");
        }

        // Parse the JDBC URL
        String r2dbcUrl = jdbcUrl.replace("jdbc:mysql://", "r2dbc:mysql://");
        URI uri = URI.create(r2dbcUrl);

        // Extract user info
        String userInfo = uri.getUserInfo();
        if (userInfo == null || !userInfo.contains(":")) {
            throw new IllegalArgumentException("Invalid JDBC URL: Missing or malformed username and password.");
        }

        String username = userInfo.split(":")[0];
        String password = userInfo.split(":")[1];

        // Extract host, port, and database
        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 3306;
        String database = uri.getPath().substring(1);

        this.connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, "mysql")
                .option(HOST, host)
                .option(PORT, port)
                .option(USER, username)
                .option(PASSWORD, password)
                .option(DATABASE, database)
                .build());

        initializeDatabase(connectionFactory);
        startDatabasePing(scheduler, connectionFactory);
    }

    protected static void initializeDatabase(ConnectionFactory connectionFactory) {
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

    protected static void startDatabasePing(ScheduledExecutorService scheduler, ConnectionFactory connectionFactory) {
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
