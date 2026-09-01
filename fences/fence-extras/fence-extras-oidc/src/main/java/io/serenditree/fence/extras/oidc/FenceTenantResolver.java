package io.serenditree.fence.extras.oidc;


import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.logging.Log;
import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@IfBuildProperty(name = "serenditree.fence.verification.enabled", stringValue = "true")
public class FenceTenantResolver implements TenantResolver {

    private final String verificationPaths;

    @Inject
    public FenceTenantResolver(
        @ConfigProperty(name = "serenditree.fence.verification.paths") String verificationPaths
    ) {
        this.verificationPaths = verificationPaths;
    }

    @Override
    public String resolve(RoutingContext context) {
        String tenant = null; // Resolves to default.

        if (context.request().path().contains(Strings.CS.removeEnd(this.verificationPaths, "*"))) {
            final String pathParamTenant = StringUtils.substringAfterLast(context.request().path(), "/");

            if (StringUtils.isNotBlank(pathParamTenant)) {
                tenant = pathParamTenant;
            }
            Log.debugv("Using tenant \"{0}\".", tenant);
        }

        return tenant;
    }

    @PostConstruct
    void postConstruct() {
        Log.infov("Started {0}", FenceTenantResolver.class.getSimpleName());
    }
}
