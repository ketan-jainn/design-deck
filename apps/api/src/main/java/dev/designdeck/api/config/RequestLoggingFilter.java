package dev.designdeck.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Per-request MDC correlation filter.
 * Assigns X-Request-ID (generated if absent), and logs method, path, status, and duration.
 * Sets userId in MDC when the request is authenticated so all log lines carry it.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
  private static final String REQUEST_ID_HEADER = "X-Request-ID";
  private static final String MDC_REQUEST_ID = "requestId";
  private static final String MDC_USER_ID = "userId";

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (requestId == null || requestId.isBlank()) {
      requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    MDC.put(MDC_REQUEST_ID, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);
    long start = System.currentTimeMillis();

    try {
      filterChain.doFilter(request, response);
      // Set userId after filter chain (auth happens inside the chain)
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.getPrincipal() instanceof java.util.UUID userId) {
        MDC.put(MDC_USER_ID, userId.toString());
      }
    } finally {
      long duration = System.currentTimeMillis() - start;
      log.info("{} {} -> {} ({}ms)",
          request.getMethod(), request.getRequestURI(),
          response.getStatus(), duration);
      MDC.remove(MDC_REQUEST_ID);
      MDC.remove(MDC_USER_ID);
    }
  }
}
