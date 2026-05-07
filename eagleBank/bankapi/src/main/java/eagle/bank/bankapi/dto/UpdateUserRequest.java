package eagle.bank.bankapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRequest {

    private String name;

    @Valid
    private AddressDto address;

    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phoneNumber must be in E.164 format (e.g. +447911123456)")
    private String phoneNumber;

    @Email(message = "email must be a valid email address")
    private String email;
}
