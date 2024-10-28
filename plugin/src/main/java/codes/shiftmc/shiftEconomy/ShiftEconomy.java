package codes.shiftmc.shiftEconomy;

import codes.shiftmc.common.cache.TypeCache;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.model.enums.CachingMethod;
import codes.shiftmc.common.model.enums.StorageMethod;
import codes.shiftmc.common.repository.TransactionRepository;
import codes.shiftmc.common.repository.UserRepository;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.shiftEconomy.configuration.CacheSource;
import codes.shiftmc.shiftEconomy.configuration.DataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShiftEconomy extends JavaPlugin {

    private DataSource dataSource;
    private CacheSource cacheSource;

    private TypeCache<UserData> userDataCache;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;

    private UserService userService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigurations();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
