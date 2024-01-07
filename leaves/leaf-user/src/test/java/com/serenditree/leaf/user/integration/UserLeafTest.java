package com.serenditree.leaf.user.integration;

import com.serenditree.branch.user.model.entities.User;
import com.serenditree.fence.model.FenceHeaders;
import com.serenditree.fence.model.enums.RoleType;
import com.serenditree.root.test.authentication.Authenticator;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.mapper.ObjectMapperType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Jakarta JSONB not yet supported by REST Assured.")
class UserLeafTest {

    static final String USERNAME = UUID.randomUUID().toString().substring(1, 20);
    static final String PASSWORD = UUID.randomUUID().toString();
    static final Headers FENCE_HEADERS = new Headers(
        new Header(FenceHeaders.USERNAME, USERNAME),
        new Header(FenceHeaders.PASSWORD, PASSWORD)
    );

    static Long userId;
    static Header fenceHeader;

    @BeforeAll
    static void beforeAll() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = "http://localhost/api/v1/user";
        RestAssured.port = 8081;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SIGN UP
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    @Order(1)
    void signUp() {
        Headers headers = given()
            .headers(FENCE_HEADERS)
            .when()
            .post("sign-up")
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .header(FenceHeaders.USERNAME, equalTo(USERNAME))
            .header(FenceHeaders.ID, not(blankOrNullString()))
            .header(HttpHeaders.AUTHORIZATION, not(blankOrNullString()))
            .extract()
            .headers();
        userId = Long.parseLong(headers.getValue(FenceHeaders.ID));
        fenceHeader = headers.get(HttpHeaders.AUTHORIZATION);
    }

    @ParameterizedTest
    @MethodSource("signUpHeadersSource")
    @Order(2)
    void signUpInvalid(Headers headers, Status status) {
        given()
            .headers(headers)
            .when()
            .post("sign-up")
            .then()
            .statusCode(status.getStatusCode())
            .header(FenceHeaders.USERNAME, nullValue())
            .header(FenceHeaders.ID, nullValue())
            .header(HttpHeaders.AUTHORIZATION, nullValue());
    }

    static Stream<Arguments> signUpHeadersSource() {
        return Stream.of(
            // Already exists.
            Arguments.of(
                FENCE_HEADERS,
                Status.BAD_REQUEST
            ),
            // Weak password.
            Arguments.of(
                new Headers(
                    new Header(FenceHeaders.USERNAME, UUID.randomUUID().toString()),
                    new Header(FenceHeaders.PASSWORD, "password")
                ),
                Status.BAD_REQUEST
            ),
            // Invalid email
            Arguments.of(
                new Headers(
                    new Header(FenceHeaders.USERNAME, UUID.randomUUID().toString()),
                    new Header(FenceHeaders.PASSWORD, PASSWORD),
                    new Header(FenceHeaders.EMAIL, "invalid@")
                ),
                Status.BAD_REQUEST
            ),
            // Username too long.
            Arguments.of(
                new Headers(
                    new Header(FenceHeaders.USERNAME, "x".repeat(21)),
                    new Header(FenceHeaders.PASSWORD, PASSWORD)
                ),
                Status.BAD_REQUEST
            )
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SIGN IN
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    @Order(3)
    void signIn() {
        given()
            .headers(FENCE_HEADERS)
            .when()
            .post("sign-in")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header(FenceHeaders.USERNAME, equalTo(USERNAME))
            .header(FenceHeaders.ID, equalTo(userId.toString()))
            .header(HttpHeaders.AUTHORIZATION, not(blankOrNullString()));
    }

    @ParameterizedTest
    @MethodSource("signInHeadersSource")
    @Order(3)
    void signInWrongUsernameOrPassword(Headers headers, Status status) {
        given()
            .headers(headers)
            .when()
            .post("sign-in")
            .then()
            .statusCode(status.getStatusCode())
            .header(HttpHeaders.AUTHORIZATION, nullValue());
    }

    static Stream<Arguments> signInHeadersSource() {
        return Stream.of(
            // Wrong username.
            Arguments.of(
                new Headers(
                    new Header(FenceHeaders.USERNAME, USERNAME.substring(1)),
                    new Header(FenceHeaders.PASSWORD, PASSWORD)
                ),
                Status.UNAUTHORIZED
            ),
            // Wrong password.
            Arguments.of(
                new Headers(
                    new Header(FenceHeaders.USERNAME, USERNAME),
                    new Header(FenceHeaders.PASSWORD, USERNAME)
                ),
                Status.BAD_REQUEST
            )
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RETRIEVE BY USERNAME
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @MethodSource("usernameSource")
    @Order(4)
    void retrieveByUsername(String username, Status status) {
        io.restassured.response.Response response = given()
            .pathParam("username", username)
            .when()
            .get("{username}")
            .then()
            .statusCode(status.getStatusCode())
            .extract()
            .response();
        System.out.println(response.asString());
        if (status == Status.OK) {
            User user = response.as(User.class, ObjectMapperType.JSONB);
            assertThat(user.getId(), equalTo(userId));
            assertThat(user.getUsername(), equalTo(USERNAME));
            assertThat(user.getPassword(), nullValue());
            assertThat(user.getRoles(), iterableWithSize(1));
            assertThat(user.getRoles().iterator().next().getRoleType(), equalTo(RoleType.USER));
        }
    }

    static Stream<Arguments> usernameSource() {
        return Stream.of(
            Arguments.of(USERNAME, Status.OK),
            Arguments.of(USERNAME.substring(1), Status.NOT_FOUND)
        );
    }

    @Test
    @Order(7)
    void retrieveByUsernameNotFound() {
        given()
            .pathParam("username", USERNAME)
            .when()
            .get("{username}")
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RETRIEVE BY SUBSTRING
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @MethodSource("substringSource")
    @Order(5)
    void retrieveBySubstring(String substring, Status status) {
        given()
            .pathParam("substring", substring)
            .when()
            .get("retrieve/{substring}")
            .then()
            .statusCode(status.getStatusCode())
            .contentType(ContentType.JSON)
            .body("$", status == Status.OK ? iterableWithSize(1) : iterableWithSize(0));
    }

    static Stream<Arguments> substringSource() {
        return Stream.of(
            Arguments.of(USERNAME.substring(0, 2), Status.OK),
            Arguments.of(USERNAME.substring(1), Status.NOT_FOUND)
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DELETE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @MethodSource("deleteSource")
    @Order(6)
    void delete(Header header, Long id, Status status) {
        given()
            .header(header)
            .pathParam("id", id)
            .when()
            .delete("delete/{id}")
            .then()
            .statusCode(status.getStatusCode());
    }

    static Stream<Arguments> deleteSource() {
        return Stream.of(
            Arguments.of(Authenticator.NOOP_HEADER, userId, Status.UNAUTHORIZED),
            Arguments.of(fenceHeader, userId + 1, Status.FORBIDDEN),
            Arguments.of(fenceHeader, userId, Status.OK),
            Arguments.of(fenceHeader, userId, Status.FORBIDDEN)
        );
    }
}
