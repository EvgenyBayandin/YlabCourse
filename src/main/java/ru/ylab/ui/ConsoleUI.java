package ru.ylab.ui;

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

public class ConsoleUI {
    private InputReader inputReader;
    private OutputWriter outputWriter;
    private UserService userService;
    private ResourceService resourceService;
    private BookingService bookingService;
    private AuthenticationService authenticationService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ConsoleUI(InputReader inputReader, OutputWriter outputWriter, UserService userService, ResourceService resourceService, BookingService bookingService, AuthenticationService authenticationService) {
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
        this.userService = userService;
        this.resourceService = new ResourceService(resourceService.getResourceRepository(), bookingService);
        this.bookingService = bookingService;
        this.authenticationService = authenticationService;
    }

    /**
     *  Запускаем вход в систему через консольное меню
     */
    public void run() {
        while (true) {
            if (!authenticationService.isAuthenticated()) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    /**
     *  Выводит первичное меню на экран
     *  1. Login - меню для входа в систему для существующих пользователей
     *  2. Register  - меню для регистрации нового пользователя
     *  3. Exit  - выход и остановка приложения
     *  Choose an option: - ожидание ввода выбранного пункта меню
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
     *  Выводит главное меню на экран
     *  1. View available resources - просмотр списка доступных ресурсов
     *  2. Make a booking  - создание бронирования
     *  3. View my bookings  - посмотреть список моих бронирований
     *  4. Cancel a booking - отмена бронирования
     *  5. View available time slots - просмотр списка доступных временных интервалов для бронирования
     *  6. Filter bookings - переход в меню для фильтрации бронирований по параметрам
     *  7. logout - разлогирование и переход в первоначальное меню
     *  8. Admin menu - переход в меню для администратора (если пользователь является администратором) пункт не доступен обычным пользователям
     *  Choose an option: - ожидание ввода выбранного пункта меню
     */
    private void showMainMenu() {
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
     * Метод для аутентификации пользователя и вход в систему
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
        }
    }

    /**
     * Метод регистрации нового пользователя и переход в меню для входа в систему
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
        }
    }


    /**
     * Метод для вывода списка доступных ресурсов
     */
    private void viewAvailableResources() {
        outputWriter.print("Enter start date and time (yyyy-MM-dd HH:mm): ");
        String startStr = inputReader.readLine();
        outputWriter.print("Enter end date and time (yyyy-MM-dd HH:mm): ");
        String endStr = inputReader.readLine();

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
     * Метод для создания бронирования
     */
    private void makeBooking() {
        outputWriter.print("Enter start date and time (yyyy-MM-dd HH:mm): ");
        String startStr = inputReader.readLine();
        outputWriter.print("Enter end date and time (yyyy-MM-dd HH:mm): ");
        String endStr = inputReader.readLine();

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
                outputWriter.printLine(resource.getId() + ": " + resource.getName() +
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
     * Метод для просмотра бронирований пользователя
     */
    private void viewMyBookings() {
        User currentUser = authenticationService.getCurrentUser();
        List<Booking> myBookings = bookingService.getBookingsByUser(currentUser);
        if (myBookings.isEmpty()) {
            outputWriter.printLine("You have no bookings.");
        } else {
            outputWriter.printLine("Your bookings:");
            for (Booking booking : myBookings) {
                outputWriter.printLine("ID: " + booking.getId() +
                        ", Resource: " + booking.getResource().getName() +
                        " (ID: " + booking.getResource().getId() + ")" +
                        ", From: " + booking.getStartTime().format(DATE_TIME_FORMATTER) +
                        ", To: " + booking.getEndTime().format(DATE_TIME_FORMATTER));
            }
        }
    }

    /**
     * Метод отмены бронирования пользователя
     */
    private void cancelBooking() {
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
                return; // Возвращаемся в предыдущее меню
            }

            try {
                Booking bookingToCancel = userBookings.stream()
                        .filter(b -> b.getId() == bookingId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

                // Подтверждение отмены
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
     * Метод для просмотра списка достыпных таймслотов для конкретного дня
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
     * Метод перехода в меню и фильртации бронирований по параметрам
     */
    private void filterBookings() {
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
     * Метод фильтрации бронирований по дате
     */
    private List<Booking> filterBookingsByDate() {
        outputWriter.print("Enter date (yyyy-MM-dd): ");
        String dateStr = inputReader.readLine();
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return bookingService.getBookingsByDate(date);
        } catch (DateTimeParseException e) {
            outputWriter.printLine("Invalid date format. Please use yyyy-MM-dd.");
            return new ArrayList<>();
        }
    }

    /**
     * Метод фильтрации бронирований по пользователю
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
            }
        }
    }

    /**
     * Метод фильтрации бронирований по ресурсу
     */
    private List<Booking> filterBookingsByResource() {
        List<Resource> allResources  = resourceService.getAllResources();
        if (allResources.isEmpty())  {
            outputWriter.printLine("No resources found.");
            return null;
        }

        outputWriter.printLine("Available resources:");
        for (Resource resource : allResources) {
            outputWriter.printLine(resource.getId() + ": " + resource.getName() + ", capacity: " + resource.getCapacity());
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
            return bookingService.getBookingsByResources(resource);
        }
    }

    /**
     * Метод вывода списка бронирований
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
     *  Выводит меню администратора на экран
     *  1. Add new resource - добавление нового ресурса
     *  2. View all resources - список всех ресурсов
     *  3. Update resource - обновление ресурса
     *  4. Remove resource - удаление ресурса
     *  5. View all bookings - список всех бронирований
     *  6. Back to main menu - возврат в главное меню
     *  Choose an option: - ожидание ввода выбранного пункта меню
     */
    private void showAdminMenu() {
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
     * Метод для создания нового ресурса
     */
    private void addNewResource() {
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
        String name = inputReader.readLine();

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
     * Метод для вывода списка всех ресурсов
     */
    private void viewAllResources() {
        List<Resource> resources = resourceService.getAllResources();
        if (resources.isEmpty()) {
            outputWriter.printLine("No resources available.");
        } else {
            outputWriter.printLine("All resources:");
            for (Resource resource : resources) {
                outputWriter.printLine(resource.getId() + ": " + resource.getName() +
                        " (Type: " + resource.getClass().getSimpleName() +
                        ", Capacity: " + resource.getCapacity() + ")");
            }
        }
    }

    /**
     * Метод для изменения ресурса по его ID
     */
    private void updateResource() {
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
            }
        }
    }

    /**
     * Метод удаления ресурса по его ID
     */
    private void removeResource() {
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
                outputWriter.printLine("Resource: " + resourceToRemove.getName() + " (ID: " + resourceToRemove.getId() + ")");
                outputWriter.print("Enter 'yes' to confirm or any other input to cancel: ");
                String confirmation = inputReader.readLine().trim().toLowerCase();

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
     * Метод вывода списка всех бронирований
     */
    private void viewAllBookings() {
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
