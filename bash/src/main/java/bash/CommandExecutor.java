package bash;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// CommandExecutor исполняет команды
public class CommandExecutor {

    private final Parser parser;
    private final CommandLine commandL;

    // конструктор
    public CommandExecutor(CommandLine cl) {
        parser = new Parser(new Environment());
        commandL = cl;
    }

    // В зависимости от того, какая команда пришла на вход -- выбирает метод, который её исполнит
    public String executeCommand(String command, String[] args, String resultOfLastCommand, int pipePosition) throws IOException {
        switch (command) {
            case ("cat"):
                return executeCat(args, resultOfLastCommand, pipePosition);
            case ("echo"):
                return executeEcho(args);
            case ("wc"):
                return executeWc(args, resultOfLastCommand, pipePosition);
            case ("pwd"):
                return executePwd(args);
            case ("exit"):
                return executeExit(args);
            case ("grep"):
                return executeGrep(args, resultOfLastCommand, pipePosition);
            case ("ls"):
                return executeLs(args);
            case ("cd"):
                return executeCd(args);
            default:
                String newCommand = command + " ";
                if (args.length == 0 & pipePosition > 1) {
                    command += resultOfLastCommand;
                } else {
                    for (String arg : args) {
                        newCommand = newCommand + arg + " ";
                    }
                }
                return executeOtherCommand(newCommand);
        }
    }

    // Выполняет команду cd. Принимает 0 или 1 аргумент. Если 0 аргументов, то выводится содержиное текущей директории.
    public String executeLs(String[] args) {
        if (args.length > 1) {
            return "error: arguments for ls";
        }
        String path;
        if (args.length == 0) {
            path = "";
        } else {
            path = args[0];
        }

        File dir = createFile(path);
        String[] dirList = dir.list();
        if (dirList == null)
            return "error: invalid path";

        final List<String> strings = new ArrayList<>(Arrays.asList(dirList));
        return String.join("\n", strings);
    }

    // Выполняет команду cd. Принимает ровно 1 аргумент
    public String executeCd(String[] args) throws IOException {
        if (args.length != 1) {
            return "error: arguments for cd";
        }
        File dir = createFile(args[0]);

        if (!dir.exists())
            return "error:" + dir + " does not exists";
        if (dir.isDirectory()) {
            System.setProperty("user.dir", dir.getCanonicalPath());
        } else {
            return "error: arguments for cd " + args[0] + " is not a directory.";
        }
        return "";
    }

    // Выполняет команду cat. Если команда не первая в очереди pipe и у нее нет аргументов -- то
    // в качестве входа ей подается результат предыдущей команды.
    public String executeCat(String[] args, String resultOfLastCommand, int pipePosition) {
        if (args.length == 0 & pipePosition > 1) {
            return resultOfLastCommand;
        }
        if (args.length < 1) {
            return "error arguments for cat";
        }
        File file = createFile(args[0]);
        String result = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                result += text;
                result += "\n";
            }
        } catch (FileNotFoundException e) {
            return "cat: file does not exist";
        } catch (IOException e) {
            return "catch IO exception";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return result;
    }

    // Выполняет команду echo.
    public String executeEcho(String[] args) {
        String result = "";
        for (int i = 0; i < args.length; i++) {
            result += args[i];
            if (i != args.length - 1) {
                result += " ";
            }
        }
        return result;
    }

    // Выполняет команду wc. Если команда не первая в очереди pipe и у нее нет аргументов -- то
    // в качестве входа ей подается результат предыдущей команды.
    public String executeWc(String[] args, String resultOfLastCommand, int pipePosition) {
        String result = "";
        long linesCount = 0;
        long wordsCount = 0;
        long bytesCount = 0;
        if (args.length == 0 & pipePosition > 1) {
            int counter = 0;
            for (char c : resultOfLastCommand.toCharArray()) {
                if (c == '\n') {
                    counter += 1;
                }
            }
            bytesCount += counter;
            String[] splitArgs = resultOfLastCommand.split("\n");
            linesCount = splitArgs.length;
            for (String str : splitArgs) {
                bytesCount += str.length();
                String[] splits = parser.splitByWhitespace(str);
                wordsCount += splits.length;
            }
            result = result + linesCount + " " + wordsCount + " " + bytesCount;
            return result;
        }
        if (args.length < 1) {
            return "error arguments for wc";
        }
        File file = createFile(args[0]);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                linesCount += 1;
                String[] arr = text.split("\\s+");
                if (arr.length != 0) {
                    if (!arr[0].equals("")) {
                        wordsCount += arr.length;
                    }
                }
            }
            bytesCount = file.length();
        } catch (FileNotFoundException e) {
            return "cat: file does not exist";
        } catch (IOException e) {
            return "catch IO exception";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        result = result + linesCount + " " + wordsCount + " " + bytesCount;
        return result;
    }

    // Выполняет команду pwd
    public String executePwd(String[] args) {
        return System.getProperty("user.dir");
    }

    // Выполняет команду exit.
    // Если у exit есть какие-то аргументы -- то ничего не делает.
    // Иначе вызывает метод exit() у командной строки.
    public String executeExit(String[] args) {
        if (args.length != 0) {
            return "error args of command exit";
        } else {
            commandL.exit();
            return "exit";
        }
    }

    // Выполняет команду bash
    // Возвращает её output в виде строки.
    public String executeOtherCommand(String command) throws IOException {
        String result = "";
        Process process = new ProcessBuilder("bash", "-c", command).start();
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = errorReader.readLine()) != null) {
            result += line;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = reader.readLine()) != null) {
            result += line;
        }
        return result;
    }

    // Выполняет команду grep. Если команда не первая в очереди pipe и у нее нет аргументов -- то
    // в качестве входа ей подается результат предыдущей команды.
    // Ключи -w, -i, -A могут стоять между аргументами в любом месте и в любом порядке.
    // Возвращает её output в виде строки.
    public String executeGrep(String[] args, String resultOfLastCommand, int pipePosition) {
        String result = "";
        Options options = new Options();
        options.addOption("w", false, "Select  only  those  lines  containing  matches that form whole words");
        options.addOption("i", false, "Ignore case");
        options.addOption("A", true, "print n last lines after find line");
        org.apache.commons.cli.CommandLine cmd;
        CommandLineParser parserCL = new DefaultParser();
        try {
            cmd = parserCL.parse(options, args);
        } catch (ParseException | NumberFormatException e) {
            return "catch grep parse exception";
        }
        String stringToParse = "";
        if (cmd.getArgs().length == 1 & pipePosition > 1) {
            stringToParse = resultOfLastCommand;
        } else {
            if (cmd.getArgs().length != 2) {
                return "error args for grep";
            }
            File file = createFile(cmd.getArgs()[1]);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String text = null;
                while ((text = reader.readLine()) != null) {
                    stringToParse += text;
                    stringToParse += System.lineSeparator();
                }
            } catch (FileNotFoundException e) {
                return "grep: file does not exist";
            } catch (IOException e) {
                return "catch IO exception";
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                }
            }
        }

        boolean ignoreCase = false;
        boolean selectWords = false;
        boolean printLastStrings = false;
        int optArg = 0;

        for (Option opt : cmd.getOptions()) {
            if ("i".equals(opt.getOpt())) {
                ignoreCase = true;
            }
            if ("w".equals(opt.getOpt())) {
                selectWords = true;
            }
            if ("A".equals(opt.getOpt())) {
                optArg = Integer.parseInt(opt.getValue());
                printLastStrings = true;
            }
        }

        String redex = cmd.getArgs()[0];

        if (ignoreCase) {
            stringToParse = stringToParse.toLowerCase();
            redex = redex.toLowerCase();
        }

        Pattern pattern = Pattern.compile(redex);
        Matcher matcher = pattern.matcher(stringToParse);
        List<String> foundMatches = new ArrayList<String>();
        if (matcher.find()) {
            for (int i = 0; i <= matcher.groupCount(); i++) {
                foundMatches.add(matcher.group(i));
            }
        }

        String[] splitStrings = stringToParse.split(System.lineSeparator());
        for (int i = 0; i < splitStrings.length; i++) {
            String str = splitStrings[i];
            for (String s : foundMatches) {
                if (!selectWords) {
                    if (str.contains(s)) {
                        result += str;
                        result += System.lineSeparator();
                        if (printLastStrings) {
                            for (int j = i + 1; j < i + optArg + 1; j++) {
                                if (j >= 0 & j < splitStrings.length) {
                                    result += splitStrings[j];
                                    result += System.lineSeparator();
                                }
                            }
                        }
                    }
                } else {
                    String[] splits = parser.splitByWhitespace(str);
                    for (String st : splits) {
                        if (s.equals(st)) {
                            result += str;
                            result += System.lineSeparator();
                            if (printLastStrings) {
                                for (int j = i + 1; j < i + optArg + 1; j++) {
                                    if (j >= 0 & j < splitStrings.length) {
                                        result += splitStrings[j];
                                        result += System.lineSeparator();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private File createFile(String path) {
        String currentDir = System.getProperty("user.dir");
        return new File(currentDir, path);
    }
}
