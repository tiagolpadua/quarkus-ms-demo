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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Provider
@Slf4j
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
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

    log.info(
        "Request started requestId={} method={} path={} remoteIp={} userAgent={}",
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

    log.info(
        "Request completed requestId={} method={} path={} status={} durationMs={} remoteIp={}",
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
