package org.acme.shared;

import org.junit.jupiter.api.Test;

/**
 * Exercises the lifecycle observer methods to ensure they are reachable and do not throw.
 *
 * <p>{@link ApplicationLifecycle} uses CDI observer methods that normally execute when the
 * container starts and stops. This test instantiates the class directly and calls the methods with
 * {@code null} events, which is sufficient for coverage because the methods only log — they do not
 * branch on the event object.
 */
class ApplicationLifecycleTest {

  @Test
  void shouldStartWithoutThrowing() {
    ApplicationLifecycle lifecycle = new ApplicationLifecycle();
    lifecycle.onStart(null);
  }

  @Test
  void shouldStopWithoutThrowing() {
    ApplicationLifecycle lifecycle = new ApplicationLifecycle();
    lifecycle.onStop(null);
  }
}
