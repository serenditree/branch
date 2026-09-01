package io.serenditree.leaf.seed.integration;

import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.mapper.ObjectMapperType;
import io.serenditree.branch.seed.model.entities.Seed;
import io.serenditree.branch.seed.model.filter.SeedFilter;
import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.root.data.geo.model.LngLat;
import io.serenditree.root.data.geo.model.LngLatBounds;
import io.serenditree.root.test.authentication.Authenticator;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SeedLeafTest {

    static FencePrincipal fencePrincipal;
    static Header fenceHeader;

    static Seed seed;
    static Seed created;

    @BeforeAll
    static void beforeAll() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = "http://localhost/api/v1/seed";
        RestAssured.port = 8082;
        RestAssured.config = RestAssuredConfig
            .config()
            .objectMapperConfig(
                ObjectMapperConfig
                    .objectMapperConfig()
                    .defaultObjectMapperType(ObjectMapperType.JSONB)
            );

        fencePrincipal = Authenticator.authenticate();
        fenceHeader = new Header(HttpHeaders.AUTHORIZATION, fencePrincipal.getToken());
    }

    @AfterAll
    static void afterAll() {
        Authenticator.cleanup(fencePrincipal);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CREATE
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    @Order(1)
    void create() {
        seed = new Seed();
        seed.setTitle("Lorem ipsum");
        seed.setText("Lorem ipsum dolor sit amet...");
        seed.setTags(Set.of("lorem", "ipsum", "loripsum", "lipsumor"));
        seed.setLocation(new LngLat(16.359914169215926, 48.2088284029927));
        seed.setUsername(fencePrincipal.getUsername());
        seed.setUserId(fencePrincipal.getId());

        created = given()
            .contentType(ContentType.JSON)
            .header(fenceHeader)
            .body(seed)
            .when()
            .post("create")
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract()
            .as(Seed.class);

        assertThat(created, not(nullValue()));
        assertThat(created.getId(), not(nullValue()));
        assertThat(created.getId(), not(blankOrNullString()));
        assertThat(created.getUsername(), equalTo(fencePrincipal.getUsername()));
        assertThat(created.getUserId(), equalTo(fencePrincipal.getId()));
        assertThat(created.getTitle(), equalTo(seed.getTitle()));
        assertThat(created.getText(), equalTo(seed.getText()));
        assertThat(created.getLocation(), equalTo(seed.getLocation()));
        assertThat(created.getTags(), equalTo(seed.getTags()));
        assertThat(created.getWater(), nullValue());
        assertThat(created.getNubits(), nullValue());
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RETRIEVE BY ID
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    @Order(2)
    void retrieveById() {
        Seed retrieved = given()
            .pathParam("id", created.getId())
            .when()
            .get("{id}")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(Seed.class);

        assertThat(retrieved, not(nullValue()));
        assertThat(retrieved.getId(), not(nullValue()));
        assertThat(retrieved.getId(), not(blankOrNullString()));
        assertThat(retrieved.getUsername(), equalTo(fencePrincipal.getUsername()));
        assertThat(retrieved.getUserId(), equalTo(fencePrincipal.getId()));
        assertThat(retrieved.getTitle(), equalTo(created.getTitle()));
        assertThat(retrieved.getText(), equalTo(created.getText()));
        assertThat(retrieved.getLocation(), equalTo(created.getLocation()));
        assertThat(retrieved.getTags(), equalTo(created.getTags()));
        assertThat(retrieved.getWater(), nullValue());
        assertThat(retrieved.getNubits(), nullValue());
    }

    @Test
    @Order(5)
    void retrieveByIdNotFound() {
        given()
            .pathParam("id", created.getId())
            .when()
            .get("{id}")
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RETRIEVE BY FILTER
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    @Order(3)
    void retrieveByFilter() {
        SeedFilter filter = new SeedFilter();
        filter.setBounds(
            new LngLatBounds(
                new LngLat(16.25537239065008, 48.193954482542836),
                new LngLat(16.46445594777859, 48.22369800478231)
            )
        );

        await()
            .atMost(1, TimeUnit.SECONDS)
            .untilAsserted(
                () -> given()
                    .contentType(ContentType.JSON)
                    .body(filter)
                    .when()
                    .post("retrieve")
                    .then()
                    .statusCode(Status.OK.getStatusCode())
                    .contentType(ContentType.JSON)
                    .body("$", iterableWithSize(1))
            );
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RETRIEVE TAGS
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    @Order(3)
    void retrieveTags() {
        await()
            .atMost(1, TimeUnit.SECONDS)
            .untilAsserted(
                () -> given()
                    .pathParam("name", "ipsum")
                    .when()
                    .get("retrieve/tags/{name}")
                    .then()
                    .statusCode(Status.OK.getStatusCode())
                    .contentType(ContentType.JSON)
                    .body("$", containsInAnyOrder("ipsum", "loripsum", "lipsumor"))
            );
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // WATER, PRUNE and NUBIT
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @MethodSource("waterPruneSource")
    @Order(3)
    void water(Header header, Status expectedStatus) {
        given()
            .header(header)
            .pathParam("id", created.getId())
            .when()
            .get("water/{id}")
            .then()
            .statusCode(expectedStatus.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("waterPruneSource")
    @Order(3)
    void prune(Header header, Status expectedStatus) {
        given()
            .header(header)
            .pathParam("id", created.getId())
            .when()
            .get("prune/{id}")
            .then()
            .statusCode(expectedStatus.getStatusCode());
    }

    static Stream<Arguments> waterPruneSource() {
        return Stream.of(
            Arguments.of(fenceHeader, Status.OK),
            Arguments.of(fenceHeader, Status.FORBIDDEN),
            Arguments.of(Authenticator.NOOP_HEADER, Status.UNAUTHORIZED)
        );
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DELETE
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @MethodSource("deleteSource")
    @Order(4)
    void delete(Header header, String id, Status expectedStatus) {
        given()
            .header(header)
            .pathParam("id", id)
            .when()
            .delete("{id}")
            .then()
            .statusCode(expectedStatus.getStatusCode());
    }

    static Stream<Arguments> deleteSource() {
        return Stream.of(
            Arguments.of(Authenticator.NOOP_HEADER, created.getId(), Status.UNAUTHORIZED),
            Arguments.of(fenceHeader, UUID.randomUUID().toString(), Status.FORBIDDEN),
            Arguments.of(fenceHeader, created.getId(), Status.OK),
            Arguments.of(fenceHeader, created.getId(), Status.FORBIDDEN)
        );
    }
}
