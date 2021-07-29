package com.taptap.tds.registration.server.core.persistence;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToManyProvider {

    Class<?> mapperInterface() default void.class;

    String mapperInterfaceName() default "";

    String method();
}
