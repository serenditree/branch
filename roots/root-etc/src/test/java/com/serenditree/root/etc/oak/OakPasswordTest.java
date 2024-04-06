package com.serenditree.root.etc.oak;

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
}
