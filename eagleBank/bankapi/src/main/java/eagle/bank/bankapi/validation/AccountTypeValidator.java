package eagle.bank.bankapi.validation;

import eagle.bank.bankapi.entity.AccountType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AccountTypeValidator implements ConstraintValidator<ValidAccountType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return AccountType.isValid(value);
    }
}
