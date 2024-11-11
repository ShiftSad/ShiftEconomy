package codes.shiftmc.shiftEconomy.language;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final JavaPlugin plugin;

    private final Map<String, Map<String, String>> messages = new HashMap<>();
    private final Map<String, Map<String, Component>> computedMessages = new HashMap<>();

    private static final MiniMessage mm = MiniMessage.builder().build();


    /**
     * Initializes the LanguageManager.
     *
     * @param plugin The main plugin instance.
     */
    private LanguageManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        var languages = new File(plugin.getDataFolder() + "/languages");

        saveDefaultLanguages(languages);
        for (File file : languages.listFiles()) {
            loadLanguageFile(file);
        }
    }

    /**
     * Retrieves the current instance of the LanguageManager, if it has been initialized.
     *
     * @return the existing LanguageManager instance, or null if it has not been initialized
     */
    @Nullable
    public static LanguageManager instance() {
        return instance;
    }

    /**
     * Initializes and retrieves the LanguageManager instance if it does not already exist.
     * If an instance exists, it returns the existing instance.
     *
     * @param plugin the main plugin instance to use for initializing the LanguageManager
     * @return the initialized LanguageManager instance, guaranteed to be non-null
     */
    @NotNull
    public static LanguageManager instance(@NotNull JavaPlugin plugin) {
        if (instance == null) {
            instance = new LanguageManager(plugin);
        }
        return instance;
    }
    
    /**
     * Saves default language files from resources to the languages folder.
     *
     * @param languagesFolder The languages directory.
     */
    private void saveDefaultLanguages(@NotNull File languagesFolder) {
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
    private void loadLanguageFile(@NotNull File langFile) {
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

    /**
     * Retrieves a precomputed message component for a given language and message key.
     * If the component is not already cached, it deserializes and caches it before returning.
     *
     * @param language the language code for the message
     * @param message the message key to retrieve
     * @return the deserialized and cached message component, or a fallback if not found
     */
    @NotNull
    public Component getMessage(@NotNull String language, @NotNull String message) {
        return computedMessages
            .computeIfAbsent(language, k -> new HashMap<>())
            .computeIfAbsent(message, k -> mm.deserialize(getRawMessage(language, message)));
    }

    /**
     * Retrieves a message component for a given language and message key,
     * with additional placeholders to resolve.
     *
     * @param language the language code for the message
     * @param message the message key to retrieve
     * @param placeholders the placeholders to resolve within the message
     * @return the deserialized message component with placeholders resolved, or a fallback if not found
     */
    @NotNull
    public Component getMessage(@NotNull String language, @NotNull String message, @NotNull TagResolver... placeholders) {
        return mm.deserialize(getRawMessage(language, message), placeholders);
    }

    /**
     * Retrieves a localized message component for the specified player and message key,
     * resolving placeholders as specified.
     *
     * @param player the player whose locale is used to determine the language
     * @param message the message key to retrieve
     * @param placeholders the placeholders to resolve within the message
     * @return the deserialized message component with resolved placeholders, localized to the player's language
     */
    @NotNull
    public Component getMessage(@NotNull Player player, @NotNull String message, @NotNull TagResolver... placeholders) {
        return getMessage(player.locale().getISO3Language(), message, placeholders);
    }

    /**
     * Retrieves a localized message component for the specified player and message key,
     * without any placeholders.
     *
     * @param player the player whose locale is used to determine the language
     * @param message the message key to retrieve
     * @return the deserialized message component, localized to the player's language
     */
    @NotNull
    public Component getMessage(@NotNull Player player, @NotNull String message) {
        return getMessage(player.locale().getISO3Language(), message);
    }

    /**
     * Sends a localized message to the specified player based on their locale,
     * without any placeholders.
     *
     * @param player the player to whom the message will be sent
     * @param message the message key to retrieve and send
     */
    public void sendMessage(@NotNull Player player, @NotNull String message) {
        player.sendMessage(getMessage(player, message));
    }

    /**
     * Sends a localized message to the specified player based on their locale,
     * with placeholders resolved.
     *
     * @param player the player to whom the message will be sent
     * @param message the message key to retrieve and send
     * @param placeholders the placeholders to resolve within the message
     */
    public void sendMessage(@NotNull Player player, @NotNull String message, @NotNull TagResolver... placeholders) {
        player.sendMessage(getMessage(player, message, placeholders));
    }

    /**
     * Retrieves the raw message string for a given language and message key.
     * If the language or message key is not found, returns a default fallback message.
     *
     * @param language the language code for the message
     * @param message the message key to retrieve
     * @return the raw message string if found, or a default fallback message
     */
    @NotNull
    private String getRawMessage(@NotNull String language, @NotNull String message) {
        return messages.getOrDefault(language, new HashMap<>()).getOrDefault(message, "Message not found");
    }
}