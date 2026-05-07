package eagle.bank.bankapi.dto;

import eagle.bank.bankapi.validation.ValidAccountType;
import lombok.Getter;

@Getter
public class UpdateBankAccountRequest {

    private String name;

    @ValidAccountType
    private String accountType;
}
