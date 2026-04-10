package org.acme.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link LoggingFilter} private methods accessed via reflection.
 *
 * <p>The JAX-RS filter contract requires Vert.x context objects ({@code UriInfo}, {@code
 * HttpServerRequest}) injected at runtime, making end-to-end testing impractical without the
 * container. Instead, this class tests the pure-logic private methods — {@code resolveRequestId},
 * {@code normalizePath}, and {@code durationMs} — directly via reflection, verifying each branch
 * without starting Quarkus.
 *
 * <p>The {@code filter} methods themselves are covered by the integration smoke tests in {@link
 * org.acme.rest.json.OpenApiResourceIT}.
 */
class LoggingFilterTest {

  private LoggingFilter filter;

  @BeforeEach
  void setUp() {
    filter = new LoggingFilter();
  }

  // ── resolveRequestId ──────────────────────────────────────────────────────

  @Test
  void shouldPreserveIncomingRequestId() throws Exception {
    ContainerRequestContext ctx = mock(ContainerRequestContext.class);
    when(ctx.getHeaderString("X-Request-Id")).thenReturn("my-existing-id");

    String result = invokeResolveRequestId(ctx);

    assertThat(result).isEqualTo("my-existing-id");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void shouldGenerateNewRequestIdWhenHeaderIsAbsentOrBlank(String headerValue) throws Exception {
    ContainerRequestContext ctx = mock(ContainerRequestContext.class);
    when(ctx.getHeaderString("X-Request-Id")).thenReturn(headerValue);

    String result = invokeResolveRequestId(ctx);

    // Generated UUIDs match the standard 8-4-4-4-12 pattern.
    assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
  }

  // ── normalizePath ─────────────────────────────────────────────────────────

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void shouldReturnRootSlashForBlankPath(String path) throws Exception {
    String result = invokeNormalizePath(path);
    assertThat(result).isEqualTo("/");
  }

  @Test
  void shouldLeavePathAlreadyStartingWithSlash() throws Exception {
    assertThat(invokeNormalizePath("/pet/1")).isEqualTo("/pet/1");
  }

  @Test
  void shouldPrependSlashToPathWithoutIt() throws Exception {
    assertThat(invokeNormalizePath("pet/1")).isEqualTo("/pet/1");
  }

  // ── durationMs ────────────────────────────────────────────────────────────

  @Test
  void shouldReturnPositiveDurationWhenStartTimeIsPresent() throws Exception {
    ContainerRequestContext ctx = mock(ContainerRequestContext.class);
    when(ctx.getProperty("requestStartTimeNanos")).thenReturn(System.nanoTime() - 5_000_000L);

    long duration = invokeDurationMs(ctx);

    assertThat(duration).isGreaterThanOrEqualTo(0L);
  }

  @Test
  void shouldReturnMinusOneWhenStartTimeIsAbsent() throws Exception {
    ContainerRequestContext ctx = mock(ContainerRequestContext.class);
    when(ctx.getProperty("requestStartTimeNanos")).thenReturn(null);

    assertThat(invokeDurationMs(ctx)).isEqualTo(-1L);
  }

  // ── filter response — header propagation ─────────────────────────────────

  @Test
  void shouldSetRequestIdHeaderOnResponse() throws Exception {
    ContainerRequestContext requestCtx = mock(ContainerRequestContext.class);
    ContainerResponseContext responseCtx = mock(ContainerResponseContext.class);
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();

    when(requestCtx.getProperty("requestId")).thenReturn("propagated-id");
    when(requestCtx.getProperty("requestStartTimeNanos")).thenReturn(null);
    when(responseCtx.getHeaders()).thenReturn(headers);
    when(responseCtx.getStatus()).thenReturn(200);
    when(requestCtx.getMethod()).thenReturn("GET");

    // Call the response filter. UriInfo and HttpServerRequest are null here because
    // normalizePath and remoteAddress handle null gracefully via their own guards.
    // We only assert the header side-effect which does not need those fields.
    Method method =
        LoggingFilter.class.getDeclaredMethod(
            "filter", ContainerRequestContext.class, ContainerResponseContext.class);
    // filter(response) path logs and sets a header — we verify the header was set.
    // The UriInfo @Context field is null so normalizePath will receive a null path from
    // info.getPath().
    // Patch the info field to avoid NPE.
    var infoField = LoggingFilter.class.getDeclaredField("info");
    infoField.setAccessible(true);
    jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
    when(uriInfo.getPath()).thenReturn("/pet");
    infoField.set(filter, uriInfo);

    var requestField = LoggingFilter.class.getDeclaredField("request");
    requestField.setAccessible(true);
    io.vertx.core.http.HttpServerRequest httpRequest =
        mock(io.vertx.core.http.HttpServerRequest.class);
    when(httpRequest.remoteAddress()).thenReturn(null);
    requestField.set(filter, httpRequest);

    method.setAccessible(true);
    method.invoke(filter, requestCtx, responseCtx);

    assertThat(headers.getFirst("X-Request-Id")).isEqualTo("propagated-id");
  }

  // ── filter request — full request filter path ─────────────────────────────

  @Test
  void shouldExecuteRequestFilterAndSetProperties() throws Exception {
    ContainerRequestContext requestCtx = mock(ContainerRequestContext.class);
    when(requestCtx.getHeaderString("X-Request-Id")).thenReturn("req-filter-id");
    when(requestCtx.getMethod()).thenReturn("POST");

    var infoField = LoggingFilter.class.getDeclaredField("info");
    infoField.setAccessible(true);
    jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
    when(uriInfo.getPath()).thenReturn("/pet");
    infoField.set(filter, uriInfo);

    var requestField = LoggingFilter.class.getDeclaredField("request");
    requestField.setAccessible(true);
    io.vertx.core.http.HttpServerRequest httpRequest =
        mock(io.vertx.core.http.HttpServerRequest.class);
    when(httpRequest.remoteAddress()).thenReturn(null);
    when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent/1.0");
    requestField.set(filter, httpRequest);

    filter.filter(requestCtx);

    org.mockito.Mockito.verify(requestCtx)
        .setProperty(org.mockito.ArgumentMatchers.eq("requestId"), org.mockito.ArgumentMatchers.eq("req-filter-id"));
    org.mockito.Mockito.verify(requestCtx)
        .setProperty(
            org.mockito.ArgumentMatchers.eq("requestStartTimeNanos"),
            org.mockito.ArgumentMatchers.any());
  }

  @Test
  void shouldUseUnknownWhenUserAgentIsAbsent() throws Exception {
    ContainerRequestContext requestCtx = mock(ContainerRequestContext.class);
    when(requestCtx.getHeaderString("X-Request-Id")).thenReturn("id-no-agent");
    when(requestCtx.getMethod()).thenReturn("GET");

    var infoField = LoggingFilter.class.getDeclaredField("info");
    infoField.setAccessible(true);
    jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
    when(uriInfo.getPath()).thenReturn("/store");
    infoField.set(filter, uriInfo);

    var requestField = LoggingFilter.class.getDeclaredField("request");
    requestField.setAccessible(true);
    io.vertx.core.http.HttpServerRequest httpRequest =
        mock(io.vertx.core.http.HttpServerRequest.class);
    when(httpRequest.remoteAddress()).thenReturn(null);
    when(httpRequest.getHeader("User-Agent")).thenReturn(null);
    requestField.set(filter, httpRequest);

    // Should not throw — "unknown" is used as the userAgent fallback.
    filter.filter(requestCtx);
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private String invokeResolveRequestId(ContainerRequestContext ctx) throws Exception {
    Method m =
        LoggingFilter.class.getDeclaredMethod("resolveRequestId", ContainerRequestContext.class);
    m.setAccessible(true);
    return (String) m.invoke(filter, ctx);
  }

  private String invokeNormalizePath(String path) throws Exception {
    Method m = LoggingFilter.class.getDeclaredMethod("normalizePath", String.class);
    m.setAccessible(true);
    return (String) m.invoke(filter, path);
  }

  private long invokeDurationMs(ContainerRequestContext ctx) throws Exception {
    Method m = LoggingFilter.class.getDeclaredMethod("durationMs", ContainerRequestContext.class);
    m.setAccessible(true);
    return (long) m.invoke(filter, ctx);
  }
}
