package com.serenditree.fence.extras.oidc;


import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class FenceTenantResolver implements TenantResolver {

    private static final Logger LOGGER = Logger.getLogger(FenceTenantResolver.class.getName());

    @Inject
    @ConfigProperty(name = "serenditree.fence.verification.paths")
    String verificationPaths;

    @Override
    public String resolve(RoutingContext context) {
        String tenant = null; // Resolves to default.

        if (context.request().path().contains(StringUtils.removeEnd(this.verificationPaths, "*"))) {
            final String pathParamTenant = StringUtils.substringAfterLast(context.request().path(), "/");

            if (StringUtils.isNotBlank(pathParamTenant)) {
                tenant = pathParamTenant;
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Using tenant \"" + tenant + "\".");
            }
        }

        return tenant;
    }
}
