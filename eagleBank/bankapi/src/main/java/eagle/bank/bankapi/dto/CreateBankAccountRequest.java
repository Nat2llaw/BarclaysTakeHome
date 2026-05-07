package eagle.bank.bankapi.dto;

import eagle.bank.bankapi.validation.ValidAccountType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateBankAccountRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "accountType must not be blank")
    @ValidAccountType
    private String accountType;
}
