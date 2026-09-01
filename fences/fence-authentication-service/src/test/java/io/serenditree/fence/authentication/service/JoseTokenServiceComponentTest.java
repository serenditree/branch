package io.serenditree.fence.authentication.service;

import io.quarkus.test.component.QuarkusComponentTest;
import io.quarkus.test.component.TestConfigProperty;
import io.serenditree.fence.authentication.service.api.TokenService;
import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.fence.model.enums.RoleType;
import io.serenditree.root.test.extension.LogLevel;
import io.serenditree.root.test.extension.LogLevelExtension;
import io.serenditree.root.test.extension.LogLevels;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@QuarkusComponentTest({JoseTokenService.class, JoseJwkProducer.class})
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
    key = "serenditree.log.pretty",
    value = "false"
)
@ExtendWith({MockitoExtension.class, LogLevelExtension.class})
class JoseTokenServiceComponentTest {

    @Inject
    TokenService tokenService;

    @Mock
    FencePrincipal notAuthenticated;

    @BeforeEach
    void setUp() {
        when(this.notAuthenticated.getId()).thenReturn(1L);
        when(this.notAuthenticated.getUsername()).thenReturn("test");
        when(this.notAuthenticated.getRoleTypes()).thenReturn(List.of(RoleType.USER, RoleType.HUMAN));
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
    void authenticateTest() {
        String token = this.tokenService.buildToken(this.notAuthenticated);
        FencePrincipal authenticated = this.tokenService.authenticate(token);

        assertEquals(this.notAuthenticated.getId(), authenticated.getId());
        assertEquals(this.notAuthenticated.getUsername(), authenticated.getUsername());
        assertArrayEquals(this.notAuthenticated.getRoleTypes().toArray(), authenticated.getRoleTypes().toArray());

        assertNull(authenticated.getPassword());
        assertNull(authenticated.getEmail());

        assertNotNull(authenticated.getToken());
    }
}
