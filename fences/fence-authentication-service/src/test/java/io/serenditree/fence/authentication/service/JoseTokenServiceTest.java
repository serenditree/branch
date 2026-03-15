package io.serenditree.fence.authentication.service;

import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.fence.model.enums.RoleType;
import io.serenditree.root.test.extension.LogLevel;
import io.serenditree.root.test.extension.LogLevelExtension;
import io.serenditree.root.test.extension.LogLevels;
import jakarta.ws.rs.NotAuthorizedException;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.spec.SecretKeySpec;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, LogLevelExtension.class})
class JoseTokenServiceTest {

    private static final String HMAC_SHA512 = "HmacSHA512";

    JoseTokenService tokenService;

    @Mock
    JsonWebKey jwkSignature;

    @Mock
    JsonWebKey jwkEncryption;

    @Mock
    JwtConsumer jwtConsumer;

    @Mock
    JwtClaims jwtClaims;

    @Mock
    FencePrincipal notAuthenticated;

    @BeforeEach
    void setUp() {
        when(this.jwkSignature.getKey()).thenReturn(new SecretKeySpec(new byte[64], HMAC_SHA512));
        when(this.jwkEncryption.getKey()).thenReturn(new SecretKeySpec(new byte[64], HMAC_SHA512));

        when(this.notAuthenticated.getId()).thenReturn(1L);
        when(this.notAuthenticated.getUsername()).thenReturn("test");
        when(this.notAuthenticated.getRoleTypes()).thenReturn(List.of(RoleType.USER, RoleType.HUMAN));

        this.tokenService = new JoseTokenService(this.jwtConsumer, this.jwkSignature, this.jwkEncryption);
    }

    @Test
    void buildTokenTest() {
        String token = this.tokenService.buildToken(this.notAuthenticated);

        assertTrue(StringUtils.isNotBlank(token));
        assertTrue(StringUtils.doesNotContainWhitespace(token));
        assertTrue(StringUtils.doesNotContainIsoControlCharacter(token));
        assertEquals(5, token.split("\\.").length);
    }

    @Test
    @LogLevel(LogLevels.WARNING)
    void authenticateTest() throws Exception {
        when(this.jwtClaims.getSubject())
            .thenReturn("1");
        when(this.jwtClaims.getStringClaimValue(JoseTokenService.JWT_USERNAME_KEY))
            .thenReturn("test");
        when(this.jwtClaims.getStringListClaimValue(JoseTokenService.JWT_ROLES_KEY))
            .thenReturn(List.of("USER", "HUMAN"));

        String token = this.tokenService.buildToken(this.notAuthenticated);
        when(this.jwtConsumer.processToClaims(token)).thenReturn(this.jwtClaims);
        FencePrincipal authenticated = this.tokenService.authenticate(token);

        assertEquals(this.notAuthenticated.getId(), authenticated.getId());
        assertEquals(this.notAuthenticated.getUsername(), authenticated.getUsername());
        assertArrayEquals(this.notAuthenticated.getRoleTypes().toArray(), authenticated.getRoleTypes().toArray());

        assertNull(authenticated.getPassword());
        assertNull(authenticated.getEmail());

        assertNotNull(authenticated.getToken());
    }

    @Test
    @LogLevel(LogLevels.SEVERE)
    void invalidTokenTest() throws Exception {
        String token = this.tokenService.buildToken(this.notAuthenticated);
        when(this.jwtConsumer.processToClaims(token))
            .thenThrow(new InvalidJwtException("Invalid token provided", Collections.emptyList(), null));

        assertThrows(NotAuthorizedException.class, () -> this.tokenService.authenticate(token));
    }
}
