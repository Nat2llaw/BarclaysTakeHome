package eagle.bank.bankapi.exception;

public class UserHasAccountsException extends RuntimeException {
    public UserHasAccountsException(String userId) {
        super("User " + userId + " cannot be deleted while they have associated bank accounts");
    }
}
