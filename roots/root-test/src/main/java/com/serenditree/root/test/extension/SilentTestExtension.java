package com.serenditree.root.test.extension;

import org.jboss.logmanager.Level;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Optional;


/**
 * Silences verbose classes during test. Annotate test class with @ExtendWith(SilentTestExtension.class) and tests
 * with @{@link SilentTest}.
 */
public class SilentTestExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final String LOG_LEVEL = "logLevel";

    /**
     * Disables logging below {@link Level}.WARN if the annotation {@link SilentTest} is present.
     *
     * @param extensionContext {@link ExtensionContext}
     */
    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        if (testMethod.isPresent() && testMethod.get().isAnnotationPresent(SilentTest.class)) {
            Logger rootLogger = LogContext.getLogContext().getLogger("");
            this.getStore(extensionContext).put(LOG_LEVEL, rootLogger.getLevel());
            rootLogger.setLevel(Level.WARN);
        }
    }

    /**
     * Resets log level.
     *
     * @param extensionContext {@link ExtensionContext}
     */
    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        LogContext
            .getLogContext()
            .getLogger("")
            .setLevel((Level) this.getStore(extensionContext).remove(LOG_LEVEL));
    }

    /**
     * Convenience method to get the {@link ExtensionContext.Store}.
     *
     * @param extensionContext {@link ExtensionContext}
     * @return {@link ExtensionContext.Store}
     */
    private ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getStore(
            ExtensionContext.Namespace.create(
                this.getClass(),
                extensionContext.getRequiredTestMethod()
            )
        );
    }
}
