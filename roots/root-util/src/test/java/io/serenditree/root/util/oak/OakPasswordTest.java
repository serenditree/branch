package io.serenditree.root.util.oak;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OakPasswordTest {

    @ParameterizedTest
    @MethodSource("passwordSource")
    void passwordTest(boolean expected, String plaintext) {
        assertEquals(expected, OakPassword.password(plaintext));
    }

    static Stream<Arguments> passwordSource() {
        return Stream.of(
            Arguments.of(true, "JdWlSV,PSIB}1rQ=]U@iS"),
            Arguments.of(true, "rand-word-list-is-good"),
            Arguments.of(true, "rand-word-list-good"),
            Arguments.of(false, "is-bad"),
            Arguments.of(false, "Jd,1*****"),
            Arguments.of(true, "Jd,1******")
        );
    }

    @ParameterizedTest
    @MethodSource("passwordHashSource")
    void passwordHashTest(boolean expected, String hash) {
        assertEquals(expected, OakPassword.passwordHash(hash));
    }

    static Stream<Arguments> passwordHashSource() {
        return Stream.of(
            Arguments.of(true, "$argon2id$v=1$m=1,t=1,p=1$salt$hash"),
            Arguments.of(true, "$argon2id$v=10$m=10,t=10,p=10$salt$hash"),
            Arguments.of(true, "$argon2i$v=10$m=10,t=10,p=10$salt$hash"),
            Arguments.of(true, "$argon2d$v=10$m=10,t=10,p=10$salt$hash"),
            Arguments.of(false, "$argon2id$v=1$m=1,t=t,p=1$salt$hash"),
            Arguments.of(false, "$argon2id$m=1,t=1,p=1$salt$hash"),
            Arguments.of(false, "$argon2id$v=1$m=1,t=1,p=1$hash")
        );
    }
}
