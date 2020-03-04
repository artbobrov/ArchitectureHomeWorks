package bash;

import java.io.IOException;

// Run запускает интерпретатор командной строки
public class Run {

    public static void main(String [] args) throws IOException {
        CommandLine commandL = new CommandLine();
        commandL.run();
    }
}