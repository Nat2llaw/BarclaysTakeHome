package eagle.bank.bankapi.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ListBankAccountsResponse {

    private List<BankAccountResponse> accounts;
}
