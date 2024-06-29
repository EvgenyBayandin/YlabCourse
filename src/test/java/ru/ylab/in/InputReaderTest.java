package ru.ylab.in;

import java.util.Scanner;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class InputReaderTest {
    private InputReader inputReader;
    private Supplier<Scanner> scannerSupplier;

    @BeforeEach
    public void setup() {
        scannerSupplier = () -> {
            Scanner mockScanner = Mockito.mock(Scanner.class);
            when(mockScanner.nextLine()).thenReturn("Hello, World!");
            when(mockScanner.nextInt()).thenReturn(42);
            return mockScanner;
        };
    }

    @Test
    public void testReadLine() {
        Scanner scanner = scannerSupplier.get();
        inputReader = new InputReader(scanner);
        String actualLine = inputReader.readLine();
        assertEquals("Hello, World!", actualLine);
    }

    @Test
    public void testReadInt() {
        Scanner scanner = scannerSupplier.get();
        inputReader = new InputReader(scanner);
        int actualInt = inputReader.readInt();
        assertEquals(42, actualInt);
    }
}
