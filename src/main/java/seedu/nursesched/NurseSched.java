package seedu.nursesched;

import java.util.Scanner;

import seedu.nursesched.command.Command;
import seedu.nursesched.ui.Ui;

public class NurseSched {
    private final Ui ui;

    public NurseSched() {
        ui = new Ui();
    }

    public void run() {
        boolean isExit = false;
        Scanner in = new Scanner(System.in);
        ui.showGreetingMessage();

        Command.executeCommands(isExit, in, ui);
    }

    public static void main(String[] args) {
        new NurseSched().run();
    }
}
