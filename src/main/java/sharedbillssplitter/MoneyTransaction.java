package sharedbillssplitter;

import sharedbillssplitter.person.Person;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

@Entity
public class MoneyTransaction {

    public static final Comparator<MoneyTransaction> NAME_COMPARATOR = Comparator.comparing(MoneyTransaction::getPersonFrom).thenComparing(MoneyTransaction::getPersonTo);

    private Long id;

    private LocalDate date;
    private Person personFrom;
    private BigDecimal amount;
    private Person personTo;
    private String itemName;


    public MoneyTransaction(LocalDate date, Person personFrom, String itemName, BigDecimal amount, Person personTo) {
        this.date = date;
        this.personFrom = personFrom;
        this.amount = amount;
        this.personTo = personTo;
        this.itemName = itemName;
    }

    public MoneyTransaction() {

    }

    public MoneyTransaction normalizeOrdered() {
        return personFrom.compareTo(personTo) <= 0 ? this
                : flipPersons(this);
    }

    public MoneyTransaction normalizePositive() {
        return amount.compareTo(BigDecimal.ZERO) >= 0 ? this
                : flipPersons(this);
    }

    private static MoneyTransaction flipPersons(MoneyTransaction transaction) {
        return new MoneyTransaction(transaction.date, transaction.personTo, transaction.itemName, transaction.amount.negate(), transaction.personFrom);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    public void setPersonFrom(Person personFrom) {
        this.personFrom = personFrom;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    public Person getPersonFrom() {
        return personFrom;
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    public Person getPersonTo() {
        return personTo;
    }

    public void setPersonTo(Person personTo) {
        this.personTo = personTo;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }
}
