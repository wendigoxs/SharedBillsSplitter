package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;

import java.time.LocalDate;

public class BalancePerfectCommand extends BalanceCommand {

    public BalancePerfectCommand(SplitCalculator splitCalculator, LocalDate date, String argLine) {
        super(splitCalculator, date, argLine);
    }

    @Override
    protected String processBalanceClose() {
        return splitCalculator.balancePerfectClose(date, persons);
    }
}
