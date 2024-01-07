package com.serenditree.leaf.seed.leaf;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.branch.seed.service.api.SeedServiceApi;
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

import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Path("seed")
@Logged
@CacheControlConfig(noCache = true)
public class SeedLeaf extends AbstractEndpointRest {

    private SeedServiceApi seedService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(
        rolesAllowed = {RoleType.USER},
        // policies = {SeedPolicies.PARENT},
        createOrDeleteRecord = true,
        recordType = FenceActionType.CRUD
    )
    @Transactional(Transactional.TxType.NEVER)
    public Response create(final Seed seed) {

        return this.buildCacheResponse(
            this.seedService.create(seed),
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
            this.seedService.retrieveById(id),
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
            this.seedService.retrieveByFilter(filter),
            this.notNullNotEmpty,
            Response.Status.NOT_FOUND
        );
    }

    @GET
    @Path("retrieve/tags/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveTags(final @PathParam("name") String name) {

        return this.buildCacheResponse(
            this.seedService.retrieveTags(name),
            Objects::nonNull,
            Response.Status.NOT_FOUND
        );
    }

    @GET
    @Path("water/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(
        rolesAllowed = {RoleType.USER},
        actionBased = true,
        createOrDeleteRecord = true,
        recordRequired = false,
        expirationTime = 1,
        expirationUnit = ChronoUnit.DAYS
    )
    @Transactional(Transactional.TxType.NEVER)
    public Response water(final @PathParam("id") ObjectId id, final @QueryParam("garden") ObjectId gardenId) {

        return this.buildResponse(
            this.seedService.water(id, gardenId),
            r -> r != null && r.getId() != null,
            Response.Status.INTERNAL_SERVER_ERROR
        );
    }

    @GET
    @Path("prune/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(
        rolesAllowed = {RoleType.USER},
        actionBased = true,
        createOrDeleteRecord = true,
        recordRequired = false,
        expirationTime = 1,
        expirationUnit = ChronoUnit.DAYS
    )
    @Transactional(Transactional.TxType.NEVER)
    public Response prune(final @PathParam("id") ObjectId id, final @QueryParam("garden") ObjectId gardenId) {

        return this.buildResponse(
            this.seedService.prune(id, gardenId),
            r -> r != null && r.getId() != null,
            Response.Status.INTERNAL_SERVER_ERROR
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
            this.seedService.delete(id),
            result -> result.getId() != null,
            id.toString(),
            Response.Status.OK,
            Response.Status.NOT_FOUND
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    void setSeedService(SeedServiceApi seedService) {
        this.seedService = seedService;
    }
}
