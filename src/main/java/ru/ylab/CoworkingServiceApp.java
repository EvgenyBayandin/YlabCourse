package ru.ylab;

import java.util.Scanner;
import ru.ylab.in.InputReader;
import ru.ylab.util.DatabaseManager;
import ru.ylab.out.OutputWriter;
import ru.ylab.repository.BookingRepository;
import ru.ylab.repository.ResourceRepository;
import ru.ylab.repository.UserRepository;
import ru.ylab.service.AuthenticationService;
import ru.ylab.service.BookingService;
import ru.ylab.service.ResourceService;
import ru.ylab.service.UserService;
import ru.ylab.ui.ConsoleUI;

public class CoworkingServiceApp {
    public static void main(String[] args) {
        try {
            DatabaseManager.initializeDatabase();
            Scanner scanner = new Scanner(System.in);
            InputReader inputReader = new InputReader(scanner);
            OutputWriter outputWriter = new OutputWriter();

            UserRepository userRepository = new UserRepository(new DatabaseManager() {
            });
            ResourceRepository resourceRepository = new ResourceRepository();
            BookingRepository bookingRepository = new BookingRepository();

            UserService userService = new UserService(userRepository);
            BookingService bookingService = new BookingService(bookingRepository);
            ResourceService resourceService = new ResourceService(resourceRepository, bookingService);
            AuthenticationService authenticationService = new AuthenticationService(userRepository);

            outputWriter.printLine("Welcome to Coworking service!");

            ConsoleUI consoleUI = new ConsoleUI(inputReader, outputWriter, userService, resourceService, bookingService, authenticationService);

            consoleUI.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
