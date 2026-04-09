package org.acme.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Optional;
import org.acme.shared.pagination.PageResult;
import org.acme.user.persistence.User;
import org.acme.user.persistence.UserRepository;
import org.acme.user.resources.dtos.UserRequest;
import org.acme.user.services.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock UserRepository repository;
  @Mock MeterRegistry meterRegistry;

  private UserService service;

  @BeforeEach
  void setUp() {
    UserMapper mapper = Mappers.getMapper(UserMapper.class);
    service = new UserService(repository, meterRegistry, mapper);
  }

  @Test
  void shouldCreateUpdateAndDeleteUser() {
    Counter counter = mock(Counter.class);
    when(meterRegistry.counter("user_create_total")).thenReturn(counter);
    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(77L);
              return null;
            })
        .when(repository)
        .persist(any(User.class));
    when(repository.findByUsername("service-user"))
        .thenReturn(
            Optional.of(user(77L, "service-user", "Service", 1)),
            Optional.of(user(77L, "service-user", "Updated", 2)),
            Optional.empty());
    when(repository.listPaged(0, 20, "username", "asc"))
        .thenReturn(List.of(user(77L, "service-user", "Updated", 2)));
    when(repository.listPageResult(0, 10, "username", "asc"))
        .thenReturn(
            new PageResult<>(
                List.of(user(77L, "service-user", "Updated", 2)), 0, 10, 1, "username", "asc"));
    when(repository.deleteByUsername("service-user")).thenReturn(true);

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
    verify(counter).increment();

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
    when(repository.deleteByUsername(username)).thenReturn(false);
    assertThat(service.delete(username)).isFalse();
  }

  private User user(Long id, String username, String firstName, Integer userStatus) {
    User user = new User();
    user.setId(id);
    user.setUsername(username);
    user.setFirstName(firstName);
    user.setLastName("User");
    user.setEmail(username + "@example.com");
    user.setPhone("+55 11 99999-1111");
    user.setUserStatus(userStatus);
    return user;
  }
}
