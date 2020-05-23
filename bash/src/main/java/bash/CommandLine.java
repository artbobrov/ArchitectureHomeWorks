package bash;

import java.io.IOException;
import java.util.Scanner;

// Класс командной строки. Считывает новые команды и выводит их результат.
public class CommandLine {

    private boolean execute;
    private final Environment environment;
    private final LinesExecutor linesExec;

    // конструктор
    public CommandLine() {
        execute = false;
        environment = new Environment();
        linesExec = new LinesExecutor(environment, this);
    }

    // Запускает бесконечный цикл, читает в этом цикле строки,
    // которые затем идут на обработку в LinesExecutor,
    // результат обработки команды -- строка, которая выводится
    // в интерпретатор. Цикл останавлиается вызовом метода exit().
    public void run() throws IOException {
        Scanner in = new Scanner(System.in);
        execute = true;
        System.out.printf("> ");
        while (execute) {
            String newLine = in.nextLine();
            if (!newLine.isEmpty()) {
                String result = linesExec.execute(newLine);
                if (!result.equals("")) {
                    System.out.println(result);
                }
                if (result.equals("exit")) {
                    exit();
                }
            }
            System.out.printf("> ");
        }
    }

    // Устанавливает переменной execute значение false
    public void exit() {
        execute = false;
    }
}
