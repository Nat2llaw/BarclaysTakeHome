package eagle.bank.bankapi.service;

import eagle.bank.bankapi.dto.CreateTransactionRequest;
import eagle.bank.bankapi.dto.ListTransactionsResponse;
import eagle.bank.bankapi.dto.TransactionResponse;
import eagle.bank.bankapi.entity.BankAccount;
import eagle.bank.bankapi.entity.Transaction;
import eagle.bank.bankapi.exception.AccountNotFoundException;
import eagle.bank.bankapi.exception.ForbiddenException;
import eagle.bank.bankapi.exception.InsufficientFundsException;
import eagle.bank.bankapi.exception.TransactionNotFoundException;
import eagle.bank.bankapi.repository.AccountRepository;
import eagle.bank.bankapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public TransactionResponse createTransaction(String accountNumber, CreateTransactionRequest request, String userId) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (!account.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this bank account");
        }

        BigDecimal amount = request.getAmount();

        if ("withdrawal".equals(request.getType())) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(accountNumber);
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }

        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setId(generateUniqueTransactionId());
        transaction.setAccountNumber(accountNumber);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setType(request.getType());
        transaction.setReference(request.getReference());
        transaction.setUserId(userId);

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public ListTransactionsResponse listTransactions(String accountNumber, String userId) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (!account.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this bank account");
        }

        List<TransactionResponse> transactions = transactionRepository.findAllByAccountNumber(accountNumber)
                .stream()
                .map(this::toResponse)
                .toList();

        return ListTransactionsResponse.builder().transactions(transactions).build();
    }

    @Transactional(readOnly = true)
    public TransactionResponse fetchTransaction(String accountNumber, String transactionId, String userId) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (!account.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this bank account");
        }

        Transaction transaction = transactionRepository.findByIdAndAccountNumber(transactionId, accountNumber)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        return toResponse(transaction);
    }

    private String generateUniqueTransactionId() {
        String candidate;
        do {
            candidate = "tan-" + generateAlphanumeric(8);
        } while (transactionRepository.existsById(candidate));
        return candidate;
    }

    private String generateAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType())
                .reference(transaction.getReference())
                .userId(transaction.getUserId())
                .createdTimestamp(transaction.getCreatedTimestamp())
                .build();
    }
}
