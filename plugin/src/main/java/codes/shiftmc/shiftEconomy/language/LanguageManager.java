package codes.shiftmc.shiftEconomy.language;

import codes.shiftmc.shiftEconomy.ShiftEconomy;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public final class LanguageManager {
    private static LanguageManager instance;
    private final ShiftEconomy plugin;

    private final Map<String, Map<String, String>> messages = new HashMap<>();
    private final Map<String, Map<String, Component>> computedMessages = new HashMap<>();

    private static final MiniMessage mm = MiniMessage.builder().build();

    /**
     * Initializes the LanguageManager.
     *
     * @param plugin The main plugin instance.
     */
    private LanguageManager(ShiftEconomy plugin) {
        this.plugin = plugin;
        var languages = new File(plugin.getDataFolder() + "/languages");

        saveDefaultLanguages(languages);
        for (File file : languages.listFiles()) {
            loadLanguageFile(file);
        }
    }

    /**
     * Saves default language files from resources to the languages folder.
     *
     * @param languagesFolder The languages directory.
     */
    private void saveDefaultLanguages(File languagesFolder) {
        String[] defaultLangs = {"USA", "BRA"};
        for (String lang : defaultLangs) {
            File langFile = new File(languagesFolder, lang + ".lang");
            if (!langFile.exists()) {
                if (plugin.getResource("languages/" + lang + ".lang") != null) {
                    plugin.saveResource("languages/" + lang + ".lang", false);
                    log.info("Saved default language file: {}.lang", lang);
                } else {
                    log.warn("Default language file not found in resources: {}.lang", lang);
                }
            }
        }
    }

    /**
     * Loads a single language file into the languages map.
     *
     * @param langFile The language file to load.
     */
    private void loadLanguageFile(File langFile) {
        String langCode = langFile.getName().replace(".lang", "");
        Properties props = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8)) {
            props.load(reader);
            Map<String, String> msg = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                msg.put(key, props.getProperty(key));
            }
            messages.put(langCode, msg);
            log.info("Loaded language file: {}", langFile.getName());
        } catch (IOException e) {
            log.error("Failed to load language file {} {}", langFile.getName(), e.getStackTrace());
        }
    }
}