package com.serenditree.root.log.event;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class LoggedLifecycle {

    private static final Logger LOGGER = Logger.getLogger(LoggedLifecycle.class.getName());

    void onStart(@Observes StartupEvent startupEvent) {
        LOGGER.info("Serenditree is starting...");
        if (ConfigProvider.getConfig().getOptionalValue("serenditree.log.config", String.class).isPresent()) {
            StreamSupport.stream(ConfigProvider.getConfig().getPropertyNames().spliterator(), false)
                    .sorted()
                    .forEach(property -> {
                        if (StringUtils.startsWithIgnoreCase(property, "serenditree") ||
                                StringUtils.startsWithIgnoreCase(property, "quarkus"))
                            LOGGER.fine(() -> property + ": " + ConfigProvider.getConfig()
                                    .getConfigValue(property)
                                    .getValue());
                    });
        }
    }

    void onStop(@Observes ShutdownEvent shutdownEvent) {
        LOGGER.info("Serenditree is stopping...");
    }
}
