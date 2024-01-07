package com.serenditree.leaf.seed.leaf;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.branch.seed.service.api.GardenServiceApi;
import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.annotation.Open;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.log.annotation.Logged;
import com.serenditree.root.rest.cache.annotation.CacheControlConfig;
import com.serenditree.root.rest.endpoint.AbstractEndpointRest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.util.Objects;

@Path("garden")
@Logged
@CacheControlConfig(noCache = true)
public class GardenLeaf extends AbstractEndpointRest {

    private GardenServiceApi gardenService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
    public Response retrieveById(final @PathParam("id") ObjectId id) {

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
    @Path("delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(
        rolesAllowed = {RoleType.USER},
        actionBased = true,
        recordRequired = true,
        recordType = FenceActionType.CRUD,
        createOrDeleteRecord = true
    )
    @Transactional(Transactional.TxType.NEVER)
    public Response delete(final @PathParam("id") ObjectId id) {

        return this.buildFenceResponse(
            this.gardenService.delete(id),
            result -> result.getId() != null,
            id.toString(),
            Response.Status.ACCEPTED,
            Response.Status.NOT_FOUND
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    void setGardenService(GardenServiceApi gardenService) {
        this.gardenService = gardenService;
    }
}
