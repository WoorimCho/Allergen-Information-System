package com.allergen_info_service.config;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;

/**
 * Wraps every outbound HTTP call to one downstream in a named
 * {@link CircuitBreaker}.
 *
 * <ul>
 *   <li>A connection failure, a read timeout, or a <b>5xx</b> from the
 *       downstream counts as a fault.</li>
 *   <li>A <b>4xx</b> passes straight through — it's a normal API answer, not an
 *       outage.</li>
 *   <li>When the breaker is <b>OPEN</b> the call is rejected immediately as a
 *       {@link ResourceAccessException} (which the exception advice maps to a
 *       502 / 503), instead of the request hanging on a dead service and the
 *       failure spreading back up the call chain.</li>
 * </ul>
 *
 * Paired with a short connect/read timeout ({@code spring.http.client.*}) so a
 * slow downstream trips the breaker via the {@code slowCallRateThreshold} rather
 * than tying up request threads.
 */
public final class CircuitBreakerInterceptor implements ClientHttpRequestInterceptor {

    private final CircuitBreaker breaker;

    public CircuitBreakerInterceptor(CircuitBreaker breaker) {
        this.breaker = breaker;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        try {
            return breaker.executeCallable(() -> {
                ClientHttpResponse response = execution.execute(request, body);
                if (response.getStatusCode().is5xxServerError()) {
                    // Count it against the breaker, and surface it as "unavailable"
                    // rather than leaking the downstream's 5xx body upward.
                    throw new IOException(breaker.getName() + " returned HTTP "
                            + response.getStatusCode().value());
                }
                return response;
            });
        } catch (CallNotPermittedException open) {
            throw new ResourceAccessException(
                    breaker.getName() + " is unavailable (circuit " + breaker.getState() + ")");
        } catch (IOException io) {
            // Transport failure (connect / read timeout) or our own 5xx signal;
            // already recorded as a fault by the breaker. Normalise to the type
            // the exception advice maps to a 502 / 503.
            throw new ResourceAccessException(breaker.getName() + " call failed: " + io.getMessage(), io);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new ResourceAccessException(breaker.getName() + " call failed", new IOException(e));
        }
    }
}
