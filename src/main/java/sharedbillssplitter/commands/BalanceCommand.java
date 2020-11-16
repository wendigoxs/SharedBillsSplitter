package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;
import sharedbillssplitter.exceptions.IllegalCommandsArgumentsException;
import sharedbillssplitter.person.GroupService;
import sharedbillssplitter.person.Person;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BalanceCommand implements Command {

    private static final String NO_REPAYMENTS = "No repayments need";
    private static final ArgEnum DEFAULT_COMMAND = ArgEnum.close;

    protected final SplitCalculator splitCalculator;
    protected final LocalDate date;
    private final String argLine;

    private ArgEnum balanceCommand = DEFAULT_COMMAND;
    protected Collection<Person> persons = Collections.emptyList();

    enum ArgEnum {
        close,
        open
    }

    public BalanceCommand(SplitCalculator splitCalculator, LocalDate date, String argLine) {
        this.splitCalculator = splitCalculator;
        this.date = date;
        this.argLine = argLine;
    }

    @Override
    public String process() {
        return prepareOutput(processCommand());
    }

    private String processCommand() {
        try {
            parseBalanceCommand();
            switch (balanceCommand) {
                default:
                case close:
                    return processBalanceClose();
                case open:
                    return processBalanceOpen();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalCommandsArgumentsException();
        }
    }

    protected String processBalanceOpen() {
        return splitCalculator.balanceOpenMonth(date, persons);
    }

    protected String processBalanceClose() {
        return splitCalculator.balanceClose(date, persons);
    }

    private void parseBalanceCommand() {
        Pattern pattern = Pattern.compile("(\\w+)(?:\\s*\\(([^)]+)\\))?"); // balance [open|close] [(list of person)]
        Matcher matcher = pattern.matcher(argLine);

        boolean matcherFound = matcher.find();
        String balanceCommand;
        if (matcherFound && matcher.groupCount() >= 1) {
            balanceCommand = matcher.group(1).trim();
            this.balanceCommand = ArgEnum.valueOf(balanceCommand);
        }
        GroupService groupService = new GroupService(splitCalculator.getGroupStorage());
        if (matcherFound && matcher.groupCount() >= 2) {
            this.persons = groupService.parsePersonHoldersString(matcher.group(2));
        }
    }

    private String prepareOutput(String output) {
        return output.isEmpty() ? NO_REPAYMENTS : output;
    }
}
