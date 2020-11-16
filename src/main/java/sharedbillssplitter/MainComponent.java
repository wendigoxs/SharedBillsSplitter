package sharedbillssplitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sharedbillssplitter.commands.Command;
import sharedbillssplitter.commands.CommandFactory;
import sharedbillssplitter.exceptions.GracefullyExitException;
import sharedbillssplitter.exceptions.IllegalCommandsArgumentsException;
import sharedbillssplitter.exceptions.UnknownCommandException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class MainComponent implements CommandLineRunner {

    @Autowired
    private CommandFactory commandFactory;

    private void mainProcessingLoop() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String line = reader.readLine();
                try {
                    processInput(line);
                } catch (GracefullyExitException ex) {
                    return;
                }
            }
        }
    }

    private void processInput(String line) {
        try {
            Command command = commandFactory.createCommand(line);
            String result = command.process();
            printResult(result);
        } catch (IllegalCommandsArgumentsException | UnknownCommandException ex) {
            System.out.println(ex);
        }
    }

    private static void printResult(String result) {
        if (!result.isEmpty()) {
            System.out.println(result);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        mainProcessingLoop();
    }
}
