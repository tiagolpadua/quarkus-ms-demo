package org.acme.user.resources;

import static org.mockito.Mockito.when;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.http.HttpServer;
import java.util.Optional;
import org.acme.user.resources.dtos.UserResponse;
import org.acme.user.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusTest
@Provider("quarkus-ms-demo-user")
@PactFolder("pacts")
class ContractVerificationTests {

  private static final String USER_EXISTS_STATE = "user exists";

  @InjectMock UserService userService;

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void pactVerificationTestTemplate(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @BeforeEach
  void beforeEach(PactVerificationContext context, HttpServer httpServer) {
    context.setTarget(new HttpTestTarget("localhost", httpServer.getPort()));
  }

  @State(USER_EXISTS_STATE)
  void userExists() {
    UserResponse response =
        new UserResponse(
            1L,
            "contract-user",
            "Contract",
            "User",
            "contract-user@example.com",
            "+55 11 90000-0000",
            1);

    when(userService.getByUsername("contract-user")).thenReturn(Optional.of(response));
  }
}
