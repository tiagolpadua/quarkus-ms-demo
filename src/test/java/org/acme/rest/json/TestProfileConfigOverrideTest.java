package org.acme.rest.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TestProfileConfigOverrideTest {

  @Test
  void shouldKeepTestSpecificConfigurationInApplicationProperties() throws IOException {
    String properties = Files.readString(Path.of("src/main/resources/application.properties"));

    assertThat(properties).contains("%test.quarkus.otel.sdk.disabled=true");
  }
}
