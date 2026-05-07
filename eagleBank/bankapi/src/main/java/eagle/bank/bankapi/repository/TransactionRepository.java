package eagle.bank.bankapi.repository;

import eagle.bank.bankapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findAllByAccountNumber(String accountNumber);

    Optional<Transaction> findByIdAndAccountNumber(String id, String accountNumber);

    boolean existsById(String id);
}
