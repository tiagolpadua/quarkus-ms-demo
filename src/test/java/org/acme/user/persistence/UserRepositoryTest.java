package org.acme.user.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link UserRepository} pure-logic private helpers.
 *
 * <p>The allowed sort fields, fallback field, and direction parsing are all exercised via
 * reflection without starting Quarkus or a database.
 */
class UserRepositoryTest {

  private UserRepository repository;

  @BeforeEach
  void setUp() {
    repository = new UserRepository();
  }

  // ── sanitizeSortField ─────────────────────────────────────────────────────

  @ParameterizedTest
  @ValueSource(strings = {"id", "username", "firstName", "lastName", "email", "userStatus"})
  void shouldReturnAllowedSortFields(String field) throws Exception {
    assertThat(invokeSanitizeSortField(field)).isEqualTo(field);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"invalid", "PHONE", "unknown"})
  void shouldFallbackToUsernameForInvalidSortField(String field) throws Exception {
    assertThat(invokeSanitizeSortField(field)).isEqualTo("username");
  }

  // ── isDescending ──────────────────────────────────────────────────────────

  @ParameterizedTest
  @ValueSource(strings = {"desc", "DESC", "Desc"})
  void shouldRecognizeDescendingVariants(String direction) throws Exception {
    assertThat(invokeIsDescending(direction)).isTrue();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"asc", "ASC", "random"})
  void shouldTreatNonDescAsAscending(String direction) throws Exception {
    assertThat(invokeIsDescending(direction)).isFalse();
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private String invokeSanitizeSortField(String sortBy) throws Exception {
    Method m = UserRepository.class.getDeclaredMethod("sanitizeSortField", String.class);
    m.setAccessible(true);
    return (String) m.invoke(repository, sortBy);
  }

  private boolean invokeIsDescending(String direction) throws Exception {
    Method m = UserRepository.class.getDeclaredMethod("isDescending", String.class);
    m.setAccessible(true);
    return (boolean) m.invoke(repository, direction);
  }
}
