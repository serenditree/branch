package com.serenditree.fence;

import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.annotation.Open;
import com.serenditree.fence.authentication.service.api.TokenServiceApi;
import com.serenditree.fence.interceptor.FenceRecordInterceptor;
import com.serenditree.fence.model.FenceContext;
import com.serenditree.fence.model.FenceHeaders;
import com.serenditree.fence.model.FenceRecord;
import com.serenditree.fence.model.FenceResponse;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.rest.cache.annotation.CacheControlConfig;
import com.serenditree.root.rest.cache.annotation.CustomCacheControl;
import com.serenditree.root.rest.transfer.ApiDescription;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;

import java.net.URI;
import java.util.Collection;
import java.util.function.Predicate;


public abstract class AbstractFenceEndpoint {

    @Inject
    @ConfigProperty(name = "serenditree.service")
    String service;

    @Inject
    @ConfigProperty(name = "serenditree.version")
    String version;

    @Inject
    @ConfigProperty(name = "serenditree.stage")
    String stage;

    @Inject
    @ConfigProperty(name = "quarkus.http.port")
    String port;

    @Inject
    @ConfigProperty(name = "quarkus.swagger-ui.always-include")
    boolean swagger;

    @Inject
    @FencedContext
    FenceContext fenceContext;

    @Context
    Request request;

    @Context
    UriInfo uriInfo;

    private CacheControl cacheControl;

    private TokenServiceApi tokenService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    @Operation(hidden = true)
    public Response retrieveApiDescription() {
        return Response
            .ok(this.buildApiDescription())
            .build();
    }

    @GET
    @Path("echo")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    @Operation(hidden = true)
    public Response retrieveEcho(@QueryParam("status") Integer status, @Context HttpHeaders httpHeaders) {
        return this.buildEcho(status, httpHeaders);
    }

    @GET
    @Path("auth/{userId}/{entityId}/{action}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(rolesAllowed = {RoleType.USER})
    @Operation(
        summary = "Authorization Simulation",
        extensions = {
            @Extension(name = FenceOASFilter.FENCED, value = "true"),
            @Extension(name = FenceOASFilter.SORTED, value = "900")
        }
    )
    public Response retrieveAuthInformation(@PathParam("userId") Long userId,
                                            @PathParam("entityId") Long entityId,
                                            @PathParam("action") String action) {
        Log.error("Retrieval of auth information not handled in filter");
        return Response
            .serverError()
            .build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: RESPONSE BUILDER
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected final Predicate<Collection<?>> notNullNotEmpty = result -> result != null && !result.isEmpty();

    protected final Predicate<Boolean> isTrue = Boolean::booleanValue;

    /**
     * Tests and tags results from services and builds a {@link Response} with status code dependent on the predicate.
     * @param result Result from service.
     * @param predicate Predicate for testing the result.
     * @param success {@link Response.Status} after successful test.
     * @param error {@link Response.Status} after failed test.
     * @return {@link Response} with {@link EntityTag} and response entity.
     * @param <E> Type of the results from services.
     */
    protected <E> Response buildCacheResponse(E result,
                                              Predicate<E> predicate,
                                              Response.Status success,
                                              Response.Status error) {
        Response.ResponseBuilder responseBuilder;

        if (predicate.test(result)) {
            EntityTag entityTag = new EntityTag(Integer.toString(result.hashCode()));
            responseBuilder = this.request.evaluatePreconditions(entityTag);

            if (responseBuilder == null) {
                responseBuilder = Response
                    .status(success)
                    .entity(result)
                    .tag(entityTag);
            }
            responseBuilder.cacheControl(this.cacheControl);
        } else {
            responseBuilder = Response.status(error);
            if (result != null) {
                responseBuilder.entity(result);
            }
        }

        return responseBuilder.build();
    }

    /**
     * Tests and tags results from services and builds a {@link Response} with status code dependent on the predicate.
     * This a shorthand version where the success status code is always 200 (OK).
     * @param result Result from service.
     * @param predicate Predicate for testing the result.
     * @param error {@link Response.Status} after failed test.
     * @return {@link Response} with {@link EntityTag} and response entity.
     * @param <E> Type of the results from services.
     */
    protected <E> Response buildCacheResponse(E result, Predicate<E> predicate, Response.Status error) {
        return this.buildCacheResponse(result, predicate, Response.Status.OK, error);
    }

    /**
     * Tests results from services and starts a {@link Response.ResponseBuilder} with status code dependent on the
     * predicate.
     * @param result Result from service.
     * @param predicate Predicate for testing the result.
     * @param success {@link Response.Status} after successful test.
     * @param error {@link Response.Status} after failed test.
     * @return {@link Response.ResponseBuilder} with response entity.
     * @param <E> Type of the results from services.
     */
    protected <E> Response.ResponseBuilder buildResponseBuilder(E result,
                                                                Predicate<E> predicate,
                                                                Response.Status success,
                                                                Response.Status error) {
        Response.ResponseBuilder responseBuilder;

        if (predicate.test(result)) {
            responseBuilder = Response
                .status(success)
                .entity(result);
        } else {
            responseBuilder = Response.status(error);
            if (result != null) {
                responseBuilder.entity(result);
            }
        }

        return responseBuilder;
    }

    /**
     * Tests results from services and builds a {@link Response} with status code dependent on the predicate.
     * @param result Result from service.
     * @param predicate Predicate for testing the result.
     * @param success {@link Response.Status} after successful test.
     * @param error {@link Response.Status} after failed test.
     * @return {@link Response} with response entity.
     * @param <E> Type of the results from services.
     */
    protected <E> Response buildResponse(E result,
                                         Predicate<E> predicate,
                                         Response.Status success,
                                         Response.Status error) {
        return this.buildResponseBuilder(result, predicate, success, error).build();
    }

    /**
     * Tests results from services and builds a {@link Response} with status code dependent on the predicate.
     * This a shorthand version where the success status code is always 200 (OK).
     * @param result Result from service.
     * @param predicate Predicate for testing the result.
     * @param error {@link Response.Status} after failed test.
     * @return {@link Response} with response entity.
     * @param <E> Type of the results from services.
     */
    protected <E> Response buildResponse(E result, Predicate<E> predicate, Response.Status error) {
        return this.buildResponse(result, predicate, Response.Status.OK, error);
    }

    /**
     * Builds a redirect {@link Response} with encoded/encrypted OIDC claims for the UI.
     * @param id ID of the verified user.
     * @param subject OIDC subject of the verified user.
     * @param apiPrefix API prefix to remove to get the host.
     * @return {@link Response} for UI-redirect.
     */
    protected Response buildFenceVerificationRedirect(final Long id, final String subject, final String apiPrefix) {
        String redirect = this.uriInfo
            .getBaseUriBuilder()
            .path("user")
            .path("settings")
            .queryParam("oidc", this.tokenService.buildVerificationToken(id, subject))
            .build()
            .toString()
            .replace(apiPrefix, "")
            .replace(this.port, "8080");

        return Response
            .seeOther(URI.create(redirect))
            .build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SECURITY
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the {@link FencePrincipal} from the {@link FenceContext}.
     * @return {@link FencePrincipal}
     */
    protected FencePrincipal getPrincipal() {
        return this.fenceContext.getUserPrincipal();
    }

    /**
     * Builds a {@link Response} with security headers from the {@link FenceContext}.
     * @param status HTTP status of the response.
     * @return {@link Response} with security headers.
     */
    protected Response buildFenceResponse(Response.Status status) {
        FencePrincipal principal = this.getPrincipal();

        return Response
            .status(status)
            .header(HttpHeaders.AUTHORIZATION, principal.getToken())
            .header(FenceHeaders.ID, principal.getId())
            .header(FenceHeaders.USERNAME, principal.getUsername())
            .header(FenceHeaders.VERIFIED, principal.getRoleTypes().contains(RoleType.HUMAN))
            .build();
    }

    /**
     * Builds a response that can be processed by the {@link FenceRecordInterceptor}.
     * @param result Result from service.
     * @param predicate Predicate for testing the result.
     * @param entityId ID for the {@link FenceRecord} to create.
     * @param success {@link Response.Status} after successful test.
     * @param error {@link Response.Status} after failed test.
     * @return {@link Response} with response entity.
     * @param <E> Type of the results from services.
     */
    protected <E> Response buildFenceResponse(E result,
                                              Predicate<E> predicate,
                                              String entityId,
                                              Response.Status success,
                                              Response.Status error) {
        return this.buildResponseBuilder(result, predicate, success, error)
            .entity(new FenceResponse(entityId))
            .build();

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: API DESCRIPTION
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ApiDescription buildApiDescription() {
        ApiDescription apiDescription = new ApiDescription();

        String servicePath = this.service.split("-")[1];
        UriBuilder uriBuilder = this.uriInfo
            .getBaseUriBuilder()
            .path(servicePath);

        apiDescription.setService("serenditree/" + this.service);
        apiDescription.setVersion(this.version);
        apiDescription.setStage(this.stage);
        apiDescription.setOpenapi(uriBuilder.path("openapi").build().toString());
        apiDescription.setOpenapiJson(uriBuilder.queryParam("format", "json").build().toString());
        if (this.swagger) {
            apiDescription.setSwagger(
                this.uriInfo
                    .getBaseUriBuilder()
                    .path(servicePath)
                    .path("swagger")
                    .build()
                    .toString()
            );
        }

        return apiDescription;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: DEBUG
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Response buildEcho(final int status, final HttpHeaders httpHeaders) {
        Response.ResponseBuilder responseBuilder = Response.status(status);

        httpHeaders.getRequestHeaders()
            .entrySet()
            .stream()
            .flatMap(entry -> entry
                .getValue()
                .stream()
                .map(value -> Pair.of(entry.getKey(), value))
            )
            .forEach(pair -> responseBuilder.header(pair.getKey(), pair.getValue()));

        return responseBuilder.build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    void setCacheControl(@CustomCacheControl CacheControl cacheControl) {
        CacheControlConfig config = this.getClass().getAnnotation(CacheControlConfig.class);

        if (config != null) {
            Log.debugv("CacheControlConfig found: {0}", config);
            cacheControl.setMaxAge(config.maxAge());
            cacheControl.setMustRevalidate(config.mustRevalidate());
            cacheControl.setNoCache(config.noCache());
            cacheControl.setNoStore(config.noStore());
            cacheControl.setNoTransform(config.noTransform());
            cacheControl.setPrivate(config.isPrivate());
            cacheControl.setProxyRevalidate(config.proxyRevalidate());
            cacheControl.setSMaxAge(config.sMaxAge());
        }

        this.cacheControl = cacheControl;
    }

    @Inject
    public void setTokenService(TokenServiceApi tokenService) {
        this.tokenService = tokenService;
    }
}
