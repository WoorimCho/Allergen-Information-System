package com.allergen_info_service.bff;

import org.springframework.cache.annotation.Cacheable;
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

    /**
     * One call for many ids. Ids that don't exist are simply absent from the
     * returned map, which is unmodifiable — callers only read it.
     *
     * <p>Cached under {@value CacheConfig#INGREDIENTS_BY_IDS} keyed by the id
     * <em>set</em> (a {@code TreeSet}, so call order doesn't matter), with a
     * short write-expiry set in {@code application.properties}. A composed recipe
     * view and the three calculators over the same recipe hit this with the same
     * ids; the portion slider hits it on every drag.
     */
    @Cacheable(cacheNames = CacheConfig.INGREDIENTS_BY_IDS, key = "new java.util.TreeSet(#ids)")
    public Map<Long, Ingredient> byIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Ingredient> found = http.get()
                .uri(uri -> uri.path("/api/ingredients/by-ids").queryParam("id", ids).build())
                .retrieve()
                .body(INGREDIENT_LIST);
        return (found == null ? List.<Ingredient>of() : found).stream()
                .collect(Collectors.toUnmodifiableMap(Ingredient::id, Function.identity(), (a, b) -> a));
    }

    public record Ingredient(Long id, String name, List<Tag> tags, Nutrition nutrition, Double densityGPerMl) {
    }

    public record Tag(String name) {
    }

    /** IngredientCatalogue's per-ingredient nutrition, all per {@code basisGrams} grams. Any field may be null. */
    public record Nutrition(
            Double basisGrams, Double kcal, Double proteinG, Double carbsG,
            Double fatG, Double fiberG, Double sugarG, Double sodiumMg) {
    }
}
