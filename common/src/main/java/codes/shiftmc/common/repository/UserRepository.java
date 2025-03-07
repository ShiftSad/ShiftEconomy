package codes.shiftmc.common.repository;

import codes.shiftmc.common.model.UserData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository {

    /**
     * Finds a user by their UUID.
     *
     * @param uuid The UUID of the user.
     * @return A Mono emitting the UserData if found, empty if not.
     */
    Mono<UserData> findByUuid(UUID uuid);

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user.
     * @return A Mono emitting the UserData if found, empty if not.
     */
    Mono<UserData> findByUsername(String username);

    /**
     * Saves a new user or updates an existing user.
     *
     * @param userData The UserData to save or update.
     * @return A Mono emitting the saved or updated UserData.
     */
    Mono<UserData> save(UserData userData);

    /**
     * Updates a user's balance.
     *
     * @param uuid The UUID of the user.
     * @param newBalance The new balance amount.
     * @return A Mono emitting the updated UserData.
     */
    Mono<UserData> updateBalance(UUID uuid, double newBalance);

    /**
     * Finds the top users based on a specified range.
     * This method retrieves users ordered by balance in descending order,
     * returning a subset based on the given range.
     *
     * @param from The starting index of the range.
     * @param to The ending index of the range (exclusive).
     * @return A Flux emitting UserData objects within the specified range.
     */
    Flux<UserData> findTopUsers(int from, int to);
}