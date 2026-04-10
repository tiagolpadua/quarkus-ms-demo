package org.acme.shared;

import io.quarkus.info.BuildInfo;
import io.quarkus.info.GitInfo;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.eclipse.microprofile.config.ConfigProvider;

@Path("/")
public class UiHomeResource {

  private static final DateTimeFormatter BUILD_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @Inject Instance<BuildInfo> buildInfo;

  @Inject Instance<GitInfo> gitInfo;

  @CheckedTemplate
  static class Templates {
    static native TemplateInstance index(String baseUrl, List<Shortcut> shortcuts, AppInfo appInfo);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance index(@Context UriInfo uriInfo) {
    String baseUrl = uriInfo.getBaseUri().toString();
    return Templates.index(baseUrl, shortcuts(), appInfo());
  }

  private AppInfo appInfo() {
    BuildInfo currentBuildInfo =
        buildInfo != null && buildInfo.isResolvable() ? buildInfo.get() : null;
    GitInfo currentGitInfo = gitInfo != null && gitInfo.isResolvable() ? gitInfo.get() : null;

    String applicationName =
        ConfigProvider.getConfig()
            .getOptionalValue("quarkus.application.name", String.class)
            .orElse("quarkus-ms-demo");

    String applicationVersion =
        currentBuildInfo != null
            ? currentBuildInfo.version()
            : ConfigProvider.getConfig()
                .getOptionalValue("quarkus.application.version", String.class)
                .orElse("unknown");

    List<String> profiles = ConfigUtils.getProfiles();
    String activeProfile = profiles.isEmpty() ? "default" : profiles.getFirst();

    String buildTimestamp =
        currentBuildInfo != null ? formatBuildTime(currentBuildInfo.time()) : "unavailable";

    String gitBranch = currentGitInfo != null ? currentGitInfo.branch() : "unavailable";
    String gitShortCommitId =
        currentGitInfo != null
            ? abbreviateCommitId(currentGitInfo.latestCommitId())
            : "unavailable";

    return new AppInfo(
        applicationName,
        applicationVersion,
        System.getProperty("java.version", "unknown"),
        activeProfile,
        buildTimestamp,
        gitBranch,
        gitShortCommitId);
  }

  private String formatBuildTime(OffsetDateTime buildTime) {
    return buildTime == null ? "unavailable" : BUILD_TIME_FORMAT.format(buildTime);
  }

  private String abbreviateCommitId(String commitId) {
    if (commitId == null || commitId.isBlank()) {
      return "unavailable";
    }

    return commitId.length() <= 7 ? commitId : commitId.substring(0, 7);
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

  record AppInfo(
      String applicationName,
      String applicationVersion,
      String javaVersion,
      String activeProfile,
      String buildTimestamp,
      String gitBranch,
      String gitShortCommitId) {}
}
