package eagle.bank.bankapi.controller;

import eagle.bank.bankapi.dto.BankAccountResponse;
import eagle.bank.bankapi.dto.CreateBankAccountRequest;
import eagle.bank.bankapi.dto.ListBankAccountsResponse;
import eagle.bank.bankapi.dto.UpdateBankAccountRequest;
import eagle.bank.bankapi.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountResponse createAccount(
            @Valid @RequestBody CreateBankAccountRequest request,
            @AuthenticationPrincipal String userId) {
        return accountService.createAccount(request, userId);
    }

    @GetMapping
    public ListBankAccountsResponse listAccounts(
            @AuthenticationPrincipal String userId) {
        return accountService.listAccountsForUser(userId);
    }

    @GetMapping("/{accountNumber}")
    public BankAccountResponse fetchAccount(
            @PathVariable @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$") String accountNumber,
            @AuthenticationPrincipal String userId) {
        return accountService.fetchAccount(accountNumber, userId);
    }

    @PatchMapping("/{accountNumber}")
    public BankAccountResponse updateAccount(
            @PathVariable @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$") String accountNumber,
            @Valid @RequestBody UpdateBankAccountRequest request,
            @AuthenticationPrincipal String userId) {
        return accountService.updateAccount(accountNumber, request, userId);
    }

    @DeleteMapping("/{accountNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(
            @PathVariable @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$") String accountNumber,
            @AuthenticationPrincipal String userId) {
        accountService.deleteAccount(accountNumber, userId);
    }
}
