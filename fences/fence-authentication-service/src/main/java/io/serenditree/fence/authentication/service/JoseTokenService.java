package io.serenditree.fence.authentication.service;

import io.quarkus.logging.Log;
import io.serenditree.fence.annotation.Enc;
import io.serenditree.fence.annotation.Fence;
import io.serenditree.fence.annotation.Sig;
import io.serenditree.fence.authentication.service.api.AuthenticationService;
import io.serenditree.fence.authentication.service.api.TokenService;
import io.serenditree.fence.model.FenceContext;
import io.serenditree.fence.model.Principal;
import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.fence.model.enums.RoleType;
import io.serenditree.root.rest.transfer.ApiResponse;
import io.serenditree.root.util.maple.Maple;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.lang.JoseException;

import java.util.List;

/**
 * Builds and verifies JOSE tokens.
 */
@Dependent
public class JoseTokenService implements TokenService {

    public static final String JWT_ISSUER = "serenditree.io";
    public static final String JWT_USERNAME_KEY = "username";
    public static final String JWT_ROLES_KEY = "roles";

    private static final String WWW_AUTHENTICATE = FenceContext.AUTHENTICATION_SCHEME + " realm=\"serenditree.io\"";
    private static final String JWE_CONTENT_TYPE = "JWT";
    private static final int JWT_EXPIRATION_TIME_MINUTES = 60 * 24;

    private final JwtConsumer jwtConsumer;
    private final JsonWebKey jwkSignature;
    private final JsonWebKey jwkEncryption;

    @Inject
    public JoseTokenService(
        @Fence JwtConsumer jwtConsumer,
        @Sig JsonWebKey jwkSignature,
        @Enc JsonWebKey jwkEncryption
    ) {
        this.jwtConsumer = jwtConsumer;
        this.jwkSignature = jwkSignature;
        this.jwkEncryption = jwkEncryption;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves user information from a signed and encrypted token of a user that is already signed in.
     *
     * @param token Signed and encrypted token.
     * @return {@link FencePrincipal} extracted from token. Verified user information.
     */
    @Override
    public FencePrincipal authenticate(String token) {
        FencePrincipal principal;

        try {
            // Retrieve user-claims/information
            JwtClaims jwtClaims = this.jwtConsumer.processToClaims(token);
            Long id = Long.parseLong(jwtClaims.getSubject());
            String username = jwtClaims.getStringClaimValue(JWT_USERNAME_KEY);
            List<RoleType> roleTypes = Maple.mapList(
                jwtClaims.getStringListClaimValue(JWT_ROLES_KEY),
                RoleType::valueOf
            );

            // Create Principal
            principal = new Principal();
            principal.setId(id);
            principal.setUsername(username);
            principal.setToken(token);
            principal.setRoleTypes(roleTypes);

        } catch (InvalidJwtException e) {
            ApiResponse apiResponse = new ApiResponse("Invalid claims provided: " + e.getMessage());
            if (e.hasExpired()) {
                throw new NotAuthorizedException(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity(apiResponse)
                        .header(HttpHeaders.WWW_AUTHENTICATE, WWW_AUTHENTICATE)
                        .build()
                );
            } else {
                Log.warn(apiResponse.getMessage());
                throw new NotAuthorizedException(apiResponse.getMessage());
            }
        } catch (MalformedClaimException e) {
            String message = "Could not process malformed claims: " + e.getMessage();
            Log.warn(message);
            throw new NotAuthorizedException(message);
        } catch (NumberFormatException e) {
            String message = "Could not retrieve subject claim: " + e.getMessage();
            Log.warn(message);
            throw new NotAuthorizedException(message);
        }

        return principal;
    }

    /**
     * Builds a JOSE token for a Principal who has already been authenticated by {@link AuthenticationService} ie
     * by username and password or oauth.
     *
     * @param principal Encapsulated ID information.
     * @return Signed and encrypted JWT.
     */
    @Override
    public String buildToken(FencePrincipal principal) {
        String jwt;
        try {
            jwt = this.buildJsonWebSignature(this.buildJwtClaims(principal)).getCompactSerialization();
        } catch (JoseException e) {
            String message = "Could not serialize JWT: " + e.getMessage();
            Log.warn(message);
            throw new NotAuthorizedException(message);
        }

        String jweSerialization;
        try {
            jweSerialization = this.buildJsonWebEncryption(jwt).getCompactSerialization();
        } catch (JoseException e) {
            String message = "Could not serialize JWE: " + e.getMessage();
            Log.warn(message);
            throw new NotAuthorizedException(message);
        }

        return jweSerialization;
    }

    /**
     * Builds a token for the OIDC verification callback.
     *
     * @param id      ID of the target user.
     * @param subject OIDC subject identifier.
     * @return Signed and encrypted JWT.
     */
    @Override
    public String buildVerificationToken(final Long id, final String subject) {
        if (id == null || id < 1L || StringUtils.isBlank(subject)) {
            throw new BadRequestException("Missing or invalid information for verification token.");
        }
        FencePrincipal fencePrincipal = new Principal();
        // ID of target user.
        fencePrincipal.setId(id);
        // OIDC sub
        fencePrincipal.setUsername(subject);
        fencePrincipal.setRoleTypes(List.of(RoleType.USER, RoleType.HUMAN));

        return this.buildToken(fencePrincipal);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: BUILD TOKEN
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private JwtClaims buildJwtClaims(FencePrincipal principal) {
        JwtClaims jwtClaims = new JwtClaims();

        jwtClaims.setIssuer(JWT_ISSUER);
        jwtClaims.setExpirationTimeMinutesInTheFuture(JWT_EXPIRATION_TIME_MINUTES);
        jwtClaims.setGeneratedJwtId();
        jwtClaims.setIssuedAtToNow();
        // Principal information
        jwtClaims.setSubject(principal.getId().toString());
        jwtClaims.setStringClaim(JWT_USERNAME_KEY, principal.getUsername());
        jwtClaims.setStringListClaim(
            JWT_ROLES_KEY,
            Maple.mapList(principal.getRoleTypes(), RoleType::toString)
        );

        return jwtClaims;
    }

    private JsonWebSignature buildJsonWebSignature(JwtClaims jwtClaims) {
        JsonWebSignature jws = new JsonWebSignature();

        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
        jws.setKey(this.jwkSignature.getKey());
        jws.setKeyIdHeaderValue(this.jwkSignature.getKeyId());
        jws.setPayload(jwtClaims.toJson());

        return jws;
    }

    private JsonWebEncryption buildJsonWebEncryption(String jwt) {
        JsonWebEncryption jwe = new JsonWebEncryption();

        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
        jwe.setContentTypeHeaderValue(JWE_CONTENT_TYPE);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512);
        jwe.setKey(this.jwkEncryption.getKey());
        jwe.setKeyIdHeaderValue(this.jwkEncryption.getKeyId());
        jwe.setPayload(jwt);

        return jwe;
    }
}
