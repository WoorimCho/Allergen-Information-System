package com.allergen_info_service.bff;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Phase 4 calculators. Each fetches a recipe from RecipeCatalogue, resolves its
 * line ingredients in one batch call to IngredientCatalogue, and does arithmetic
 * the services can't (they don't see each other):
 *
 * <ul>
 *   <li><b>nutrition</b> — sum each line's {@code amount + unit} × the
 *       ingredient's per-100&nbsp;g nutrition. Lines whose unit isn't a weight
 *       (cups, pieces) or whose ingredient has no nutrition are listed, not
 *       counted.</li>
 *   <li><b>calories</b> — the kcal slice of the above.</li>
 *   <li><b>portions</b> — scale every line's numeric amount by a factor, given
 *       directly or derived from a target amount for one line.</li>
 * </ul>
 */
@Service
public class RecipeCalculatorService {

    private final RecipeCatalogueClient recipes;
    private final IngredientCatalogueClient ingredients;

    public RecipeCalculatorService(RecipeCatalogueClient recipes, IngredientCatalogueClient ingredients) {
        this.recipes = recipes;
        this.ingredients = ingredients;
    }

    // ── nutrition ────────────────────────────────────────────────────────────

    public NutritionResult nutrition(long recipeId, int servings) {
        int serv = Math.max(servings, 1);
        RecipeCatalogueClient.Recipe recipe = recipes.getRecipe(recipeId);
        Map<Long, IngredientCatalogueClient.Ingredient> resolved = resolveLineIngredients(recipe);

        List<LineNutrition> lines = new ArrayList<>();
        List<String> notCounted = new ArrayList<>();
        double[] total = new double[7];               // kcal, protein, carbs, fat, fiber, sugar, sodium
        boolean[] any = new boolean[7];

        double countedGrams = 0;
        for (RecipeCatalogueClient.Ingredient line : nullToEmpty(recipe.ingredients())) {
            IngredientCatalogueClient.Ingredient entry = resolved.get(line.ingredientId());
            String name = entry != null ? entry.name() : "unknown ingredient (#" + line.ingredientId() + ")";
            Double density = densityFor(entry);
            OptionalDouble grams = Units.toGrams(line.amount(), line.unit(), density);
            Double millilitres = millilitresOf(line, grams, density);
            IngredientCatalogueClient.Nutrition n = entry == null ? null : entry.nutrition();

            String note = whyNotCounted(line, entry, grams, n);
            if (note != null) {
                lines.add(new LineNutrition(line.ingredientId(), name, line.amount(), line.unit(),
                        grams.isPresent() ? round1(grams.getAsDouble()) : null, millilitres,
                        line.optional(), false, note, null));
                notCounted.add(name + " — " + note);
                continue;
            }

            double factor = grams.getAsDouble() / n.basisGrams();
            NutritionSum c = new NutritionSum(
                    scaled(n.kcal(), factor), scaled(n.proteinG(), factor), scaled(n.carbsG(), factor),
                    scaled(n.fatG(), factor), scaled(n.fiberG(), factor), scaled(n.sugarG(), factor),
                    scaled(n.sodiumMg(), factor));
            addTo(total, any, c);
            countedGrams += grams.getAsDouble();
            lines.add(new LineNutrition(line.ingredientId(), name, line.amount(), line.unit(),
                    round1(grams.getAsDouble()), millilitres, line.optional(), true, null, c));
        }

        NutritionSum totalSum = toSum(total, any, 1);
        NutritionSum perServing = toSum(total, any, serv);
        // energy density: the totals spread back over the counted weight
        Double gramsCounted = countedGrams > 0 ? round1(countedGrams) : null;
        NutritionSum per100g = countedGrams > 0 ? toSumDivided(total, any, countedGrams / 100.0) : null;
        return new NutritionResult(recipe.id(), recipe.name(), serv, totalSum, perServing,
                per100g, gramsCounted, lines, notCounted);
    }

    public CalorieResult calories(long recipeId, int servings) {
        NutritionResult n = nutrition(recipeId, servings);
        long counted = n.lines().stream().filter(LineNutrition::counted).count();
        Double per100 = n.per100g() == null ? null : n.per100g().kcal();
        Double perGram = per100 == null ? null : round2(per100 / 100.0);
        return new CalorieResult(n.recipeId(), n.recipeName(), n.servings(),
                n.total().kcal(), n.perServing().kcal(), per100, perGram, n.countedGrams(),
                (int) counted, n.lines().size());
    }

    // ── portions ─────────────────────────────────────────────────────────────

    public PortionResult portions(long recipeId, Double scale,
                                  Long anchorIngredientId, Double anchorAmount, String anchorUnit) {
        RecipeCatalogueClient.Recipe recipe = recipes.getRecipe(recipeId);
        double factor = resolveScale(recipe, scale, anchorIngredientId, anchorAmount, anchorUnit);
        Map<Long, IngredientCatalogueClient.Ingredient> resolved = resolveLineIngredients(recipe);

        List<ScaledLine> lines = new ArrayList<>();
        for (RecipeCatalogueClient.Ingredient line : nullToEmpty(recipe.ingredients())) {
            IngredientCatalogueClient.Ingredient entry = resolved.get(line.ingredientId());
            String name = entry != null ? entry.name() : "unknown ingredient (#" + line.ingredientId() + ")";
            Double scaledAmount = line.amount() == null ? null : round1(line.amount() * factor);

            // volume↔mass equivalents, scaled by the same factor, when we can derive them
            Double density = densityFor(entry);
            OptionalDouble grams = Units.toGrams(line.amount(), line.unit(), density);
            Double millilitres = millilitresOf(line, grams, density);
            Double scaledGrams = grams.isPresent() ? round1(grams.getAsDouble() * factor) : null;
            Double scaledMillilitres = millilitres == null ? null : round1(millilitres * factor);

            lines.add(new ScaledLine(line.ingredientId(), name, line.amount(), line.unit(),
                    scaledAmount, scaledGrams, scaledMillilitres, line.quantity(), line.amount() != null));
        }
        return new PortionResult(recipe.id(), recipe.name(), round2(factor), lines);
    }

    private static double resolveScale(RecipeCatalogueClient.Recipe recipe, Double scale,
                                       Long anchorIngredientId, Double anchorAmount, String anchorUnit) {
        boolean anchored = anchorIngredientId != null || anchorAmount != null || anchorUnit != null;
        if (scale != null && anchored) {
            throw new IllegalArgumentException("give either scale or the anchor params, not both");
        }
        if (scale != null) {
            if (scale <= 0) {
                throw new IllegalArgumentException("scale must be > 0");
            }
            return scale;
        }
        if (!anchored) {
            return 1.0;
        }
        if (anchorIngredientId == null || anchorAmount == null || anchorUnit == null) {
            throw new IllegalArgumentException("anchor needs anchorIngredientId, anchorAmount and anchorUnit");
        }
        RecipeCatalogueClient.Ingredient line = nullToEmpty(recipe.ingredients()).stream()
                .filter(l -> anchorIngredientId.equals(l.ingredientId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ingredient #" + anchorIngredientId + " is not in this recipe"));
        if (line.amount() == null || line.unit() == null) {
            throw new IllegalArgumentException("the anchor line has no structured amount to scale from");
        }
        if (Units.dimension(line.unit()) != Units.dimension(anchorUnit)) {
            throw new IllegalArgumentException("anchorUnit '" + anchorUnit + "' is a different kind of unit than the recipe's '" + line.unit() + "'");
        }
        OptionalDouble lineBase = Units.toBase(line.amount(), line.unit());
        OptionalDouble anchorBase = Units.toBase(anchorAmount, anchorUnit);
        if (lineBase.isEmpty() || anchorBase.isEmpty() || lineBase.getAsDouble() == 0) {
            throw new IllegalArgumentException("can't convert the anchor to a comparable amount");
        }
        return anchorBase.getAsDouble() / lineBase.getAsDouble();
    }

    // ── shared ───────────────────────────────────────────────────────────────

    private Map<Long, IngredientCatalogueClient.Ingredient> resolveLineIngredients(RecipeCatalogueClient.Recipe recipe) {
        Set<Long> ids = new LinkedHashSet<>();
        for (RecipeCatalogueClient.Ingredient line : nullToEmpty(recipe.ingredients())) {
            ids.add(line.ingredientId());
        }
        return ingredients.byIds(ids);
    }

    private static String whyNotCounted(RecipeCatalogueClient.Ingredient line,
                                        IngredientCatalogueClient.Ingredient entry,
                                        OptionalDouble grams, IngredientCatalogueClient.Nutrition n) {
        if (entry == null) {
            return "unknown ingredient";
        }
        if (line.amount() == null || line.unit() == null) {
            return "no structured amount";
        }
        if (grams.isEmpty()) {
            return switch (Units.dimension(line.unit())) {
                case VOLUME -> "volume unit '" + line.unit() + "' needs a density — set one on the ingredient";
                case COUNT -> "unit '" + line.unit() + "' is a count — needs a per-item weight";
                default -> "unit '" + line.unit() + "' can't be converted to a weight";
            };
        }
        if (n == null || n.basisGrams() == null || n.basisGrams() <= 0 || !hasAnyFigure(n)) {
            return "no nutrition data";
        }
        return null;
    }

    /** The ingredient's own density if set, else a common-name guess, else null. */
    private static Double densityFor(IngredientCatalogueClient.Ingredient entry) {
        if (entry == null) {
            return null;
        }
        if (entry.densityGPerMl() != null && entry.densityGPerMl() > 0) {
            return entry.densityGPerMl();
        }
        return CommonDensities.lookup(entry.name());
    }

    /** A line's volume in millilitres: direct for VOLUME units, via density for MASS units, else null. */
    private static Double millilitresOf(RecipeCatalogueClient.Ingredient line, OptionalDouble grams, Double density) {
        if (Units.dimension(line.unit()) == Units.Dimension.VOLUME) {
            OptionalDouble ml = Units.toBase(line.amount(), line.unit());
            return ml.isPresent() ? round1(ml.getAsDouble()) : null;
        }
        if (grams.isPresent() && density != null && density > 0) {
            return round1(grams.getAsDouble() / density);
        }
        return null;
    }

    private static boolean hasAnyFigure(IngredientCatalogueClient.Nutrition n) {
        return n.kcal() != null || n.proteinG() != null || n.carbsG() != null || n.fatG() != null
                || n.fiberG() != null || n.sugarG() != null || n.sodiumMg() != null;
    }

    private static Double scaled(Double value, double factor) {
        return value == null ? null : round1(value * factor);
    }

    private static void addTo(double[] total, boolean[] any, NutritionSum c) {
        Double[] v = {c.kcal(), c.proteinG(), c.carbsG(), c.fatG(), c.fiberG(), c.sugarG(), c.sodiumMg()};
        for (int i = 0; i < 7; i++) {
            if (v[i] != null) {
                total[i] += v[i];
                any[i] = true;
            }
        }
    }

    private static NutritionSum toSum(double[] total, boolean[] any, int divisor) {
        return toSumDivided(total, any, divisor);
    }

    /** Same as {@link #toSum} but the divisor can be fractional (e.g. countedGrams/100 for per-100 g). */
    private static NutritionSum toSumDivided(double[] total, boolean[] any, double divisor) {
        return new NutritionSum(
                any[0] ? roundKcal(total[0] / divisor) : null,
                any[1] ? round1(total[1] / divisor) : null,
                any[2] ? round1(total[2] / divisor) : null,
                any[3] ? round1(total[3] / divisor) : null,
                any[4] ? round1(total[4] / divisor) : null,
                any[5] ? round1(total[5] / divisor) : null,
                any[6] ? round1(total[6] / divisor) : null);
    }

    private static double round1(double d) {
        return Math.round(d * 10.0) / 10.0;
    }

    private static double round2(double d) {
        return Math.round(d * 100.0) / 100.0;
    }

    private static double roundKcal(double d) {
        return Math.round(d);
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    // ── result shapes ────────────────────────────────────────────────────────

    /** All figures nullable — a figure is null iff no counted line contributed it. kcal is whole, the rest to 1 dp. */
    public record NutritionSum(
            Double kcal, Double proteinG, Double carbsG, Double fatG,
            Double fiberG, Double sugarG, Double sodiumMg) {
    }

    public record LineNutrition(
            Long ingredientId, String name, Double amount, String unit, Double grams, Double millilitres,
            boolean optional, boolean counted, String note, NutritionSum contribution) {
    }

    public record NutritionResult(
            Long recipeId, String recipeName, int servings,
            NutritionSum total, NutritionSum perServing,
            NutritionSum per100g, Double countedGrams,
            List<LineNutrition> lines, List<String> notCounted) {
    }

    public record CalorieResult(
            Long recipeId, String recipeName, int servings,
            Double totalKcal, Double perServingKcal,
            Double kcalPer100g, Double kcalPerGram, Double countedGrams,
            int countedLines, int totalLines) {
    }

    /**
     * {@code scaledAmount} is in the line's own {@code unit}; {@code scaledGrams}
     * / {@code scaledMillilitres} are the volume↔mass equivalents (present only
     * when a density is known), so the UI can show the amount either way.
     */
    public record ScaledLine(
            Long ingredientId, String name, Double originalAmount, String unit,
            Double scaledAmount, Double scaledGrams, Double scaledMillilitres,
            String quantityText, boolean scaled) {
    }

    public record PortionResult(Long recipeId, String recipeName, double scale, List<ScaledLine> lines) {
    }
}
