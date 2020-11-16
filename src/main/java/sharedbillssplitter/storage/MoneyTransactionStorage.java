package sharedbillssplitter.storage;

import sharedbillssplitter.MoneyTransaction;

import java.time.LocalDate;
import java.util.List;

public interface MoneyTransactionStorage {
    void add(MoneyTransaction transaction);

    List<MoneyTransaction> getMoneyTransactions(LocalDate date);

    void writeOff(LocalDate date);
}
