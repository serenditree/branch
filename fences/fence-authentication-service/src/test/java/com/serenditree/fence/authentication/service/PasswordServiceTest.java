package com.serenditree.fence.authentication.service;

import com.serenditree.fence.authentication.service.api.PasswordServiceApi;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.ws.rs.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    final PasswordServiceApi passwordService = new PasswordService();

    @ParameterizedTest
    @ValueSource(strings = {"plain-text-word-list", "a569b17e-edcc-40eb-b94a-09ff267f0456", "Strong#1..."})
    void hash(final String plainText) {
        String hash1 = this.passwordService.hash(plainText);
        String hash2 = this.passwordService.hash(plainText);
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2);
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
    }
}
