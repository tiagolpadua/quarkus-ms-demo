package org.acme.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for the pure-logic private methods of {@link UiHomeResource}.
 *
 * <p>The JAX-RS endpoint itself requires Qute templates and CDI beans that are only available
 * inside Quarkus. These tests target the helper methods ({@code abbreviateCommitId} and {@code
 * formatBuildTime}) directly via reflection to cover all their branches without starting the
 * container.
 */
class UiHomeResourceTest {

  private UiHomeResource resource;

  @BeforeEach
  void setUp() {
    // Pass null instances — the private helpers under test do not use them.
    resource = new UiHomeResource(null, null);
  }

  // ── abbreviateCommitId ────────────────────────────────────────────────────

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void shouldReturnUnavailableForBlankCommitId(String commitId) throws Exception {
    assertThat(invokeAbbreviateCommitId(commitId)).isEqualTo("unavailable");
  }

  @Test
  void shouldReturnCommitIdAsIsWhenSevenCharsOrFewer() throws Exception {
    assertThat(invokeAbbreviateCommitId("abc1234")).isEqualTo("abc1234");
    assertThat(invokeAbbreviateCommitId("abc")).isEqualTo("abc");
  }

  @Test
  void shouldTruncateLongCommitIdToSevenChars() throws Exception {
    assertThat(invokeAbbreviateCommitId("abc1234def5678")).isEqualTo("abc1234");
  }

  // ── formatBuildTime ───────────────────────────────────────────────────────

  @Test
  void shouldReturnUnavailableForNullBuildTime() throws Exception {
    assertThat(invokeFormatBuildTime(null)).isEqualTo("unavailable");
  }

  @Test
  void shouldFormatBuildTimeCorrectly() throws Exception {
    OffsetDateTime time = OffsetDateTime.parse("2026-04-09T14:30:00Z");
    assertThat(invokeFormatBuildTime(time)).isEqualTo("2026-04-09 14:30");
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private String invokeAbbreviateCommitId(String commitId) throws Exception {
    Method m = UiHomeResource.class.getDeclaredMethod("abbreviateCommitId", String.class);
    m.setAccessible(true);
    return (String) m.invoke(resource, commitId);
  }

  private String invokeFormatBuildTime(OffsetDateTime time) throws Exception {
    Method m = UiHomeResource.class.getDeclaredMethod("formatBuildTime", OffsetDateTime.class);
    m.setAccessible(true);
    return (String) m.invoke(resource, time);
  }
}
