package org.acme.shared;

import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOG = Logger.getLogger(LoggingFilter.class);
  private static final String REQUEST_ID = "requestId";
  private static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final String START_TIME_NANOS = "requestStartTimeNanos";

  @Context UriInfo info;

  @Context HttpServerRequest request;

  @Override
  public void filter(ContainerRequestContext context) {
    String requestId = resolveRequestId(context);
    context.setProperty(REQUEST_ID, requestId);
    context.setProperty(START_TIME_NANOS, System.nanoTime());
    MDC.put(REQUEST_ID, requestId);

    LOG.infof(
        "Request started requestId=%s method=%s path=%s remoteIp=%s userAgent=%s",
        requestId,
        context.getMethod(),
        normalizePath(info.getPath()),
        remoteAddress(),
        userAgent());
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    String requestId = (String) requestContext.getProperty(REQUEST_ID);
    responseContext.getHeaders().putSingle(REQUEST_ID_HEADER, requestId);

    LOG.infof(
        "Request completed requestId=%s method=%s path=%s status=%d durationMs=%d remoteIp=%s",
        requestId,
        requestContext.getMethod(),
        normalizePath(info.getPath()),
        responseContext.getStatus(),
        durationMs(requestContext),
        remoteAddress());

    MDC.remove(REQUEST_ID);
  }

  private String resolveRequestId(ContainerRequestContext context) {
    String incomingRequestId = context.getHeaderString(REQUEST_ID_HEADER);
    if (incomingRequestId != null && !incomingRequestId.isBlank()) {
      return incomingRequestId;
    }
    return UUID.randomUUID().toString();
  }

  private long durationMs(ContainerRequestContext context) {
    Object startTimeNanos = context.getProperty(START_TIME_NANOS);
    if (startTimeNanos instanceof Long startTime) {
      return (System.nanoTime() - startTime) / 1_000_000;
    }
    return -1;
  }

  private String normalizePath(String path) {
    if (path == null || path.isBlank()) {
      return "/";
    }
    return path.startsWith("/") ? path : "/" + path;
  }

  private String remoteAddress() {
    return request.remoteAddress() == null ? "unknown" : request.remoteAddress().toString();
  }

  private String userAgent() {
    String userAgent = request.getHeader("User-Agent");
    return userAgent == null || userAgent.isBlank() ? "unknown" : userAgent;
  }
}
