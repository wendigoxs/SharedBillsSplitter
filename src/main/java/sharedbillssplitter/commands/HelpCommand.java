package sharedbillssplitter.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HelpCommand implements Command {

    public HelpCommand() {

    }

    @Override
    public String process() {
        return Arrays.stream(CommandEnum.values())
                .map(Enum::toString)
                .sorted()
                .collect(Collectors.joining("\n"));
    }
}
