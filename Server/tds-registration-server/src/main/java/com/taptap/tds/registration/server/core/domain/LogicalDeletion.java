package com.taptap.tds.registration.server.core.domain;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LogicalDeletion {

    String value() default "is_deleted";
}
