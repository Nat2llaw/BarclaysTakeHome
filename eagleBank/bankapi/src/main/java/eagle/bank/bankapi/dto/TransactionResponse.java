package eagle.bank.bankapi.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class TransactionResponse {

    private String id;
    private BigDecimal amount;
    private String currency;
    private String type;
    private String reference;
    private String userId;
    private Instant createdTimestamp;
}
