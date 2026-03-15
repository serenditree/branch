package com.serenditree.leaf.seed.leaf;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.branch.seed.service.api.GardenServiceApi;
import com.serenditree.fence.FenceOASFilter;
import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.annotation.Open;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.log.annotation.Logged;
import com.serenditree.root.rest.cache.annotation.CacheControlConfig;
import com.serenditree.fence.AbstractFenceEndpoint;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;

import java.util.Objects;

@Path("garden")
@Logged
@CacheControlConfig(noCache = true)
public class GardenLeaf extends AbstractFenceEndpoint {

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
    @Operation(
        extensions = {
            @Extension(name = FenceOASFilter.FENCED, value = "true"),
            @Extension(name = FenceOASFilter.SORTED, value = "200")
        }
    )
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
    @Operation(extensions = @Extension(name = FenceOASFilter.SORTED, value = "201"))
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
    @Operation(extensions = @Extension(name = FenceOASFilter.SORTED, value = "202"))
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
    @Operation(
        extensions = {
            @Extension(name = FenceOASFilter.FENCED, value = "true"),
            @Extension(name = FenceOASFilter.SORTED, value = "203")
        }
    )
    public Response delete(final @PathParam("id") String id) {

        return this.buildFenceResponse(
            this.gardenService.delete(id),
            result -> result.getId() != null,
            id,
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
