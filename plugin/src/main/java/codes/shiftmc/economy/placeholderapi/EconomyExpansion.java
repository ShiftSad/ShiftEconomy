package codes.shiftmc.economy.placeholderapi;

import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.service.UserService;
import codes.shiftmc.common.util.NumberFormatter;
import com.google.common.collect.Queues;
import lombok.AllArgsConstructor;
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
        final var args = params.split("_");
        switch (args[0]) {
            case "rank" -> {
                var position = Integer.parseInt(args[2]);
                switch (args[1]) {
                    case "playername":
                        return users.get(position).getUsername();
                    case "balance-format":
                        return NumberFormatter.format(users.get(position).getBalance());
                    case "balance":
                        return String.valueOf(users.get(position).getBalance());
                }
            }
        }
        return "Unknown placeholder";
    }
}
