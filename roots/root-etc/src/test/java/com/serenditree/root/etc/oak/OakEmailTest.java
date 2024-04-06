package com.serenditree.root.etc.oak;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OakEmailTest {

    @ParameterizedTest
    @MethodSource("emailSource")
    void emailTest(boolean expected, String email) {
        assertEquals(expected, OakEmail.email(email));
    }

    static Stream<Arguments> emailSource() {
        return Stream.of(
            Arguments.of(true, "name@domain.com"),
            Arguments.of(true, "name1@domain.com"),
            Arguments.of(true, "first.last@domain.com"),
            Arguments.of(true, "first-last@domain.com"),
            Arguments.of(true, "first_last@domain.com"),
            Arguments.of(true, "first.last1@domain.com"),
            Arguments.of(true, "first.last@domain.sub.domain"),
            Arguments.of(false, "first#last@domain.com"),
            Arguments.of(false, ".@domain.com"),
            Arguments.of(false, "-@domain.com"),
            Arguments.of(false, "_@domain.com"),
            Arguments.of(false, "name@domain"),
            Arguments.of(false, "domain.com"),
            Arguments.of(false, "@domain.com")
        );
    }
}
