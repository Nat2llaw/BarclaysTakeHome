package eagle.bank.bankapi.exception;

public class AccountBalanceException extends RuntimeException {
    public AccountBalanceException(String accountNumber) {
        super("Account " + accountNumber + " has an outstanding balance and cannot be deleted");
    }
}
