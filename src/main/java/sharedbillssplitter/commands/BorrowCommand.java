package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;
import sharedbillssplitter.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BorrowCommand extends BasePaymentCommand {
    public BorrowCommand(SplitCalculator splitCalculator, LocalDate date, String argLine) {
        super(splitCalculator, date, argLine);
    }

    @Override
    protected String submitToCalculator(Person personFrom, Person personTo, BigDecimal amount) {
        return splitCalculator.addBorrow(date, personFrom, "", amount, personTo);
    }
}
