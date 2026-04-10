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

    assertThat(template).contains("/webjars/bootstrap/5.3.8/dist/css/bootstrap.min.css");
    assertThat(template).contains("/webjars/bootstrap/5.3.8/dist/js/bootstrap.bundle.min.js");
    assertThat(template).contains("navbar navbar-expand-lg sticky-top");
    assertThat(template).contains("Application Info");
    assertThat(template).contains("educationalAccordion");
    assertThat(template).contains("Architecture Overview");
    assertThat(template).contains("API Domains");
    assertThat(template).contains("Technology Stack");
    assertThat(template).contains("https://github.com/tiagolpadua/quarkus-ms-demo");
    assertThat(template).doesNotContain("cdn.jsdelivr.net");
    assertThat(template).doesNotContain("unpkg.com");
  }
}
