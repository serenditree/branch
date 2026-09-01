package io.serenditree.leaf.user.leaf;

import io.quarkus.oidc.IdToken;
import io.serenditree.branch.user.service.api.UserService;
import io.serenditree.fence.AbstractFenceEndpoint;
import io.serenditree.fence.annotation.*;
import io.serenditree.fence.model.enums.FenceActionType;
import io.serenditree.fence.model.enums.RoleType;
import io.serenditree.leaf.user.config.UserRoot;
import io.serenditree.root.log.annotation.Logged;
import io.serenditree.root.rest.cache.CacheControlProducer;
import io.serenditree.root.rest.cache.annotation.CacheControlConfig;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Objects;

@Path("user")
@Logged
@CacheControlConfig(maxAge = CacheControlProducer.DAY_IN_SECONDS)
public class UserLeaf extends AbstractFenceEndpoint {

    @Inject
    @IdToken
    JsonWebToken idToken;

    private final UserService userService;

    @Inject
    public UserLeaf(UserService userService) {
        this.userService = userService;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @POST
    @Path("sign-up")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced
    @SignUp
    public Response signUp() {

        return this.buildFenceResponse(Response.Status.CREATED);
    }

    @POST
    @Path("sign-in")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced
    @SignIn
    public Response signIn() {

        return this.buildFenceResponse(Response.Status.OK);
    }

    @PUT
    @Path("verify")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(rolesAllowed = {RoleType.USER})
    @Verify
    public Response verify() {

        return this.buildFenceResponse(Response.Status.OK);
    }

    @GET
    @Path("verify/callback/{country}")
    @Open
    public Response verifyCallback(final @PathParam("country") String country, final @QueryParam("id") Long id) {

        return this.buildFenceVerificationRedirect(
            id,
            country + this.idToken.getSubject(),
            UserRoot.class.getAnnotation(ApplicationPath.class).value()
        );
    }

    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveByUsername(final @PathParam("username") String username) {

        return this.buildCacheResponse(
            this.userService.retrieveByUsername(username),
            Objects::nonNull,
            Response.Status.NOT_FOUND
        );
    }

    @GET
    @Path("retrieve/{substring}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveBySubstring(final @PathParam("substring") String substring) {

        return this.buildCacheResponse(
            this.userService.retrieveBySubstring(substring),
            this.notNullNotEmpty,
            Response.Status.NOT_FOUND
        );
    }

    @DELETE
    @Path("{id}/{includeContributions}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(
        rolesAllowed = {RoleType.USER},
        actionBased = true,
        recordRequired = true,
        recordType = FenceActionType.CRUD,
        createOrDeleteRecord = true
    )
    @Cleanup
    @Transactional
    public Response delete(
        final @PathParam("id") Long id,
        final @PathParam("includeContributions") boolean includeContributions
    ) {

        return this.buildFenceResponse(
            this.userService.delete(id, includeContributions),
            result -> result.getId() != null,
            id.toString(),
            Response.Status.OK,
            Response.Status.NOT_FOUND
        );
    }
}
