package bash;

import java.io.IOException;
import java.util.Arrays;

// LineExecutor -- посредник между командной строкой и исполнителем команд.
// Отправляет строки парсеру (Parser), обрабатывает результаты
// перед тем как отдать команды исполнителю команд.
public class LinesExecutor {

    private final Parser parser;
    private final CommandExecutor command;

    // конструктор
    public LinesExecutor(Environment env, CommandLine cl) {
        parser = new Parser(env);
        command = new CommandExecutor(cl);
    }

    // обрабатывает строки по такому алгоритму:
    // сначала заменяет переменные (которые не в одинарных кавычках),
    // затем удаляет кавычки, проверяет -- является ли выражение присваиванием,
    // если да, то возвращает результат
    // если нет, то разделяет строку по "|" и отправляет по порядку
    // получившиеся команды на исполнение. Сохраняет результат их выполнения
    // и подает этот результат на вход следуещей команде
    public String execute(String line) throws IOException {
        line = parser.substituteVars(line);
        if (line == null) {
            return "error expression";
        }
        line = parser.removeQuoting(line);
        if (line == null) {
            return "error expression";
        }
        boolean isAddVar = parser.parseAddVar(line);
        if (isAddVar) {
            return "add variable successful";
        }
        if (line == null) {
            return "error variables";
        }
        String[] commandsStr = parser.splitByPipes(line);
        String result = null;
        int counter = 1;
        for (String comm : commandsStr) {
            String[] splitComm = parser.splitByWhitespace(comm);
            String newCommand = splitComm[0];
            String[] args = Arrays.copyOfRange(splitComm, 1, splitComm.length);
            result = command.executeCommand(newCommand, args, result, counter);
            counter += 1;
        }
        return result;
    }
}
