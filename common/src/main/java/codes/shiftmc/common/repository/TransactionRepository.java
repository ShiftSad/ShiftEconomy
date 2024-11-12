package codes.shiftmc.common.repository;

import codes.shiftmc.common.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransactionRepository {

    /**
     * Saves a new transaction record.
     *
     * @param transaction The Transaction to save.
     * @return A Mono emitting the saved Transaction.
     */
    Mono<Transaction> save(Transaction transaction);

    /**
     * Finds all transactions where the specified user is either the sender or receiver.
     *
     * @param uuid The UUID of the user.
     * @return A Flux emitting each Transaction involving the user.
     */
    Flux<Transaction> findByUser(UUID uuid);

    /**
     * Finds all transactions where the specified user is either the sender or receiver
     * and the transaction amount is within the specified bounds.
     *
     * @param uuid The UUID of the user.
     * @param lowerBound The lower bound of the transaction amount.
     * @param upperBound The upper bound of the transaction amount.
     * @return A Flux emitting each Transaction involving the user within the specified bounds.
     */
    Flux<Transaction> findByUserWithAmountBounds(UUID uuid, int lowerBound, int upperBound);

    /**
     * Finds all transactions for a given receiver UUID.
     *
     * @param receiverUuid The UUID of the transaction receiver.
     * @return A Flux emitting each Transaction received by the user.
     */
    Flux<Transaction> findByReceiverUuid(UUID receiverUuid);

    /**
     * Finds all transactions for a given sender UUID.
     *
     * @param senderUuid The UUID of the transaction sender.
     * @return A Flux emitting each Transaction sent by the user.
     */
    Flux<Transaction> findBySenderUuid(UUID senderUuid);
}