package io.serenditree.root.log.interceptor;

import io.serenditree.root.log.annotation.Logged;
import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

@Dependent
@Interceptor
@Logged(binding = LoggedMessageInterceptor.class)
@Priority(Interceptor.Priority.APPLICATION + 100)
public class LoggedMessageInterceptor {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {

        if (invocationContext.getMethod().isAnnotationPresent(Incoming.class)) {
            Message<?> message = (Message<?>) invocationContext.getParameters()[0];
            Log.debugv(
                "{0}::{1} [{2}]",
                invocationContext.getMethod().getDeclaringClass().getSimpleName(),
                invocationContext.getMethod().getName(),
                message.getPayload()
            );
        }

        return invocationContext.proceed();
    }
}
