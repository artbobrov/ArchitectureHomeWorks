package bash;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
    public void testAdd() {
    }
}
