package com.taptap.tds.registration.server.core.mysql;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DuplicateKeyUpdate {

    boolean nullable() default false;
}
