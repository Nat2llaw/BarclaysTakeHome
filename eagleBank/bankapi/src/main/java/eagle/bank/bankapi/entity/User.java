package eagle.bank.bankapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "address_line3")
    private String addressLine3;

    @Column(name = "address_town", nullable = false)
    private String addressTown;

    @Column(name = "address_county", nullable = false)
    private String addressCounty;

    @Column(name = "address_postcode", nullable = false)
    private String addressPostcode;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

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
