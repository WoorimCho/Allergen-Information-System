package com.allergen_info_service.bff;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Talks to the User service: verifies credentials (for the BFF login), and reads
 * the bits of an account the BFF needs to personalise a recipe (dietary
 * restrictions, favourite substitutions). Nested records are this app's view of
 * the User service's responses.
 */
public class UserServiceClient {

    private static final ParameterizedTypeReference<List<String>> STRING_LIST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<FavoriteAlternative>> ALT_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient http;

    public UserServiceClient(RestClient http) {
        this.http = http;
    }

    /** Verify a username-or-email + password. Throws {@code HttpClientErrorException.Unauthorized} on a bad match. */
    public Identity authenticate(String identifier, String password) {
        return http.post().uri("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest(identifier, password))
                .retrieve().body(Identity.class);
    }

    /** Throws {@code HttpClientErrorException.NotFound} for an unknown account. */
    public Set<String> restrictions(long accountId) {
        List<String> codes = http.get().uri("/api/accounts/{id}/restrictions", accountId)
                .retrieve().body(STRING_LIST);
        return codes == null ? Set.of() : new LinkedHashSet<>(codes);
    }

    public List<Long> favoriteRecipeIds(long accountId) {
        List<Long> ids = http.get().uri("/api/accounts/{id}/favorites/recipes", accountId)
                .retrieve().body(LONG_LIST);
        return ids == null ? List.of() : ids;
    }

    public List<FavoriteAlternative> favoriteAlternatives(long accountId) {
        List<FavoriteAlternative> alts = http.get().uri("/api/accounts/{id}/favorites/alternatives", accountId)
                .retrieve().body(ALT_LIST);
        return alts == null ? List.of() : alts;
    }

    public record FavoriteAlternative(Long id, Long recipeId, Long ingredientId, Long replacementIngredientId) {
    }

    public record Identity(Long accountId, String username, String displayName) {
    }

    private record AuthRequest(String identifier, String password) {
    }
}
