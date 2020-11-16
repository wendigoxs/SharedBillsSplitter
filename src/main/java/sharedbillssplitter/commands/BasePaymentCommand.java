package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;
import sharedbillssplitter.exceptions.IllegalCommandsArgumentsException;
import sharedbillssplitter.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class BasePaymentCommand implements Command {
    protected final SplitCalculator splitCalculator;
    protected final LocalDate date;
    protected final String argLine;

    public BasePaymentCommand(SplitCalculator splitCalculator, LocalDate date, String argLine) {
        this.splitCalculator = splitCalculator;
        this.date = date;
        this.argLine = argLine;
    }

    @Override
    public String process() {
        try {
            String[] split = argLine.split("\\s+");
            Person personFrom = new Person(split[0].trim());
            Person personTo = new Person(split[1].trim());
            BigDecimal amount = new BigDecimal(split[2]);
            return submitToCalculator(personFrom, personTo, amount);
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            throw new IllegalCommandsArgumentsException();
        }
    }

    protected abstract String submitToCalculator(Person personFrom, Person personTo, BigDecimal amount);
}
