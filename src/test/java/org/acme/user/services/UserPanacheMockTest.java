package org.acme.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.acme.user.persistence.User;
import org.acme.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Demonstrates advanced Mockito patterns: {@code @Spy}, {@code @NullAndEmptySource}, and boundary
 * testing of the {@link UserRepository} wrapper methods.
 *
 * <p><strong>{@code @Spy}</strong> wraps a real object. Calls go to the real implementation by
 * default; you can stub only specific methods with {@code doReturn(...).when(spy).method()}. This
 * is useful when you want to test a class that delegates to a real collaborator for most operations
 * but you need to control one specific interaction.
 *
 * <p><strong>{@code @NullAndEmptySource}</strong> is a composite annotation that runs the test with
 * {@code null} and {@code ""} as arguments. Ideal for validating guard clauses on string
 * parameters.
 */
@ExtendWith(MockitoExtension.class)
class UserPanacheMockTest {

  @Mock UserRepository repository;

  /**
   * A real {@link User} instance wrapped by a Spy. Specific getters can be overridden while the
   * rest of the object behaves normally.
   */
  @Spy User spyUser = buildUser(1L, "spy-user", "Spy");

  @Test
  void shouldKeepUserEntityMutableForJpaLifecycle() {
    User user = new User();
    user.setId(123L);
    user.setUsername("panache-user");
    user.setFirstName("Panache");
    user.setLastName("User");
    user.setEmail("panache-user@example.com");
    user.setPhone("+55 11 99999-1111");
    user.setUserStatus(1);

    assertThat(user.getId()).isEqualTo(123L);
    assertThat(user.getUsername()).isEqualTo("panache-user");
    assertThat(user.getFirstName()).isEqualTo("Panache");
    assertThat(user.getUserStatus()).isEqualTo(1);
  }

  @Test
  void shouldDemonstrateSpyBehavior() {
    // Real method executes — @Spy wraps the actual object, not a mock.
    assertThat(spyUser.getUsername()).isEqualTo("spy-user");
    assertThat(spyUser.getFirstName()).isEqualTo("Spy");
  }

  @Test
  void shouldFindUserByUsernameViaMockedRepository() {
    User stored = buildUser(10L, "repo-user", "Repo");
    when(repository.findByUsername("repo-user")).thenReturn(Optional.of(stored));

    Optional<User> found = repository.findByUsername("repo-user");

    assertThat(found).isPresent();
    assertThat(found.orElseThrow().getId()).isEqualTo(10L);
    verify(repository).findByUsername("repo-user");
  }

  @Test
  void shouldReturnEmptyWhenUsernameNotFound() {
    when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThat(repository.findByUsername("ghost")).isEmpty();
  }

  @Test
  void shouldReturnAllUsersFromMockedRepository() {
    List<User> users = List.of(buildUser(1L, "alpha", "Alpha"), buildUser(2L, "beta", "Beta"));
    when(repository.listAll()).thenReturn(users);

    assertThat(repository.listAll())
        .hasSize(2)
        .extracting(User::getUsername)
        .contains("alpha", "beta");
  }

  /**
   * {@code @NullAndEmptySource} runs the test twice: once with {@code null}, once with {@code ""}.
   *
   * <p>This is the idiomatic way to document and verify that a method handles blank username inputs
   * gracefully, without writing two separate test methods.
   */
  @ParameterizedTest
  @NullAndEmptySource
  void shouldReturnEmptyForBlankOrNullUsername(String username) {
    when(repository.findByUsername(username)).thenReturn(Optional.empty());

    assertThat(repository.findByUsername(username)).isEmpty();
  }

  /**
   * {@code @ValueSource} with strings — verifies that delete returns false for several unknown
   * usernames.
   */
  @ParameterizedTest
  @ValueSource(strings = {"unknown-a", "unknown-b", "unknown-c"})
  void shouldReturnFalseWhenDeletingUnknownUsername(String username) {
    when(repository.deleteByUsername(username)).thenReturn(false);

    assertThat(repository.deleteByUsername(username)).isFalse();
  }

  private static User buildUser(Long id, String username, String firstName) {
    User user = new User();
    user.setId(id);
    user.setUsername(username);
    user.setFirstName(firstName);
    user.setLastName("Test");
    user.setEmail(username + "@example.com");
    user.setPhone("+55 11 99999-0000");
    user.setUserStatus(1);
    return user;
  }

  /**
   * <strong>Example of quarkus-panache-mock:</strong> When testing code that uses Panache static
   * methods (e.g., {@code User.find(...)}), use {@code io.quarkus.panache.mock.PanacheMock} to stub
   * them. This avoids hitting the database in unit tests.
   *
   * <p><strong>Note:</strong> This requires the {@code quarkus-panache-mock} dependency. In a test
   * class annotated with {@code @QuarkusTest}, you would write:
   *
   * <pre>{@code
   * @QuarkusTest
   * class UserServicePanacheMockTest {
   *   @Test
   *   void shouldMockPanacheStaticMethods() {
   *     User user = new User();
   *     user.setId(99L);
   *     user.setUsername("panache-mocked");
   *
   *     // Mock the static Panache method
   *     PanacheMock.mock(User.class);
   *     when(User.findById(99L)).thenReturn(user);
   *
   *     User found = User.findById(99L);
   *     assertThat(found.getUsername()).isEqualTo("panache-mocked");
   *   }
   * }
   * }</pre>
   *
   * <p>This pattern is essential when testing service methods that delegate to Panache repositories
   * without wanting to initialize the actual database.
   */
}
