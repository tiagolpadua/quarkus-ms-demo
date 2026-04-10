package org.acme.pet.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link PetRepository} pure-logic methods.
 *
 * <p>Methods that delegate directly to Panache ({@code findByIdOptional}, {@code persist}, etc.) are
 * tested by mocking the EntityManager or via in-memory integration tests. The private helper methods
 * {@code sanitizeSortField} and {@code isDescending} are exercised via reflection so that every
 * branch is covered without starting Quarkus.
 */
class PetRepositoryTest {

  private PetRepository repository;

  @BeforeEach
  void setUp() {
    repository = new PetRepository();
  }

  // ── sanitizeSortField ─────────────────────────────────────────────────────

  @ParameterizedTest
  @ValueSource(strings = {"id", "name", "status"})
  void shouldReturnAllowedSortFields(String field) throws Exception {
    assertThat(invokeSanitizeSortField(field)).isEqualTo(field);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"unknown", "email", "INVALID"})
  void shouldFallbackToIdForInvalidSortField(String field) throws Exception {
    assertThat(invokeSanitizeSortField(field)).isEqualTo("id");
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

  // ── updatePartial ─────────────────────────────────────────────────────────

  @Test
  void shouldApplyNameAndStatusWhenBothProvided() {
    PetRepository spy = petRepositoryWithFind(Optional.of(pet("oldName", "oldStatus")));

    Optional<Pet> result = spy.updatePartial(1L, "newName", "newStatus");

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getName()).isEqualTo("newName");
    assertThat(result.orElseThrow().getStatus()).isEqualTo("newStatus");
  }

  @Test
  void shouldSkipBlankNameAndStatus() {
    PetRepository spy = petRepositoryWithFind(Optional.of(pet("keepName", "keepStatus")));

    Optional<Pet> result = spy.updatePartial(1L, "  ", "  ");

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getName()).isEqualTo("keepName");
    assertThat(result.orElseThrow().getStatus()).isEqualTo("keepStatus");
  }

  @Test
  void shouldSkipNullNameAndStatus() {
    PetRepository spy = petRepositoryWithFind(Optional.of(pet("keepName", "keepStatus")));

    Optional<Pet> result = spy.updatePartial(1L, null, null);

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getName()).isEqualTo("keepName");
    assertThat(result.orElseThrow().getStatus()).isEqualTo("keepStatus");
  }

  @Test
  void shouldReturnEmptyWhenPetNotFound() {
    PetRepository spy = petRepositoryWithFind(Optional.empty());

    assertThat(spy.updatePartial(999L, "name", "status")).isEmpty();
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private String invokeSanitizeSortField(String sortBy) throws Exception {
    Method m = PetRepository.class.getDeclaredMethod("sanitizeSortField", String.class);
    m.setAccessible(true);
    return (String) m.invoke(repository, sortBy);
  }

  private boolean invokeIsDescending(String direction) throws Exception {
    Method m = PetRepository.class.getDeclaredMethod("isDescending", String.class);
    m.setAccessible(true);
    return (boolean) m.invoke(repository, direction);
  }

  /**
   * Returns a {@link PetRepository} subclass that overrides {@code findByIdOptional} to return the
   * given value, allowing {@code updatePartial} to be exercised without a database.
   */
  @SuppressWarnings("unchecked")
  private PetRepository petRepositoryWithFind(Optional<Pet> stub) {
    return new PetRepository() {
      @Override
      public Optional<Pet> findByIdOptional(Object id) {
        return stub;
      }
    };
  }

  private Pet pet(String name, String status) {
    Pet pet = new Pet();
    pet.setName(name);
    pet.setStatus(status);
    return pet;
  }
}
