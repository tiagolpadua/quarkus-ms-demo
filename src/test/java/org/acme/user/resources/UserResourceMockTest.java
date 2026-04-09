package org.acme.user.resources;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Optional;
import org.acme.user.resources.dtos.UserResponse;
import org.acme.user.services.UserService;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UserResourceMockTest {

  @InjectMock UserService service;

  @Test
  void shouldUseServiceToListAndGetUser() {
    UserResponse mockUser =
        new UserResponse(
            1L, "mock-user", "Mock", "User", "mock-user@example.com", "+55 11 99999-3333", 1);

    when(service.list(0, 20, "username", "asc")).thenReturn(List.of(mockUser));
    when(service.getByUsername("mock-user")).thenReturn(Optional.of(mockUser));

    given()
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("items.size()", org.hamcrest.CoreMatchers.is(1))
        .body("items[0].username", org.hamcrest.CoreMatchers.is("mock-user"));

    given()
        .when()
        .get("/user/mock-user")
        .then()
        .statusCode(200)
        .body("username", org.hamcrest.CoreMatchers.is("mock-user"));

    verify(service).list(0, 20, "username", "asc");
    verify(service).getByUsername("mock-user");
  }

  @Test
  void shouldReturnNotFoundWhenUserDoesNotExist() {
    when(service.getByUsername("missing-user")).thenReturn(Optional.empty());

    given().when().get("/user/missing-user").then().statusCode(404);

    verify(service).getByUsername("missing-user");
  }
}
