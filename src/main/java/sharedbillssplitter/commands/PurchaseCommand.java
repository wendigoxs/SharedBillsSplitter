package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;
import sharedbillssplitter.exceptions.IllegalCommandsArgumentsException;
import sharedbillssplitter.person.GroupService;
import sharedbillssplitter.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PurchaseCommand implements Command {
    private static final int REQUIRED_ARGS_AT_LEAST = 4;

    private final SplitCalculator splitCalculator;
    private final LocalDate date;
    private final String argLine;

    public PurchaseCommand(SplitCalculator splitCalculator, LocalDate date, String argLine) {
        this.splitCalculator = splitCalculator;
        this.date = date;
        this.argLine = argLine;
    }

    @Override
    public String process() {
        GroupService groupService = new GroupService(splitCalculator.getGroupStorage());

        Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s+([0-9.]+)(?:\\s*\\(([^)]+)\\))"); // purchase Person itemName amount (GROUP)
        Matcher matcher = pattern.matcher(argLine);
        if (!matcher.find() || matcher.groupCount() < REQUIRED_ARGS_AT_LEAST) {
            throw new IllegalCommandsArgumentsException();
        }
        String personName = matcher.group(1).trim();
        String itemName = matcher.group(2).trim();
        String amountStr = matcher.group(3).trim();
        String groupStr = matcher.group(4).trim();

        BigDecimal amount = null;
        try {
            amount = new BigDecimal(amountStr);
        } catch (Exception e) {
            throw new IllegalCommandsArgumentsException();
        }
        Collection<Person> persons = groupService.parsePersonHoldersString(groupStr);
        return purchase(personName, itemName, amount, persons);
    }

    protected String purchase(String personName, String itemName, BigDecimal amount, Collection<Person> persons) {
        return splitCalculator.addPurchase(date, new Person(personName), itemName, amount, persons);
    }
}
