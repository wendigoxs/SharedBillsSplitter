package sharedbillssplitter.exceptions;

public class UnknownCommandException extends RuntimeException {
    private static final String UNKNOWN_COMMAND = "Unknown command. Print help to show commands list";

    @Override
    public String toString() {
        return UNKNOWN_COMMAND;
    }
}
