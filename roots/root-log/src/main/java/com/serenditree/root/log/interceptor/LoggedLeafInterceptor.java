package com.serenditree.root.log.interceptor;

import com.serenditree.root.log.annotation.Logged;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Response;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Generic logging interceptor.
 */
@Dependent
@Interceptor
@Logged
@Priority(Interceptor.Priority.APPLICATION + 100)
public class LoggedLeafInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggedLeafInterceptor.class.getName());
    private static final String BEFORE = "Calling method ";
    private static final String AFTER = "Returning from ";

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
        Object response;

        if (LOGGER.isLoggable(Level.FINE) &&
            invocationContext.getMethod().getModifiers() == Modifier.PUBLIC) {
            final String declaringClass = invocationContext
                .getMethod()
                .getDeclaringClass()
                .getSimpleName();
            final String method = invocationContext
                .getMethod()
                .getName();
            final String methodSignature = declaringClass + "::" + method;

            final boolean isVoid = invocationContext
                .getMethod()
                .getReturnType()
                .equals(Void.TYPE);

            this.before(methodSignature, invocationContext.getParameters());
            response = invocationContext.proceed();
            this.after(methodSignature, response, isVoid);
        } else {
            response = invocationContext.proceed();
        }

        return response;
    }

    /**
     * Logs method signature and parameters before the intercepted method executes.
     *
     * @param method     Method signature.
     * @param parameters Method parameters.
     */
    private void before(final String method, final Object[] parameters) {

        String message = BEFORE + method;

        if (parameters != null && parameters.length > 0) {
            message += Arrays
                .stream(parameters)
                .map(parameter -> parameter == null ? "null" : parameter.toString())
                .collect(Collectors.joining("], [", " with parameters: [", "]"));
        }

        LOGGER.fine(message);
    }

    /**
     * Logs method signature and return value after the intercepted method was executed.
     *
     * @param method      Method signature.
     * @param returnValue Return value of the intercepted method.
     * @param isVoid      Flag for void return type.
     */
    private void after(final String method, final Object returnValue, final boolean isVoid) {
        if (!isVoid) {
            if (returnValue instanceof Response) {
                Response response = (Response) returnValue;
                if (response.getEntity() instanceof Collection) {
                    this.logCollection(method, (Collection<?>) response.getEntity(), response.getStatusInfo());
                } else {
                    this.logObject(method, response.getEntity(), response.getStatusInfo());
                }
            } else if (returnValue instanceof Collection) {
                this.logCollection(method, (Collection<?>) returnValue);
            } else {
                this.logObject(method, returnValue);
            }
        } else {
            LOGGER.fine(() -> AFTER + method);
        }
    }

    private <T> void logCollection(final String method,
                                   final Collection<T> collection,
                                   final Response.StatusType status) {
        LOGGER.fine(() ->
                        AFTER + method +
                        " Status: " + status +
                        " Results: " + collection.size()
        );
    }

    private <T> void logCollection(final String method, final Collection<T> collection) {
        LOGGER.fine(() ->
                        AFTER + method +
                        " Results: " + collection.size()
        );
    }

    private void logObject(final String method, final Object returnValue, final Response.StatusType status) {
        LOGGER.fine(() ->
                        AFTER + method +
                        " Status: " + status +
                        " Result: " + returnValue
        );
    }

    private void logObject(final String method, final Object returnValue) {
        LOGGER.fine(() ->
                        AFTER + method +
                        " Result: " + returnValue
        );
    }
}
