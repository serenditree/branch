package com.serenditree.root.rest.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface CacheControlConfig {

    boolean isPrivate() default true;

    boolean noCache() default false;

    boolean noStore() default false;

    boolean noTransform() default true;

    boolean mustRevalidate() default true;

    boolean proxyRevalidate() default false;

    int maxAge() default 0;

    int sMaxAge() default 0;
}
