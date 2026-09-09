package com.allergen_info_service.bff;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the typed clients this app uses to reach the downstream services. URLs
 * come from {@code recipe-catalogue.url} / {@code ingredient-catalogue.url} /
 * {@code user-service.url}, which default to localhost and are overridden with
 * the compose/k8s service names when the whole system runs together.
 */
@Configuration
class CatalogueClientConfig {

    @Bean
    RecipeCatalogueClient recipeCatalogueClient(RestClient.Builder builder,
                                            @Value("${recipe-catalogue.url}") String baseUrl) {
        return new RecipeCatalogueClient(builder.baseUrl(baseUrl).build());
    }

    @Bean
    IngredientCatalogueClient ingredientCatalogueClient(RestClient.Builder builder,
                                                    @Value("${ingredient-catalogue.url}") String baseUrl) {
        return new IngredientCatalogueClient(builder.baseUrl(baseUrl).build());
    }

    @Bean
    UserServiceClient userServiceClient(RestClient.Builder builder,
                                        @Value("${user-service.url}") String baseUrl) {
        return new UserServiceClient(builder.baseUrl(baseUrl).build());
    }
}
