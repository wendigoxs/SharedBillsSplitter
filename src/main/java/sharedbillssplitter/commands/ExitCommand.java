package sharedbillssplitter.commands;

import sharedbillssplitter.exceptions.GracefullyExitException;

public class ExitCommand implements Command {

    public ExitCommand() {
    }

    @Override
    public String process() {
        throw new GracefullyExitException();
    }
}
