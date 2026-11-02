package com.allergen_info_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SECURITY.md N5: the BFF exposes the same minimal actuator surface as the other
 * services. {@code DELETE /actuator/caches} used to flush the byIds cache with no
 * authentication, which forces a re-fan-out to the catalogues on every request.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorExposureTest {

    @Autowired
    MockMvc mvc;

    @Test
    void healthAndPrometheusStayExposed() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk());
    }

    @Test
    void cacheEndpointIsNotExposed() throws Exception {
        mvc.perform(get("/actuator/caches")).andExpect(status().isNotFound());
        mvc.perform(delete("/actuator/caches")).andExpect(status().isNotFound());
        mvc.perform(delete("/actuator/caches/ingredientsByIds")).andExpect(status().isNotFound());
    }

    @Test
    void metricsEndpointIsNotExposed() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isNotFound());
    }

    @Test
    void theByIdsCacheIsStillObservableThroughPrometheus() throws Exception {
        // Why `metrics`/`caches` are safe to drop: the hit/miss counters are scraped from here.
        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("cache_gets_total")));
    }
}
