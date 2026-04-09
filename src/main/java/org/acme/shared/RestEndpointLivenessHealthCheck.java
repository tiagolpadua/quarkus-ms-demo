package org.acme.shared;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class RestEndpointLivenessHealthCheck implements HealthCheck {

  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();

  @ConfigProperty(name = "quarkus.http.host", defaultValue = "localhost")
  String host;

  @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
  int port;

  @Override
  public HealthCheckResponse call() {
    String effectiveHost = normalizeHost(host);
    String target = "http://" + effectiveHost + ":" + port + "/q/openapi";

    HttpRequest request =
        HttpRequest.newBuilder(URI.create(target)).timeout(Duration.ofSeconds(2)).GET().build();

    try {
      HttpResponse<Void> response =
          HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
      boolean up = response.statusCode() >= 200 && response.statusCode() < 500;

      return (up
              ? HealthCheckResponse.named("REST endpoint liveness").up()
              : HealthCheckResponse.named("REST endpoint liveness").down())
          .withData("endpoint", target)
          .withData("statusCode", response.statusCode())
          .build();
    } catch (IOException | InterruptedException ex) {
      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }

      return HealthCheckResponse.named("REST endpoint liveness")
          .down()
          .withData("endpoint", target)
          .withData("error", ex.getClass().getSimpleName())
          .build();
    }
  }

  private String normalizeHost(String rawHost) {
    if (rawHost == null || rawHost.isBlank() || "0.0.0.0".equals(rawHost) || "::".equals(rawHost)) {
      return "localhost";
    }
    return rawHost;
  }
}
