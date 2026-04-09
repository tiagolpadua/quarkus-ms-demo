package org.acme.shared;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ApplicationLifecycle {

  void onStart(@Observes StartupEvent event) {
    log.info("The application is starting with profile {}", ConfigUtils.getProfiles());
  }

  void onStop(@Observes ShutdownEvent event) {
    log.info("The application is stopping...");
  }
}
