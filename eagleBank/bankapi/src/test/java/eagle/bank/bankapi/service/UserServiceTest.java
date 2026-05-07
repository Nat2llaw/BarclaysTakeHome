package eagle.bank.bankapi.service;

import eagle.bank.bankapi.dto.AddressDto;
import eagle.bank.bankapi.dto.UpdateUserRequest;
import eagle.bank.bankapi.dto.UserResponse;
import eagle.bank.bankapi.entity.User;
import eagle.bank.bankapi.exception.ForbiddenException;
import eagle.bank.bankapi.exception.UserHasAccountsException;
import eagle.bank.bankapi.exception.UserNotFoundException;
import eagle.bank.bankapi.repository.AccountRepository;
import eagle.bank.bankapi.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static eagle.bank.bankapi.service.TestFixtures.buildCreateUserRequest;
import static eagle.bank.bankapi.service.TestFixtures.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UserService userService;

    private static final String USER_ID = "usr-abc123";
    private static final String OTHER_USER_ID = "usr-xyz999";

    @Nested
    class CreateUser {

        @Test
        void createsUserAndReturnsResponse() {
            // given
            User saved = buildUser(USER_ID);
            when(userRepository.save(any(User.class))).thenReturn(saved);

            // when
            UserResponse response = userService.createUser(buildCreateUserRequest());

            // then
            assertThat(response.getId()).isEqualTo(USER_ID);
            assertThat(response.getName()).isEqualTo("Test User");
            assertThat(response.getPhoneNumber()).isEqualTo("+447911123456");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        void setsAllFieldsOnPersistedUserEntity() {
            // given
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            userService.createUser(buildCreateUserRequest());

            // then
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User persisted = captor.getValue();
            assertThat(persisted.getName()).isEqualTo("Test User");
            assertThat(persisted.getAddressLine1()).isEqualTo("1 High Street");
            assertThat(persisted.getAddressTown()).isEqualTo("London");
            assertThat(persisted.getAddressCounty()).isEqualTo("Greater London");
            assertThat(persisted.getAddressPostcode()).isEqualTo("EC1A 1BB");
            assertThat(persisted.getPhoneNumber()).isEqualTo("+447911123456");
            assertThat(persisted.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        void generatedUserIdMatchesPattern() {
            // given
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            userService.createUser(buildCreateUserRequest());

            // then
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getId()).matches("^usr-[A-Za-z0-9]+$");
        }

    }

    @Nested
    class FetchUser {

        @Test
        void returnsUserWhenFoundAndOwned() {
            // given
            User user = buildUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // when
            UserResponse response = userService.fetchUser(USER_ID, USER_ID);

            // then
            assertThat(response.getId()).isEqualTo(USER_ID);
            assertThat(response.getName()).isEqualTo("Test User");
        }

        @Test
        void throwsUserNotFoundWhenUserDoesNotExist() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.fetchUser(USER_ID, USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(USER_ID);
        }

        @Test
        void throwsForbiddenWhenRequestingDifferentUsersRecord() {
            // given
            User user = buildUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // when / then
            assertThatThrownBy(() -> userService.fetchUser(USER_ID, OTHER_USER_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

    }

    @Nested
    class UpdateUser {

        @Test
        void updatesNameWhenProvided() {
            // given
            User user = buildUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            UpdateUserRequest request = new UpdateUserRequest();
            request.setName("New Name");

            // when
            UserResponse response = userService.updateUser(USER_ID, request, USER_ID);

            // then
            assertThat(response.getName()).isEqualTo("New Name");
        }

        @Test
        void updatesAddressWhenProvided() {
            // given
            User user = buildUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            AddressDto newAddress = AddressDto.builder()
                    .line1("99 New Road")
                    .town("Manchester")
                    .county("Greater Manchester")
                    .postcode("M1 1AA")
                    .build();
            UpdateUserRequest request = new UpdateUserRequest();
            request.setAddress(newAddress);

            // when
            UserResponse response = userService.updateUser(USER_ID, request, USER_ID);

            // then
            assertThat(response.getAddress().getLine1()).isEqualTo("99 New Road");
            assertThat(response.getAddress().getTown()).isEqualTo("Manchester");
            assertThat(response.getAddress().getPostcode()).isEqualTo("M1 1AA");
        }

        @Test
        void doesNotOverwriteFieldsWhenNullInRequest() {
            // given
            User user = buildUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            UpdateUserRequest request = new UpdateUserRequest();

            // when
            UserResponse response = userService.updateUser(USER_ID, request, USER_ID);

            // then
            assertThat(response.getName()).isEqualTo("Test User");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getPhoneNumber()).isEqualTo("+447911123456");
        }

        @Test
        void throwsUserNotFoundWhenUserDoesNotExist() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.updateUser(USER_ID, new UpdateUserRequest(), USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(USER_ID);
        }

        @Test
        void throwsForbiddenWhenRequestingDifferentUsersRecord() {
            // given
            User user = buildUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // when / then
            assertThatThrownBy(() -> userService.updateUser(USER_ID, new UpdateUserRequest(), OTHER_USER_ID))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deletesUserWithNoAccounts() {
            // given
            User user = buildUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(accountRepository.existsByUserId(USER_ID)).thenReturn(false);

            // when
            userService.deleteUser(USER_ID);

            // then
            verify(userRepository).delete(user);
        }

        @Test
        void throwsUserNotFoundWhenUserDoesNotExist() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.deleteUser(USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(USER_ID);
        }

        @Test
        void throwsUserHasAccountsExceptionWhenUserHasAccounts() {
            // given
            User user = buildUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(accountRepository.existsByUserId(USER_ID)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> userService.deleteUser(USER_ID))
                    .isInstanceOf(UserHasAccountsException.class)
                    .hasMessageContaining(USER_ID);
        }

    }
}
