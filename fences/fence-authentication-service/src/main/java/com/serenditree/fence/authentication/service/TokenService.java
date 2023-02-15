package com.serenditree.fence.authentication.service;

import com.serenditree.fence.authentication.service.api.AuthenticationServiceApi;
import com.serenditree.fence.authentication.service.api.TokenServiceApi;
import com.serenditree.fence.model.FenceContext;
import com.serenditree.fence.model.Principal;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.etc.maple.Maple;
import com.serenditree.root.rest.transfer.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jose4j.json.JsonUtil;
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
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import javax.enterprise.context.Dependent;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Builds and verifies JOSE tokens. The environment needs to provide an 256bit key in a variable called
 * SERENDITREE_JSON_WEB_KEY.
 */
@Dependent
public class TokenService implements TokenServiceApi {

    private static final Logger LOGGER = Logger.getLogger(TokenService.class.getName());

    private static final String ISSUER = "serenditree.io";
    private static final String WWW_AUTHENTICATE = FenceContext.AUTHENTICATION_SCHEME + " realm=\"serenditree.io\"";
    private static final String ROLES_KEY = "roles";
    private static final String USERNAME_KEY = "username";
    private static final int JWT_CONSUMER_ALLOWED_SKEW_SECONDS = 30;
    private static final int CLAIMS_EXPIRATION_TIME_MINUTES = 60 * 24;
    private static final int CLAIMS_NOT_BEFORE_MINUTES = 2;
    private static final String JWE_CONTENT_TYPE = "JWT";
    private static final Map<String, String> JWK_PARAMS = Map.of(
        "kty", "oct",
        "k", ConfigProvider.getConfig().getValue("serenditree.json.web.key", String.class)
    );
    private static final JsonWebKey JWK;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INIT JWK
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static {
        // Static initialization because of the key generation costs.
        try {
            JWK = JsonWebKey.Factory.newJwk(JsonUtil.toJson(JWK_PARAMS));
        } catch (JoseException e) {
            String message = "Could not create a new JsonWebKey: " + e.getMessage();
            LOGGER.severe(message);
            throw new SecurityException(message);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves user information from a signed and encrypted token of a user that is already signed in.
     *
     * @param token Signed and encrypted token
     * @return {@link FencePrincipal} extracted from token. Verified user information.
     */
    @Override
    public FencePrincipal authenticate(String token) {
        FencePrincipal principal;

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setRequireExpirationTime()
            .setAllowedClockSkewInSeconds(TokenService.JWT_CONSUMER_ALLOWED_SKEW_SECONDS)
            .setRequireSubject()
            .setExpectedIssuer(TokenService.ISSUER)
            .setDecryptionKey(TokenService.JWK.getKey())
            .setVerificationKey(TokenService.JWK.getKey()).build();

        try {
            // Retrieve user-claims/information
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            Long id = Long.parseLong(jwtClaims.getSubject());
            String username = jwtClaims.getStringClaimValue(TokenService.USERNAME_KEY);
            List<RoleType> roleTypes = Maple.mapList(
                jwtClaims.getStringListClaimValue(TokenService.ROLES_KEY), RoleType::valueOf);

            // Create Principal
            principal = new Principal();
            principal.setId(id);
            principal.setUsername(username);
            principal.setToken(token);
            principal.setRoleTypes(roleTypes);

        } catch (InvalidJwtException e) {
            // TODO check reason. If it is not expired it is possibly a threat.
            ApiResponse apiResponse = new ApiResponse("Invalid claims provided: " + e.getMessage());
            LOGGER.warning(apiResponse.getMessage());
            if (e.hasExpired()) {
                throw new NotAuthorizedException(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity(apiResponse)
                        .header(HttpHeaders.WWW_AUTHENTICATE, WWW_AUTHENTICATE)
                        .build()
                );
            } else {
                throw new NotAuthorizedException(apiResponse.getMessage());
            }
        } catch (MalformedClaimException e) {
            // TODO decide if this is threatening.
            String message = "Could not process malformed claims: " + e.getMessage();
            LOGGER.warning(message);
            throw new NotAuthorizedException(message);
        } catch (NumberFormatException e) {
            String message = "Could not retrieve subject claim: " + e.getMessage();
            LOGGER.warning(message);
            throw new NotAuthorizedException(message);
        }

        return principal;
    }

    /**
     * Builds a JOSE token for a Principal who has already been authenticated by {@link AuthenticationServiceApi} ie
     * by username and password or oauth.
     *
     * @param principal Encapsulated ID information
     * @return Token
     */
    @Override
    public String buildToken(FencePrincipal principal) {
        String jwt;
        try {
            jwt = this.buildJsonWebSignature(this.buildJwtClaims(principal)).getCompactSerialization();
        } catch (JoseException e) {
            String message = "Could not serialize JWT: " + e.getMessage();
            LOGGER.warning(message);
            throw new NotAuthorizedException(message);
        }

        String jweSerialization;
        try {
            jweSerialization = this.buildJsonWebEncryption(jwt).getCompactSerialization();
        } catch (JoseException e) {
            String message = "Could not serialize JWE: " + e.getMessage();
            LOGGER.warning(message);
            throw new NotAuthorizedException(message);
        }

        return jweSerialization;
    }

    @Override
    public String buildVerificationToken(final Long id, final String subject) {
        if (id == null || id < 1L || StringUtils.isBlank(subject)) {
            throw new BadRequestException("Missing or invalid information for verification token.");
        }
        FencePrincipal fencePrincipal = new Principal();
        // ID of target user.
        fencePrincipal.setId(id);
        fencePrincipal.setUsername(subject);
        fencePrincipal.setRoleTypes(List.of(RoleType.USER, RoleType.HUMAN));

        return this.buildToken(fencePrincipal);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB: BUILD TOKEN
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private JwtClaims buildJwtClaims(FencePrincipal principal) {
        JwtClaims jwtClaims = new JwtClaims();

        jwtClaims.setIssuer(TokenService.ISSUER);
        jwtClaims.setExpirationTimeMinutesInTheFuture(TokenService.CLAIMS_EXPIRATION_TIME_MINUTES);
        jwtClaims.setGeneratedJwtId();
        jwtClaims.setIssuedAtToNow();
        jwtClaims.setNotBeforeMinutesInThePast(TokenService.CLAIMS_NOT_BEFORE_MINUTES);
        // Principal information
        jwtClaims.setSubject(principal.getId().toString());
        jwtClaims.setStringClaim(TokenService.USERNAME_KEY, principal.getUsername());
        jwtClaims.setStringListClaim(
            TokenService.ROLES_KEY,
            Maple.mapList(principal.getRoleTypes(), RoleType::toString)
        );

        return jwtClaims;
    }

    private JsonWebSignature buildJsonWebSignature(JwtClaims jwtClaims) {
        JsonWebSignature jws = new JsonWebSignature();

        jws.setPayload(jwtClaims.toJson());
        jws.setKeyIdHeaderValue(TokenService.JWK.getKeyId());
        jws.setKey(TokenService.JWK.getKey());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);

        return jws;
    }

    private JsonWebEncryption buildJsonWebEncryption(String jwt) {
        JsonWebEncryption jwe = new JsonWebEncryption();

        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        jwe.setKey(TokenService.JWK.getKey());
        jwe.setKeyIdHeaderValue(TokenService.JWK.getKeyId());
        jwe.setContentTypeHeaderValue(TokenService.JWE_CONTENT_TYPE);
        jwe.setPayload(jwt);

        return jwe;
    }
}
