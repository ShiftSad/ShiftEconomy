package codes.shiftmc.economy.language;

import codes.shiftmc.economy.configuration.SettingsSource;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public final class LanguageManager {
    public static String DEFAULT_LANGUAGE = "en-US";
    private static LanguageManager instance;
    private final JavaPlugin plugin;

    private final Map<String, Map<String, String>> messages = new HashMap<>();
    private final Map<String, Map<String, Component>> computedMessages = new HashMap<>();

    private static final MiniMessage mm = MiniMessage.builder().build();
    private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[a-zA-Z]+([.-][a-zA-Z]+)*$");

    /**
     * Initializes the LanguageManager.
     *
     * @param plugin The main plugin instance.
     */
    private LanguageManager(@NotNull JavaPlugin plugin, @NotNull String defaultLanguage) {
        this.plugin = plugin;
        DEFAULT_LANGUAGE = defaultLanguage;
        var languages = new File(plugin.getDataFolder() + "/languages");

        saveDefaultLanguages(languages);
        for (File file : languages.listFiles()) {
            loadLanguageFile(file);
        }
    }

    /**
     * Retrieves the current instance of the LanguageManager if it has been initialized.
     *
     * @return the existing LanguageManager instance, or null if it has not been initialized
     */
    @UnknownNullability
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
    public static LanguageManager instance(@NotNull JavaPlugin plugin, @NotNull SettingsSource settingsSource) {
        if (instance == null) {
            instance = new LanguageManager(plugin, settingsSource.defaultLanguage());
        }
        return instance;
    }

    /**
     * Reloads the language files and reinitializes the LanguageManager instance.
     * This method clears the existing data and loads fresh data from the language files,
     * effectively reloading the language resources in case any updates were made to them.
     * <p>
     * This is achieved by reinitializing the `LanguageManager` with the same plugin instance,
     * which causes it to re-execute the constructor, loading the language files again.
     *
     * @param settingsSource The place to get the Default Language.
     * @return An instance of itself
     */
    public LanguageManager reload(SettingsSource settingsSource) {
        instance = new LanguageManager(plugin, settingsSource.defaultLanguage());
        return instance;
    }

    /**
     * Saves default language files from resources to the languages folder.
     *
     * @param languagesFolder The languages' directory.
     */
    private void saveDefaultLanguages(@NotNull File languagesFolder) {
        String[] defaultLangs = {"en-US", "pt-BR"};
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
     * Loads a single language file into the messages map.
     *
     * @param langFile The language file to load.
     */
    private void loadLanguageFile(@NotNull File langFile) {
        String langCode = langFile.getName().replace(".lang", "");
        Map<String, String> msg = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(langFile, StandardCharsets.UTF_8))) {
            StringBuilder lineBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Ignore comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Concatenate lines ending with a backslash
                if (line.endsWith("\\")) {
                    lineBuilder.append(line, 0, line.length() - 1).append(" ");
                    continue;
                } else {
                    lineBuilder.append(line);
                }

                String fullLine = lineBuilder.toString();
                lineBuilder.setLength(0); // Reset for the next potential multi-line value

                // Split the line on the first "=" character
                int separatorIndex = fullLine.indexOf("=");
                if (separatorIndex == -1) {
                    log.warn("Skipping invalid line (no '=' found): {}", fullLine);
                    continue;
                }

                String key = fullLine.substring(0, separatorIndex).trim();
                String value = fullLine.substring(separatorIndex + 1).trim();

                // Validate key against the regex pattern
                if (!VALID_KEY_PATTERN.matcher(key).matches()) {
                    log.warn("Skipping invalid key format: {}", key);
                    continue;
                }

                // Store the key-value pair
                msg.put(key, value);
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
    public Component getMessage(String language, @NotNull String message) {
        var languageTemp = messages.containsKey(language) ? language : DEFAULT_LANGUAGE;
        return computedMessages
            .computeIfAbsent(language, k -> new HashMap<>())
            .computeIfAbsent(message, k -> mm.deserialize(getRawMessage(languageTemp, message)));
    }

    /**
     * Retrieves a precomputed message component for a given language and message key.
     * If the component is not already cached, it deserializes and caches it before returning.
     *
     * @param message the message key to retrieve
     * @return the deserialized and cached message component, or a fallback if not found
     */
    @NotNull
    public Component getMessage(@NotNull String message) {
        return computedMessages
            .computeIfAbsent(DEFAULT_LANGUAGE, k -> new HashMap<>())
            .computeIfAbsent(message, k -> mm.deserialize(getRawMessage(DEFAULT_LANGUAGE, message)));
    }

    /**
     * Retrieves a message component for a given language and message key,
     * with additional placeholders to resolve.
     *
     * @param message the message key to retrieve
     * @param placeholders the placeholders to resolve within the message
     * @return the deserialized message component with placeholders resolved, or a fallback if not found
     */
    @NotNull
    public Component getMessage(@NotNull String message, @NotNull TagResolver... placeholders) {
        return mm.deserialize(getRawMessage(DEFAULT_LANGUAGE, message), placeholders);
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
        return getMessage(player.locale().toLanguageTag(), message, placeholders);
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
        return getMessage(player.locale().toLanguageTag(), message);
    }

    /**
     * Retrieves a localized message component for the specified sender and message key,
     * resolving placeholders as specified.
     *
     * @param sender the sender whose may or may not be a player
     * @param message the message key to retrieve
     * @param placeholders the placeholders to resolve within the message
     * @return the deserialized message component with resolved placeholders, localized to the player's language
     */
    @NotNull
    public Component getMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull TagResolver... placeholders) {
        if (sender instanceof Player player) return getMessage(player.locale().toLanguageTag(), message, placeholders);
        else return getMessage(message, placeholders);
    }

    /**
     * Retrieves a localized message component for the specified sender and message key,
     * without any placeholders.
     *
     * @param sender the sender whose may or may not be a player
     * @param message the message key to retrieve
     * @return the deserialized message component, localized to the player's language
     */
    @NotNull
    public Component getMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (sender instanceof Player player) return getMessage(player.locale().toLanguageTag(), message);
        else return getMessage(message);
    }

    /**
     * Sends a localized message to the specified sender based on their optional locale,
     * without any placeholders.
     *
     * @param sender the sender to whom the message will be sent
     * @param message the message key to retrieve and send
     */
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(getMessage(sender, message));
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
     * @param sender the sender to whom the message will be sent
     * @param message the message key to retrieve and send
     * @param placeholders the placeholders to resolve within the message
     */
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull TagResolver... placeholders) {
        sender.sendMessage(getMessage(sender, message, placeholders));
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
    public String getRawMessage(@NotNull String language, @NotNull String message) {
        language = messages.containsKey(language) ? language : DEFAULT_LANGUAGE;
        return messages.getOrDefault(language, new HashMap<>()).getOrDefault(message, "Message " + message + " not found");
    }

    /**
     * Retrieves the raw message string for a given language and message key.
     * If the language or message key is not found, returns a default fallback message.
     *
     * @param message the message key to retrieve
     * @return the raw message string if found, or a default fallback message
     */
    @NotNull
    public String getRawMessage(@NotNull String message) {
        return getRawMessage(DEFAULT_LANGUAGE, message);
    }

    /**
     * Retrieves the raw message string for a given language and message key.
     * If the language or message key is not found, returns a default fallback message.
     *
     * @param player the player to query the language
     * @param message the message key to retrieve
     * @return the raw message string if found, or a default fallback message
     */
    @NotNull
    public String getRawMessage(@NotNull Player player, @NotNull String message) {
        return getRawMessage(player.locale().toLanguageTag(), message);
    }

    /**
     * Retrieves the raw message string for a given language and message key.
     * If the language or message key is not found, returns a default fallback message.
     *
     * @param sender the maybe player to query the language
     * @param message the message key to retrieve
     * @return the raw message string if found, or a default fallback message
     */
    @NotNull
    public String getRawMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (sender instanceof Player player) return getRawMessage(player, message);
        else return getRawMessage(message);
    }

    /**
     * Retrieves the raw message string for an OfflinePlayer.
     * If the language or message key is not found, returns a default fallback message.
     *
     * @param offlinePlayer the OfflinePlayer to query the language
     * @param message       the message key to retrieve
     * @return the raw message string if found, or a default fallback message
     */
    @NotNull
    public String getRawMessage(@NotNull OfflinePlayer offlinePlayer, @NotNull String message) {
        String language = getOfflinePlayerLanguage(offlinePlayer);
        return getRawMessage(language, message);
    }

    /**
     * Retrieves the localized message component for an OfflinePlayer.
     *
     * @param offlinePlayer the OfflinePlayer to query the language
     * @param message       the message key to retrieve
     * @return the localized message component
     */
    @NotNull
    public Component getMessage(@NotNull OfflinePlayer offlinePlayer, @NotNull String message) {
        String language = getOfflinePlayerLanguage(offlinePlayer);
        return getMessage(language, message);
    }

    /**
     * Sends a localized message to an OfflinePlayer.
     * If the OfflinePlayer is online, sends the message to their player instance.
     *
     * @param offlinePlayer the OfflinePlayer to send the message to
     * @param message       the message key to retrieve and send
     */
    public void sendMessage(@NotNull OfflinePlayer offlinePlayer, @NotNull String message) {
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            sendMessage(offlinePlayer.getPlayer(), message);
        }
    }

    /**
     * Sends a localized message with placeholders to an OfflinePlayer.
     * If the OfflinePlayer is online, sends the message to their player instance.
     *
     * @param offlinePlayer the OfflinePlayer to send the message to
     * @param message       the message key to retrieve and send
     * @param placeholders  the placeholders to resolve within the message
     */
    public void sendMessage(@NotNull OfflinePlayer offlinePlayer, @NotNull String message, @NotNull TagResolver... placeholders) {
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            sendMessage(offlinePlayer.getPlayer(), message, placeholders);
        }
    }

    /**
     * Retrieves the language of an OfflinePlayer.
     * Defaulting to `DEFAULT_LANGUAGE` if unavailable.
     *
     * @param offlinePlayer the OfflinePlayer to query the language
     * @return the language code
     */
    @NotNull
    private String getOfflinePlayerLanguage(@NotNull OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            return offlinePlayer.getPlayer().locale().toLanguageTag();
        }
        return DEFAULT_LANGUAGE;
    }
}