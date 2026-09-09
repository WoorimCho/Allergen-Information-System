package com.allergen_info_service.bff;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Reads recipes from RecipeCatalogue. The nested records are this app's view of
 * that service's {@code RecipeResponse} — only the fields we consume. Unknown
 * fields are ignored by the JSON reader.
 */
public class RecipeCatalogueClient {

    private static final ParameterizedTypeReference<List<Recipe>> RECIPE_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient http;

    public RecipeCatalogueClient(RestClient http) {
        this.http = http;
    }

    /** Throws {@link org.springframework.web.client.HttpClientErrorException.NotFound} for an unknown id. */
    public Recipe getRecipe(long id) {
        return http.get().uri("/api/recipes/{id}", id).retrieve().body(Recipe.class);
    }

    /** Batch resolve; unknown ids are simply absent from the result. */
    public List<Recipe> getRecipesByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Recipe> found = http.get()
                .uri(uri -> uri.path("/api/recipes/by-ids").queryParam("id", ids).build())
                .retrieve().body(RECIPE_LIST);
        return found == null ? List.of() : found;
    }

    public record Recipe(
            Long id, String name, String creator, Integer version,
            List<Step> steps, List<Ingredient> ingredients, List<Tag> tags) {
    }

    public record Step(Integer position, String text, Set<String> tools) {
    }

    public record Ingredient(
            Long ingredientId, String quantity, Double amount, String unit,
            boolean optional, boolean replaceable,
            List<Replacement> replacements) {
    }

    public record Replacement(Long ingredientId, Long recipeId) {
    }

    public record Tag(String name) {
    }
}
