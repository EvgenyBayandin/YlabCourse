package ru.ylab.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OutputWriterTest {

    private OutputWriter outputWriter;

    @BeforeEach
    void setUp() {
        outputWriter = Mockito.mock(OutputWriter.class);
    }


    @Test
    void testPrintLine() {
        String message = "Hello, world!";

        outputWriter.printLine(message);

        Mockito.verify(outputWriter).printLine(message);
    }

    @Test
    void testPrint() {
        String message = "Hello, world!";

        outputWriter.print(message);

        Mockito.verify(outputWriter).print(message);
    }
}

