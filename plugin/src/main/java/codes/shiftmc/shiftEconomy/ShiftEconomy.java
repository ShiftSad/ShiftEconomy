package codes.shiftmc.shiftEconomy;

import codes.shiftmc.common.cache.LocalTypeCache;
import codes.shiftmc.common.cache.RedisTypeCache;
import codes.shiftmc.common.cache.TypeCache;
import codes.shiftmc.common.connectors.MongoConnector;
import codes.shiftmc.common.connectors.MySQLConnector;
import codes.shiftmc.common.messaging.EmptyMessagingManager;
import codes.shiftmc.common.messaging.MessagingManager;
import codes.shiftmc.common.messaging.RedisMessagingManager;
import codes.shiftmc.common.messaging.packet.SendOnlinePacket;
import codes.shiftmc.common.messaging.packet.objects.SimplePlayer;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.model.enums.CachingMethod;
import codes.shiftmc.common.model.enums.MessagingMethod;
import codes.shiftmc.common.model.enums.StorageMethod;
import codes.shiftmc.common.repository.TransactionRepository;
import codes.shiftmc.common.repository.UserRepository;
import codes.shiftmc.common.repository.impl.MongoTransactionRepository;
import codes.shiftmc.common.repository.impl.MongoUserRepository;
import codes.shiftmc.common.repository.impl.MySQLTransactionRepository;
import codes.shiftmc.common.repository.impl.MySQLUserRepository;
import codes.shiftmc.common.service.TransactionService;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.shiftEconomy.commands.MoneyCommand;
import codes.shiftmc.shiftEconomy.configuration.CacheSource;
import codes.shiftmc.shiftEconomy.configuration.DataSource;
import codes.shiftmc.shiftEconomy.configuration.MessagingSource;
import codes.shiftmc.shiftEconomy.language.LanguageManager;
import codes.shiftmc.shiftEconomy.listeners.AsyncPlayerPreLoginListener;
import codes.shiftmc.shiftEconomy.packet.PaymentPacketListener;
import codes.shiftmc.shiftEconomy.packet.SendOnlinePacketListener;
import codes.shiftmc.shiftEconomy.vault.VaultEconomyHook;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class ShiftEconomy extends JavaPlugin {

    public static final UUID serverUUID = UUID.randomUUID();

    private DataSource dataSource;
    private CacheSource cacheSource;
    private MessagingSource messagingSource;

    private MessagingManager messagingManager;
    private UserService userService;
    private TransactionService transactionService;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    public void onEnable() {
        // Shading CommandAPI
        CommandAPI.onEnable();

        LanguageManager.instance(this);

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

        // Register commands
        new MoneyCommand(
                userService,
                transactionService,
                messagingManager,
                this
        ).register();

        // Register listeners
        Bukkit.getServer().getPluginManager().registerEvents(new AsyncPlayerPreLoginListener(userService), this);

        // Broadcast online players every minute
        executorService.schedule(() -> {
            var packet = new SendOnlinePacket(serverUUID.toString(), getSimplePlayers());
            messagingManager.sendPacket(packet);
        }, 1, TimeUnit.MINUTES);

        // Register packets
        messagingManager.addListener(new SendOnlinePacketListener());
        messagingManager.addListener(new PaymentPacketListener(userService));
    }

    @EventHandler
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .shouldHookPaperReload(true)
                .usePluginNamespace()
        );
    }

    private void connectDataSources() {
        UserRepository userRepository;
        TransactionRepository transactionRepository;
        switch (dataSource.storageMethod()) {
            case MONGODB -> {
                var mongoConnector = new MongoConnector(dataSource.mongodbConnectionUri(), dataSource.database());
                userRepository = new MongoUserRepository(mongoConnector.getMongoDatabase());
                transactionRepository = new MongoTransactionRepository(mongoConnector.getMongoDatabase());
            }
            case MYSQL -> {
                var host = dataSource.address().split(":")[0];
                var port = Integer.parseInt(dataSource.address().split(":")[1]);
                var mysqlConnector = new MySQLConnector(host, port, dataSource.database(), dataSource.username(), dataSource.password());
                userRepository = new MySQLUserRepository(mysqlConnector.getConnectionFactory());
                transactionRepository = new MySQLTransactionRepository(mysqlConnector.getConnectionFactory());
            }
            default -> throw new IllegalStateException("Not yet implemented: " + dataSource.storageMethod());
        }

        TypeCache<UserData> userDataCache;
        switch (cacheSource.cachingMethod()) {
            case REDIS -> {
                RedisClient redisClient = RedisClient.create("redis://" + cacheSource.password() + "@" + cacheSource.address() + ":" + cacheSource.port());
                RedisReactiveCommands<String, String> redisCommands = redisClient.connect().reactive();
                userDataCache = new RedisTypeCache<>(redisCommands, UserData.class);
            }
            case LOCAL -> userDataCache = new LocalTypeCache<>();

            default -> throw new IllegalStateException("Not yet implemented: " + cacheSource.cachingMethod());
        }

        switch (messagingSource.messagingMethod()) {
            case REDIS -> messagingManager = new RedisMessagingManager("redis://" + cacheSource.password() + "@" + cacheSource.address() + ":" + cacheSource.port());
            case NONE -> messagingManager = new EmptyMessagingManager();
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

        // Load of save default MessagingSource
        if (!config.contains("messagingSource")) {
            messagingSource = new MessagingSource(MessagingMethod.NONE);
            config.createSection("messagingSource", messagingSource.serialize());
            saveConfig();
        } else messagingSource = MessagingSource.deserialize(config.getConfigurationSection("messagingSource").getValues(false));
    }

    private static SimplePlayer[] getSimplePlayers() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        return onlinePlayers.stream()
                .map(player -> new SimplePlayer(
                        ShiftEconomy.serverUUID.toString(),
                        player.getUniqueId().toString(),
                        player.getName()
                ))
                .toArray(SimplePlayer[]::new);
    }
}