package io.serenditree.fence.authentication.service;

import io.quarkus.logging.Log;
import io.serenditree.fence.annotation.Enc;
import io.serenditree.fence.annotation.Fence;
import io.serenditree.fence.annotation.Sig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.DecryptionKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Produces JWKs for JWT encryption and verification.
 * Active and retiring JWKs are maintained for zero downtime key rotation.
 */
@ApplicationScoped
public class JoseJwkProducer {

    public static final String JKW_SIGNATURE_CONFIG_KEY = "serenditree.jwk.signature";
    public static final String JKW_ENCRYPTION_CONFIG_KEY = "serenditree.jwk.encryption";
    public static final String JKW_SIGNATURE_RETIRING_CONFIG_KEY = "serenditree.jwk.signature.retiring";
    public static final String JKW_ENCRYPTION_RETIRING_CONFIG_KEY = "serenditree.jwk.encryption.retiring";

    private static final String JWK_KID = "kid";
    private static final String JWK_SIGNATURE_USE = "sig";
    private static final String JWK_ENCRYPTION_USE = "enc";

    private static final JsonWebKey JWK_SIGNATURE_ACTIVE;
    private static final JsonWebKey JWK_ENCRYPTION_ACTIVE;
    private static final Map<String, JsonWebKey> JWKS_SIGNATURE;
    private static final Map<String, JsonWebKey> JWKS_ENCRYPTION;

    private static final DecryptionKeyResolver DECRYPTION_KEY_RESOLVER = (jwe, context) -> {
        if (jwe.getKeyIdHeaderValue() == null ||
            !JoseJwkProducer.JWKS_ENCRYPTION.containsKey(jwe.getKeyIdHeaderValue())) {
            throw new UnresolvableKeyException("Unresolvable decryption key: " + jwe.getKeyIdHeaderValue());
        }

        return JoseJwkProducer.JWKS_ENCRYPTION
            .get(jwe.getKeyIdHeaderValue())
            .getKey();
    };
    private static final VerificationKeyResolver VERIFICATION_KEY_RESOLVER = (jws, context) -> {
        if (jws.getKeyIdHeaderValue() == null ||
            !JoseJwkProducer.JWKS_SIGNATURE.containsKey(jws.getKeyIdHeaderValue())) {
            throw new UnresolvableKeyException("Unresolvable verification key: " + jws.getKeyIdHeaderValue());
        }

        return JoseJwkProducer.JWKS_SIGNATURE
            .get(jws.getKeyIdHeaderValue())
            .getKey();
    };
    private static final JwtConsumer JWT_CONSUMER = new JwtConsumerBuilder()
        .setDecryptionKeyResolver(DECRYPTION_KEY_RESOLVER)
        .setVerificationKeyResolver(VERIFICATION_KEY_RESOLVER)
        .setRequireJwtId()
        .setRequireIssuedAt()
        .setRequireExpirationTime()
        .setRequireSubject()
        .setExpectedIssuer(JoseTokenService.JWT_ISSUER)
        .setAllowedClockSkewInSeconds(30)
        .build();

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Producer
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Fence
    @Produces
    public JwtConsumer getJwtConsumer() {
        return JWT_CONSUMER;
    }

    @Sig
    @Produces
    public JsonWebKey getSigningKey() {
        return JWK_SIGNATURE_ACTIVE;
    }

    @Enc
    @Produces
    public JsonWebKey getEncryptionKey() {
        return JWK_ENCRYPTION_ACTIVE;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INIT JWKs
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static {
        try {
            var jwkSignatureParams = buildJwkParams(JWK_SIGNATURE_USE, JKW_SIGNATURE_CONFIG_KEY);
            var jwkEncryptionParams = buildJwkParams(JWK_ENCRYPTION_USE, JKW_ENCRYPTION_CONFIG_KEY);
            JWK_SIGNATURE_ACTIVE = JsonWebKey.Factory.newJwk(JsonUtil.toJson(jwkSignatureParams));
            JWK_ENCRYPTION_ACTIVE = JsonWebKey.Factory.newJwk(JsonUtil.toJson(jwkEncryptionParams));

            var jwkSignatureParamsRetiring = buildJwkParams(JWK_SIGNATURE_USE, JKW_SIGNATURE_RETIRING_CONFIG_KEY);
            var jwkEncryptionParamsRetiring = buildJwkParams(JWK_ENCRYPTION_USE, JKW_ENCRYPTION_RETIRING_CONFIG_KEY);
            var jwkSignatureRetiring = JsonWebKey.Factory.newJwk(JsonUtil.toJson(jwkSignatureParamsRetiring));
            var jwkEncryptionRetiring = JsonWebKey.Factory.newJwk(JsonUtil.toJson(jwkEncryptionParamsRetiring));

            JWKS_SIGNATURE = Map.of(
                jwkSignatureParams.get(JWK_KID), JWK_SIGNATURE_ACTIVE,
                jwkSignatureParamsRetiring.get(JWK_KID), jwkSignatureRetiring
            );
            JWKS_ENCRYPTION = Map.of(
                jwkEncryptionParams.get(JWK_KID), JWK_ENCRYPTION_ACTIVE,
                jwkEncryptionParamsRetiring.get(JWK_KID), jwkEncryptionRetiring
            );
        } catch (JoseException e) {
            String message = "Could not create a new JsonWebKey: " + e.getMessage();
            Log.error(message);
            throw new SecurityException(message);
        }
    }

    private static Map<String, String> buildJwkParams(final String use, final String configKey) {
        String key = ConfigProvider.getConfig().getValue(configKey, String.class);

        return Map.of(
            JWK_KID, UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString(),
            "kty", "oct",
            "use", use,
            "k", key
        );
    }
}
