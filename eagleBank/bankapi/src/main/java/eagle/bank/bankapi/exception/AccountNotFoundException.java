package eagle.bank.bankapi.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountNumber) {
        super("Bank account not found: " + accountNumber);
    }
}
