package org.acme.rest.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URL;
import org.junit.jupiter.api.Test;

@QuarkusTest
@WithPlaywright
class SwaggerUiPlaywrightTest {

  @InjectPlaywright BrowserContext browserContext;

  @TestHTTPResource("/q/swagger-ui")
  URL swaggerUi;

  @Test
  void shouldLoadSwaggerUiPage() {
    Page page = browserContext.newPage();

    Response response = page.navigate(swaggerUi.toString());

    assertThat(response).isNotNull();
    assertThat(response.status()).isBetween(200, 399);
    assertThat(page.title()).containsIgnoringCase("openapi");
  }
}
