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


/**
 * Main class for the Coworking Service application.
 * This class initializes all necessary components and starts the application.
 */
public class CoworkingServiceApp {


    /**
     * The main method that serves as the entry point for the application.
     *
     * @param args command line arguments (not used in this application)
     */
    public static void main (String[]args){

        try {
            initializeDatabase();
            InputReader inputReader = new InputReader(new Scanner(System.in));
            OutputWriter outputWriter = new OutputWriter();

            DatabaseManager databaseManager = new DatabaseManager() {
            };
            UserRepository userRepository = new UserRepository(databaseManager);
            ResourceRepository resourceRepository = new ResourceRepository(databaseManager);
            BookingRepository bookingRepository = new BookingRepository(databaseManager, userRepository, resourceRepository);

            UserService userService = new UserService(userRepository);
            BookingService bookingService = new BookingService(bookingRepository, userRepository, resourceRepository);
            ResourceService resourceService = new ResourceService(resourceRepository, bookingService);
            AuthenticationService authenticationService = new AuthenticationService(userRepository);

            ConsoleUI consoleUI = new ConsoleUI(inputReader, outputWriter, userService, resourceService, bookingService, authenticationService);

            outputWriter.printLine("Welcome to Coworking Service!");
            consoleUI.run();
        } catch (Exception e) {
            System.err.println("An error occurred while starting the application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            DatabaseManager.stopAndRemoveContainer();
        }
    }

    /**
     * Initializes the database using the DatabaseManager.
     *
     * @throws Exception if there's an error initializing the database
     */
    private static void initializeDatabase () throws Exception {
        DatabaseManager.initializeDatabase();
    }

}
