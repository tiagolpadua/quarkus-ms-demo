package org.acme.user.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.user.persistence.User;
import org.junit.jupiter.api.Test;

class UserPanacheMockTest {

  @Test
  void shouldKeepUserEntityAsMutableJpaModel() {
    User user = new User();
    user.setId(123L);
    user.setUsername("panache-user");

    assertThat(user.getId()).isEqualTo(123L);
    assertThat(user.getUsername()).isEqualTo("panache-user");
  }
}
