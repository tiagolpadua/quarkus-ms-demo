package org.acme.rest.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import org.acme.shared.UiHomeResource;
import org.junit.jupiter.api.Test;

class OpenApiResourceTest {

  @Test
  void shouldExposeExpectedShortcutDefinitions() throws Exception {
    UiHomeResource resource = new UiHomeResource(null, null);
    Method shortcutsMethod = UiHomeResource.class.getDeclaredMethod("shortcuts");
    shortcutsMethod.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<Object> shortcuts = (List<Object>) shortcutsMethod.invoke(resource);

    assertThat(shortcuts).hasSize(8);
    assertThat(shortcuts)
        .extracting(Object::toString)
        .anyMatch(value -> value.contains("/q/openapi"))
        .anyMatch(value -> value.contains("/q/swagger-ui"))
        .anyMatch(value -> value.contains("/q/dev-ui"))
        .anyMatch(value -> value.contains("/q/health"))
        .anyMatch(value -> value.contains("/q/metrics"))
        .anyMatch(value -> value.contains("/q/info"));
  }
}
