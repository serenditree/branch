package io.serenditree.fence.authentication.service;

import io.serenditree.fence.authentication.service.api.PasswordService;
import io.serenditree.root.util.oak.OakPassword;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ArgonPasswordServiceTest {

    final PasswordService passwordService = new ArgonPasswordService();

    @ParameterizedTest
    @ValueSource(strings = {"plain-text-word-list", "a569b17e-edcc-40eb-b94a-09ff267f0456", "Strong#1..."})
    void hash(final String plainText) {
        String hash1 = this.passwordService.hash(plainText);
        String hash2 = this.passwordService.hash(plainText);
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2);
        assertTrue(OakPassword.passwordHash(hash1));
        assertTrue(OakPassword.passwordHash(hash2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "weak123", "weak#123", "weak-list", "123456789"})
    void hashWeak(final String plainText) {
        assertThrows(BadRequestException.class, () -> this.passwordService.hash(plainText));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plain-text-word-list", "a569b17e-edcc-40eb-b94a-09ff267f0456", "Strong#1..."})
    void verify(final String plainText) {
        String hash = this.passwordService.hash(plainText);
        assertTrue(this.passwordService.verify(hash, plainText));
        assertFalse(this.passwordService.verify(hash, plainText + "-wrong"));
        assertThrows(IllegalArgumentException.class, () -> this.passwordService.verify("invalid-hash", plainText));
    }
}
