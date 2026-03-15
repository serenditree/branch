package io.serenditree.root.test.extension;

import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;


/**
 * Sets log levels during test execution. Annotate test class with @ExtendWith(LogLevelExtension.class) and tests
 * with @{@link LogLevel}.
 */
public class LogLevelExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final String LOG_LEVEL = "serenditree.log.level";

    /**
     * Sets log levels during test execution if the annotation {@link LogLevel} is present.
     *
     * @param extensionContext {@link ExtensionContext}
     */
    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        if (testMethod.isPresent() && testMethod.get().isAnnotationPresent(LogLevel.class)) {
            Logger rootLogger = LogContext.getLogContext().getLogger("");
            this.getStore(extensionContext).put(LOG_LEVEL, rootLogger.getLevel());

            rootLogger.setLevel(
                Level.parse(
                    String.valueOf(
                        testMethod.get()
                            .getAnnotation(LogLevel.class)
                            .value()
                            .getLevel()
                    )
                )
            );
        }
    }

    /**
     * Resets log level.
     *
     * @param extensionContext {@link ExtensionContext}
     */
    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        Level storedLevel = this.getStore(extensionContext).getOrDefault(LOG_LEVEL, Level.class, null);
        if (storedLevel != null) {
            LogContext
                .getLogContext()
                .getLogger("")
                .setLevel(storedLevel);
        }
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
