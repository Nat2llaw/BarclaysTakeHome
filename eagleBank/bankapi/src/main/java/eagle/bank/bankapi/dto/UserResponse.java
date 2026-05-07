package eagle.bank.bankapi.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserResponse {

    private String id;
    private String name;
    private AddressDto address;
    private String phoneNumber;
    private String email;
    private Instant createdTimestamp;
    private Instant updatedTimestamp;
}
