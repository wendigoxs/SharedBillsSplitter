package sharedbillssplitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import sharedbillssplitter.person.Person;
import sharedbillssplitter.storage.GroupStorage;
import sharedbillssplitter.storage.MoneyTransactionStorage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class SplitCalculator {
    private static final String EMPTY_STRING = "";
    private static final BigDecimal MIN_MONEY = new BigDecimal("0.01");
    public static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);
    private static final int MONEY_SCALE = 2;


    @Autowired
    //@Qualifier("TRANSACTION_STORAGE_IN_MEMORY")
    @Qualifier("TRANSACTION_STORAGE_SPRING_H2")
    private MoneyTransactionStorage moneyTransactionStorage;

    @Autowired
    //@Qualifier("GROUP_STORAGE_IN_MEMORY")
    @Qualifier("GROUP_STORAGE_SPRING_H2")
    private GroupStorage groupStorage;

    public SplitCalculator() {
    }

    private String addTransaction(LocalDate date, Person personFrom, String itemName, BigDecimal amount, Person personTo) {
        moneyTransactionStorage.add(new MoneyTransaction(date, personFrom, itemName, amount, personTo));
        return EMPTY_STRING;
    }


    public String addBorrow(LocalDate date, Person borrower, String itemName, BigDecimal amount, Person lender) {
        return addTransaction(date, borrower, itemName, amount, lender);
    }

    public String addRepayment(LocalDate date, Person payer, String itemName, BigDecimal amount, Person receiver) {
        return addTransaction(date, payer, itemName, amount.negate(), receiver);
    }

    public String addPurchase(LocalDate date, Person payer, String itemName, BigDecimal amount, Collection<Person> consumers) {
        if (consumers.isEmpty()) {
            return EMPTY_STRING;
        }
        int idx = 0;
        TreeSet<Person> sortedCustomers = new TreeSet<>(consumers);
        List<BigDecimal> divList = divideValue(amount, sortedCustomers.size());
        checkDivision(divList, amount);
        for (Person consumer : sortedCustomers) {
            addBorrow(date, consumer, itemName, divList.get(idx++), payer);
        }
        return EMPTY_STRING;
    }

    private void checkDivision(List<BigDecimal> divList, BigDecimal amount) {
        if (divList.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO).compareTo(amount) != 0) {
            throw new RuntimeException("wrong division");
        }
    }

    private static List<BigDecimal> divideValue(BigDecimal valueRaw, int divisor) {
        int signum = valueRaw.signum();
        BigDecimal groupSize = new BigDecimal(String.valueOf(divisor));
        BigDecimal divResRaw = valueRaw.abs().divide(groupSize, MATH_CONTEXT);
        BigDecimal divRes = divResRaw.setScale(MONEY_SCALE, RoundingMode.DOWN);
        BigDecimal rest = valueRaw.abs().subtract(divRes.multiply(groupSize));
        int extCount = rest.divide(MIN_MONEY, RoundingMode.UNNECESSARY).intValue();
        return IntStream.range(0, divisor)
                .mapToObj(idx -> idx < extCount ? divRes.add(MIN_MONEY) : divRes)
                .map(it -> signum < 0 ? it.negate() : it)
                .collect(Collectors.toList());
    }

    public String balanceClose(LocalDate date, Collection<Person> filter) {
        return getTextFromMoneyTransactions(getBalanceCloseMoneyTransactionStream(date, filter));
    }

    private String getTextFromMoneyTransactions(Stream<MoneyTransaction> stream) {
        return stream
                .sorted(MoneyTransaction.NAME_COMPARATOR)
                .map(SplitCalculator::getOwesText)
                .collect(Collectors.joining("\n"));
    }


    private Stream<MoneyTransaction> getBalanceCloseMoneyTransactionStream(LocalDate date, Collection<Person> filter) {
        Map<List<Person>, BigDecimal> transactionsMap = moneyTransactionStorage.getMoneyTransactions(date).stream()
                .filter(it -> !it.getDate().isAfter(date))
                .filter(it -> isFiltered(filter, it.getPersonFrom()) || isFiltered(filter, it.getPersonTo()))
                .filter(it -> !it.getPersonFrom().equals(it.getPersonTo())) // ignore borrows and repayments to self
                .map(MoneyTransaction::normalizeOrdered)
                .collect(Collectors.toMap(
                        (it) -> Arrays.asList(it.getPersonFrom(), it.getPersonTo()),
                        it -> it.getAmount(),
                        BigDecimal::add));

        Stream<MoneyTransaction> moneyTransactionStream = transactionsMap.entrySet().stream()
                .map(it -> new MoneyTransaction(null, it.getKey().get(0), "", it.getValue(), it.getKey().get(1)))
                .filter(it -> it.getAmount().compareTo(BigDecimal.ZERO) != 0)
                .map(MoneyTransaction::normalizePositive)
                .filter(it -> isFiltered(filter, it.getPersonFrom()));
        return moneyTransactionStream;
    }

    public String balancePerfectClose(LocalDate date, Collection<Person> persons) {
        List<MoneyTransaction> moneyTransactions = getBalanceCloseMoneyTransactionStream(date, persons).collect(Collectors.toList());
        GraphSolver graphSolver = new GraphSolver(moneyTransactions);
        List<MoneyTransaction> result = graphSolver.solvePerfectBalance();
        return getTextFromMoneyTransactions(result.stream());
    }

    private boolean isFiltered(Collection<Person> filterPersons, Person person) {
        return filterPersons.isEmpty() || filterPersons.contains(person);
    }

    public void writeOff(LocalDate date) {
        moneyTransactionStorage.writeOff(date);
    }

    private static String getOwesText(MoneyTransaction it) {
        return String.format("%s owes %s %s", it.getPersonFrom(), it.getPersonTo(), it.getAmount().setScale(MONEY_SCALE, RoundingMode.UNNECESSARY));
    }

    public String balanceOpenMonth(LocalDate date, Collection<Person> persons) {
        LocalDate lastDayOfPrevMonth = date.withDayOfMonth(1).minus(1, ChronoUnit.DAYS);
        return balanceClose(lastDayOfPrevMonth, persons);
    }

    public GroupStorage getGroupStorage() {
        return groupStorage;
    }


}
