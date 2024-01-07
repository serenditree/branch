package com.serenditree.fence.annotation;

import jakarta.enterprise.util.Nonbinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint as being open for requests without authentication.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Open {
    /**
     * Identifiers for request body policies.
     *
     * @return Array of policies.
     */
    @Nonbinding
    String[] policies() default {};
}
