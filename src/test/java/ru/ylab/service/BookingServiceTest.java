package ru.ylab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.model.User;
import ru.ylab.repository.BookingRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для {@link BookingService}.
 * Проверяет функциональность сервиса бронирования.
 */
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingService = new BookingService(bookingRepository);
    }

    /**
     * Проверяет создание нового бронирования без конфликтов.
     */
    @Test
    void createBooking_withoutConflict_shouldCreateBooking() {
        User user = new User("a", "a", true);
        Resource resource = new Resource() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getName() {
                return "Meeting Room A";
            }

            @Override
            public int getCapacity() {
                return 100;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        when(bookingRepository.getBookingsByResources(resource)).thenReturn(List.of());

        Booking result = bookingService.createBooking(user, resource, start, end);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getResource()).isEqualTo(resource);
        assertThat(result.getStartTime()).isEqualTo(start);
        assertThat(result.getEndTime()).isEqualTo(end);

        verify(bookingRepository).addBooking(result);
    }

    /**
     * Проверяет, что при попытке создать конфликтующее бронирование
     * выбрасывается исключение {@link IllegalStateException}.
     */
    @Test
    void createBooking_withConflict_shouldThrowException() {
        User user = new User("a", "a", true);
        Resource resource = new Resource() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getName() {
                return "Meeting Room A";
            }

            @Override
            public int getCapacity() {
                return 100;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        Booking existingBooking = new Booking(1, user, resource, start.minusMinutes(30), end.minusMinutes(30));
        when(bookingRepository.getBookingsByResources(resource)).thenReturn(List.of(existingBooking));

        assertThatThrownBy(() -> bookingService.createBooking(user, resource, start, end))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Booking conflict");
    }

    /**
     * Проверяет отмену бронирования.
     */
    @Test
    void cancelBooking_shouldDeleteBooking() {
        Booking booking = new Booking(1, new User("a", "a", true), new Resource() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getName() {
                return "Room A";
            }

            @Override
            public int getCapacity() {
                return 200;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        },
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        bookingService.cancelBooking(booking);

        verify(bookingRepository).deleteBooking(booking);
    }

    /**
     * Проверяет получение списка бронирований для конкретного пользователя.
     */
    @Test
    void getBookingsByUser_shouldReturnUserBookings() {
        User user = new User("a", "a", true);
        List<Booking> expectedBookings = Arrays.asList(
                new Booking(1, user, new Resource() {
                    @Override
                    public int getId() {
                        return 1;
                    }

                    @Override
                    public String getName() {
                        return "Room A";
                    }

                    @Override
                    public int getCapacity() {
                        return 200;
                    }

                    @Override
                    public void setName(String name) {

                    }

                    @Override
                    public void setCapacity(int capacity) {

                    }
                }, LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
                new Booking(2, user, new Resource() {
                    @Override
                    public int getId() {
                        return 2;
                    }

                    @Override
                    public String getName() {
                        return "Room B";
                    }

                    @Override
                    public int getCapacity() {
                        return 300;
                    }

                    @Override
                    public void setName(String name) {

                    }

                    @Override
                    public void setCapacity(int capacity) {

                    }
                }, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2))
        );

        when(bookingRepository.getBookingsByUser(user)).thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingsByUser(user);

        assertThat(result).isEqualTo(expectedBookings);
    }

    /**
     * Проверяет получение списка бронирований для конкретного ресурса.
     */
    @Test
    void getBookingsByResources_shouldReturnResourceBookings() {
        Resource resource = new Resource() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getName() {
                return "Meeting Room A";
            }

            @Override
            public int getCapacity() {
                return 100;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        List<Booking> expectedBookings = Arrays.asList(
                new Booking(1, new User("a", "a", true), resource, LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
                new Booking(2, new User("u", "u", false), resource, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2))
        );

        when(bookingRepository.getBookingsByResources(resource)).thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingsByResources(resource);

        assertThat(result).isEqualTo(expectedBookings);
    }

    /**
     * Проверяет получение списка бронирований на конкретную дату.
     */
    @Test
    void getBookingsByDate_shouldReturnBookingsForSpecificDate() {
        LocalDate targetDate = LocalDate.now();
        List<Booking> allBookings = Arrays.asList(
                new Booking(1, new User("a", "a", true), new Resource() {
                    @Override
                    public int getId() {
                        return 1;
                    }

                    @Override
                    public String getName() {
                        return "Room A";
                    }

                    @Override
                    public int getCapacity() {
                        return 200;
                    }

                    @Override
                    public void setName(String name) {

                    }

                    @Override
                    public void setCapacity(int capacity) {

                    }
                },
                        targetDate.atTime(10, 0), targetDate.atTime(11, 0)),
                new Booking(2, new User("u", "u", false), new Resource() {
                    @Override
                    public int getId() {
                        return 2;
                    }

                    @Override
                    public String getName() {
                        return "Room B";
                    }

                    @Override
                    public int getCapacity() {
                        return 300;
                    }

                    @Override
                    public void setName(String name) {

                    }

                    @Override
                    public void setCapacity(int capacity) {

                    }
                },
                        targetDate.plusDays(1).atTime(14, 0), targetDate.plusDays(1).atTime(15, 0))
        );

        when(bookingRepository.getAllBookings()).thenReturn(allBookings);

        List<Booking> result = bookingService.getBookingsByDate(targetDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime().toLocalDate()).isEqualTo(targetDate);
    }
}
