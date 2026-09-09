package com.allergen_info_service.bff;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves catalogue-entry ids to names + tags via IngredientCatalogue's batch
 * endpoint. The nested records are this app's view of that service's
 * {@code IngredientResponse}.
 */
public class IngredientCatalogueClient {

    private static final ParameterizedTypeReference<List<Ingredient>> INGREDIENT_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient http;

    public IngredientCatalogueClient(RestClient http) {
        this.http = http;
    }

    /** One call for many ids. Ids that don't exist are simply absent from the returned map. */
    public Map<Long, Ingredient> byIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Ingredient> found = http.get()
                .uri(uri -> uri.path("/api/ingredients/by-ids").queryParam("id", ids).build())
                .retrieve()
                .body(INGREDIENT_LIST);
        return (found == null ? List.<Ingredient>of() : found).stream()
                .collect(Collectors.toMap(Ingredient::id, Function.identity(), (a, b) -> a));
    }

    public record Ingredient(Long id, String name, List<Tag> tags, Nutrition nutrition) {
    }

    public record Tag(String name) {
    }

    /** IngredientCatalogue's per-ingredient nutrition, all per {@code basisGrams} grams. Any field may be null. */
    public record Nutrition(
            Double basisGrams, Double kcal, Double proteinG, Double carbsG,
            Double fatG, Double fiberG, Double sugarG, Double sodiumMg) {
    }
}
