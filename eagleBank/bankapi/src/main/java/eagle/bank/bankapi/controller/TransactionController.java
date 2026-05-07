package eagle.bank.bankapi.controller;

import eagle.bank.bankapi.dto.CreateTransactionRequest;
import eagle.bank.bankapi.dto.ListTransactionsResponse;
import eagle.bank.bankapi.dto.TransactionResponse;
import eagle.bank.bankapi.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @PathVariable @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$") String accountNumber,
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal String userId) {
        return transactionService.createTransaction(accountNumber, request, userId);
    }

    @GetMapping
    public ListTransactionsResponse listTransactions(
            @PathVariable @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$") String accountNumber,
            @AuthenticationPrincipal String userId) {
        return transactionService.listTransactions(accountNumber, userId);
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse fetchTransaction(
            @PathVariable @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$") String accountNumber,
            @PathVariable @Pattern(regexp = "^tan-[A-Za-z0-9]+$", message = "transactionId must match ^tan-[A-Za-z0-9]+$") String transactionId,
            @AuthenticationPrincipal String userId) {
        return transactionService.fetchTransaction(accountNumber, transactionId, userId);
    }
}
