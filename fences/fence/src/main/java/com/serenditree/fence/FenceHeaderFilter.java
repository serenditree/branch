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
public class FenceHeaderFilter implements ContainerResponseFilter {

    private static final String HOST = ConfigProvider
        .getConfig()
        .getOptionalValue("serenditree.host", String.class)
        .orElse("https://serenditree.io");

    private static final String ACCESS_CONTROL_ALLOW_HEADERS = String.join(
        ", ",
        "Origin",
        FenceHeaders.EMAIL,
        FenceHeaders.ID,
        FenceHeaders.PASSWORD,
        FenceHeaders.USERNAME,
        FenceHeaders.VERIFICATION,
        HttpHeaders.ACCEPT,
        HttpHeaders.AUTHORIZATION,
        HttpHeaders.CONTENT_TYPE,
        HttpHeaders.ETAG,
        HttpHeaders.WWW_AUTHENTICATE
    );

    private static final String ACCESS_CONTROL_EXPOSE_HEADERS = String.join(
        ", ",
        FenceHeaders.ID,
        FenceHeaders.USERNAME,
        HttpHeaders.AUTHORIZATION,
        HttpHeaders.ETAG,
        HttpHeaders.WWW_AUTHENTICATE
    );

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext) {

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Headers", ACCESS_CONTROL_ALLOW_HEADERS);
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        headers.add("Access-Control-Allow-Origin", HOST);
        headers.add("Access-Control-Expose-Headers", ACCESS_CONTROL_EXPOSE_HEADERS);
        headers.add("Access-Control-Max-Age", "86400");
        headers.add("Content-Security-Policy", "default-src 'self'");
        headers.add("Strict-Transport-Security", "max-age=63072000; includeSubDomains; preload");
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "deny");
        headers.add("X-XSS-Protection", "1; mode=block");
    }
}
