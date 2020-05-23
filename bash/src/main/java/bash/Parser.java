package bash;

import java.util.ArrayList;
import java.util.List;

// Parser обрабатывает строки по определенным правилам.
public class Parser {

    private final Environment environment;

    // конструктор
    public Parser(Environment env) {
        environment = env;
    }

    // Заменяет $var на переменную окружения, если она создавалась.
    // Если переменной нет -- возвращает null. Если выражение '$var' -- в одинарных
    // кавычках, то замены не происходит.
    public String substituteVars(String line) {
        String var = "";
        String newLine = line;
        boolean flag = false;
        boolean openQuoting = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\'') {
                openQuoting = !openQuoting;
            }
            if (flag == true & i == line.length() - 1 & c != '\"') {
                var += c;
            }
            if (flag == true & (c == ' ' | c == '\"' | i == line.length() - 1)) {
                flag = false;
                if (environment.getVar(var) == null) {
                    return null;
                } else {
                    newLine = newLine.replace('$' + var, environment.getVar(var));
                    var = "";
                }
            }
            if (flag == true) {
                var += c;
            }
            if (c == '$' & !openQuoting) {
                flag = true;
            }
        }
        return newLine;
    }

    // Принимает строку, разделяет её на слова по пробельным символам,
    // причем все пробельные символы вокруг слов -- тоже удаляются.
    // Возвращает массив слов.
    public String[] splitByWhitespace(String line) {
        String[] splits = line.split("\\s+");
        List<String> list = new ArrayList<String>();
        for (String str : splits) {
            String result = "";
            for (char c : str.toCharArray()) {
                if (c != ' ') {
                    result += c;
                }
            }
            if (!result.equals("")) {
                list.add(result);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    // принимает строку, разделяет её по "|" и возвращает массив строк
    public String[] splitByPipes(String line) {
        return line.split("\\|");
    }

    // Проверяет: является ли строка выражением, которое добавляет переменную в
    // bash. Выражение не должно содержать пробельных символов, должно содержать знак равно,
    // и при split("=") распадаться на две части: причем первая часть должна начинаться с
    // латинской буквы.
    public boolean parseAddVar(String line) {
        for (char c : line.toCharArray()) {
            if (c == ' ' | c == '\t') {
                return false;
            }
        }
        String[] splits = line.split("=");
        if (splits.length != 2 || splits[0].length() == 0 || splits[0].matches("$[a-zA-Z]")) {
            return false;
        }
        environment.addVar(splits[0], splits[1]);
        return true;
    }

    // Принимает строку и удаляет кавычки -- двойные и одинарные. Если кавычек
    // (тех или других) нечетное количество, то возвращается null, так как
    // считается, что в этом случае выражение -- неверное.
    public String removeQuoting(String line) {
        int counter1 = 0;
        int counter2 = 0;
        String newLine = "";
        for (char c : line.toCharArray()) {
            if (c == '\'') {
                counter1 += 1;
            } else if (c == '\"') {
                counter2 += 1;
            } else {
                newLine += c;
            }
        }
        if (counter1 % 2 != 0 | counter2 % 2 != 0) {
            return null;
        }
        return newLine;
    }
}
