package io.serenditree.fence.extras.scheduler.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FenceSchedulerTest {

    @BeforeAll
    static void beforeAll() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = "http://localhost/api/v1/internal/fence";
        RestAssured.port = 8081;
    }

    @Test
    void cleanupTest() {
        String result = given()
            .when()
            .get("cleanup")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.TEXT)
            .extract()
            .asString();

        assertNotNull(result);
        assertEquals("0", result);
    }
}
