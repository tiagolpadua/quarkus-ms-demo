package org.acme.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import org.acme.user.persistence.User;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UserPanacheMockTest {

  @Test
  void shouldMockActiveRecordLookupWithPanacheMock() {
    PanacheMock.mock(User.class);

    User user = new User();
    user.setId(123L);
    user.setUsername("panache-user");

    when(User.findByIdOptional(123L)).thenReturn(Optional.of(user));

    Optional<User> found = User.findByIdOptional(123L);

    assertThat(found).isPresent();
    assertThat(found.orElseThrow().getUsername()).isEqualTo("panache-user");

    PanacheMock.verify(User.class).findByIdOptional(123L);
  }
}
