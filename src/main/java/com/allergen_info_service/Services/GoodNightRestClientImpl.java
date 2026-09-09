package com.allergen_info_service.Services;

import tools.jackson.databind.JsonNode;  // Jackson 3 (Boot 4 default)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Client for the GoodNightWorld tester service. Uses the Boot-auto-configured
 * {@link RestClient.Builder}; base URL from {@code goodnight.url} (env
 * {@code GOODNIGHT_URL}). Downstream failures are logged and swallowed — this is
 * a tester, not a critical dependency.
 */
@Service
public class GoodNightRestClientImpl implements GoodNightRestClient {

    private static final Logger log = LoggerFactory.getLogger(GoodNightRestClientImpl.class);

    private final RestClient http;

    public GoodNightRestClientImpl(RestClient.Builder builder,
                                   @Value("${goodnight.url:http://localhost:8085}") String goodNightUrl) {
        this.http = builder.baseUrl(goodNightUrl).build();
    }

    public void getNight() {
        try {
            String body = http.get().uri("/night").retrieve().body(String.class);
            log.info("Response from /night endpoint: {}", body);
        } catch (RestClientException e) {
            log.error("Error calling /night endpoint", e);
        }
    }

    public void fatter() {
        try {
            Object body = http.get().uri("/fat").retrieve().body(Object.class);
            log.info("Response from /fat endpoint: {}", body);
        } catch (RestClientException e) {
            log.error("Error calling /fat endpoint", e);
        }
    }

    public String getSnack(JsonNode rawJson) {
        try {
            String body = http.post().uri("/snack").body(rawJson).retrieve().body(String.class);
            log.info("Response from /snack endpoint: {}", body);
            return body;
        } catch (RestClientException e) {
            log.error("Error calling /snack endpoint", e);
            return "Error: " + e.getMessage();
        }
    }
}
