package org.acme.user.services;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.user.resources.dtos.UserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestTransaction
class UserServiceTest {

  @Inject UserService service;

  @Test
  void shouldCreateUpdateAndDeleteUser() {
    UserRequest createRequest =
        new UserRequest(
            null,
            "service-user",
            "Service",
            "User",
            "service-user@example.com",
            "+55 11 99999-1111",
            1);

    var created = service.create(createRequest);

    assertThat(created.id()).isNotNull();
    assertThat(created.username()).isEqualTo("service-user");
    assertThat(created.firstName()).isEqualTo("Service");

    assertThat(service.getByUsername("service-user")).isPresent();

    UserRequest updateRequest =
        new UserRequest(
            created.id(),
            "service-user",
            "Updated",
            "User",
            "updated-service-user@example.com",
            "+55 11 99999-2222",
            2);

    var updated = service.update("service-user", updateRequest);

    assertThat(updated).isPresent();
    assertThat(updated.orElseThrow().firstName()).isEqualTo("Updated");
    assertThat(updated.orElseThrow().userStatus()).isEqualTo(2);

    var list = service.list(0, 20, "username", "asc");
    assertThat(list).extracting("username").contains("service-user");

    var paged = service.listPaged(0, 10, "username", "asc");
    assertThat(paged.page().size()).isEqualTo(10);
    assertThat(paged.sort().by()).isEqualTo("username");

    assertThat(service.delete("service-user")).isTrue();
    assertThat(service.getByUsername("service-user")).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"missing-user-a", "missing-user-b", "missing-user-c"})
  void shouldReturnFalseWhenDeletingUnknownUser(String username) {
    assertThat(service.delete(username)).isFalse();
  }
}
