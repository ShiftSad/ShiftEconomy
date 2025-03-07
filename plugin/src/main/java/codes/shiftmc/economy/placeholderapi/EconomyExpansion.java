package codes.shiftmc.economy.placeholderapi;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import codes.shiftmc.economy.language.LanguageManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EconomyExpansion extends PlaceholderExpansion {

    private final UserService userService;
    private final LinkedList<UserData> users = new LinkedList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public EconomyExpansion(UserService userService) {
        this.userService = userService;

        // Update the top users every 5 min
        scheduler.scheduleAtFixedRate(() -> {
            userService.findTopUsers(0, 1000)
                    .collectList()
                    .defaultIfEmpty(Collections.emptyList())
                    .doOnNext(u -> {
                        users.clear();
                        users.addAll(u);
                    })
                    .subscribe();
        }, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "shifteconomy";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ShiftSad";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        var lang = LanguageManager.instance();
        final var args = params.split("_");
        switch (args[0]) {
            case "rank" -> {
                var position = Integer.parseInt(args[2]) - 1;
                UserData user;
                if (position < 0 || position >= users.size()) {
                    user = new UserData(UUID.randomUUID(), lang.getRawMessage(player, "player.top.missing"), 0);
                } else user = users.get(position);

                switch (args[1]) {
                    case "playername":
                        return user.getUsername();
                    case "balance-format":
                        return NumberFormatter.format(user.getBalance());
                    case "balance":
                        return String.valueOf(user.getBalance());
                }
            }
        }
        return "Unknown placeholder";
    }
}
