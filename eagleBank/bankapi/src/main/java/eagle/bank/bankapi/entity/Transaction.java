package eagle.bank.bankapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "account_number", nullable = false, updatable = false)
    private String accountNumber;

    @Column(name = "amount", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, updatable = false)
    private String currency = "GBP";

    @Column(name = "type", nullable = false, updatable = false)
    private String type;

    @Column(name = "reference", updatable = false)
    private String reference;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Instant createdTimestamp;

    @PrePersist
    void onCreate() {
        createdTimestamp = Instant.now();
    }
}
