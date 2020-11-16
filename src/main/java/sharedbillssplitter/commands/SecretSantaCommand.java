package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;
import sharedbillssplitter.exceptions.IllegalCommandsArgumentsException;
import sharedbillssplitter.person.GroupService;

import java.util.stream.Collectors;

public class SecretSantaCommand implements Command {
    public static final int REQUIRED_ARGS_AT_LEAST = 1;
    private final SplitCalculator splitCalculator;
    private final String argLine;

    public SecretSantaCommand(SplitCalculator splitCalculator, String argLine) {
        this.splitCalculator = splitCalculator;
        this.argLine = argLine;
    }

    @Override
    public String process() {
        GroupService groupService = new GroupService(splitCalculator.getGroupStorage());
        String[] tokens = argLine.split("\\s+");
        if (tokens.length < REQUIRED_ARGS_AT_LEAST) {
            throw new IllegalCommandsArgumentsException();
        }
        String groupName = tokens[0].trim();
        if (!groupService.isGroup(groupName)) {
            throw new IllegalCommandsArgumentsException("group name should be UPPERCASE");
        }
        String result = groupService.secretSantaShuffle(groupService.getGroupPersons(groupName))
                .entrySet().stream()
                .map(it -> String.format("%s gift to %s", it.getKey(), it.getValue()))
                .collect(Collectors.joining("\n"));
        return result;
    }
}
