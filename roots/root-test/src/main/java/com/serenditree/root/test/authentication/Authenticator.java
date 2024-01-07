package com.serenditree.root.test.authentication;

import com.serenditree.fence.model.FenceHeaders;
import com.serenditree.fence.model.Principal;
import com.serenditree.fence.model.api.FencePrincipal;
import io.restassured.http.Header;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class Authenticator {

    public static final Header NOOP_HEADER = new Header(HttpHeaders.ACCEPT, MediaType.WILDCARD);

    private static final Client CLIENT = ClientBuilder.newClient();
    private static final Entity<?> EMPTY_ENTITY = Entity.entity(null, MediaType.WILDCARD_TYPE);

    private Authenticator() {
    }

    public static FencePrincipal authenticate() {
        Principal principal = new Principal();
        principal.setPassword(UUID.randomUUID().toString());
        principal.setUsername(principal.getPassword().substring(1, 20));

        try (Response response = CLIENT.target("http://localhost:8081/api/v1/user/sign-up")
            .request()
            .header(FenceHeaders.USERNAME, principal.getUsername())
            .header(FenceHeaders.PASSWORD, principal.getPassword())
            .post(EMPTY_ENTITY)) {

            principal.setId(Long.parseLong(response.getHeaderString(FenceHeaders.ID)));
            principal.setToken(response.getHeaderString(HttpHeaders.AUTHORIZATION));
        }

        if (StringUtils.isBlank(principal.getToken())) {
            throw new AssertionError("Authentication failed!");
        }

        return principal;
    }

    public static void cleanup(FencePrincipal principal) {
        try (Response response = CLIENT.target("http://localhost:8081/api/v1/user/delete/{id}")
            .resolveTemplate("id", principal.getId())
            .request()
            .header(HttpHeaders.AUTHORIZATION, principal.getToken())
            .delete()) {

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new AssertionError("Cleanup failed!");
            }
        }
    }
}
