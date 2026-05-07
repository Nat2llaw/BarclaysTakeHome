package eagle.bank.bankapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BadRequestErrorResponse {

    private String message;
    private List<FieldError> details;

    @Getter
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private String type;
    }
}
