package com.allergen_info_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CircuitBreakerInterceptorTest {

    private CircuitBreaker breaker;
    private final MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://svc/x"));

    @BeforeEach
    void setUp() {
        breaker = CircuitBreaker.of("test", CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50f)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build());
    }

    private ClientHttpResponse run(ClientHttpRequestExecution execution) throws IOException {
        return new CircuitBreakerInterceptor(breaker).intercept(request, new byte[0], execution);
    }

    @Test
    void passesSuccessThrough_noFault() throws IOException {
        ClientHttpResponse resp = run((req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(breaker.getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(1);
        assertThat(breaker.getMetrics().getNumberOfFailedCalls()).isZero();
    }

    @Test
    void passesClientErrorThrough_noFault() throws IOException {
        ClientHttpResponse resp = run((req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(breaker.getMetrics().getNumberOfFailedCalls()).isZero();
    }

    @Test
    void serverErrorBecomesResourceAccessException_andCountsAsFault() {
        assertThatThrownBy(() -> run((req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.BAD_GATEWAY)))
                .isInstanceOf(ResourceAccessException.class);
        assertThat(breaker.getMetrics().getNumberOfFailedCalls()).isEqualTo(1);
    }

    @Test
    void transportFailureBecomesResourceAccessException_andCountsAsFault() {
        assertThatThrownBy(() -> run((req, body) -> { throw new IOException("connect timed out"); }))
                .isInstanceOf(ResourceAccessException.class)
                .hasRootCauseInstanceOf(IOException.class);
        assertThat(breaker.getMetrics().getNumberOfFailedCalls()).isEqualTo(1);
    }

    @Test
    void whenOpen_rejectsImmediatelyWithoutCallingDownstream() {
        breaker.transitionToOpenState();
        AtomicInteger downstreamCalls = new AtomicInteger();

        assertThatThrownBy(() -> run((req, body) -> {
            downstreamCalls.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        }))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("circuit");

        assertThat(downstreamCalls).hasValue(0);
    }
}
