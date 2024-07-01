package ru.ylab.ui;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import ru.ylab.in.InputReader;
import ru.ylab.model.*;
import ru.ylab.out.OutputWriter;
import ru.ylab.service.AuthenticationService;
import ru.ylab.service.BookingService;
import ru.ylab.service.ResourceService;
import ru.ylab.service.UserService;

/**
 * ConsoleUI class represents the user interface for the resource booking system.
 * It handles user interactions through a console-based menu system.
 */
public class ConsoleUI {
    private InputReader inputReader;
    private OutputWriter outputWriter;
    private UserService userService;
    private ResourceService resourceService;
    private BookingService bookingService;
    private AuthenticationService authenticationService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Constructs a ConsoleUI with necessary services and I/O handlers.
     *
     * @param inputReader           Input reader for user input
     * @param outputWriter          Output writer for displaying information
     * @param userService           Service for user-related operations
     * @param resourceService       Service for resource-related operations
     * @param bookingService        Service for booking-related operations
     * @param authenticationService Service for authentication operations
     */
    public ConsoleUI(InputReader inputReader, OutputWriter outputWriter, UserService userService, ResourceService resourceService, BookingService bookingService, AuthenticationService authenticationService) throws SQLException {
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
        this.userService = userService;
        this.resourceService = new ResourceService(resourceService.getResourceRepository(), bookingService);
        this.bookingService = bookingService;
        this.authenticationService = authenticationService;
    }

    /**
     * Starts the console user interface.
     * This method runs the main loop of the application, handling login and main menu operations.
     *
     * @throws SQLException if a database access error occurs
     */
    public void run() throws SQLException {
        while (true) {
            if (!authenticationService.isAuthenticated()) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    /**
     * Displays the login menu and handles user input for login operations.
     */
    private void showLoginMenu() {
        while (true) {
            outputWriter.printLine("1. Login");
            outputWriter.printLine("2. Register");
            outputWriter.printLine("3. Exit");
            outputWriter.print("Choose an option: ");

            Integer option = inputReader.readIntSafely();
            if (option == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            switch (option) {
                case 1:
                    login();
                    return;
                case 2:
                    register();
                    return;
                case 3:
                    System.exit(0);
                default:
                    outputWriter.printLine("Invalid option. Please try again");
            }
        }
    }

    /**
     * Displays the main menu and handles user input for various operations.
     *
     * @throws SQLException if a database access error occurs
     */
    private void showMainMenu() throws SQLException {
        while (true) {
            if (!authenticationService.isAuthenticated()) {
                return;
            }
            outputWriter.printLine("1. View available resources");
            outputWriter.printLine("2. Make a booking");
            outputWriter.printLine("3. View my bookings");
            outputWriter.printLine("4. Cancel a booking");
            outputWriter.printLine("5. View available time slots");
            outputWriter.printLine("6. Filter bookings");
            outputWriter.printLine("7. logout");
            if (authenticationService.getCurrentUser().isAdmin()) {
                outputWriter.printLine("8. Admin menu");
            }
            outputWriter.print("Choose an option:  ");

            Integer option = inputReader.readIntSafely();
            if (option == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            switch (option) {
                case 1:
                    viewAvailableResources();
                    break;
                case 2:
                    makeBooking();
                    break;
                case 3:
                    viewMyBookings();
                    break;
                case 4:
                    cancelBooking();
                    break;
                case 5:
                    viewAvailableTimeSlots();
                    break;
                case 6:
                    filterBookings();
                    break;
                case 7:
                    authenticationService.logout();
                    outputWriter.printLine("Logged out successfully.");
                    return;
                case 8:
                    if (authenticationService.getCurrentUser().isAdmin()) {
                        showAdminMenu();
                    } else {
                        outputWriter.printLine("Invalid option. Please try again");
                    }
                    break;
                default:
                    outputWriter.printLine("Invalid option. Please try again");
            }
        }
    }

    /**
     * Handles the user login process.
     */
    private void login() {
        outputWriter.print("Enter username: ");
        String username = inputReader.readLine().trim();

        outputWriter.print("Enter password: ");
        String password = inputReader.readLine().trim();

        try {
            User user = authenticationService.authenticate(username, password);
            outputWriter.printLine("Login successful. Welcome, " + user.getUsername());
        } catch (IllegalArgumentException e) {
            outputWriter.printLine("Login failed: " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the user registration process.
     */
    private void register() {
        outputWriter.print("Enter username: ");
        String username = inputReader.readLine().trim();
        outputWriter.print("Enter password: ");
        String password = inputReader.readLine().trim();

        try {
            User user = userService.register(username, password);
            outputWriter.printLine("Registration successful. Please login.");
        } catch (IllegalArgumentException e) {
            outputWriter.printLine("Registration failed: " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Displays available resources for a specified time period.
     */
    private void viewAvailableResources() {
        outputWriter.print("Enter start date and time (yyyy-MM-dd HH:mm): ");
        String startStr = inputReader.readLine().trim();
        outputWriter.print("Enter end date and time (yyyy-MM-dd HH:mm): ");
        String endStr = inputReader.readLine().trim();

        try {
            LocalDateTime start = LocalDateTime.parse(startStr, DATE_TIME_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endStr, DATE_TIME_FORMATTER);

            List<Resource> availableResources = resourceService.getAvailableResources(start, end);
            if (availableResources.isEmpty()) {
                outputWriter.printLine("No available resources for the selected time period.");
            } else {
                outputWriter.printLine("Available resources:");
                for (Resource resource : availableResources) {
                    outputWriter.printLine(resource.getId() + ": " + resource.getName() +
                            " (Type: " + resource.getClass().getSimpleName() +
                            ", Capacity: " + resource.getCapacity() + ")");
                }
            }
        } catch (DateTimeParseException e) {
            outputWriter.printLine("Invalid date/time format. Please use yyyy-MM-dd HH:mm.");
        } catch (Exception e) {
            outputWriter.printLine("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handles the process of making a new booking.
     */
    private void makeBooking() {
        outputWriter.print("Enter start date and time (yyyy-MM-dd HH:mm): ");
        String startStr = inputReader.readLine().trim();
        outputWriter.print("Enter end date and time (yyyy-MM-dd HH:mm): ");
        String endStr = inputReader.readLine().trim();

        try {
            LocalDateTime start = LocalDateTime.parse(startStr, DATE_TIME_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endStr, DATE_TIME_FORMATTER);

            List<Resource> availableResources = resourceService.getAvailableResources(start, end);
            if (availableResources.isEmpty()) {
                outputWriter.printLine("No resources available for the selected time period.");
                return;
            }

            outputWriter.printLine("Available resources:");
            for (Resource resource : availableResources) {
                outputWriter.printLine(resource.getId() +
                        ": " + resource.getName() +
                        " (Type: " + resource.getClass().getSimpleName() +
                        ", Capacity: " + resource.getCapacity() + ")");
            }

            outputWriter.print("Enter resource ID to book: ");
            int resourceId = inputReader.readInt();
            inputReader.readLine();

            Resource selectedResource = availableResources.stream()
                    .filter(r -> r.getId() == resourceId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid resource ID"));

            User currentUser = authenticationService.getCurrentUser();
            Booking booking = bookingService.createBooking(currentUser, selectedResource, start, end);
            outputWriter.printLine("Booking successful. Booking ID: " + booking.getId());
        } catch (Exception e) {
            outputWriter.printLine("Booking failed: " + e.getMessage());
        }
    }

    /**
     * Displays all bookings for the current user.
     *
     * @throws SQLException if a database access error occurs
     */
    private void viewMyBookings() throws SQLException {
        User currentUser = authenticationService.getCurrentUser();
        List<Booking> myBookings = bookingService.getBookingsByUser(currentUser);
        if (myBookings.isEmpty()) {
            outputWriter.printLine("You have no bookings.");
        } else {
            outputWriter.printLine("Your bookings:");
            for (Booking booking : myBookings) {
                outputWriter.printLine(
                        "ID: " + booking.getId() +
                                ", Resource: " + booking.getResource().getName() +
                                " (ID: " + booking.getResource().getId() + ")" +
                                ", From: " + booking.getStartTime().format(DATE_TIME_FORMATTER) +
                                ", To: " + booking.getEndTime().format(DATE_TIME_FORMATTER));
            }
        }
    }

    /**
     * Handles the process of canceling a booking.
     *
     * @throws SQLException if a database access error occurs
     */
    private void cancelBooking() throws SQLException {
        User currentUser = authenticationService.getCurrentUser();
        List<Booking> userBookings = bookingService.getBookingsByUser(currentUser);

        if (userBookings.isEmpty()) {
            outputWriter.printLine("You have no active bookings.");
            return;
        }

        viewMyBookings();

        while (true) {
            outputWriter.print("Enter booking ID to cancel (or 0 to go back): ");
            Integer bookingId = inputReader.readIntSafely();

            if (bookingId == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            if (bookingId == 0) {
                return;
            }

            try {
                Booking bookingToCancel = userBookings.stream()
                        .filter(b -> b.getId() == bookingId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

                outputWriter.printLine("Are you sure you want to cancel this booking?");
                outputWriter.printLine("Booking details: " + bookingToCancel.toString());
                outputWriter.print("Enter 'yes' to confirm or any other input to cancel: ");
                String confirmation = inputReader.readLine().trim().toLowerCase();

                if (confirmation.equals("yes")) {
                    bookingService.cancelBooking(bookingToCancel);
                    outputWriter.printLine("Booking cancelled successfully.");
                } else {
                    outputWriter.printLine("Booking cancellation cancelled.");
                }
                return;
            } catch (IllegalArgumentException e) {
                outputWriter.printLine("Booking not found. Please try again.");
            } catch (Exception e) {
                outputWriter.printLine("Cancellation failed: " + e.getMessage());
            }
        }
    }

    /**
     * Displays available time slots for a specific date and duration.
     */
    private void viewAvailableTimeSlots() {
        LocalDate date = null;
        while (date == null) {
            outputWriter.print("Enter date (yyyy-MM-dd) or 'back' to return: ");
            String dateStr = inputReader.readLine().trim();

            if (dateStr.equalsIgnoreCase("back")) {
                return;
            }

            try {
                date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                outputWriter.printLine("Invalid date format. Please use yyyy-MM-dd.");
            }
        }

        Integer duration = null;
        while (duration == null) {
            outputWriter.print("Enter duration in minutes (step 30 minutes) or 0 to go back: ");
            duration = inputReader.readIntSafely();

            if (duration == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            if (duration == 0) {
                return;
            }

            if (duration % 30 != 0 || duration <= 0) {
                outputWriter.printLine("Invalid duration. Please enter a positive multiple of 30.");
                duration = null;
            }
        }

        try {
            List<TimeSlot> availableSlots = resourceService.getAvailableTimeSlots(date, duration);

            if (availableSlots.isEmpty()) {
                outputWriter.printLine("No available slots for the selected date and duration.");
            } else {
                outputWriter.printLine("Available slots:");
                for (TimeSlot slot : availableSlots) {
                    outputWriter.printLine(slot.toString());
                }
            }
        } catch (Exception e) {
            outputWriter.printLine("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handles the process of filtering bookings based on various criteria.
     *
     * @throws SQLException if a database access error occurs
     */
    private void filterBookings() throws SQLException {
        while (true) {
            outputWriter.printLine("Filter bookings by:");
            outputWriter.printLine("1. Date");
            outputWriter.printLine("2. User");
            outputWriter.printLine("3. Resource");
            outputWriter.printLine("4. Show all bookings");
            outputWriter.printLine("5. Back to main menu");
            outputWriter.print("Choose an option: ");

            Integer option = inputReader.readIntSafely();
            if (option == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            List<Booking> bookings;

            switch (option) {
                case 1:
                    bookings = filterBookingsByDate();
                    break;
                case 2:
                    bookings = filterBookingsByUser();
                    if (bookings == null) {
                        continue;
                    }
                    break;
                case 3:
                    bookings = filterBookingsByResource();
                    if (bookings == null) {
                        continue;
                    }
                    break;
                case 4:
                    bookings = bookingService.getAllBookings();
                    break;
                case 6:
                    return;
                default:
                    outputWriter.printLine("Invalid option.");
                    return;
            }

            displayBookings(bookings);
        }
    }

    /**
     * Filters bookings by a specific date.
     * Prompts the user to enter a date and retrieves all bookings for that date.
     *
     * @return A list of bookings for the specified date, or an empty list if the date is invalid
     */
    private List<Booking> filterBookingsByDate() {
        outputWriter.print("Enter date (yyyy-MM-dd): ");
        String dateStr = inputReader.readLine().trim();
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return bookingService.getBookingsByDate(date);
        } catch (DateTimeParseException e) {
            outputWriter.printLine("Invalid date format. Please use yyyy-MM-dd.");
            return new ArrayList<>();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Filters bookings by a specific user.
     * Prompts the user to enter a username and retrieves all bookings for that user.
     *
     * @return A list of bookings for the specified user, or null if the user chooses to go back
     */
    private List<Booking> filterBookingsByUser() {
        while (true) {
            outputWriter.print("Enter username (or 'back' to return to filter menu): ");
            String username = inputReader.readLine().trim();

            if (username.equalsIgnoreCase("back")) {
                return null; // Возвращаемся в меню выбора фильтра
            }

            try {
                User user = userService.getUser(username);
                return bookingService.getBookingsByUser(user);
            } catch (IllegalArgumentException e) {
                outputWriter.printLine("User not found. Please try again.");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Filters bookings by a specific resource.
     * Displays a list of available resources and prompts the user to select one.
     *
     * @return A list of bookings for the selected resource, or null if the user chooses to go back
     * @throws SQLException if there's an error accessing the database
     */
    private List<Booking> filterBookingsByResource() throws SQLException {
        List<Resource> allResources = resourceService.getAllResources();
        if (allResources.isEmpty()) {
            outputWriter.printLine("No resources found.");
            return null;
        }

        outputWriter.printLine("Available resources:");
        for (Resource resource : allResources) {
            outputWriter.printLine(resource.getId() +
                    ": " + resource.getName() +
                    ", capacity: " + resource.getCapacity());
        }

        while (true) {
            outputWriter.print("Enter resource ID (or 0 to go back): ");
            Integer resourceId = inputReader.readIntSafely();
            if (resourceId == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            if (resourceId == 0) {
                return null;
            }

            Resource resource = resourceService.getResourceById(resourceId);
            if (resource == null) {
                outputWriter.printLine("Resource not found. Please try again.");
                continue;
            }
            return bookingService.getBookingsByResource(resource);
        }
    }

    /**
     * Displays a list of bookings.
     *
     * @param bookings The list of bookings to display
     */
    private void displayBookings(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            outputWriter.printLine("No bookings found.");
        } else {
            outputWriter.printLine("Bookings:");
            for (Booking booking : bookings) {
                outputWriter.printLine("ID: " + booking.getId() +
                        ", User: " + booking.getUser().getUsername() +
                        ", Resource: " + booking.getResource().getName() +
                        " (ID: " + booking.getResource().getId() + ")" +
                        ", From: " + booking.getStartTime().format(DATE_TIME_FORMATTER) +
                        ", To: " + booking.getEndTime().format(DATE_TIME_FORMATTER));
            }
        }
    }

    /**
     * Displays the admin menu and handles admin operations.
     *
     * @throws SQLException if there's an error accessing the database
     */
    private void showAdminMenu() throws SQLException {
        while (true) {
            outputWriter.printLine("Admin menu:");
            outputWriter.printLine("1. Add new resource");
            outputWriter.printLine("2. View all resources");
            outputWriter.printLine("3. Update resource");
            outputWriter.printLine("4. Remove resource");
            outputWriter.printLine("5. View all bookings");
            outputWriter.printLine("6. Back to main menu");
            outputWriter.print("Chose any option:  ");

            Integer option = inputReader.readIntSafely();
            if (option == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            switch (option) {
                case 1:
                    addNewResource();
                    break;
                case 2:
                    viewAllResources();
                    break;
                case 3:
                    updateResource();
                    break;
                case 4:
                    removeResource();
                    break;
                case 5:
                    viewAllBookings();
                    break;
                case 6:
                    return;
                default:
                    outputWriter.printLine("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Adds a new resource to the system.
     * Prompts the admin for resource details and creates a new resource.
     *
     * @throws SQLException if there's an error accessing the database
     */
    private void addNewResource() throws SQLException {
        outputWriter.printLine("Adding new resource:");

        Integer type = null;
        while (type == null) {
            outputWriter.print("Enter resource type (1 for WorkSpace, 2 for ConferenceRoom): ");
            type = inputReader.readIntSafely();
            if (type == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
            }
        }

        outputWriter.print("Enter resource name: ");
        String name = inputReader.readLine().trim();

        Integer capacity = null;
        while (capacity == null) {
            outputWriter.print("Enter resource capacity: ");
            capacity = inputReader.readIntSafely();
            if (capacity == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
            }
        }

        Resource newResource;
        if (type == 1) {
            newResource = new WorkSpace(0, name, capacity);
        } else if (type == 2) {
            newResource = new ConferenceRoom(0, name, capacity);
        } else {
            outputWriter.printLine("Invalid resource type.");
            return;
        }

        resourceService.addResource(newResource);
        outputWriter.printLine("Resource added successfully.");
    }


    /**
     * Displays all resources in the system.
     *
     * @throws SQLException if there's an error accessing the database
     */
    private void viewAllResources() throws SQLException {
        List<Resource> resources = resourceService.getAllResources();
        if (resources.isEmpty()) {
            outputWriter.printLine("No resources available.");
        } else {
            outputWriter.printLine("All resources:");
            for (Resource resource : resources) {
                outputWriter.printLine(resource.getId() +
                        ": " + resource.getName() +
                        " (Type: " + resource.getClass().getSimpleName() +
                        ", Capacity: " + resource.getCapacity() +
                        ")");
            }
        }
    }

    /**
     * Updates an existing resource in the system.
     * Prompts the admin to select a resource and enter new details.
     *
     * @throws SQLException if there's an error accessing the database
     */
    private void updateResource() throws SQLException {
        List<Resource> resources = resourceService.getAllResources();
        if (resources.isEmpty()) {
            outputWriter.printLine("No resources available.");
            return;
        }
        viewAllResources();

        while (true) {
            outputWriter.print("Enter resource ID to update (or 0 to go back): ");
            Integer resourceId = inputReader.readIntSafely();
            if (resourceId == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            if (resourceId == 0) {
                return; // Возвращаемся в предыдущее меню
            }

            try {
                Resource resourceToUpdate = resourceService.getResourceById(resourceId);
                if (resourceToUpdate == null) {
                    outputWriter.printLine("Resource not found.");
                    return;
                }

                outputWriter.print("Enter new name (or press Enter to keep current): ");
                String newName = inputReader.readLine().trim();
                if (!newName.isEmpty()) {
                    resourceToUpdate.setName(newName);
                }

                Integer newCapacity = null;
                while (newCapacity == null) {
                    outputWriter.print("Enter new capacity (or 0 to keep current): ");
                    newCapacity = inputReader.readIntSafely();
                    if (newCapacity == null) {
                        outputWriter.printLine("Invalid input. Please enter a number.");
                    }
                }

                if (newCapacity > 0) {
                    resourceToUpdate.setCapacity(newCapacity);
                }

                resourceService.updateResource(resourceToUpdate);
                outputWriter.printLine("Resource updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                outputWriter.printLine("Resource not found. Please try again.");
            }
        }
    }

    /**
     * Removes a resource from the system.
     * Prompts the admin to select a resource for removal and confirms the action.
     *
     * @throws SQLException if there's an error accessing the database
     */
    private void removeResource() throws SQLException {
        List<Resource> resources = resourceService.getAllResources();
        if (resources.isEmpty()) {
            outputWriter.printLine("No resources available.");
            return;
        }

        viewAllResources();

        while (true) {
            outputWriter.print("Enter resource ID to remove (or 0 to go back): ");
            Integer resourceId = inputReader.readIntSafely();

            if (resourceId == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            if (resourceId == 0) {
                return; // Возвращаемся в предыдущее меню
            }

            try {
                Resource resourceToRemove = resourceService.getResourceById(resourceId);

                // Подтверждение удаления
                outputWriter.printLine("Are you sure you want to remove this resource?");
                outputWriter.printLine("Resource: " +
                        resourceToRemove.getName() +
                        " (ID: " + resourceToRemove.getId() +
                        ")");
                outputWriter.print("Enter 'yes' to confirm or any other input to cancel: ");
                String confirmation = inputReader.readLine()
                        .trim()
                        .toLowerCase();

                if (confirmation.equals("yes")) {
                    resourceService.deleteResource(resourceToRemove);
                    outputWriter.printLine("Resource removed successfully.");
                } else {
                    outputWriter.printLine("Resource removal cancelled.");
                }
                return;
            } catch (IllegalArgumentException e) {
                outputWriter.printLine("Resource not found. Please try again.");
            }
        }
    }

    /**
     * Displays all bookings in the system.
     *
     * @throws SQLException if there's an error accessing the database
     */
    private void viewAllBookings() throws SQLException {
        List<Booking> allBookings = bookingService.getAllBookings();
        if (allBookings.isEmpty()) {
            outputWriter.printLine("There are no bookings");
        } else {
            outputWriter.printLine("All bookings:");
            for (Booking booking : allBookings) {
                outputWriter.printLine("Booking ID:  " + booking.getId() +
                        ", Resource:  " + booking.getResource().getName() +
                        ", Start:   " + booking.getStartTime().format(DATE_TIME_FORMATTER) +
                        ", End:    " + booking.getEndTime().format(DATE_TIME_FORMATTER));
            }
        }
    }

}
