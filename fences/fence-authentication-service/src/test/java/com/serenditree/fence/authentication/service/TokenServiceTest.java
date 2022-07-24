package com.serenditree.fence.authentication.service;

import com.serenditree.fence.authentication.service.api.TokenServiceApi;
import com.serenditree.fence.model.Principal;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.root.test.extension.SilentTest;
import com.serenditree.root.test.extension.SilentTestExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SilentTestExtension.class)
class TokenServiceTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BEFORE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @BeforeAll
    public static void beforeAll() {
        assertEquals("9SYiJgVsBJscfJ/1HjElV2YN0m0CwULyE2rg4k6kCdw=", System.getenv("SERENDITREE_JSON_WEB_KEY"));
        assertEquals("test", System.getProperty("serenditree.context"));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TESTS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void buildTokenTest() {
        TokenServiceApi tokenService = new TokenService();
        FencePrincipal expectedPrincipal = new Principal(1L, "test", "test", null, "test@serenditree.io");
        String token = tokenService.buildToken(expectedPrincipal);

        assertTrue(StringUtils.isNotBlank(token));
        assertTrue(StringUtils.doesNotContainWhitespace(token));
        assertTrue(StringUtils.doesNotContainIsoControlCharacter(token));
    }

    @Test
    @SilentTest
    void authenticateTest() {
        TokenServiceApi tokenService = new TokenService();
        FencePrincipal notAuthenticated = new Principal(1L, "test", "test", null, "test@serenditree.io");
        String token = tokenService.buildToken(notAuthenticated);
        FencePrincipal authenticated = tokenService.authenticate(token);

        assertEquals(notAuthenticated.getId(), authenticated.getId());
        assertEquals(notAuthenticated.getUsername(), authenticated.getUsername());

        assertNull(authenticated.getPassword());
        assertNull(authenticated.getEmail());

        assertNotNull(authenticated.getToken());
    }
}
