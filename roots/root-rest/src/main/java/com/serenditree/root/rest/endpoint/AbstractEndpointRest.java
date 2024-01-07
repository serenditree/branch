package com.serenditree.root.rest.endpoint;

import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.annotation.Open;
import com.serenditree.fence.model.FenceContext;
import com.serenditree.fence.model.FenceHeaders;
import com.serenditree.fence.model.FenceResponse;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.rest.cache.annotation.CacheControlConfig;
import com.serenditree.root.rest.cache.annotation.CustomCacheControl;
import com.serenditree.root.rest.transfer.ApiDescription;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractEndpointRest {

    private static final Logger LOGGER = Logger.getLogger(AbstractEndpointRest.class.getName());

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
    @ConfigProperty(name = "serenditree.host", defaultValue = "https://serenditree.io")
    String host;

    @Inject
    @FencedContext
    FenceContext fenceContext;

    @Context
    Request request;

    CacheControl cacheControl;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response retrieveApiDescription() {
        return Response
            .ok(this.buildApiDescription())
            .build();
    }

    @GET
    @Path("auth/{userId}/{entityId}/{action}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fenced(rolesAllowed = {RoleType.USER})
    public Response retrieveAuthInformation(@PathParam("userId") Long userId,
                                            @PathParam("entityId") Long entityId,
                                            @PathParam("action") String action) {
        LOGGER.severe("Retrieval of auth information not handled in filter");
        return Response
            .serverError()
            .build();
    }

    @GET
    @Path("echo")
    @Produces(MediaType.APPLICATION_JSON)
    @Open
    public Response echo(@QueryParam("status") Integer status, @Context HttpHeaders httpHeaders) {
        return this.buildEcho(status, httpHeaders);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: RESPONSE BUILDER
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    protected <E> Response buildCacheResponse(E result, Predicate<E> predicate, Response.Status error) {

        return this.buildCacheResponse(result, predicate, Response.Status.OK, error);
    }

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

    protected <E> Response buildResponse(E result,
                                         Predicate<E> predicate,
                                         Response.Status success,
                                         Response.Status error) {

        return this.buildResponseBuilder(result, predicate, success, error).build();
    }

    protected <E> Response buildResponse(E result, Predicate<E> predicate, Response.Status error) {
        return this.buildResponse(result, predicate, Response.Status.OK, error);
    }

    protected Response buildRedirect(final String path) {
        URI redirect = URI.create(
            StringUtils.removeEnd(this.host, "/")
            + "/"
            + StringUtils.removeStart(path, "/")
        );

        return Response
            .seeOther(redirect)
            .build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: CONVENIENCE PREDICATES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected final Predicate<Collection<?>> notNullNotEmpty = result -> result != null && !result.isEmpty();

    protected final Predicate<Boolean> isTrue = Boolean::booleanValue;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SECURITY
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected FencePrincipal getPrincipal() {
        return this.fenceContext.getUserPrincipal();
    }

    protected Response buildFenceResponse(Response.Status status) {
        FencePrincipal principal = this.getPrincipal();

        return Response
            .status(status)
            .header(HttpHeaders.AUTHORIZATION, principal.getToken())
            .header(FenceHeaders.ID, principal.getId())
            .header(FenceHeaders.USERNAME, principal.getUsername())
            .build();
    }

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
    // SUB: DEBUG
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Response buildEcho(final int status, final HttpHeaders httpHeaders) {
        Response.ResponseBuilder responseBuilder = Response.status(status);
        httpHeaders.getRequestHeaders().entrySet().stream()
            .flatMap(entry -> entry.getValue().stream().map(value -> Pair.of(entry.getKey(), value)))
            .forEach(pair -> responseBuilder.header(pair.getKey(), pair.getValue()));

        return responseBuilder.build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: API DESCRIPTION
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static String getHttpMethod(Method method) {
        String httpMethod;

        if (method.isAnnotationPresent(GET.class)) {
            httpMethod = GET.class.getSimpleName();
        } else if (method.isAnnotationPresent(POST.class)) {
            httpMethod = POST.class.getSimpleName();
        } else if (method.isAnnotationPresent(PUT.class)) {
            httpMethod = PUT.class.getSimpleName();
        } else if (method.isAnnotationPresent(DELETE.class)) {
            httpMethod = DELETE.class.getSimpleName();
        } else if (method.isAnnotationPresent(HEAD.class)) {
            httpMethod = HEAD.class.getSimpleName();
        } else if (method.isAnnotationPresent(OPTIONS.class)) {
            httpMethod = OPTIONS.class.getSimpleName();
        } else {
            throw new IllegalStateException("Undefined HTTP method.");
        }

        return httpMethod + " ";
    }

    private Map<String, String> getApi() {
        return Arrays.stream(this.getClass().getMethods())
            .filter(m -> m.isAnnotationPresent(Path.class))
            .collect(
                Collectors.toMap(
                    Method::getName,
                    m -> AbstractEndpointRest.getHttpMethod(m) + m.getAnnotation(Path.class).value(),
                    (m1, m2) -> {
                        throw new IllegalStateException("Overloading API is not allowed.");
                    },
                    TreeMap::new
                )
            );
    }

    private ApiDescription buildApiDescription() {

        ApiDescription apiDescription = new ApiDescription();

        apiDescription.setServiceName(this.service);
        apiDescription.setArtifactVersion(this.version);
        apiDescription.setStage(this.stage);
        apiDescription.setApi(this.getApi());

        return apiDescription;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    void setCacheControl(@CustomCacheControl CacheControl cacheControl) {

        CacheControlConfig config = this.getClass().getAnnotation(CacheControlConfig.class);

        if (config != null) {
            LOGGER.fine(() -> "CacheControlConfig found: " + config);
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
}
