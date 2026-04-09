package org.acme.rest.json;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ClientResourceTest {

  @Test
  public void testList() {
    given().when().get("/clients").then().statusCode(200);
  }

  @Test
  public void testCrudLifecycle() {
    // create
    int id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test User\",\"email\":\"testuser@example.com\"}")
            .when()
            .post("/clients")
            .then()
            .statusCode(201)
            .body(
                "id", notNullValue(), "name", is("Test User"), "email", is("testuser@example.com"))
            .extract()
            .path("id");

    // get by id
    given()
        .when()
        .get("/clients/" + id)
        .then()
        .statusCode(200)
        .body("name", is("Test User"), "email", is("testuser@example.com"));

    // update
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"Updated User\",\"email\":\"updated@example.com\"}")
        .when()
        .put("/clients/" + id)
        .then()
        .statusCode(200)
        .body("name", is("Updated User"), "email", is("updated@example.com"));

    // delete
    given().when().delete("/clients/" + id).then().statusCode(204);

    // confirm deleted
    given().when().get("/clients/" + id).then().statusCode(404);
  }

  @Test
  public void testGetNotFound() {
    given().when().get("/clients/999999").then().statusCode(404);
  }

  @Test
  public void testUpdateNotFound() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"Nobody\",\"email\":\"nobody@example.com\"}")
        .when()
        .put("/clients/999999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testDeleteNotFound() {
    given().when().delete("/clients/999999").then().statusCode(404);
  }
}
