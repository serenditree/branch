package com.serenditree.fence.extras.oidc;


import io.quarkus.logging.Log;
import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class FenceTenantResolver implements TenantResolver {

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
            Log.debugv("Using tenant \"{0}\".", tenant);
        }

        return tenant;
    }
}
