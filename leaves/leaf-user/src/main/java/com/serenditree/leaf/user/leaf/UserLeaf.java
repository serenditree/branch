package com.serenditree.leaf.user.leaf;

import com.serenditree.branch.user.service.api.UserServiceApi;
import com.serenditree.fence.AbstractFenceEndpoint;
import com.serenditree.fence.FenceOASFilter;
import com.serenditree.fence.annotation.*;
import com.serenditree.fence.model.FenceHeaders;
import com.serenditree.fence.model.enums.FenceActionType;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.leaf.user.config.UserRoot;
import com.serenditree.root.log.annotation.Logged;
import com.serenditree.root.rest.cache.CacheControlProducer;
import com.serenditree.root.rest.cache.annotation.CacheControlConfig;
import io.quarkus.oidc.IdToken;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.Objects;

@Path("user")
@Logged
@CacheControlConfig(maxAge = CacheControlProducer.DAY_IN_SECONDS)
public class UserLeaf extends AbstractFenceEndpoint {

    private UserServiceApi userService;

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
    @Operation(extensions = @Extension(name = FenceOASFilter.SORTED, value = "100"))
    public Response signUp(@HeaderParam(FenceHeaders.USERNAME)
                           @Parameter(
                               name = FenceHeaders.USERNAME,
                               in = ParameterIn.HEADER,
                               required = true
                           )
                           String username,
                           @HeaderParam(FenceHeaders.PASSWORD)
                           @Parameter(
                               name = FenceHeaders.PASSWORD,
                               in = ParameterIn.HEADER,
                               required = true
                           )
                           String password) {

        return this.buildFenceResponse(Response.Status.CREATED);
    }

    @POST
    @Path("sign-in")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced
    @SignIn
    @Operation(extensions = @Extension(name = FenceOASFilter.SORTED, value = "101"))
    public Response signIn(@HeaderParam(FenceHeaders.USERNAME)
                           @Parameter(
                               name = FenceHeaders.USERNAME,
                               in = ParameterIn.HEADER,
                               required = true
                           )
                           String username,
                           @HeaderParam(FenceHeaders.PASSWORD)
                           @Parameter(
                               name = FenceHeaders.PASSWORD,
                               in = ParameterIn.HEADER,
                               required = true
                           )
                           String password) {

        return this.buildFenceResponse(Response.Status.OK);
    }

    @PUT
    @Path("verify")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(rolesAllowed = {RoleType.USER})
    @Verify
    @Operation(
        extensions = {
            @Extension(name = FenceOASFilter.FENCED, value = "true"),
            @Extension(name = FenceOASFilter.SORTED, value = "102")
        }
    )
    public Response verify() {

        return this.buildFenceResponse(Response.Status.OK);
    }

    @GET
    @Path("verify/callback/{country}")
    @Open
    @Operation(extensions = @Extension(name = FenceOASFilter.SORTED, value = "103"))
    public Response verifyCallback(@PathParam("country") String country, @QueryParam("id") Long id) {

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
    @Operation(extensions = @Extension(name = FenceOASFilter.SORTED, value = "104"))
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
    @Operation(extensions = @Extension(name = FenceOASFilter.SORTED, value = "105"))
    public Response retrieveBySubstring(final @PathParam("substring") String substring) {

        return this.buildCacheResponse(
            this.userService.retrieveBySubstring(substring),
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
    @Transactional
    @Operation(
        extensions = {
            @Extension(name = FenceOASFilter.FENCED, value = "true"),
            @Extension(name = FenceOASFilter.SORTED, value = "106")
        }
    )
    public Response delete(final @PathParam("id") Long id) {

        return this.buildFenceResponse(
            this.userService.delete(id),
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
    void setUserService(UserServiceApi userService) {
        this.userService = userService;
    }
}
