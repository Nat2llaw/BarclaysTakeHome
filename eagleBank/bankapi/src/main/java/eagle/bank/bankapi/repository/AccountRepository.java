package eagle.bank.bankapi.repository;

import eagle.bank.bankapi.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<BankAccount, String> {

    List<BankAccount> findAllByUserId(String userId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByUserId(String userId);
}
