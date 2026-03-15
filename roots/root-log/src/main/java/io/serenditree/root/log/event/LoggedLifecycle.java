package io.serenditree.root.log.event;

import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.apache.commons.lang3.Strings;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.stream.StreamSupport;

@ApplicationScoped
public class LoggedLifecycle {

    private static final boolean LOG_PROPERTIES = ConfigProvider
        .getConfig()
        .getValue("serenditree.log.properties", Boolean.class);

    void onStart(@Observes StartupEvent startupEvent) {
        Log.info("Serenditree is starting...");
        if (LOG_PROPERTIES) {
            StreamSupport.stream(ConfigProvider.getConfig().getPropertyNames().spliterator(), false)
                .filter(property -> Strings.CI.startsWithAny(property, "serenditree", "quarkus", "mp"))
                .sorted()
                .forEach(this::logProperty);
        }
    }

    void onStop(@Observes ShutdownEvent shutdownEvent) {
        Log.info("Serenditree is stopping...");
    }

    private void logProperty(final String property) {
        String value = ConfigProvider.getConfig().getConfigValue(property).getValue();
        Log.debugv("{0}: {1}", property, value);
    }
}
