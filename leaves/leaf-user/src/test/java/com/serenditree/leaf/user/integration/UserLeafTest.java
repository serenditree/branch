package com.serenditree.leaf.user.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.ws.rs.core.Response;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

class UserLeafTest {

    @BeforeAll
    static public void setup() {
        RestAssured.baseURI = "http://localhost/api/v1/user";
        RestAssured.port = 8081;
    }

    @Test
    void ping() {
        given()
                .when()
                .get()
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void signUp() {
    }

    @Test
    void signIn() {
    }

    static Stream<Arguments> usernameSource() {
        return Stream.of(
                Arguments.of("tanwald", Response.Status.OK),
                Arguments.of("dlawnat", Response.Status.NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("usernameSource")
    void retrieveByUsername(String username, Response.Status status) {
        given()
                .when()
                .get(username)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(status.getStatusCode());

    }

    static Stream<Arguments> substringSource() {
        return Stream.of(
                Arguments.of("tan", Response.Status.OK),
                Arguments.of("nat", Response.Status.NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("substringSource")
    void retrieveBySubstring(String substring, Response.Status status) {
        given()
                .when()
                .pathParam("substring", substring)
                .get("retrieve/{substring}")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(status.getStatusCode());
    }

    @Test
    void delete() {
    }
}
