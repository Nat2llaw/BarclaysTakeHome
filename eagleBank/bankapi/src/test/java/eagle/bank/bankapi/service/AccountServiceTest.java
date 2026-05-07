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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private static final String USER_ID = "usr-abc123";
    private static final String OTHER_USER_ID = "usr-xyz999";
    private static final String ACCOUNT_NUMBER = "01234567";

    @Nested
    class CreateAccount {

        @Test
        void createsAccountAndReturnsResponse() {
            // given
            CreateBankAccountRequest request = new CreateBankAccountRequest();
            request.setName("My Account");
            request.setAccountType("personal");
            when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
            when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            BankAccountResponse response = accountService.createAccount(request, USER_ID);

            // then
            assertThat(response.getSortCode()).isEqualTo("10-10-10");
            assertThat(response.getName()).isEqualTo("My Account");
            assertThat(response.getAccountType()).isEqualTo("personal");
            assertThat(response.getBalance()).isEqualByComparingTo("0.00");
            assertThat(response.getCurrency()).isEqualTo("GBP");
        }

        @Test
        void setsNameAccountTypeAndUserIdOnSavedEntity() {
            // given
            CreateBankAccountRequest request = new CreateBankAccountRequest();
            request.setName("My Account");
            request.setAccountType("personal");
            BankAccount saved = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
            when(accountRepository.save(any(BankAccount.class))).thenReturn(saved);

            // when
            accountService.createAccount(request, USER_ID);

            // then
            ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
            verify(accountRepository).save(captor.capture());
            BankAccount persisted = captor.getValue();
            assertThat(persisted.getName()).isEqualTo("My Account");
            assertThat(persisted.getAccountType()).isEqualTo("personal");
            assertThat(persisted.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        void retriesAccountNumberGenerationOnCollision() {
            // given
            CreateBankAccountRequest request = new CreateBankAccountRequest();
            request.setName("My Account");
            request.setAccountType("personal");
            BankAccount saved = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            when(accountRepository.existsByAccountNumber(anyString()))
                    .thenReturn(true)
                    .thenReturn(false);
            when(accountRepository.save(any(BankAccount.class))).thenReturn(saved);

            // when
            accountService.createAccount(request, USER_ID);

            // then
            verify(accountRepository, times(2)).existsByAccountNumber(anyString());
        }

        @Test
        void generatedAccountNumberMatchesPattern() {
            // given
            CreateBankAccountRequest request = new CreateBankAccountRequest();
            request.setName("My Account");
            request.setAccountType("personal");
            when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
            when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            accountService.createAccount(request, USER_ID);

            // then
            ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
            verify(accountRepository).save(captor.capture());
            assertThat(captor.getValue().getAccountNumber()).matches("^01\\d{6}$");
        }
    }

    @Nested
    class ListAccountsForUser {

        @Test
        void returnsAccountsOwnedByUser() {
            // given
            BankAccount account1 = buildAccount("01000001", USER_ID, 0.00);
            BankAccount account2 = buildAccount("01000002", USER_ID, 100.00);
            when(accountRepository.findAllByUserId(USER_ID)).thenReturn(List.of(account1, account2));

            // when
            ListBankAccountsResponse response = accountService.listAccountsForUser(USER_ID);

            // then
            assertThat(response.getAccounts()).hasSize(2);
            assertThat(response.getAccounts())
                    .extracting(BankAccountResponse::getAccountNumber)
                    .containsExactly("01000001", "01000002");
        }

        @Test
        void returnsEmptyListWhenUserHasNoAccounts() {
            // given
            when(accountRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

            // when
            ListBankAccountsResponse response = accountService.listAccountsForUser(USER_ID);

            // then
            assertThat(response.getAccounts()).isEmpty();
        }
    }

    @Nested
    class FetchAccount {

        @Test
        void returnsAccountWhenFoundAndOwned() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 50.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when
            BankAccountResponse response = accountService.fetchAccount(ACCOUNT_NUMBER, USER_ID);

            // then
            assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
            assertThat(response.getBalance()).isEqualByComparingTo("50.00");
        }

        @Test
        void throwsAccountNotFoundWhenAccountDoesNotExist() {
            // given
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> accountService.fetchAccount(ACCOUNT_NUMBER, USER_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

        @Test
        void throwsForbiddenWhenAccountBelongsToDifferentUser() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, OTHER_USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when / then
            assertThatThrownBy(() -> accountService.fetchAccount(ACCOUNT_NUMBER, USER_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void throws404NotForbiddenWhenAccountDoesNotExistEvenForDifferentUser() {
            // given — account missing entirely; must not leak ownership via 403
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> accountService.fetchAccount(ACCOUNT_NUMBER, OTHER_USER_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

    }

    @Nested
    class UpdateAccount {

        @Test
        void updatesNameWhenProvided() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            UpdateBankAccountRequest request = new UpdateBankAccountRequest();
            setField(request, "name", "New Name");

            // when
            BankAccountResponse response = accountService.updateAccount(ACCOUNT_NUMBER, request, USER_ID);

            // then
            assertThat(response.getName()).isEqualTo("New Name");
            assertThat(response.getAccountType()).isEqualTo("personal");
        }

        @Test
        void doesNotOverwriteFieldsWhenNullInRequest() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            UpdateBankAccountRequest request = new UpdateBankAccountRequest();

            // when
            BankAccountResponse response = accountService.updateAccount(ACCOUNT_NUMBER, request, USER_ID);

            // then
            assertThat(response.getName()).isEqualTo("Test Account");
            assertThat(response.getAccountType()).isEqualTo("personal");
        }

        @Test
        void throwsAccountNotFoundWhenAccountDoesNotExist() {
            // given
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> accountService.updateAccount(ACCOUNT_NUMBER, new UpdateBankAccountRequest(), USER_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

        @Test
        void throwsForbiddenWhenAccountBelongsToDifferentUser() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, OTHER_USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when / then
            assertThatThrownBy(() -> accountService.updateAccount(ACCOUNT_NUMBER, new UpdateBankAccountRequest(), USER_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

    }

    @Nested
    class DeleteAccount {

        @Test
        void deletesAccountWithZeroBalance() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when
            accountService.deleteAccount(ACCOUNT_NUMBER, USER_ID);

            // then
            verify(accountRepository).delete(account);
        }

        @Test
        void throwsAccountNotFoundWhenAccountDoesNotExist() {
            // given
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> accountService.deleteAccount(ACCOUNT_NUMBER, USER_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

        @Test
        void throwsForbiddenWhenAccountBelongsToDifferentUser() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, OTHER_USER_ID, 0.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when / then
            assertThatThrownBy(() -> accountService.deleteAccount(ACCOUNT_NUMBER, USER_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void throwsAccountBalanceExceptionWhenBalanceIsNonZero() {
            // given
            BankAccount account = buildAccount(ACCOUNT_NUMBER, USER_ID, 100.00);
            when(accountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

            // when / then
            assertThatThrownBy(() -> accountService.deleteAccount(ACCOUNT_NUMBER, USER_ID))
                    .isInstanceOf(AccountBalanceException.class)
                    .hasMessageContaining(ACCOUNT_NUMBER);
        }

    }

    // UpdateBankAccountRequest has no setter (Lombok @Getter only) so we set fields via reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
