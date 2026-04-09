package org.acme.rest.json;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
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
              "password": "secret",
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
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "username": "user1",
              "firstName": "Updated",
              "lastName": "One",
              "email": "updated.user1@example.com",
              "password": "secret",
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
        .queryParam("username", "user1")
        .queryParam("password", "secret")
        .when()
        .get("/user/login")
        .then()
        .statusCode(200)
        .header("X-Expires-After", containsString("T"))
        .header("X-Rate-Limit", is("500"))
        .body(containsString("logged in user session:user1"));

    given().when().get("/user/logout").then().statusCode(200);

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
                "password": "secret",
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
                "password": "secret",
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

    given().when().get("/user/user1").then().statusCode(404);
  }
}
