package org.acme.pet.resources;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import org.acme.pet.resources.dtos.CategoryResponse;
import org.acme.pet.resources.dtos.PetRequest;
import org.acme.pet.resources.dtos.PetResponse;
import org.acme.pet.resources.dtos.TagResponse;
import org.acme.pet.services.PetService;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PetResourceMockTest {

  @InjectMock PetService service;

  @Test
  void shouldDelegateCreateToService() {
    when(service.add(any(PetRequest.class)))
        .thenReturn(
            new PetResponse(
                99L,
                new CategoryResponse(10L, "dogs"),
                "mock-dog",
                List.of("https://example.com/mock-dog.jpg"),
                List.of(new TagResponse(20L, "friendly")),
                "available"));

    String locationHeader =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "category": {"id": 10, "name": "dogs"},
                  "name": "mock-dog",
                  "photoUrls": ["https://example.com/mock-dog.jpg"],
                  "tags": [{"id": 20, "name": "friendly"}],
                  "status": "available"
                }
                """)
            .when()
            .post("/pet")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

    assertThat(locationHeader).endsWith("/pet/99");
    verify(service).add(any(PetRequest.class));
  }
}
