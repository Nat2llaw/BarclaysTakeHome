package eagle.bank.bankapi.service;

import eagle.bank.bankapi.dto.AddressDto;
import eagle.bank.bankapi.dto.CreateTransactionRequest;
import eagle.bank.bankapi.dto.CreateUserRequest;
import eagle.bank.bankapi.entity.BankAccount;
import eagle.bank.bankapi.entity.Transaction;
import eagle.bank.bankapi.entity.User;

import java.math.BigDecimal;
import java.time.Instant;

class TestFixtures {

    static BankAccount buildAccount(String accountNumber, String userId, double balance) {
        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setSortCode("10-10-10");
        account.setName("Test Account");
        account.setAccountType("personal");
        account.setBalance(BigDecimal.valueOf(balance));
        account.setCurrency("GBP");
        account.setUserId(userId);
        account.setCreatedTimestamp(Instant.parse("2026-01-01T00:00:00Z"));
        account.setUpdatedTimestamp(Instant.parse("2026-01-01T00:00:00Z"));
        return account;
    }

    static Transaction buildTransaction(String id, String accountNumber, String userId, double amount, String type) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAccountNumber(accountNumber);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setCurrency("GBP");
        transaction.setType(type);
        transaction.setUserId(userId);
        transaction.setCreatedTimestamp(Instant.parse("2026-01-01T00:00:00Z"));
        return transaction;
    }

    static User buildUser(String id) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setAddressLine1("1 High Street");
        user.setAddressLine2(null);
        user.setAddressLine3(null);
        user.setAddressTown("London");
        user.setAddressCounty("Greater London");
        user.setAddressPostcode("EC1A 1BB");
        user.setPhoneNumber("+447911123456");
        user.setEmail("test@example.com");
        user.setCreatedTimestamp(Instant.parse("2026-01-01T00:00:00Z"));
        user.setUpdatedTimestamp(Instant.parse("2026-01-01T00:00:00Z"));
        return user;
    }

    static AddressDto buildAddress() {
        return AddressDto.builder()
                .line1("1 High Street")
                .town("London")
                .county("Greater London")
                .postcode("EC1A 1BB")
                .build();
    }

    static CreateUserRequest buildCreateUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Test User");
        request.setAddress(buildAddress());
        request.setPhoneNumber("+447911123456");
        request.setEmail("test@example.com");
        return request;
    }

    static CreateTransactionRequest buildTransactionRequest(double amount, String type) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(amount));
        request.setCurrency("GBP");
        request.setType(type);
        return request;
    }
}
