package sharedbillssplitter.commands;

import org.springframework.stereotype.Component;
import sharedbillssplitter.SplitCalculator;
import sharedbillssplitter.exceptions.IllegalCommandsArgumentsException;
import sharedbillssplitter.exceptions.UnknownCommandException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommandFactory {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final SplitCalculator splitCalculator;

    public CommandFactory(SplitCalculator splitCalculator) {
        this.splitCalculator = splitCalculator;
    }

    public Command createCommand(String line) {
        Pattern patternDateCommand = Pattern.compile("\\s*(\\d\\d\\d\\d\\.\\d\\d\\.\\d\\d)?\\s*(.*)");

        Matcher matcher = patternDateCommand.matcher(line);
        if (matcher.find()) {
            String dateStr = matcher.group(1);
            String commandStr = matcher.group(2);
            LocalDate date = Optional.ofNullable(dateStr)
                    .map(it -> LocalDate.parse(it, DATE_FORMATTER))
                    .orElse(LocalDate.now());
            return createCommand(date, commandStr);
        } else {
            throw new IllegalCommandsArgumentsException();
        }
    }

    private Command createCommand(LocalDate date, String commandLine) {
        try {
            String command;
            String argLine = "";
            int idx = commandLine.indexOf(" ");
            if (idx > 0) {
                command = commandLine.substring(0, idx);
                argLine = commandLine.substring(idx).trim();
            } else {
                command = commandLine;
            }
            CommandEnum enumValue = CommandEnum.valueOf(command);
            switch (enumValue) {
                case exit:
                    return new ExitCommand();
                case help:
                    return new HelpCommand();
                case borrow:
                    return new BorrowCommand(splitCalculator, date, argLine);
                case repay:
                    return new RepayCommand(splitCalculator, date, argLine);
                case balance:
                    return new BalanceCommand(splitCalculator, date, argLine);
                case group:
                    return new GroupCommand(splitCalculator, argLine);
                case purchase:
                    return new PurchaseCommand(splitCalculator, date, argLine);
                case secretSanta:
                    return new SecretSantaCommand(splitCalculator, argLine);
                case cashBack:
                    return new CashBackCommand(splitCalculator, date, argLine);
                case writeOff:
                    return new WriteOffCommand(splitCalculator, date);
                case balancePerfect:
                    return new BalancePerfectCommand(splitCalculator, date, argLine);

                default:
                    throw new UnknownCommandException();
            }
        } catch (IllegalArgumentException e) {
            throw new UnknownCommandException();
        }
    }
}
