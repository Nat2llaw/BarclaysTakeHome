package eagle.bank.bankapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bank_account")
@Getter
@Setter
@NoArgsConstructor
public class BankAccount {

    @Id
    @Column(name = "account_number", length = 8, nullable = false, unique = true)
    private String accountNumber;

    @Version
    private Long version;

    @Column(name = "sort_code", nullable = false)
    private String sortCode = "10-10-10";

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false)
    private String currency = "GBP";

    // The user ID extracted from the JWT sub claim
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Instant createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private Instant updatedTimestamp;

    @PrePersist
    void onCreate() {
        createdTimestamp = Instant.now();
        updatedTimestamp = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedTimestamp = Instant.now();
    }
}
