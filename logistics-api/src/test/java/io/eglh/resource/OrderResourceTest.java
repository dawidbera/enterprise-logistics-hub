package io.eglh.resource;

import io.eglh.model.OrderRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Integration tests for the OrderResource API.
 * Uses QuarkusTest to spin up a test instance of the application.
 */
@QuarkusTest
@TestHTTPEndpoint(OrderResource.class)
public class OrderResourceTest {

    @Test
    public void testCreateOrder() {
        OrderRequest request = new OrderRequest();
        request.origin = "Paris";
        request.destination = "Berlin";

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post()
            .then()
            .statusCode(202)
            .body("id", notNullValue())
            .body("origin", is("Paris"))
            .body("destination", is("Berlin"))
            .body("status", is("PENDING"));
    }

    @Test
    public void testGetNonExistentOrder() {
        given()
            .when().get("/999")
            .then()
            .statusCode(404);
    }
}
