package com.allergen_info_service.bff;

import com.allergen_info_service.config.DownstreamResilience;
import com.allergen_info_service.internal.InternalAuthClientInterceptor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the typed clients this app uses to reach the downstream services. URLs
 * come from {@code recipe-catalogue.url} / {@code ingredient-catalogue.url} /
 * {@code user-service.url}, which default to localhost and are overridden with
 * the compose/k8s service names when the whole system runs together.
 *
 * <p>Each client gets a short connect/read timeout ({@code spring.http.client.*}
 * on the auto-configured builder) and a per-downstream circuit-breaker
 * interceptor, so one slow or dead service fails fast here instead of hanging
 * the composed request.
 */
@Configuration
class CatalogueClientConfig {

    private final CircuitBreakerRegistry breakers;
    private final InternalAuthClientInterceptor internalAuth;

    CatalogueClientConfig(CircuitBreakerRegistry breakers,
                          @Value("${internal-auth.secret:dev-internal-secret-change-me}") String internalAuthSecret) {
        this.breakers = breakers;
        this.internalAuth = new InternalAuthClientInterceptor(internalAuthSecret);
    }

    @Bean
    RecipeCatalogueClient recipeCatalogueClient(RestClient.Builder builder,
                                                @Value("${recipe-catalogue.url}") String baseUrl) {
        return new RecipeCatalogueClient(builder.baseUrl(baseUrl)
                .requestInterceptor(internalAuth)
                .requestInterceptor(DownstreamResilience.forDownstream(breakers, "recipe-catalogue"))
                .build());
    }

    @Bean
    IngredientCatalogueClient ingredientCatalogueClient(RestClient.Builder builder,
                                                        @Value("${ingredient-catalogue.url}") String baseUrl) {
        return new IngredientCatalogueClient(builder.baseUrl(baseUrl)
                .requestInterceptor(internalAuth)
                .requestInterceptor(DownstreamResilience.forDownstream(breakers, "ingredient-catalogue"))
                .build());
    }

    @Bean
    UserServiceClient userServiceClient(RestClient.Builder builder,
                                        @Value("${user-service.url}") String baseUrl) {
        return new UserServiceClient(builder.baseUrl(baseUrl)
                .requestInterceptor(internalAuth)
                .requestInterceptor(DownstreamResilience.forDownstream(breakers, "user-service"))
                .build());
    }
}
