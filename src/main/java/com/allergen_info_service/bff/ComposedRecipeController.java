package com.allergen_info_service.bff;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BFF composition endpoints, personalised for the logged-in account (see
 * {@link BffSecurityConfig} — a session established via {@code POST /bff/login}).
 * The account id comes from the session, never a request parameter.
 */
@RestController
@RequestMapping("/bff")
public class ComposedRecipeController {

    private final RecipeCompositionService composition;

    public ComposedRecipeController(RecipeCompositionService composition) {
        this.composition = composition;
    }

    /** A recipe resolved and personalised for the caller (restriction conflicts + preferred substitutions). */
    @GetMapping("/recipes/{id}")
    public RecipeCompositionService.RecipeView recipe(@PathVariable long id,
                                                     @AuthenticationPrincipal BffPrincipal principal) {
        return composition.compose(id, principal.accountId());
    }

    /** The caller's favourite recipes, as summaries. */
    @GetMapping("/accounts/{accountId}/favorite-recipes")
    public List<RecipeCompositionService.RecipeSummary> favoriteRecipes(
            @PathVariable long accountId, @AuthenticationPrincipal BffPrincipal principal) {
        if (!principal.accountId().equals(accountId)) {
            throw new AccessDeniedException("You can only view your own favourites.");
        }
        return composition.favoriteRecipeSummaries(accountId);
    }
}
