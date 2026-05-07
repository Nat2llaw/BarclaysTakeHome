package eagle.bank.bankapi.controller;

import eagle.bank.bankapi.dto.CreateUserRequest;
import eagle.bank.bankapi.dto.UpdateUserRequest;
import eagle.bank.bankapi.dto.UserResponse;
import eagle.bank.bankapi.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/{userId}")
    public UserResponse fetchUser(
            @PathVariable @Pattern(regexp = "^usr-[A-Za-z0-9]+$", message = "userId must match ^usr-[A-Za-z0-9]+$") String userId,
            @AuthenticationPrincipal String requestingUserId) {
        return userService.fetchUser(userId, requestingUserId);
    }

    @PatchMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable @Pattern(regexp = "^usr-[A-Za-z0-9]+$", message = "userId must match ^usr-[A-Za-z0-9]+$") String userId,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal String requestingUserId) {
        return userService.updateUser(userId, request, requestingUserId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal String userId) {
        userService.deleteUser(userId);
    }
}
