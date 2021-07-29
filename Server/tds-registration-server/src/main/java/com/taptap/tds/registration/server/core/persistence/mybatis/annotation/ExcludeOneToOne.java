package com.taptap.tds.registration.server.core.persistence.mybatis.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExcludeOneToOne {

    String[] value();
}
