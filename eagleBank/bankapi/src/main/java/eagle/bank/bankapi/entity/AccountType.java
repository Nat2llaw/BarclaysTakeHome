package eagle.bank.bankapi.entity;

public enum AccountType {
    personal;

    public static boolean isValid(String value) {
        if (value == null) return true; // null handled by @NotBlank
        for (AccountType t : values()) {
            if (t.name().equals(value)) return true;
        }
        return false;
    }
}
