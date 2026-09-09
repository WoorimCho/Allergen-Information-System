package com.allergen_info_service.Services;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoodNightRestClientImplTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private MockRestServiceServer server;
    private GoodNightRestClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new GoodNightRestClientImpl(builder, CircuitBreakerRegistry.ofDefaults(), "http://goodnight");
    }

    @Test
    void getSnackPostsAndReturnsTheBody() throws Exception {
        server.expect(requestTo(endsWith("/snack"))).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("\"sleep tight\"", MediaType.APPLICATION_JSON));

        JsonNode payload = JSON.readTree("{\"name\":\"cookie\"}");
        assertThat(client.getSnack(payload)).isEqualTo("\"sleep tight\"");
        server.verify();
    }

    @Test
    void getSnackSwallowsDownstreamErrors() throws Exception {
        server.expect(requestTo(endsWith("/snack")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThat(client.getSnack(JSON.readTree("{}"))).startsWith("Error:");
    }
}
