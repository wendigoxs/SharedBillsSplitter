package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;

import java.time.LocalDate;

public class WriteOffCommand implements Command {

    private static final String EMPTY_STRING = "";

    private final SplitCalculator splitCalculator;
    private final LocalDate date;

    public WriteOffCommand(SplitCalculator splitCalculator, LocalDate date) {
        this.splitCalculator = splitCalculator;
        this.date = date;

    }

    @Override
    public String process() {
        splitCalculator.writeOff(date);
        return EMPTY_STRING;
    }

}
