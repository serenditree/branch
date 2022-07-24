package com.serenditree.root.log.annotation;

import com.serenditree.root.log.interceptor.LoggedLeafInterceptor;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Logged {
    Class<?> binding() default LoggedLeafInterceptor.class;
}
