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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static eagle.bank.bankapi.service.TestFixtures.buildAccount;
import static eagle.bank.bankapi.service.TestFixtures.buildTransaction;
import static eagle.bank.bankapi.service.TestFixtures.buildTransactionRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private static final String USER_ID = "usr-abc123";
    private static final String OTHER_USER_ID = "usr-xyz999";
    private static final String ACCOUNT_NUMBER = "01234567";
    private static final String TRANSACTION_ID = "tan-abcd1234";

    @Nested
    class CreateTransaction {

        @Test
        void createsDepositAndReturnsResponse() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            Transaction saved = buildTransaction(TRANSACTION_ID, ACCOUNT_NUMBER, USER_ID, 100.00, "deposit");
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

            // when
            TransactionResponse response = transactionService.createTransaction(ACCOUNT_NUMBER, buildTransactionRequest(100.00, "deposit"), USER_ID);

            // then
            assertThat(response.getId()).isEqualTo(TRANSACTION_ID);
            assertThat(response.getAmount()).isEqualByComparingTo("100.00");
            assertThat(response.getCurrency()).isEqualTo("GBP");
            assertThat(response.getType()).isEqualTo("deposit");
            assertThat(response.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        void depositIncreasesAccountBalance() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 200.00);
            Transaction saved = buildTransaction(TRANSACTION_ID, ACCOUNT_NUMBER, USER_ID, 50.00, "deposit");
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

            // when
            transactionService.createTransaction(ACCOUNT_NUMBER, buildTransactionRequest(50.00, "deposit"), USER_ID);

            // then
            ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
            verify(accountRepository).save(captor.capture());
            assertThat(captor.getValue().getBalance()).isEqualByComparingTo("250.00");
        }

        @Test
        void withdrawalDecreasesAccountBalance() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 200.00);
            Transaction saved = buildTransaction(TRANSACTION_ID, ACCOUNT_NUMBER, USER_ID, 50.00, "withdrawal");
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

            // when
            transactionService.createTransaction(ACCOUNT_NUMBER, buildTransactionRequest(50.00, "withdrawal"), USER_ID);

            // then
            ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
            verify(accountRepository).save(captor.capture());
            assertThat(captor.getValue().getBalance()).isEqualByComparingTo("150.00");
        }

        @Test
        void setsFieldsOnPersistedTransactionEntity() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 100.00);
            Transaction saved = buildTransaction(TRANSACTION_ID, ACCOUNT_NUMBER, USER_ID, 30.00, "withdrawal");
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
            CreateTransactionRequest request = buildTransactionRequest(30.00, "withdrawal");
            request.setReference("Rent");

            // when
            transactionService.createTransaction(ACCOUNT_NUMBER, request, USER_ID);

            // then
            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            Transaction persisted = captor.getValue();
            assertThat(persisted.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
            assertThat(persisted.getAmount()).isEqualByComparingTo("30.00");
            assertThat(persisted.getCurrency()).isEqualTo("GBP");
            assertThat(persisted.getType()).isEqualTo("withdrawal");
            assertThat(persisted.getReference()).isEqualTo("Rent");
            assertThat(persisted.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        void retriesTransactionIdGenerationOnCollision() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            Transaction saved = buildTransaction(TRANSACTION_ID, ACCOUNT_NUMBER, USER_ID, 10.00, "deposit");
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.existsById(anyString()))
                    .thenReturn(true)
                    .thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

            // when
            transactionService.createTransaction(ACCOUNT_NUMBER, buildTransactionRequest(10.00, "deposit"), USER_ID);

            // then
            verify(transactionRepository, times(2)).existsById(anyString());
        }

        @Test
        void generatedTransactionIdMatchesPattern() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.existsById(anyString())).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            transactionService.createTransaction(ACCOUNT_NUMBER, buildTransactionRequest(10.00, "deposit"), USER_ID);

            // then
            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            assertThat(captor.getValue().getId()).matches("^tan-[A-Za-z0-9]+$");
        }

        @Test
        void throwsAccountNotFoundWhenAccountDoesNotExist() {
            // given
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> transactionService.createTransaction(ACCOUNT_NUMBER, buildTransactionRequest(10.00, "deposit"), USER_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

        @Test
        void throwsForbiddenWhenAccountBelongsToDifferentUser() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, OTHER_USER_ID, 100.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when / then
            assertThatThrownBy(() -> transactionService.createTransaction(ACCOUNT_NUMBER, buildTransactionRequest(10.00, "deposit"), USER_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void throwsInsufficientFundsWhenWithdrawalExceedsBalance() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 50.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when / then
            assertThatThrownBy(() -> transactionService.createTransaction(ACCOUNT_NUMBER, buildTransactionRequest(100.00, "withdrawal"), USER_ID))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

    }

    @Nested
    class ListTransactions {

        @Test
        void returnsAllTransactionsForAccount() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            Transaction t1 = buildTransaction("tan-aaa00001", ACCOUNT_NUMBER, USER_ID, 100.00, "deposit");
            Transaction t2 = buildTransaction("tan-bbb00002", ACCOUNT_NUMBER, USER_ID, 50.00, "withdrawal");
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.findAllByAccountNumber(ACCOUNT_NUMBER)).thenReturn(List.of(t1, t2));

            // when
            ListTransactionsResponse response = transactionService.listTransactions(ACCOUNT_NUMBER, USER_ID);

            // then
            assertThat(response.getTransactions()).hasSize(2);
            assertThat(response.getTransactions())
                    .extracting(TransactionResponse::getId)
                    .containsExactly("tan-aaa00001", "tan-bbb00002");
        }

        @Test
        void returnsEmptyListWhenNoTransactions() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.findAllByAccountNumber(ACCOUNT_NUMBER)).thenReturn(List.of());

            // when
            ListTransactionsResponse response = transactionService.listTransactions(ACCOUNT_NUMBER, USER_ID);

            // then
            assertThat(response.getTransactions()).isEmpty();
        }

        @Test
        void throwsAccountNotFoundWhenAccountDoesNotExist() {
            // given
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> transactionService.listTransactions(ACCOUNT_NUMBER, USER_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

        @Test
        void throwsForbiddenWhenAccountBelongsToDifferentUser() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, OTHER_USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when / then
            assertThatThrownBy(() -> transactionService.listTransactions(ACCOUNT_NUMBER, USER_ID))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    class FetchTransaction {

        @Test
        void returnsTransactionWhenFoundAndOwned() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            Transaction transaction = buildTransaction(TRANSACTION_ID, ACCOUNT_NUMBER, USER_ID, 75.00, "deposit");
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.findByIdAndAccountNumber(TRANSACTION_ID, ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(transaction));

            // when
            TransactionResponse response = transactionService.fetchTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID);

            // then
            assertThat(response.getId()).isEqualTo(TRANSACTION_ID);
            assertThat(response.getAmount()).isEqualByComparingTo("75.00");
            assertThat(response.getType()).isEqualTo("deposit");
        }

        @Test
        void throwsAccountNotFoundWhenAccountDoesNotExist() {
            // given
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> transactionService.fetchTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

        @Test
        void throwsForbiddenWhenAccountBelongsToDifferentUser() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, OTHER_USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when / then
            assertThatThrownBy(() -> transactionService.fetchTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void throwsTransactionNotFoundWhenTransactionDoesNotExist() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(transactionRepository.findByIdAndAccountNumber(TRANSACTION_ID, ACCOUNT_NUMBER))
                    .thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> transactionService.fetchTransaction(ACCOUNT_NUMBER, TRANSACTION_ID, USER_ID))
                    .isInstanceOf(TransactionNotFoundException.class)
                    .hasMessageContaining(TRANSACTION_ID);
        }

    }
}
