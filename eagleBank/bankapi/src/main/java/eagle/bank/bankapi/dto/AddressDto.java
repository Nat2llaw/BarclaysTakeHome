package eagle.bank.bankapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotBlank(message = "address.line1 must not be blank")
    private String line1;

    private String line2;

    private String line3;

    @NotBlank(message = "address.town must not be blank")
    private String town;

    @NotBlank(message = "address.county must not be blank")
    private String county;

    @NotBlank(message = "address.postcode must not be blank")
    private String postcode;
}
