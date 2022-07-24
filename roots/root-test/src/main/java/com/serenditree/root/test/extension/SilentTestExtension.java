package com.serenditree.root.test.extension;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Silences verbose classes during test. Annotate test class with @ExtendWith(SilentTestExtension.class) and tests
 * with @{@link SilentTest}.
 */
public class SilentTestExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    /**
     * Disables logging below Level.WARNING if the annotation {@link SilentTest} is present.
     *
     * @param extensionContext {@link ExtensionContext}
     */
    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        if (testMethod.isPresent() && testMethod.get().isAnnotationPresent(SilentTest.class)) {
            LogManager
                    .getLogManager()
                    .getLogger("")
                    .setLevel(Level.WARNING);
        }
    }

    /**
     * Resets log level to JUnit default (INFO).
     *
     * @param extensionContext {@link ExtensionContext}
     */
    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        LogManager
                .getLogManager()
                .getLogger("")
                .setLevel(Level.INFO);
    }
}
