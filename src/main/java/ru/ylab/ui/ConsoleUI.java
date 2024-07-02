package ru.ylab.ui;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.ylab.in.InputReader;
import ru.ylab.model.*;
import ru.ylab.out.OutputWriter;
import ru.ylab.service.AuthenticationService;
import ru.ylab.service.BookingService;
import ru.ylab.service.ResourceService;
import ru.ylab.service.UserService;
import ru.ylab.util.DateUtils;
import ru.ylab.util.ResourceNotFoundException;

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
    public void run() throws SQLException, ResourceNotFoundException {
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
    private void showMainMenu() throws SQLException, ResourceNotFoundException {
        while (true) {
            if (!authenticationService.isAuthenticated()) {
                return;
            }

            outputWriter.printLine("1. View available resources");
            outputWriter.printLine("2. Make a booking");
            outputWriter.printLine("3. View my bookings");
            outputWriter.printLine("4. Update my booking");
            outputWriter.printLine("5. Cancel my booking");
            outputWriter.printLine("6. View available time slots");
            outputWriter.printLine("7. Filter bookings");
            outputWriter.printLine("8. logout");

            if (authenticationService.getCurrentUser().isAdmin()) {
                outputWriter.printLine("9. Admin menu");
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
                    updateBooking();
                    break;
                case 5:
                    cancelBooking();
                    break;
                case 6:
                    viewAvailableTimeSlots();
                    break;
                case 7:
                    filterBookings();
                    break;
                case 8:
                    authenticationService.logout();
                    outputWriter.printLine("Logged out successfully.");
                    return;
                case 9:
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
        String username = inputReader.readLine();

        outputWriter.print("Enter password: ");
        String password = inputReader.readLine();

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
        String username = inputReader.readLine();

        outputWriter.print("Enter password: ");
        String password = inputReader.readLine();

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

        Timestamp[] timeRange = DateUtils.parseTimeRange(inputReader, outputWriter);
        if (timeRange == null) return;

        try {
            List<Resource> availableResources = resourceService.getAvailableResources(timeRange[0], timeRange[1]);
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
        } catch (Exception e) {
            outputWriter.printLine("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handles the process of making a new booking.
     */
    private void makeBooking() throws RuntimeException {

        Timestamp[] timeRange = DateUtils.parseTimeRange(inputReader, outputWriter);
        if (timeRange == null) return;

        try {
            List<Resource> availableResources = resourceService.getAvailableResources(timeRange[0], timeRange[1]);
            if (availableResources.isEmpty()) {
                outputWriter.printLine("No resources available for the selected time period.");
                return;
            }

            outputWriter.printLine("Available resources:");
            for (Resource resource : availableResources) {
                outputWriter.printLine("ID: " + resource.getId() +
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

            Booking booking = bookingService.createBooking(currentUser, selectedResource, timeRange[0], timeRange[1]);

            Optional<Booking> createdBooking = bookingService.getBookingById(booking.getId());
            if (createdBooking != null) {
                outputWriter.printLine("Booking successfully created in the database. Booking ID: " + booking.getId()
                        + ", name: " + selectedResource.getName()
                        + ", capacity: " + selectedResource.getCapacity()
                        + ", from: " + booking.getStartTime().toLocalDateTime().format(DATE_TIME_FORMATTER)
                        + " to: " + booking.getEndTime().toLocalDateTime().format(DATE_TIME_FORMATTER));
            } else {
                outputWriter.printLine("Warning: Booking was created but could not be retrieved.");
            }
        } catch (Exception e) {
            outputWriter.printLine("An error occurred during the booking process.");
            outputWriter.printLine("Error type: " + e.getClass().getSimpleName());
            outputWriter.printLine("Error message: " + e.getMessage());
            if (e.getCause() != null) {
                outputWriter.printLine("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
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
                                ", From: " + booking.getStartTime().toLocalDateTime().format(DATE_TIME_FORMATTER) +
                                ", To: " + booking.getEndTime().toLocalDateTime().format(DATE_TIME_FORMATTER));
            }
        }
    }

    private void updateBooking() throws SQLException {
        User currentUser = authenticationService.getCurrentUser();
        List<Booking> userBookings = bookingService.getBookingsByUser(currentUser);

        if (userBookings.isEmpty()) {
            outputWriter.printLine("You have no active bookings.");
            return;
        }

        viewMyBookings();

        while (true) {
            outputWriter.print("Enter booking ID to update (or 0 to go back): ");
            Integer bookingId = inputReader.readIntSafely();

            if (bookingId == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            if (bookingId == 0) {
                return;
            }

            try {
                Booking bookingToUpdate = userBookings.stream()
                        .filter(b -> b.getId() == bookingId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

                outputWriter.printLine("Are you sure you want to update and set new time for this booking?");
                outputWriter.printLine("Booking details: " + "name: " + bookingToUpdate.getResource().getName()
                        + ", from: " + bookingToUpdate.getStartTime().toLocalDateTime().format(DATE_TIME_FORMATTER)
                        + " to: " + bookingToUpdate.getEndTime().toLocalDateTime().format(DATE_TIME_FORMATTER));

                outputWriter.print("Enter 'yes' to confirm or any other input to cancel: ");
                String confirmation = inputReader.readLine().toLowerCase();

                if (confirmation.equals("yes") || confirmation.equals("да")) {
                    boolean timeUpdated = false;
                    while (!timeUpdated) {
                        try {
                            Timestamp newStartTime = DateUtils.parseDateTime("Enter new start time (yyyy-MM-dd HH:mm): ", inputReader, outputWriter);
                            Timestamp newEndTime = DateUtils.parseDateTime("Enter new end time (yyyy-MM-dd HH:mm): ", inputReader, outputWriter);

                            if (newStartTime.after(newEndTime)) {
                                throw new IllegalArgumentException("Start time must be before end time");
                            }

                            // Check if the new time slot is available
                            boolean hasConflict = bookingService.hasBookingConflict(bookingToUpdate.getResource(), newStartTime, newEndTime);

                            if (hasConflict) {
                                // Check if the conflict is with the current booking
                                boolean conflictWithSelf = (newStartTime.before(bookingToUpdate.getEndTime()) &&
                                        newEndTime.after(bookingToUpdate.getStartTime())) ||
                                        newStartTime.equals(bookingToUpdate.getStartTime()) ||
                                        newEndTime.equals(bookingToUpdate.getEndTime());

                                if (!conflictWithSelf) {
                                    outputWriter.printLine("Error: The selected time slot is not available");
                                    outputWriter.print("Do you want to try another time slot? (yes/no): ");
                                    String retry = inputReader.readLine().toLowerCase();
                                    if (!retry.equals("yes") && !retry.equals("да")) {
                                        return;
                                    }
                                    continue;
                                }
                            }

                            // Update the booking with new times
                            bookingToUpdate.setStartTime(newStartTime);
                            bookingToUpdate.setEndTime(newEndTime);
                            bookingService.updateBooking(bookingToUpdate);
                            outputWriter.printLine("Booking updated successfully.");
                            timeUpdated = true;
                        } catch (Exception e) {
                            outputWriter.printLine("Error: " + e.getMessage());
                            outputWriter.print("Do you want to try again? (yes/no): ");
                            String retry = inputReader.readLine().toLowerCase();
                            if (!retry.equals("yes") && !retry.equals("да")) {
                                return;
                            }
                        }
                    }
                } else {
                    outputWriter.printLine("Booking updating cancelled.");
                }
                return;
            } catch (IllegalArgumentException e) {
                outputWriter.printLine("Booking not found. Please try again.");
            } catch (Exception e) {
                outputWriter.printLine("Updating failed: " + e.getMessage());
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

                outputWriter.printLine("Booking details: " + "name: " + bookingToCancel.getResource().getName()
                        + ", from: " + bookingToCancel.getStartTime().toLocalDateTime().format(DATE_TIME_FORMATTER)
                        + " to: " + bookingToCancel.getEndTime().toLocalDateTime().format(DATE_TIME_FORMATTER));

                outputWriter.print("Enter 'yes' to confirm or any other input to cancel: ");

                String confirmation = inputReader.readLine().toLowerCase();

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
    private void viewAvailableTimeSlots() throws RuntimeException {

        Timestamp date = DateUtils.parseDate("Enter date (yyyy-MM-dd) or 'back' to return: ", inputReader, outputWriter);
        if (date == null) return;

        Integer duration = getDuration();
        if (duration == null) return;

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
        try {
            Timestamp date = DateUtils.parseDate("Enter date (yyyy-MM-dd): ", inputReader, outputWriter);
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
            String username = inputReader.readLine();

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
    private List<Booking> filterBookingsByResource() {
        try {
            List<Resource> allResources = resourceService.getAllResources();
            if (allResources.isEmpty()) {
                outputWriter.printLine("No resources found.");
                return null;
            }

            while (true) {
                outputWriter.printLine("Available resources:");
                for (Resource resource : allResources) {
                    outputWriter.printLine(resource.getId() +
                            ": " + resource.getName() +
                            ", capacity: " + resource.getCapacity());
                }

                outputWriter.print("Enter resource ID (or 0 to go back): ");
                Integer resourceId = inputReader.readIntSafely();
                if (resourceId == null) {
                    outputWriter.printLine("Invalid input. Please enter a number.");
                    continue;
                }

                if (resourceId == 0) {
                    return null;
                }

                try {
                    Resource resource = resourceService.getResourceById(resourceId);
                    return bookingService.getBookingsByResource(resource);
                } catch (ResourceNotFoundException e) {
                    outputWriter.printLine("Resource not found. Please try again.");
                } catch (SQLException e) {
                    outputWriter.printLine("An error occurred while fetching the resource: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            outputWriter.printLine("An error occurred while fetching resources: " + e.getMessage());
            return null;
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
                        ", user: " + booking.getUser().getUsername() +
                        ", resource: " + booking.getResource().getName() +
                        " (ID: " + booking.getResource().getId() + ")" +
                        ", from: " + booking.getStartTime().toLocalDateTime().format(DATE_TIME_FORMATTER) +
                        " to: " + booking.getEndTime().toLocalDateTime().format(DATE_TIME_FORMATTER));
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
        Integer capacity = null;

        while (type == null) {
            outputWriter.print("Enter resource type (1 for WorkSpace, 2 for ConferenceRoom): ");
            type = inputReader.readIntSafely();
            if (type == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
            }
        }

        outputWriter.print("Enter resource name: ");
        String name = inputReader.readLine();

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
                outputWriter.printLine("ID: " + resource.getId() +
                        ", " + resource.getName() +
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
                String newName = inputReader.readLine();
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
            } catch (ResourceNotFoundException e) {
                throw new RuntimeException(e);
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

                outputWriter.printLine("Are you sure you want to remove this resource?");
                outputWriter.printLine("Resource: " +
                        resourceToRemove.getName() +
                        " (ID: " + resourceToRemove.getId() +
                        ")");
                outputWriter.print("Enter 'yes' to confirm or any other input to cancel: ");
                String confirmation = inputReader.readLine()
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
            } catch (ResourceNotFoundException e) {
                throw new RuntimeException(e);
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
                outputWriter.printLine("Booking ID: " + booking.getId() +
                        ", resource: " + booking.getResource().getName() +
                        ", from: " + booking.getStartTime().toLocalDateTime().format(DATE_TIME_FORMATTER) +
                        ", to: " + booking.getEndTime().toLocalDateTime().format(DATE_TIME_FORMATTER));
            }
        }
    }

    private Integer getDuration() {
        while (true) {
            outputWriter.print("Enter duration in minutes (step 30 minutes) or 0 to go back: ");
            Integer duration = inputReader.readIntSafely();

            if (duration == null) {
                outputWriter.printLine("Invalid input. Please enter a number.");
                continue;
            }

            if (duration == 0) {
                return null;
            }

            if (duration % 30 != 0 || duration <= 0) {
                outputWriter.printLine("Invalid duration. Please enter a positive multiple of 30.");
                continue;
            }

            return duration;
        }
    }


}
