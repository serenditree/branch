package io.serenditree.fence.authentication.service;

import io.quarkus.test.component.QuarkusComponentTest;
import io.quarkus.test.component.TestConfigProperty;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.UnresolvableKeyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusComponentTest(JoseJwkProducer.class)
@TestConfigProperty(
    key = JoseJwkProducer.JKW_SIGNATURE_CONFIG_KEY,
    value = "XwI1Zq+mVUd2MJpS4x3DlcXwQ8Bw8vBHkD6FI87Y6iAEf+pv6V9+sitDJT05KDvWV5wK3GulGiXo8LZe3iueMQ=="
)
@TestConfigProperty(
    key = JoseJwkProducer.JKW_ENCRYPTION_CONFIG_KEY,
    value = "BOgiBDNCztiTVzY3KvlK3d7jZVQx74d4Kz8Iil0PmDXjR2Dk7juEZrbiZRm7JBZitp4ZGbTBhP6nEjnAiuJCIQ=="
)
@TestConfigProperty(
    key = JoseJwkProducer.JKW_SIGNATURE_RETIRING_CONFIG_KEY,
    value = "/LtX55NablR3e/Uu9iZ66Z3rK2/HLsRsjZQk3fbsNYVMA8fKoAjbhteQJDdkJ20pki+/WdD+xPof4yyy/4TszQ=="
)
@TestConfigProperty(
    key = JoseJwkProducer.JKW_ENCRYPTION_RETIRING_CONFIG_KEY,
    value = "lWHaL+f6YiKojn0yo/g8XezPZa6qZfDWJytcSc4enHyoIABx2rteoKT/YDW3KbKRYMjxdwtOJh5uWKgokMW9sA=="
)
@TestConfigProperty(
    key = JoseJwkProducerComponentTest.JWT_RETIRED_CONFIG_KEY, // Signed and encrypted with retired keys. Expires 6109.
    value = "eyJhbGciOiJkaXIiLCJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwia2lkIjoiMmVlMzYxMjktYTg3NC0zYmFhLTgyZTAtZmIxMWI2MDc0OGJhIn0..8nexY_5mNts4iwSvP3HKVQ.OCjjrkgy-vgdeyJ1f2v4XSAICrIq3614_vAaVBy11IuhTniMvN8OuzPCY1lsk0vNkLvRfSHaUK63jmNIeXJSPnNk3e2_K2Pt1nh0uXFRbNloJSD1NoBHZQy7V1ZwhJt25zcShC2oiIyGXxjzt0DeOD5HxJ5bZR3wTnuJs4zkiRaTaKtKREM3S3YX-b7CnUSSG8ShmMHVY10rpwpiRi93h3apJ0UUe_yUxu22uB9rRPkgAAQV4RjDU0Wmd0xlS7pClehIGfkR-Xce9_AE4WNwTvn6698LantV4BQ0_o84IkQOtF0ijKWvQiGoNpA9ZPOz2KkCV6wuaak1yysxOWZa1WLOj3GM9a30kSVFfN2QSq5jW4Qz7crgr4GBDotzyhtci8kSS8QiObBX6BAxqRG1XSx5He0KEjr3Rx2QFspfUNi-HX74o05wdRMMNlZ6F64BjvBQP-vcofz7jn69X_pa6DrYghZGdTGoGRRb3D_ySQM.aPrQa6YbOTcuIzSawIVOPEWeYh8gH31wLZEUx3Chjpg"
)
@TestConfigProperty(
    key = JoseJwkProducerComponentTest.JWT_RETIRING_CONFIG_KEY, // Signed and encrypted with retiring keys. Expires 6109.
    value = "eyJhbGciOiJkaXIiLCJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwia2lkIjoiOTRhNWFlMmQtY2MzOS0zMDUwLWJjOTEtYmQ1NmI0OWE1YjJjIn0.._kncJoxwdZ6Cht9Pwc0LOA.9V1LezANv5NbDfJSi6nsPYWibyNLcyx5YVuQT42doUd-fZFiTLbUmESYy9-wWofFORflyGJdz3bW_xwLuELAsoykdaZUUig-GUomwy409NGeGSTdmdtrpkDKudJiqHJSUTto40mk3fQOdy0dnFX7KBfr-qXKcT02T_VWgCpS0ZIvrYhjkgxAgMOfHpTailoL5pFO1w0xg7ls3vutqMhsZnrzASIOIP-YEY-GbaQM-duBxtfrc4GbKMa4s7YbYB4P3K4rq5MJcznYCOhRB3wgC2n4JoTq-a44k3oDXkrnChS0hweV00wObo0VfyzLpCJOIVQHI4rOp8FP9U7VK8n_RYwQ_K1pPASD5s8JHOPnmhlciPoqRhN9qrwKZH3CoOS1ZGnap3nrjf7IT4-suCyh5HPJKKL7STiZOGYcWat1cBuM-JNEz4_Kt1_Oy5i3_GEqwIXvy7eRwOYyvxMzNKAMKg9nlMdV3br8qeaNMFF_wN8.bSoM-Pp-t7DXp5RuOydURhtxsJRJLLYTZNBC9Ra0DME"
)
@TestConfigProperty(
    key = JoseJwkProducerComponentTest.JWT_EXPIRED_CONFIG_KEY, // Expired JWT.
    value = "eyJhbGciOiJkaXIiLCJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwia2lkIjoiOTIwNmUyNGItMGViYS0zNjEyLWExMDUtYTAxNDZkMWJmYmY4In0..8eOxJKr7tothFWQrBCVh6A.ofFW3-mVL8SMZRS2cE3P-J4HsV3gwjklg-A2nAlxKxoZjxOsonLh2fxKwjmCI65QxM8UHh5C0JY9Yku6byd1alg4NWbjrZHfn_Ipn-ZtdQvPXD8S7hCd4bOFjlpoBsWvigQv8_rJlxvS1KL9lhBTO6wnj1NDWq_txnXv8SCQ4vl2aFosBnOKanPCxE-iZngaOtnLYV1hhXgdtHCatrFeWX0uMDlmPGExUEhHrT76Kjuaa0ln22xzQ38BWh-3tLnLtxJSKChU84zUwVIlEeRYTQYqmzBjqvzzZ9tkEGFW6UcHfZyHOAj1K3NU6qegB7oY8ScKCJ3ihxLPqsN3pak1Bai0icyLuj-VLDDJ2ppS-7GBYAg-4VHqLOFpuyFxS40fbSqsu0C4hzGMky1aXQnwVOQOcS0DI55JlY7JXo0xpN5hCcCtnHxOeiYz_7iiXZ3QFFwFb1qC3VaCGfUGBnnOp7brVb27zisPyhqJ6kXq_bk.L23omK7VREhKN-TAQafhmWk_7FfAq0wkngl7xTBwsxo"
)
@TestConfigProperty(
    key = "serenditree.log.pretty",
    value = "false"
)
class JoseJwkProducerComponentTest {

    public static final String JWT_RETIRED_CONFIG_KEY = "serenditree.jwt.retired";
    public static final String JWT_RETIRING_CONFIG_KEY = "serenditree.jwt.retiring";
    public static final String JWT_EXPIRED_CONFIG_KEY = "serenditree.jwt.expired";

    @Inject
    JoseJwkProducer joseJwkProducer;

    @BeforeAll
    static void beforeAll() {
        assertTrue(LocalDate.now().isBefore(LocalDate.of(6109, 1, 1)));
    }

    @Test
    void keyIdTest() {
        final String signatureKey = ConfigProvider
            .getConfig()
            .getValue(JoseJwkProducer.JKW_SIGNATURE_CONFIG_KEY, String.class);
        final String encryptionKey = ConfigProvider
            .getConfig()
            .getValue(JoseJwkProducer.JKW_ENCRYPTION_CONFIG_KEY, String.class);

        final UUID signatureId = UUID.nameUUIDFromBytes(signatureKey.getBytes(StandardCharsets.UTF_8));
        final UUID encryptionId = UUID.nameUUIDFromBytes(encryptionKey.getBytes(StandardCharsets.UTF_8));

        assertEquals(signatureId.toString(), this.joseJwkProducer.getSigningKey().getKeyId());
        assertEquals(encryptionId.toString(), this.joseJwkProducer.getEncryptionKey().getKeyId());
    }

    @Test
    void jwtConsumerRetiredTest() {
        final String retiredJwt = ConfigProvider.getConfig().getValue(JWT_RETIRED_CONFIG_KEY, String.class);

        assertEquals(
            UnresolvableKeyException.class,
            assertThrows(
                InvalidJwtException.class,
                () -> this.joseJwkProducer.getJwtConsumer().processToClaims(retiredJwt)
            ).getCause().getClass()
        );
    }

    @Test
    void jwtConsumerRetiringTest() {
        final String retiringJwt = ConfigProvider.getConfig().getValue(JWT_RETIRING_CONFIG_KEY, String.class);

        assertDoesNotThrow(() -> this.joseJwkProducer.getJwtConsumer().processToClaims(retiringJwt));
    }

    @Test
    void jwtConsumerExpiredTest() {
        final String expiredJwt = ConfigProvider.getConfig().getValue(JWT_EXPIRED_CONFIG_KEY, String.class);

        assertTrue(
            assertThrows(
                InvalidJwtException.class,
                () -> this.joseJwkProducer.getJwtConsumer().processToClaims(expiredJwt)
            ).hasExpired()
        );
    }
}
