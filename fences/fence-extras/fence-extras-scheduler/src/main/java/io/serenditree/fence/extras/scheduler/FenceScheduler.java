package io.serenditree.fence.extras.scheduler;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.logging.Log;
import io.serenditree.fence.AbstractFenceEndpoint;
import io.serenditree.fence.annotation.Open;
import io.serenditree.fence.authorization.repository.api.AuthorizationRepository;
import io.serenditree.root.log.annotation.Logged;
import io.serenditree.root.rest.cache.annotation.CacheControlConfig;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("internal/fence")
@Logged
@CacheControlConfig(noCache = true)
@IfBuildProperty(name = "serenditree.fence.cleanup.enabled", stringValue = "true")
public class FenceScheduler extends AbstractFenceEndpoint {

    private final AuthorizationRepository authorizationRepository;

    @Inject
    public FenceScheduler(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    @GET
    @Path("cleanup")
    @Produces(MediaType.TEXT_PLAIN)
    @Open
    public Response cleanup() {

        return this.buildResponse(
            this.authorizationRepository.deleteFenceRecordsByExpiration(),
            result -> result > -1,
            Response.Status.OK
        );
    }

    @PostConstruct
    void postConstruct() {
        Log.infov("Started {0}", FenceScheduler.class.getSimpleName());
    }
}
