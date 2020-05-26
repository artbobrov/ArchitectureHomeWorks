package bash;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class TestBash {

    private CommandLine cl;
    private Parser parser;
    private Environment env = new Environment();
    
    @Before
    public void setUp() {
        cl = new CommandLine();
        parser = new Parser(new Environment());
        env = new Environment();
    }

    @Test
    public void testLsCommand() throws IOException {
        CommandExecutor executor = new CommandExecutor(cl);

        String result = executor.executeCommand("ls", new String[0], "", 0);
        assertFalse(result.isEmpty());
        assertFalse(result.contains("error:"));
    }


    @Test
    public void testCDCommand() throws IOException {
        CommandExecutor executor = new CommandExecutor(cl);

        String result = executor.executeCommand("cd", Collections.singletonList("/").toArray(new String[0]), "", 0);
        assertTrue(result.isEmpty());
    }
}
