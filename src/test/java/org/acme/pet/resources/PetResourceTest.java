package org.acme.pet.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PetResourceTest {

  @Test
  public void testCreateAndQueryPetLifecycle() {
    Number petIdValue =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "category": {"id": 10, "name": "dogs"},
                  "name": "doggie-plus",
                  "photoUrls": ["https://example.com/doggie-plus.jpg"],
                  "tags": [{"id": 20, "name": "friendly"}],
                  "status": "available"
                }
                """)
            .when()
            .post("/pet")
            .then()
            .statusCode(200)
            .body("id", notNullValue(), "name", is("doggie-plus"), "status", is("available"))
            .extract()
            .path("id");
    long petId = petIdValue.longValue();

    given()
        .when()
        .get("/pet/" + petId)
        .then()
        .statusCode(200)
        .body("name", is("doggie-plus"), "category.name", is("dogs"));

    given()
        .queryParam("status", "available")
        .when()
        .get("/pet/findByStatus")
        .then()
        .statusCode(200)
        .body("name", containsInAnyOrder("doggie", "doggie-plus"));

    given()
        .queryParam("tags", "friendly")
        .when()
        .get("/pet/findByTags")
        .then()
        .statusCode(200)
        .body("name", containsInAnyOrder("doggie", "doggie-plus"));

    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "id": %d,
              "category": {"id": 10, "name": "dogs"},
              "name": "doggie-updated",
              "photoUrls": ["https://example.com/doggie-updated.jpg"],
              "tags": [{"id": 20, "name": "friendly"}],
              "status": "pending"
            }
            """
                .formatted(petId))
        .when()
        .put("/pet")
        .then()
        .statusCode(200)
        .body("name", is("doggie-updated"), "status", is("pending"));

    given()
        .contentType("application/x-www-form-urlencoded")
        .formParam("name", "doggie-form")
        .formParam("status", "sold")
        .when()
        .post("/pet/" + petId)
        .then()
        .statusCode(200)
        .body("name", is("doggie-form"), "status", is("sold"));

    given()
        .contentType("multipart/form-data")
        .multiPart("additionalMetadata", "cover")
        .multiPart("file", "pet.png")
        .when()
        .post("/pet/" + petId + "/uploadImage")
        .then()
        .statusCode(200)
        .body("type", is("success"));

    given().header("api_key", "special-key").when().delete("/pet/" + petId).then().statusCode(204);

    given()
        .when()
        .get("/pet/" + petId)
        .then()
        .statusCode(404)
        .contentType(containsString("application/problem+json"));
  }

  @Test
  public void testPetValidation() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "name": "",
              "photoUrls": [],
              "status": "invalid"
            }
            """)
        .when()
        .post("/pet")
        .then()
        .statusCode(400)
        .contentType(containsString("application/problem+json"));
  }
}
