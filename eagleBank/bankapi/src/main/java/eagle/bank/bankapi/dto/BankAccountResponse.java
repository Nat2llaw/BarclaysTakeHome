package eagle.bank.bankapi.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class BankAccountResponse {

    private String accountNumber;
    private String sortCode;
    private String name;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private Instant createdTimestamp;
    private Instant updatedTimestamp;
}
