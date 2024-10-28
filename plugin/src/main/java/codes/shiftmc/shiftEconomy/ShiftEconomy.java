package codes.shiftmc.shiftEconomy;

import org.bukkit.plugin.java.JavaPlugin;

public final class ShiftEconomy extends JavaPlugin {

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
