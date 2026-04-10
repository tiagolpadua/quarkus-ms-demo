package org.acme.shared;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link RestEndpointLivenessHealthCheck}.
 *
 * <p>Uses WireMock to serve a real HTTP endpoint so that {@code call()} can exercise the full
 * request/response path, including the up/down status decision, without starting Quarkus.
 *
 * <p>The {@code normalizeHost} private method is also tested directly via reflection to cover all
 * of its branches (null, blank, wildcard bind addresses).
 */
class RestEndpointLivenessHealthCheckTest {

  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  private RestEndpointLivenessHealthCheck check;

  @BeforeEach
  void setUp() throws Exception {
    check = new RestEndpointLivenessHealthCheck();
    setField("host", "localhost");
    setField("port", wireMock.getPort());
  }

  // ── call() — happy path ───────────────────────────────────────────────────

  @Test
  void shouldReportUpWhen200() throws Exception {
    wireMock.stubFor(get("/q/openapi").willReturn(aResponse().withStatus(200)));

    HealthCheckResponse response = check.call();

    assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    assertThat(response.getData()).isPresent();
    assertThat(response.getData().get()).containsKey("endpoint");
    assertThat(response.getData().get().get("statusCode")).isEqualTo(200L);
  }

  @Test
  void shouldReportUpWhen404() throws Exception {
    // 404 is < 500 so the check considers the server alive.
    wireMock.stubFor(get("/q/openapi").willReturn(aResponse().withStatus(404)));

    HealthCheckResponse response = check.call();

    assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
  }

  @Test
  void shouldReportDownWhen500() throws Exception {
    wireMock.stubFor(get("/q/openapi").willReturn(aResponse().withStatus(500)));

    HealthCheckResponse response = check.call();

    assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
    assertThat(response.getData().get().get("statusCode")).isEqualTo(500L);
  }

  @Test
  void shouldReportDownWhenConnectionRefused() throws Exception {
    // Point to a port where nothing is listening.
    setField("port", 1);

    HealthCheckResponse response = check.call();

    assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
    assertThat(response.getData().get()).containsKey("error");
  }

  // ── normalizeHost ─────────────────────────────────────────────────────────

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"0.0.0.0", "::", "   "})
  void shouldNormalizeWildcardHostToLocalhost(String rawHost) throws Exception {
    assertThat(invokeNormalizeHost(rawHost)).isEqualTo("localhost");
  }

  @Test
  void shouldReturnHostAsIsWhenNormal() throws Exception {
    assertThat(invokeNormalizeHost("192.168.1.1")).isEqualTo("192.168.1.1");
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private String invokeNormalizeHost(String rawHost) throws Exception {
    Method m =
        RestEndpointLivenessHealthCheck.class.getDeclaredMethod("normalizeHost", String.class);
    m.setAccessible(true);
    return (String) m.invoke(check, rawHost);
  }

  private void setField(String name, Object value) throws Exception {
    Field f = RestEndpointLivenessHealthCheck.class.getDeclaredField(name);
    f.setAccessible(true);
    f.set(check, value);
  }
}
