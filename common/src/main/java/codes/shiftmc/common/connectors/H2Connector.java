package codes.shiftmc.common.connectors;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static codes.shiftmc.common.connectors.MySQLConnector.initializeDatabase;
import static codes.shiftmc.common.connectors.MySQLConnector.startDatabasePing;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Slf4j
public class H2Connector {

    @Getter
    private final ConnectionFactory connectionFactory;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public H2Connector(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:h2:")) {
            throw new IllegalArgumentException("Invalid H2 JDBC URL. Expected format: jdbc:h2:<database_path>");
        }

        String r2dbcUrl = jdbcUrl.replace("jdbc:h2:", "file:");

        this.connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, "h2") // Use H2 R2DBC driver
                .option(PROTOCOL, "file") // File-based storage
                .option(DATABASE, r2dbcUrl) // Path to the H2 database file
                .build());

        initializeDatabase(connectionFactory);
        startDatabasePing(scheduler, connectionFactory);
    }
}
