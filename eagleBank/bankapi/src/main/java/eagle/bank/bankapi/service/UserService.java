package eagle.bank.bankapi.service;

import eagle.bank.bankapi.dto.AddressDto;
import eagle.bank.bankapi.dto.CreateUserRequest;
import eagle.bank.bankapi.dto.UpdateUserRequest;
import eagle.bank.bankapi.dto.UserResponse;
import eagle.bank.bankapi.entity.User;
import eagle.bank.bankapi.exception.DuplicateEmailException;
import eagle.bank.bankapi.exception.ForbiddenException;
import eagle.bank.bankapi.exception.UserHasAccountsException;
import eagle.bank.bankapi.exception.UserNotFoundException;
import eagle.bank.bankapi.repository.AccountRepository;
import eagle.bank.bankapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        User user = new User();
        user.setId("usr-" + UUID.randomUUID().toString().replace("-", ""));
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        applyAddress(user, request.getAddress());

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse fetchUser(String userId, String requestingUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (!user.getId().equals(requestingUserId)) {
            throw new ForbiddenException("You are not allowed to access this user");
        }
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request, String requestingUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (!user.getId().equals(requestingUserId)) {
            throw new ForbiddenException("You are not allowed to update this user");
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAddress() != null) {
            applyAddress(user, request.getAddress());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateEmailException(request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (!user.getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to delete this user");
        }
        if (accountRepository.existsByUserId(userId)) {
            throw new UserHasAccountsException(userId);
        }
        userRepository.delete(user);
    }

    private void applyAddress(User user, AddressDto address) {
        user.setAddressLine1(address.getLine1());
        user.setAddressLine2(address.getLine2());
        user.setAddressLine3(address.getLine3());
        user.setAddressTown(address.getTown());
        user.setAddressCounty(address.getCounty());
        user.setAddressPostcode(address.getPostcode());
    }

    private UserResponse toResponse(User user) {
        AddressDto address = AddressDto.builder()
                .line1(user.getAddressLine1())
                .line2(user.getAddressLine2())
                .line3(user.getAddressLine3())
                .town(user.getAddressTown())
                .county(user.getAddressCounty())
                .postcode(user.getAddressPostcode())
                .build();

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .address(address)
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .createdTimestamp(user.getCreatedTimestamp())
                .updatedTimestamp(user.getUpdatedTimestamp())
                .build();
    }
}
