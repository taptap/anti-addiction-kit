package com.taptap.tds.registration.server.core.enums;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public final class EnumValueUtils {

    private static final ConcurrentMap<Class<? extends EnumValue<?>>, Class<?>> ENUM_VALUE_TYPE_CACHE = new ConcurrentHashMap<>();

    private EnumValueUtils() {
        throw new AssertionError();
    }

    public static Class<?> getEnumValueActualType(Class<? extends EnumValue<?>> enumValueClass) {
        return ENUM_VALUE_TYPE_CACHE.computeIfAbsent(enumValueClass,
                clazz -> GenericTypeResolver.resolveTypeArgument(clazz, EnumValue.class));
    }

    @SuppressWarnings("rawtypes")
    public static void scanEnumValuePackages(Consumer<Class<EnumValue>> consumer, String... enumValuePackages) {
        scanEnumValuePackages(EnumValue.class, consumer, enumValuePackages);
    }

    public static <T extends EnumValue<?>> void scanEnumValuePackages(Class<T> enumValueType, Consumer<Class<T>> consumer,
                                                                                                  String... enumValuePackages) {

        if (ArrayUtils.isEmpty(enumValuePackages)) {
            return;
        }

        ClassPathEnumValueScanner<T> scanner = new ClassPathEnumValueScanner<>(enumValueType);

        for (String enumValuePackage : enumValuePackages) {
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(enumValuePackage)) {
                @SuppressWarnings("unchecked")
                Class<T> type = (Class<T>) ClassUtils.resolveClassName(beanDefinition.getBeanClassName(),
                        ClassUtils.getDefaultClassLoader());
                if (!type.isAnonymousClass() && type.isEnum()) {
                    consumer.accept(type);
                }
            }
        }
    }

    public static Object unwrap(Object value) {
        if (value instanceof Collection) {
            Collection<?> c = (Collection<?>) value;
            if (c.iterator().next() instanceof EnumValue) {
                Collection<Object> result = new ArrayList<>(c.size());
                for (Object obj : c) {
                    result.add(((EnumValue<?>) obj).getValue());
                }
                return result;
            } else {
                return value;
            }
        } else if (value.getClass().isArray()) {
            if (EnumValue.class.isAssignableFrom(value.getClass().getComponentType())) {
                Object[] array = (Object[]) value;
                Object[] result = new Object[array.length];
                for (int i = 0; i < array.length; i++) {
                    result[i] = ((EnumValue<?>) array[i]).getValue();
                }
                return result;
            } else {
                return value;
            }
        }
        return value instanceof EnumValue ? ((EnumValue<?>) value).getValue() : value;
    }
}
