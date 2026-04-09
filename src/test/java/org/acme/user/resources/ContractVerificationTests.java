package org.acme.user.resources;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.user.resources.dtos.UserResponse;
import org.junit.jupiter.api.Test;

class ContractVerificationTests {

  @Test
  void shouldKeepUserResponseContractStable() {
    UserResponse response =
        new UserResponse(
            1L,
            "contract-user",
            "Contract",
            "User",
            "contract-user@example.com",
            "+55 11 90000-0000",
            1);

    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.username()).isEqualTo("contract-user");
    assertThat(response.firstName()).isEqualTo("Contract");
    assertThat(response.lastName()).isEqualTo("User");
    assertThat(response.email()).isEqualTo("contract-user@example.com");
    assertThat(response.phone()).isEqualTo("+55 11 90000-0000");
    assertThat(response.userStatus()).isEqualTo(1);
  }
}
