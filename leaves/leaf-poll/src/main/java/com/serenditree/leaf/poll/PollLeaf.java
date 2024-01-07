package com.serenditree.leaf.poll;

import com.serenditree.branch.poll.service.api.PollServiceApi;
import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.annotation.Open;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.log.annotation.Logged;
import com.serenditree.root.rest.cache.annotation.CacheControlConfig;
import com.serenditree.root.rest.endpoint.AbstractEndpointRest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("poll")
@Logged
@CacheControlConfig(noCache = true)
public class PollLeaf extends AbstractEndpointRest {

    private PollServiceApi pollService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GET
    @Path("seed/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveBySeed(final @PathParam("id") String seedId) {

        return this.buildCacheResponse(
            this.pollService.retrieveBySeed(seedId),
            this.notNullNotEmpty,
            Response.Status.NOT_FOUND
        );
    }

    @GET
    @Path("vote/{id}/{optionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(
        rolesAllowed = {RoleType.USER},
        actionBased = true,
        createOrDeleteRecord = true,
        recordRequired = false
    )
    @Transactional
    public Response vote(final @PathParam("id") Long pollId, final @PathParam("optionId") Long optionId) {

        return this.buildResponse(
            this.pollService.vote(pollId, optionId),
            r -> r != null && r.getId() != null,
            Response.Status.INTERNAL_SERVER_ERROR
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    void setPollService(PollServiceApi pollService) {
        this.pollService = pollService;
    }
}
