package sharedbillssplitter.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sharedbillssplitter.MoneyTransaction;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

@Component(value = "TRANSACTION_STORAGE_SPRING_H2")
public class MoneyTransactionStorageSpringH2 implements MoneyTransactionStorage {

    @Autowired
    MoneyTransactionRepository moneyTransactionRepository;

    @Autowired
    PersonRepository personRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void add(MoneyTransaction moneyTransaction) {
        add_v2(moneyTransaction);
    }


    private void add_v2(MoneyTransaction moneyTransaction) {
        entityManager.merge(moneyTransaction);
    }

    private void add_v1(MoneyTransaction moneyTransaction) {
        String personFromName = moneyTransaction.getPersonFrom().getName();
        String personToName = moneyTransaction.getPersonTo().getName();

        if (personRepository.existsById(personFromName)) {
            moneyTransaction.setPersonFrom(
                    personRepository.getOne(personFromName)
            );
        }
        if (personRepository.existsById(personToName)) {
            moneyTransaction.setPersonTo(
                    personRepository.getOne(personToName)
            );
        }
        moneyTransactionRepository.save(moneyTransaction);
    }

    @Override
    public List<MoneyTransaction> getMoneyTransactions(LocalDate date) {
        return moneyTransactionRepository.getAllByDateIsLessThanEqual(date);
    }

    @Transactional
    @Override
    public void writeOff(LocalDate date) {
        moneyTransactionRepository.deleteAllByDateIsLessThanEqual(date);
    }
}
