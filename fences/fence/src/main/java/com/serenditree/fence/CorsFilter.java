package com.serenditree.fence;

import com.serenditree.fence.model.FenceHeaders;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.ConfigProvider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    private static final String HOST = ConfigProvider
        .getConfig()
        .getOptionalValue("serenditree.host", String.class)
        .orElse("https://serenditree.io");

    private static final String ACCESS_CONTROL_ALLOW_HEADERS = String.join(
        ", ",
        "Origin",
        HttpHeaders.ACCEPT,
        HttpHeaders.AUTHORIZATION,
        HttpHeaders.WWW_AUTHENTICATE,
        HttpHeaders.CONTENT_TYPE,
        HttpHeaders.ETAG,
        FenceHeaders.ID,
        FenceHeaders.USERNAME,
        FenceHeaders.PASSWORD,
        FenceHeaders.EMAIL,
        FenceHeaders.VERIFICATION
    );

    private static final String ACCESS_CONTROL_EXPOSE_HEADERS = String.join(
        ", ",
        "X-XSS-Protection",
        HttpHeaders.AUTHORIZATION,
        HttpHeaders.WWW_AUTHENTICATE,
        HttpHeaders.ETAG,
        FenceHeaders.ID,
        FenceHeaders.USERNAME
    );

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext) {

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Origin", HOST);
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Headers", ACCESS_CONTROL_ALLOW_HEADERS);
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        headers.add("Access-Control-Expose-Headers", ACCESS_CONTROL_EXPOSE_HEADERS);
        headers.add("Access-Control-Max-Age", 86400);
        headers.add("X-XSS-Protection", 1);
    }
}
