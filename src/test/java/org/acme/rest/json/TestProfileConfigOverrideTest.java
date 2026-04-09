package org.acme.rest.json;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.util.Map;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TestProfileConfigOverrideTest.CustomTestProfile.class)
class TestProfileConfigOverrideTest {

  @Test
  void shouldApplyConfigOverrideFromTestProfile() {
    String marker = ConfigProvider.getConfig().getValue("test.profile.marker", String.class);
    assertThat(marker).isEqualTo("enabled");
  }

  public static class CustomTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("test.profile.marker", "enabled");
    }
  }
}
