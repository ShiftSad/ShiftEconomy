package codes.shiftmc.shiftEconomy;

import codes.shiftmc.common.cache.RedisTypeCache;
import codes.shiftmc.common.cache.TypeCache;
import codes.shiftmc.common.connectors.MongoConnector;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.model.enums.CachingMethod;
import codes.shiftmc.common.model.enums.StorageMethod;
import codes.shiftmc.common.repository.TransactionRepository;
import codes.shiftmc.common.repository.UserRepository;
import codes.shiftmc.common.repository.impl.MongoTransactionRepository;
import codes.shiftmc.common.repository.impl.MongoUserRepository;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.shiftEconomy.configuration.CacheSource;
import codes.shiftmc.shiftEconomy.configuration.DataSource;
import codes.shiftmc.shiftEconomy.vault.VaultEconomyHook;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShiftEconomy extends JavaPlugin {

    private DataSource dataSource;
    private CacheSource cacheSource;

    private TypeCache<UserData> userDataCache;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;

    private UserService userService;
    private TransactionService transactionService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigurations();
        connectDataSources();

        Bukkit.getServer().getServicesManager()
                .register(
                        Economy.class,
                        new VaultEconomyHook(userService, transactionService),
                        this,
                        ServicePriority.Highest
                );
    }

    private void connectDataSources() {
        switch (dataSource.storageMethod()) {
            case MONGODB -> {
                var mongoConnector = new MongoConnector(dataSource.mongodbConnectionUri(), dataSource.database());
                userRepository = new MongoUserRepository(mongoConnector.getMongoDatabase());
                transactionRepository = new MongoTransactionRepository(mongoConnector.getMongoDatabase());
            }
            default -> throw new IllegalStateException("Not yet implemented: " + dataSource.storageMethod());
        }

        switch (cacheSource.cachingMethod()) {
            case REDIS -> {
                RedisClient redisClient = RedisClient.create("redis://" + cacheSource.password() + "@" + cacheSource.address() + ":" + cacheSource.port());
                RedisReactiveCommands<String, String> redisCommands = redisClient.connect().reactive();
                userDataCache = new RedisTypeCache<>(redisCommands, UserData.class);
            }

            default -> throw new IllegalStateException("Not yet implemented: " + cacheSource.cachingMethod());
        }

        // Initialize UserService with the repositories and cache
        userService = new UserService(userRepository, userDataCache);
        transactionService = new TransactionService(transactionRepository);
    }

    private void loadConfigurations() {
        FileConfiguration config = getConfig();

        // Load or save default DataSource
        if (!config.contains("dataSource")) {
            dataSource = new DataSource(StorageMethod.MONGODB, "localhost", "money", "user", "password", "mongodb+srv://");
            config.createSection("dataSource", dataSource.serialize());
            saveConfig();
        } else dataSource = DataSource.deserialize(config.getConfigurationSection("dataSource").getValues(false));

        // Load or save default CacheSource
        if (!config.contains("cacheSource")) {
            cacheSource = new CacheSource(CachingMethod.REDIS, "localhost", 6379, "redispassword");
            config.createSection("cacheSource", cacheSource.serialize());
            saveConfig();
        } else cacheSource = CacheSource.deserialize(config.getConfigurationSection("cacheSource").getValues(false));
    }
}
