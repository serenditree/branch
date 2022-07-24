package com.serenditree.leaf.user.leaf;

import com.serenditree.branch.user.service.api.UserServiceApi;
import com.serenditree.fence.annotation.*;
import com.serenditree.fence.authentication.service.api.TokenServiceApi;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.log.annotation.Logged;
import com.serenditree.root.rest.cache.CacheControlProducer;
import com.serenditree.root.rest.cache.annotation.CacheControlConfig;
import com.serenditree.root.rest.endpoint.AbstractEndpointRest;
import io.quarkus.oidc.IdToken;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

@Path("user")
@Logged
@CacheControlConfig(maxAge = CacheControlProducer.DAY_IN_SECONDS)
public class UserLeaf extends AbstractEndpointRest {

    private UserServiceApi userService;

    private TokenServiceApi tokenService;

    @Inject
    @IdToken
    JsonWebToken idToken;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
    public Response verifyCallback(@PathParam("country") String country, @QueryParam("id") Long id) {

        return this.buildRedirect("/user/settings?oidc="
                + this.tokenService.buildVerificationToken(id, country + this.idToken.getSubject()));
    }

    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveByUsername(final @PathParam("username") String username) {

        return this.buildCacheResponse(
                this.userService.retrieveByUsername(username),
                Objects::nonNull,
                Response.Status.NOT_FOUND);
    }

    @GET
    @Path("retrieve/{substring}")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveBySubstring(final @PathParam("substring") String substring) {

        return this.buildCacheResponse(
                this.userService.retrieveBySubstring(substring),
                Objects::nonNull,
                Response.Status.NOT_FOUND);
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
    @Transactional
    public Response delete(final @PathParam("id") Long id) {

        return this.buildFenceResponse(
                this.userService.delete(id),
                result -> result.getId() != null,
                id.toString(),
                Response.Status.ACCEPTED,
                Response.Status.NOT_FOUND);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    void setUserService(UserServiceApi userService) {
        this.userService = userService;
    }

    @Inject
    public void setTokenService(TokenServiceApi tokenService) {
        this.tokenService = tokenService;
    }
}
