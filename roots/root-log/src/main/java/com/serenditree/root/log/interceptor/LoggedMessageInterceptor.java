package com.serenditree.root.log.interceptor;

import com.serenditree.root.log.annotation.Logged;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.logging.Logger;

@Dependent
@Interceptor
@Logged(binding = LoggedMessageInterceptor.class)
@Priority(Interceptor.Priority.APPLICATION + 100)
public class LoggedMessageInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggedMessageInterceptor.class.getName());

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {

        if (invocationContext.getMethod().isAnnotationPresent(Incoming.class)) {
            Message<?> message = (Message) invocationContext.getParameters()[0];
            LOGGER.fine(() ->
                    invocationContext.getMethod().getDeclaringClass().getSimpleName() + "::" +
                            invocationContext.getMethod().getName() +
                            " [" + message.getPayload() + "]"
            );
        }

        return invocationContext.proceed();
    }
}
