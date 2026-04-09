package org.acme.user.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UserResourceTest {

  @Test
  public void testUserOperations() {
    given().when().get("/user").then().statusCode(200).body("$.size()", greaterThanOrEqualTo(2));

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
        .statusCode(200);

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
        .body("username", org.hamcrest.Matchers.hasItems("seed-user-1", "seed-user-2", "user1"));

    given()
        .queryParam("emailDomain", "example.com")
        .when()
        .get("/user/examples/named-native-query")
        .then()
        .statusCode(200)
        .body("username", org.hamcrest.Matchers.hasItems("seed-user-1", "seed-user-2", "user1"));

    given()
        .queryParam("usernamePrefix", "seed-user")
        .queryParam("status", 1)
        .queryParam("emailDomain", "example.com")
        .when()
        .get("/user/examples/criteria")
        .then()
        .statusCode(200)
        .body("$.size()", is(2))
        .body("username", org.hamcrest.Matchers.hasItems("seed-user-1", "seed-user-2"));

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
        .statusCode(200);

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
        .statusCode(200);

    given().when().delete("/user/user1").then().statusCode(204);

    given()
        .when()
        .get("/user/user1")
        .then()
        .statusCode(404)
        .contentType(containsString("application/problem+json"));
  }
}
