package com.vbforge;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloWorldTest {

    @Test
    public void testMainOutput() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        HelloWorld.main(new String[]{});

        assertEquals("Hello World!!!" + System.lineSeparator(), outContent.toString());
    }

    @Test
    public void testMainDoesNotThrowException() {
        assertDoesNotThrow(() -> HelloWorld.main(new String[]{}));
    }

    @Test
    public void testMainWithArguments() {
//        assertDoesNotThrow(() -> HelloWorld.main(new String[]{"arg1", "arg2", "arg3", "arg4", "arg5"}));
        assertThrow(() -> HelloWorld.main(new String[]{"arg1", "arg2", "arg3", "arg4", "arg5"}));
    }

}