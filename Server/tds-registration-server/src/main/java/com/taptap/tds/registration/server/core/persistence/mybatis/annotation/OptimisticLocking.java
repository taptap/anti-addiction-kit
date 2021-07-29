package com.taptap.tds.registration.server.core.persistence.mybatis.annotation;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OptimisticLocking {

    HttpStatus value() default HttpStatus.CONFLICT;

    boolean requireMessage() default true;
}
