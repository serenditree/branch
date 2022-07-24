package com.serenditree.leaf.poll;

import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.branch.poll.service.api.PollServiceApi;
import com.serenditree.fence.annotation.Open;
import com.serenditree.root.log.annotation.Logged;
import com.serenditree.root.rest.cache.annotation.CacheControlConfig;
import com.serenditree.root.rest.endpoint.AbstractEndpointRest;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("internal/poll")
@Logged
@CacheControlConfig(noCache = true)
public class PollLeafInternal extends AbstractEndpointRest {

    private PollServiceApi pollService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
                Response.Status.INTERNAL_SERVER_ERROR);
    }

    @DELETE
    @Path("delete/seed/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    @Transactional
    public Response deleteBySeed(@PathParam("id") String seedId) {

        return this.buildFenceResponse(
                this.pollService.deleteBySeed(seedId),
                result -> result > 0,
                seedId,
                Response.Status.ACCEPTED,
                Response.Status.NOT_FOUND);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    void setPollService(PollServiceApi pollService) {
        this.pollService = pollService;
    }
}
