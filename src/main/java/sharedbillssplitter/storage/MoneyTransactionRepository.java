package sharedbillssplitter.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import sharedbillssplitter.MoneyTransaction;

import java.time.LocalDate;
import java.util.List;

public interface MoneyTransactionRepository extends JpaRepository<MoneyTransaction, Long> {

    List<MoneyTransaction> getAllByDateIsLessThanEqual(LocalDate date);


   void deleteAllByDateIsLessThanEqual(LocalDate date);
}
