package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;
import sharedbillssplitter.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

public class CashBackCommand extends PurchaseCommand {

    public CashBackCommand(SplitCalculator splitCalculator, LocalDate date, String argLine) {
        super(splitCalculator, date, argLine);
    }

    @Override
    protected String purchase(String personName, String itemName, BigDecimal amount, Collection<Person> persons) {
        return super.purchase(personName, itemName, amount.negate(), persons);
    }
}
