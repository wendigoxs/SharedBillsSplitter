package sharedbillssplitter.commands;

import sharedbillssplitter.SplitCalculator;
import sharedbillssplitter.exceptions.IllegalCommandsArgumentsException;
import sharedbillssplitter.person.GroupService;
import sharedbillssplitter.person.Person;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupCommand implements Command {
    private static final int REQUIRED_ARGS_AT_LEAST = 2;
    private final SplitCalculator splitCalculator;
    private final String argLine;

    enum GroupOperationEnum {
        create,
        show,
        add,
        remove
    }

    public GroupCommand(SplitCalculator splitCalculator, String argLine) {
        this.splitCalculator = splitCalculator;
        this.argLine = argLine;
    }

    @Override
    public String process() {
        GroupService groupService = new GroupService(splitCalculator.getGroupStorage());

        Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)(?:\\s*\\(([^)]+)\\))?"); // group [create|show] GROUPNAME [(list of person)]
        Matcher matcher = pattern.matcher(argLine);
        if (!matcher.find() || matcher.groupCount() < REQUIRED_ARGS_AT_LEAST) {
            throw new IllegalCommandsArgumentsException();
        }
        String groupCommand = matcher.group(1).trim();
        GroupOperationEnum groupOperation = GroupOperationEnum.valueOf(groupCommand);
        String groupName = matcher.group(2).trim();
        if (!groupService.isGroup(groupName)) {
            throw new IllegalCommandsArgumentsException("group name should be UPPERCASE");
        }
        Supplier<Collection<Person>> parsePersons = () -> groupService.parsePersonHoldersString(matcher.group(3));

        switch (groupOperation) {
            case create:
                return groupService.createGroup(groupName, parsePersons.get());
            default:
            case show:
                return groupService.showGroup(groupName);
            case add:
                return groupService.addToGroup(groupName, parsePersons.get());
            case remove:
                return groupService.removeFromGroup(groupName, parsePersons.get());
        }
    }


}
