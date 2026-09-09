package com.allergen_info_service.bff;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The recipe/ingredient "join" a monolith would do in SQL: fetch a recipe from
 * RecipeCatalogue, collect every catalogue id it points at (line items and their
 * substitutes), resolve them in one call to IngredientCatalogue, and stitch names
 * + tags back in. Also derives {@code inheritedTags} — the union of the line
 * ingredients' tags — which is why that lives here and not in RecipeCatalogue.
 *
 * <p>When an {@code accountId} is supplied it also asks the User service for
 * that account's restrictions and favourite substitutions, then annotates the
 * result: {@code restrictionConflicts} lists any line ingredient carrying a tag
 * the account avoids, and each line gets the account's preferred replacement for
 * it (if any). Unresolved ids are tolerated ({@code resolved=false} + a
 * placeholder name).
 */
@Service
public class RecipeCompositionService {

    private final RecipeCatalogueClient recipes;
    private final IngredientCatalogueClient ingredients;
    private final UserServiceClient users;

    public RecipeCompositionService(RecipeCatalogueClient recipes,
                                    IngredientCatalogueClient ingredients,
                                    UserServiceClient users) {
        this.recipes = recipes;
        this.ingredients = ingredients;
        this.users = users;
    }

    public RecipeView compose(long recipeId) {
        return compose(recipeId, null);
    }

    public RecipeView compose(long recipeId, Long accountId) {
        RecipeCatalogueClient.Recipe recipe = recipes.getRecipe(recipeId);

        Set<String> restrictions = Set.of();
        Map<Long, Long> preferredByIngredient = Map.of();
        if (accountId != null) {
            restrictions = users.restrictions(accountId);
            preferredByIngredient = preferredReplacements(users.favoriteAlternatives(accountId), recipeId);
        }

        Set<Long> idsToResolve = new LinkedHashSet<>();
        for (RecipeCatalogueClient.Ingredient line : nullToEmpty(recipe.ingredients())) {
            idsToResolve.add(line.ingredientId());
            for (RecipeCatalogueClient.Replacement rep : nullToEmpty(line.replacements())) {
                idsToResolve.add(rep.ingredientId());
            }
        }
        idsToResolve.addAll(preferredByIngredient.values());
        Map<Long, IngredientCatalogueClient.Ingredient> resolved = ingredients.byIds(idsToResolve);

        List<Line> lines = new ArrayList<>();
        Set<String> inheritedTags = new LinkedHashSet<>();
        for (RecipeCatalogueClient.Ingredient line : nullToEmpty(recipe.ingredients())) {
            IngredientCatalogueClient.Ingredient entry = resolved.get(line.ingredientId());
            List<String> tags = tagNames(entry);
            inheritedTags.addAll(tags);

            List<Replacement> reps = new ArrayList<>();
            for (RecipeCatalogueClient.Replacement rep : nullToEmpty(line.replacements())) {
                IngredientCatalogueClient.Ingredient sub = resolved.get(rep.ingredientId());
                reps.add(new Replacement(rep.ingredientId(), nameOf(sub, rep.ingredientId()),
                        sub != null, rep.recipeId()));
            }

            Long preferredId = preferredByIngredient.get(line.ingredientId());
            String preferredName = preferredId == null
                    ? null : nameOf(resolved.get(preferredId), preferredId);

            lines.add(new Line(line.ingredientId(), nameOf(entry, line.ingredientId()), entry != null,
                    line.quantity(), line.amount(), line.unit(), line.optional(), line.replaceable(), tags, reps,
                    preferredId, preferredName));
        }

        List<Step> steps = nullToEmpty(recipe.steps()).stream()
                .map(s -> new Step(s.position(), s.text(), s.tools()))
                .toList();
        List<String> ownTags = nullToEmpty(recipe.tags()).stream()
                .map(RecipeCatalogueClient.Tag::name)
                .toList();

        List<RestrictionConflict> conflicts = null;
        Boolean compatible = null;
        if (accountId != null) {
            conflicts = restrictionConflicts(restrictions, lines);
            compatible = conflicts.isEmpty();
        }

        return new RecipeView(recipe.id(), recipe.name(), recipe.creator(), recipe.version(),
                steps, lines, ownTags, List.copyOf(inheritedTags), conflicts, compatible);
    }

    /** An account's favourite recipes, resolved to summaries. */
    public List<RecipeSummary> favoriteRecipeSummaries(long accountId) {
        List<Long> ids = users.favoriteRecipeIds(accountId);
        return recipes.getRecipesByIds(ids).stream()
                .map(r -> new RecipeSummary(r.id(), r.name(), r.creator(), r.version(),
                        nullToEmpty(r.tags()).stream().map(RecipeCatalogueClient.Tag::name).toList()))
                .toList();
    }

    private static Map<Long, Long> preferredReplacements(
            List<UserServiceClient.FavoriteAlternative> alternatives, long recipeId) {
        Map<Long, Long> byIngredient = new LinkedHashMap<>();
        for (UserServiceClient.FavoriteAlternative alt : alternatives) {
            if (Objects.equals(alt.recipeId(), recipeId)) {
                byIngredient.put(alt.ingredientId(), alt.replacementIngredientId());
            }
        }
        return byIngredient;
    }

    private static List<RestrictionConflict> restrictionConflicts(Set<String> restrictions, List<Line> lines) {
        List<RestrictionConflict> conflicts = new ArrayList<>();
        for (String restriction : restrictions) {
            List<Line> hits = lines.stream().filter(line -> line.tags().contains(restriction)).toList();
            if (!hits.isEmpty()) {
                conflicts.add(new RestrictionConflict(restriction,
                        hits.stream().map(Line::ingredientId).toList(),
                        hits.stream().map(Line::name).toList()));
            }
        }
        return conflicts;
    }

    private static List<String> tagNames(IngredientCatalogueClient.Ingredient entry) {
        if (entry == null || entry.tags() == null) {
            return List.of();
        }
        return entry.tags().stream().map(IngredientCatalogueClient.Tag::name).toList();
    }

    private static String nameOf(IngredientCatalogueClient.Ingredient entry, long id) {
        return entry != null ? entry.name() : "unknown ingredient (#" + id + ")";
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    public record RecipeView(
            Long id, String name, String creator, Integer version,
            List<Step> steps, List<Line> ingredients,
            List<String> tags, List<String> inheritedTags,
            List<RestrictionConflict> restrictionConflicts, Boolean compatibleWithAccount) {
    }

    public record Step(Integer position, String text, Set<String> tools) {
    }

    public record Line(
            Long ingredientId, String name, boolean resolved, String quantity,
            Double amount, String unit,
            boolean optional, boolean replaceable,
            List<String> tags, List<Replacement> replacements,
            Long preferredReplacementId, String preferredReplacementName) {
    }

    public record Replacement(Long ingredientId, String name, boolean resolved, Long recipeId) {
    }

    public record RestrictionConflict(String restriction, List<Long> ingredientIds, List<String> ingredientNames) {
    }

    public record RecipeSummary(Long id, String name, String creator, Integer version, List<String> tags) {
    }
}
