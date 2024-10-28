package codes.shiftmc.common.service;

import codes.shiftmc.common.model.Transaction;
import codes.shiftmc.common.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Saves a new transaction.
     *
     * @param transaction The Transaction to save.
     * @return A Mono emitting the saved Transaction.
     */
    public Mono<Transaction> createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    /**
     * Retrieves all transactions where the specified user is either the sender or receiver.
     *
     * @param uuid The UUID of the user.
     * @return A Flux emitting each Transaction involving the user.
     */
    public Flux<Transaction> getTransactionsByUser(UUID uuid) {
        return transactionRepository.findByUser(uuid);
    }

    /**
     * Retrieves all transactions received by a given user.
     *
     * @param receiverUuid The UUID of the transaction receiver.
     * @return A Flux emitting each Transaction received by the user.
     */
    public Flux<Transaction> getTransactionsByReceiver(UUID receiverUuid) {
        return transactionRepository.findByReceiverUuid(receiverUuid);
    }

    /**
     * Retrieves all transactions sent by a given user.
     *
     * @param senderUuid The UUID of the transaction sender.
     * @return A Flux emitting each Transaction sent by the user.
     */
    public Flux<Transaction> getTransactionsBySender(UUID senderUuid) {
        return transactionRepository.findBySenderUuid(senderUuid);
    }

    /**
     * Retrieves all transactions sent by a given user to a specific receiver.
     *
     * @param senderUuid   The UUID of the transaction sender.
     * @param receiverUuid The UUID of the transaction receiver.
     * @return A Flux emitting each Transaction sent by the user to the receiver.
     */
    public Flux<Transaction> getTransactionsBySenderAndReceiver(UUID senderUuid, UUID receiverUuid) {
        return transactionRepository.findBySenderUuid(senderUuid)
                .filter(transaction -> transaction.receiverUUID().equals(receiverUuid));
    }
}