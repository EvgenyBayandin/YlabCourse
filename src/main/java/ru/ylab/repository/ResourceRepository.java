package ru.ylab.repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.ylab.model.Resource;

public class ResourceRepository {
    private List<Resource> resources = new ArrayList<>();
    private int nextId = 1;

    public void addResource(Resource resource) {
        if (resource.getId() == 0) {
            try {
                Field idField = resource.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(resource, nextId++);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set resource ID", e);
            }
        }
        resources.add(resource);
    }

    public List<Resource> getAllResources() {
        return new ArrayList<>(resources);
    }

    public Optional<Resource> findById(int id) {
        return resources.stream()
                .filter(resource -> resource.getId() == id)
                .findFirst();
    }

    public void updateResource(Resource resource) {
        resources.set(resources.indexOf(resource), resource);
    }

    public void deleteResource(Resource resource) {
        resources.remove(resource);
    }

}
