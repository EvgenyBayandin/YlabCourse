package ru.ylab.in;

import java.util.Scanner;

public class InputReader implements ConsoleReader{
    private Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine()  {
        return scanner.nextLine();
    }

    public int readInt()   {
        return scanner.nextInt();
    }

    @Override
    public Integer readIntSafely() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
