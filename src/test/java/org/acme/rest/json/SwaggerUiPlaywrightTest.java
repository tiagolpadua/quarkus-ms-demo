package org.acme.rest.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SwaggerUiPlaywrightTest {

  @Test
  void shouldReferenceLocalBootstrapAssetsWithoutCdn() throws IOException {
    String template =
        Files.readString(Path.of("src/main/resources/templates/UiHomeResource/index.html"));

    assertThat(template)
        .contains("/webjars/bootstrap/5.3.8/dist/css/bootstrap.min.css")
        .contains("/webjars/bootstrap/5.3.8/dist/js/bootstrap.bundle.min.js")
        .contains("navbar navbar-expand-lg sticky-top")
        .contains("Application Info")
        .contains("educationalAccordion")
        .contains("Architecture Overview")
        .contains("API Domains")
        .contains("Technology Stack")
        .contains("https://github.com/tiagolpadua/quarkus-ms-demo")
        .doesNotContain("cdn.jsdelivr.net")
        .doesNotContain("unpkg.com");
  }
}
