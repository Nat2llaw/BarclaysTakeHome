package eagle.bank.bankapi.service;

import eagle.bank.bankapi.dto.BankAccountResponse;
import eagle.bank.bankapi.dto.CreateBankAccountRequest;
import eagle.bank.bankapi.dto.ListBankAccountsResponse;
import eagle.bank.bankapi.dto.UpdateBankAccountRequest;
import eagle.bank.bankapi.entity.BankAccount;
import eagle.bank.bankapi.exception.AccountBalanceException;
import eagle.bank.bankapi.exception.AccountNotFoundException;
import eagle.bank.bankapi.exception.ForbiddenException;
import eagle.bank.bankapi.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public BankAccountResponse createAccount(CreateBankAccountRequest request, String userId) {
        BankAccount account = new BankAccount();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setName(request.getName());
        account.setAccountType(request.getAccountType());
        account.setUserId(userId);

        BankAccount saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ListBankAccountsResponse listAccountsForUser(String userId) {
        List<BankAccountResponse> accounts = accountRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ListBankAccountsResponse.builder().accounts(accounts).build();
    }

    @Transactional(readOnly = true)
    public BankAccountResponse fetchAccount(String accountNumber, String userId) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        if (!account.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this bank account");
        }
        return toResponse(account);
    }

    @Transactional
    public BankAccountResponse updateAccount(String accountNumber, UpdateBankAccountRequest request, String userId) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        if (!account.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to update this bank account");
        }
        if (request.getName() != null) {
            account.setName(request.getName());
        }
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(String accountNumber, String userId) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        if (!account.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to delete this bank account");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountBalanceException(accountNumber);
        }
        accountRepository.delete(account);
    }

    private String generateUniqueAccountNumber() {
        String candidate;
        do {
            // Produces a number between 0 and 999999, zero-padded to 6 digits, prefixed with "01"
            candidate = String.format("01%06d", SECURE_RANDOM.nextInt(1_000_000));
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    private BankAccountResponse toResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .sortCode(account.getSortCode())
                .name(account.getName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .createdTimestamp(account.getCreatedTimestamp())
                .updatedTimestamp(account.getUpdatedTimestamp())
                .build();
    }
}
