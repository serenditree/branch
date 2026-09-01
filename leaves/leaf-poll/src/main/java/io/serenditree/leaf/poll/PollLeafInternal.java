package io.serenditree.leaf.poll;

import io.serenditree.branch.poll.model.entities.Poll;
import io.serenditree.branch.poll.service.api.PollService;
import io.serenditree.fence.AbstractFenceEndpoint;
import io.serenditree.fence.annotation.Open;
import io.serenditree.root.log.annotation.Logged;
import io.serenditree.root.rest.cache.annotation.CacheControlConfig;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("internal/poll")
@Logged
@CacheControlConfig(noCache = true)
public class PollLeafInternal extends AbstractFenceEndpoint {

    private final PollService pollService;

    @Inject
    public PollLeafInternal(PollService pollService) {
        this.pollService = pollService;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    @Transactional
    public Response create(final List<Poll> polls) {

        return this.buildResponse(
            this.pollService.create(polls),
            this.notNullNotEmpty,
            Response.Status.CREATED,
            Response.Status.INTERNAL_SERVER_ERROR
        );
    }

    @DELETE
    @Path("seed/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    @Transactional
    public Response deleteBySeed(@PathParam("id") String seedId) {

        return this.buildFenceResponse(
            this.pollService.deleteBySeed(seedId),
            result -> result > 0,
            seedId,
            Response.Status.ACCEPTED,
            Response.Status.NOT_FOUND
        );
    }
}
