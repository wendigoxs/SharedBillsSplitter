package sharedbillssplitter.exceptions;

public class IllegalCommandsArgumentsException extends RuntimeException {
    private static final String ILLEGAL_COMMAND_ARGUMENTS = "Illegal command arguments";

    public IllegalCommandsArgumentsException(String message) {
        super(message);
    }

    public IllegalCommandsArgumentsException() {
        super();
    }

    @Override
    public String toString() {
        String msg = getMessage();
        return msg == null ? ILLEGAL_COMMAND_ARGUMENTS
                : String.format("%s: %s", ILLEGAL_COMMAND_ARGUMENTS, msg);
    }
}
