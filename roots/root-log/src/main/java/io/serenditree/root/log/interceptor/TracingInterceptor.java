package io.serenditree.root.log.interceptor;

import io.serenditree.root.log.annotation.NotTraced;
import io.serenditree.root.log.annotation.Traced;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Dependent
@Interceptor
@Traced
@Priority(Interceptor.Priority.APPLICATION + 100)
public class TracingInterceptor {

    private Tracer tracer;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
        Object response;
        final Method method = invocationContext.getMethod();

        if (method.isAnnotationPresent(NotTraced.class) || !Modifier.isPublic(method.getModifiers())) {
            response = invocationContext.proceed();
        } else {
            final String traced = method.getDeclaringClass().getSimpleName() + "." + method.getName();
            Log.debugv("Tracing {0}", traced);
            final Span interceptorSpan = this.tracer
                .spanBuilder(traced)
                .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
            response = invocationContext.proceed();
            interceptorSpan.end();
        }

        return response;
    }

    @Inject
    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }
}
