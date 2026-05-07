package eagle.bank.bankapi.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "amount must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "amount must be at least 0.00")
    @DecimalMax(value = "10000.0", inclusive = true, message = "amount must be at most 10000.00")
    private BigDecimal amount;

    @NotBlank(message = "currency must not be blank")
    @Pattern(regexp = "GBP", message = "currency must be 'GBP'")
    private String currency;

    @NotBlank(message = "type must not be blank")
    @Pattern(regexp = "deposit|withdrawal", message = "type must be 'deposit' or 'withdrawal'")
    private String type;

    private String reference;
}
