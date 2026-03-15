package io.serenditree.leaf.seed.leaf;

import io.serenditree.branch.seed.model.entities.Garden;
import io.serenditree.branch.seed.model.filter.SeedFilter;
import io.serenditree.branch.seed.service.api.GardenService;
import io.serenditree.fence.AbstractFenceEndpoint;
import io.serenditree.fence.annotation.Fenced;
import io.serenditree.fence.annotation.Open;
import io.serenditree.fence.model.enums.FenceActionType;
import io.serenditree.fence.model.enums.RoleType;
import io.serenditree.root.log.annotation.Logged;
import io.serenditree.root.rest.cache.annotation.CacheControlConfig;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Objects;

@Path("garden")
@Logged
@CacheControlConfig(noCache = true)
public class GardenLeaf extends AbstractFenceEndpoint {

    private final GardenService gardenService;

    @Inject
    public GardenLeaf(GardenService gardenService) {
        this.gardenService = gardenService;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(rolesAllowed = {RoleType.USER}, createOrDeleteRecord = true, recordType = FenceActionType.CRUD)
    @Transactional(Transactional.TxType.NEVER)
    public Response create(final Garden garden) {

        return this.buildCacheResponse(
            this.gardenService.create(garden),
            Objects::nonNull,
            Response.Status.CREATED,
            Response.Status.INTERNAL_SERVER_ERROR
        );
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveById(final @PathParam("id") String id) {

        return this.buildCacheResponse(
            this.gardenService.retrieveById(id),
            Objects::nonNull,
            Response.Status.NOT_FOUND
        );
    }

    @POST
    @Path("retrieve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveByFilter(final SeedFilter filter) {

        return this.buildCacheResponse(
            this.gardenService.retrieveByFilter(filter),
            this.notNullNotEmpty,
            Response.Status.NOT_FOUND
        );
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(
        rolesAllowed = {RoleType.USER},
        actionBased = true,
        recordRequired = true,
        recordType = FenceActionType.CRUD,
        createOrDeleteRecord = true
    )
    @Transactional(Transactional.TxType.NEVER)
    public Response delete(final @PathParam("id") String id) {

        return this.buildFenceResponse(
            this.gardenService.delete(id),
            result -> result.getId() != null,
            id,
            Response.Status.ACCEPTED,
            Response.Status.NOT_FOUND
        );
    }
}
