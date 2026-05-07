package eagle.bank.bankapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "address must not be null")
    @Valid
    private AddressDto address;

    @NotBlank(message = "phoneNumber must not be blank")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phoneNumber must be in E.164 format (e.g. +447911123456)")
    private String phoneNumber;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    private String email;
}
