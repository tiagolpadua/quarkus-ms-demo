package org.acme.user.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UserResourceTest {

  @Test
  void testUserOperations() {
    given()
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("items.size()", greaterThanOrEqualTo(2));

    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "username": "user1",
              "firstName": "User",
              "lastName": "One",
              "email": "user1@example.com",
              "phone": "111-111",
              "userStatus": 1
            }
            """)
        .when()
        .post("/user")
        .then()
        .statusCode(201);

    given()
        .when()
        .get("/user/user1")
        .then()
        .statusCode(200)
        .body("firstName", is("User"), "email", is("user1@example.com"));

    given()
        .queryParam("status", 1)
        .when()
        .get("/user/examples/named-query")
        .then()
        .statusCode(200)
        .body(
            "items.username",
            org.hamcrest.Matchers.hasItems("seed-user-1", "seed-user-2", "user1"));

    given()
        .queryParam("emailDomain", "example.com")
        .when()
        .get("/user/examples/named-native-query")
        .then()
        .statusCode(200)
        .body(
            "items.username",
            org.hamcrest.Matchers.hasItems("seed-user-1", "seed-user-2", "user1"));

    given()
        .queryParam("usernamePrefix", "seed-user")
        .queryParam("status", 1)
        .queryParam("emailDomain", "example.com")
        .when()
        .get("/user/examples/criteria")
        .then()
        .statusCode(200)
        .body("items.size()", is(2))
        .body("items.username", org.hamcrest.Matchers.hasItems("seed-user-1", "seed-user-2"));

    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "username": "user1",
              "firstName": "Updated",
              "lastName": "One",
              "email": "updated.user1@example.com",
              "phone": "222-222",
              "userStatus": 2
            }
            """)
        .when()
        .put("/user/user1")
        .then()
        .statusCode(200)
        .body("firstName", is("Updated"), "userStatus", is(2));

    given()
        .contentType(ContentType.JSON)
        .body(
            """
            [
              {
                "username": "bulk-array",
                "firstName": "Bulk",
                "lastName": "Array",
                "email": "bulk-array@example.com",
                "phone": "333-333",
                "userStatus": 1
              }
            ]
            """)
        .when()
        .post("/user/createWithArray")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
            [
              {
                "username": "bulk-list",
                "firstName": "Bulk",
                "lastName": "List",
                "email": "bulk-list@example.com",
                "phone": "444-444",
                "userStatus": 1
              }
            ]
            """)
        .when()
        .post("/user/createWithList")
        .then()
        .statusCode(201);

    given().when().delete("/user/user1").then().statusCode(204);

    given()
        .when()
        .get("/user/user1")
        .then()
        .statusCode(404)
        .contentType(containsString("application/problem+json"));
  }

  @Test
  void testUserValidation() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "username": "ab",
              "firstName": "",
              "lastName": "",
              "email": "invalid-email",
              "phone": "x",
              "userStatus": 99
            }
            """)
        .when()
        .post("/user")
        .then()
        .statusCode(400)
        .contentType(containsString("application/problem+json"));
  }

  @Test
  void testUserListPaginationAndOrdering() {
    given()
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("sort", "username")
        .queryParam("direction", "desc")
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("items.size()", is(1));
  }

  @Test
  void testUserPagedListReturnsPaginationMetadata() {
    given()
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("sort", "username")
        .queryParam("direction", "desc")
        .when()
        .get("/user/paged")
        .then()
        .statusCode(200)
        .header("X-Total-Count", not(emptyOrNullString()))
        .header("Link", containsString("rel=\"next\""))
        .body("items.size()", is(1))
        .body("page.number", is(0))
        .body("page.size", is(1))
        .body("page.totalElements", greaterThanOrEqualTo(2))
        .body("page.totalPages", greaterThanOrEqualTo(2))
        .body("page.first", is(true))
        .body("page.last", is(false))
        .body("page.hasNext", is(true))
        .body("page.hasPrevious", is(false))
        .body("sort.by", is("username"))
        .body("sort.direction", is("desc"));

    given()
        .queryParam("page", 1)
        .queryParam("size", 1)
        .queryParam("sort", "username")
        .queryParam("direction", "desc")
        .when()
        .get("/user/paged")
        .then()
        .statusCode(200)
        .header("Link", containsString("rel=\"prev\""))
        .body("items.size()", is(1))
        .body("page.number", is(1))
        .body("page.first", is(false))
        .body("page.hasPrevious", is(true));
  }

  @Test
  void testRequestCorrelationHeaders() {
    given()
        .header("X-Request-Id", "manual-correlation-id")
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .header("X-Request-Id", is("manual-correlation-id"));

    given()
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .header("X-Request-Id", not(emptyOrNullString()));
  }

  @Test
  void testBusinessMetricsExposure() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "username": "metrics-user",
              "firstName": "Metrics",
              "lastName": "User",
              "email": "metrics-user@example.com",
              "phone": "555-555",
              "userStatus": 1
            }
            """)
        .when()
        .post("/user")
        .then()
        .statusCode(201);

    given()
        .when()
        .get("/q/metrics")
        .then()
        .statusCode(200)
        .body(containsString("user_create_total"))
        .body(containsString("http_server_requests"));
  }
}
