package bash;

import java.util.HashMap;

// Environment хранит переменные окружения в hash map и
// умеет их добавлять, удалять и возвращать значение
public class Environment {

    private final HashMap<String, String> variables;

    // конструктор
    public Environment() {
        variables = new HashMap<>();
    }

    // добавляет переменную var со значением value
    public void addVar(String var, String value) {
        variables.put(var, value);
    }

    // удаляет переменную по её имени
    public void removeVar(String var) {
        variables.remove(var);
    }

    // возвращает значение переменной
    public String getVar(String var) {
        if (variables.containsKey(var)) {
            return variables.get(var);
        }
        return null;
    }
}
