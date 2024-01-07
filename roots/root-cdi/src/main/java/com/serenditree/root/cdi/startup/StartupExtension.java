package com.serenditree.root.cdi.startup;

import com.serenditree.root.cdi.startup.annotation.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * CDI extension that enables startup beans.
 */
public class StartupExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(StartupExtension.class.getName());

    private final List<Bean<?>> startupBeans = new ArrayList<>();

    public <T> void collect(@Observes ProcessBean<T> event) {
        if (event.getAnnotated().isAnnotationPresent(Startup.class)
            && event.getAnnotated().isAnnotationPresent(ApplicationScoped.class)) {
            this.startupBeans.add(event.getBean());
        }
    }

    public void load(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        for (Bean<?> bean : this.startupBeans) {
            String reference = beanManager
                .getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean))
                .toString();
            LOGGER.info(() -> "Loaded " + reference);
        }
    }
}
