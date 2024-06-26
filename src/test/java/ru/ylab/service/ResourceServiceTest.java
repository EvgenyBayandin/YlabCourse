package ru.ylab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import ru.ylab.model.Resource;
import ru.ylab.model.TimeSlot;
import ru.ylab.repository.ResourceRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для {@link ResourceService}.
 * Проверяет функциональность сервиса для работы с ресурсами.
 */

class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private BookingService bookingService;

    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resourceService = new ResourceService(resourceRepository, bookingService);
    }

    /**
     * Проверяет создание нового ресурса.
     */

    @Test
    void addResource_shouldCallRepository() {
        Resource resource = new Resource() {
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
                return 1000;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        resourceService.addResource(resource);
        verify(resourceRepository).addResource(resource);
    }

    /**
     * Проверяет получение существующего ресурса по id.
     */

    @Test
    void getResourceById_existingResource_shouldReturnResource() {
        Resource resource = new Resource() {
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
                return 1000;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        when(resourceRepository.findById(1)).thenReturn(Optional.of(resource));

        Resource result = resourceService.getResourceById(1);
        assertEquals(resource, result);
    }

    /**
     * Проверяет получение несуществующего ресурса по id.
     */

    @Test
    void getResourceById_nonExistingResource_shouldThrowException() {
        when(resourceRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> resourceService.getResourceById(1));
    }

    /**
     * Проверяет изменение ресурса.
     */

    @Test
    void updateResource_shouldCallRepository() {
        Resource resource = new Resource() {
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
                return 1000;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        resourceService.updateResource(resource);
        verify(resourceRepository).updateResource(resource);
    }

    /**
     * Проверяет получение всех ресурсов.
     */

    @Test
    void getAllResources_shouldReturnAllResources() {
        List<Resource> resources = Arrays.asList(new Resource() {
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
                return 100;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        }, new Resource() {
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
                return 200;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        });
        when(resourceRepository.getAllResources()).thenReturn(resources);

        List<Resource> result = resourceService.getAllResources();
        assertEquals(resources, result);
    }

    /**
     * Проверяет удаление ресурса.
     */

    @Test
    void deleteResource_shouldCallRepository() {
        Resource resource = new Resource() {
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
                return 1000;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        resourceService.deleteResource(resource);
        verify(resourceRepository).deleteResource(resource);
    }

    /**
     * Проверяет получение доступного ресурса.
     */

    @Test
    void getAvailableResources_shouldReturnAvailableResources() {
        Resource resource1 = new Resource() {
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
                return 100;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        Resource resource2 = new Resource() {
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
                return 200;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);

        when(resourceRepository.getAllResources()).thenReturn(Arrays.asList(resource1, resource2));
        when(bookingService.getBookingsByResources(resource1)).thenReturn(List.of());
        when(bookingService.getBookingsByResources(resource2)).thenReturn(List.of(
                new ru.ylab.model.Booking(1, new ru.ylab.model.User("user", "pass", false), resource2, start.minusMinutes(30), end.minusMinutes(30))
        ));

        List<Resource> result = resourceService.getAvailableResources(start, end);
        assertEquals(1, result.size());
        assertEquals(resource1, result.get(0));
    }

    /**
     * Проверяет получение доступных таймслотов.
     */

    @Test
    void getAvailableTimeSlots_shouldReturnAvailableSlots() {
        Resource resource = new Resource() {
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
                return 1000;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        LocalDate date = LocalDate.now();
        int durationMinutes = 30;

        when(resourceRepository.getAllResources()).thenReturn(List.of(resource));
        when(bookingService.getBookingsByResources(resource)).thenReturn(List.of());

        List<TimeSlot> result = resourceService.getAvailableTimeSlots(date, durationMinutes);
        assertFalse(result.isEmpty());
        assertEquals(18, result.size()); // Примем за рабочий день с 9 до 18 часов, доступно 18 слотов  для двух дотсупных ресурсов
    }
}