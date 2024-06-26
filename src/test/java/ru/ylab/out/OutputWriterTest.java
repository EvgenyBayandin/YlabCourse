package ru.ylab.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Тестовый класс для {@link OutputWriter}.
 * Проверяет работу класса.
 */

class OutputWriterTest {

    private OutputWriter outputWriter;

    @BeforeEach
    void setUp() {
        outputWriter = Mockito.mock(OutputWriter.class);
    }

    /**
     * Проверяет метод вывода строки с переносом каретки.
     */

    @Test
    void testPrintLine() {
        String message = "Hello, world!";

        outputWriter.printLine(message);

        Mockito.verify(outputWriter).printLine(message);
    }

    /**
     * Проверяет метод вывода строки без переноса каретки.
     */

    @Test
    void testPrint() {
        String message = "Hello, world!";

        outputWriter.print(message);

        Mockito.verify(outputWriter).print(message);
    }
}

