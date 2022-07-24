package com.serenditree.fence.annotation;

import com.serenditree.fence.model.FenceRecord;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.fence.model.enums.RoleType;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;
import java.time.temporal.ChronoUnit;

/**
 * Interceptor binding for security interceptor.
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Fenced {

    /**
     * Roles which are allowed to access the annotated resource.
     *
     * @return Array of {@link RoleType}.
     */
    @Nonbinding
    RoleType[] rolesAllowed() default {};

    /**
     * Identifiers for additional policies.
     *
     * @return Array of policies.
     */
    @Nonbinding
    String[] policies() default {};

    /**
     * Individual authorization check is required to access the annotated resource.
     *
     * @return Flag to indicate action based authorization.
     */
    @Nonbinding
    boolean actionBased() default false;

    /**
     * Triggers the record interceptor.
     *
     * @return Flag which indicates if a record should be created or deleted.
     */
    boolean createOrDeleteRecord() default false;

    /**
     * Indicates which record is created.
     *
     * @return {@link FenceActionType}
     */
    @Nonbinding
    FenceActionType recordType() default FenceActionType.METHOD;

    /**
     * Flag to indicate whether the existence or absence of an {@link FenceRecord} record is required.
     *
     * @return True or false.
     */
    @Nonbinding
    boolean recordRequired() default true;

    /**
     * Number of records which are allowed or required for authorization.
     *
     * @return Number of records
     */
    @Nonbinding
    int recordCount() default -1;

    /**
     * Expiration time of {@link FenceRecord}.
     *
     * @return Expiration time.
     */
    @Nonbinding
    int expirationTime() default 0;

    /**
     * Unit of {@code expirationTime}.
     *
     * @return {@link ChronoUnit}.
     */
    @Nonbinding
    ChronoUnit expirationUnit() default ChronoUnit.FOREVER;
}
