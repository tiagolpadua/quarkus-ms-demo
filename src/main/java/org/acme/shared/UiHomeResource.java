package org.acme.shared;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

@Path("/")
public class UiHomeResource {

  @CheckedTemplate
  static class Templates {
    static native TemplateInstance index(String baseUrl, List<Shortcut> shortcuts);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance index(@Context UriInfo uriInfo) {
    String baseUrl = uriInfo.getBaseUri().toString();
    return Templates.index(baseUrl, shortcuts());
  }

  private List<Shortcut> shortcuts() {
    return List.of(
        new Shortcut(
            "Documentation",
            "OpenAPI",
            "Raw API description exposed by SmallRye OpenAPI.",
            "/q/openapi",
            "Open OpenAPI"),
        new Shortcut(
            "Documentation",
            "Swagger UI",
            "Interactive interface to explore and test REST endpoints.",
            "/q/swagger-ui",
            "Open Swagger UI"),
        new Shortcut(
            "Tools",
            "Dev UI",
            "Quarkus development panel with extensions, H2, and local utilities.",
            "/q/dev-ui",
            "Open Dev UI"),
        new Shortcut(
            "Health",
            "Health",
            "Consolidated view of application health checks.",
            "/q/health",
            "Open Health"),
        new Shortcut(
            "Health",
            "Readiness",
            "Endpoint to confirm whether the application is ready to receive traffic.",
            "/q/health/ready",
            "Open Readiness"),
        new Shortcut(
            "Health",
            "Liveness",
            "Endpoint to verify whether the application is still alive during execution.",
            "/q/health/live",
            "Open Liveness"),
        new Shortcut(
            "Observability",
            "Metrics",
            "Micrometer metrics exposed by the runtime for inspection and scraping.",
            "/q/metrics",
            "Open Metrics"),
        new Shortcut(
            "Observability",
            "Info",
            "Build, git, and general service information metadata.",
            "/q/info",
            "Open Info"));
  }

  record Shortcut(String tag, String title, String description, String href, String cta) {}
}
