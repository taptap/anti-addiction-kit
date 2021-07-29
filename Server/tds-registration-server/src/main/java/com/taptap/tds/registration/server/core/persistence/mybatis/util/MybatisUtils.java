package com.taptap.tds.registration.server.core.persistence.mybatis.util;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.Map;

public abstract class MybatisUtils {

    public static Class<?> getMapperClass(String mappedStatementId) {
        try {
            String className = mappedStatementId.substring(0, mappedStatementId.lastIndexOf("."));
            return ClassUtils.forName(className, null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getMapperEntityType(String mappedStatementId) {
        Class<?> mapperType = getMapperClass(mappedStatementId);
        return getMapperEntityType(mapperType);
    }

    public static Class<?> getMapperEntityType(Class<?> mapperType) {
        Type[] interfaces = mapperType.getGenericInterfaces();
        for (Type iface : interfaces) {
            // Only need to check interfaces that use generics
            if (iface instanceof ParameterizedType) {
                ParameterizedType pi = (ParameterizedType) iface;
                // Look for the generic interface
                if (pi.getRawType() instanceof Class) {
                    Type type = pi.getActualTypeArguments()[0];
                    if (type instanceof Class<?>) {
                        return (Class<?>) type;
                    }
                }
            } else if (iface instanceof Class) {
                return getMapperEntityType((Class<?>) iface);
            }
        }

        throw new IllegalArgumentException("Can not find generic type of mapper" + mapperType);
    }

    public static Method getMappedStatementMethod(MappedStatement mappedStatement) {
        return getMappedStatementMethod(mappedStatement.getId());
    }

    public static Method getMappedStatementMethod(String mappedStatementId) {
        Class<?> mapperClass = getMapperClass(mappedStatementId);
        Class<?>[] paramTypes = null;
        return ReflectionUtils.findMethod(mapperClass, mappedStatementId.substring(mappedStatementId.lastIndexOf(".") + 1),
                paramTypes);
    }

    @SuppressWarnings("unchecked")
    public static <T> T extractMapperMethodParameter(Object parameterObject, String paramName) {
        if (parameterObject instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) parameterObject;
            return params.containsKey(paramName) ? (T) params.get(paramName) : null;
        }
        return null;
    }

    public static Class<?> getMapperMethodReturnType(Class<?> mapperType, Method method) {

        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperType);
        if (resolvedReturnType instanceof Class) {
            returnType = (Class<?>) resolvedReturnType;
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }
            if (void.class.equals(returnType)) {
                ResultType rt = method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        } else if (resolvedReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) resolvedReturnType;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType) || Cursor.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    Type returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue #443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        // support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            } else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                // Do not look into Maps if there is not MapKey annotation
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                    Type returnTypeParameter = actualTypeArguments[1];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            }
        }

        return returnType;
    }
}
