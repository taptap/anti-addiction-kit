package com.taptap.tds.registration.server.core.persistence;

import javax.persistence.Entity;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EntityInformationFactory {

    private static final ConcurrentMap<Class<?>, EntityInformation> ENTITY_INFORMATION_CACHE = new ConcurrentHashMap<>();

    public static EntityInformation getEntityInformation(Object entity) {
        return getEntityInformation(entity.getClass());
    }

    public static EntityInformation getEntityInformation(Class<?> entityClass) {
        return ENTITY_INFORMATION_CACHE.computeIfAbsent(entityClass, type -> {
            if (!type.isAnnotationPresent(Entity.class)) {
                throw new IllegalArgumentException("Class " + type + " is not annotated with @Entity");
            }
            EntityAnnotationBuilder builder = new EntityAnnotationBuilder(type);
            return builder.parse();
        });
    }
}
