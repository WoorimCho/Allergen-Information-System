package com.allergen_info_service.bff;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 4 recipe calculators. Under {@code /bff/**}, so a session is required
 * (see {@link BffSecurityConfig}); none of these personalise, they just do the
 * cross-service arithmetic.
 */
@RestController
@RequestMapping("/bff")
public class RecipeCalculatorController {

    private final RecipeCalculatorService calculator;

    public RecipeCalculatorController(RecipeCalculatorService calculator) {
        this.calculator = calculator;
    }

    /** Full nutrition breakdown: per-line contributions, total, and per-serving. */
    @GetMapping("/recipes/{id}/nutrition")
    public RecipeCalculatorService.NutritionResult nutrition(
            @PathVariable long id,
            @RequestParam(defaultValue = "1") int servings) {
        return calculator.nutrition(id, servings);
    }

    /** Just calories: total and per-serving, plus how many lines could be counted. */
    @GetMapping("/recipes/{id}/calories")
    public RecipeCalculatorService.CalorieResult calories(
            @PathVariable long id,
            @RequestParam(defaultValue = "1") int servings) {
        return calculator.calories(id, servings);
    }

    /**
     * Scale every line's numeric amount. Either {@code scale} (a multiplier) or
     * the anchor trio ({@code anchorIngredientId} + {@code anchorAmount} +
     * {@code anchorUnit}, "make enough to use this much of that line"), not both.
     */
    @GetMapping("/recipes/{id}/portions")
    public RecipeCalculatorService.PortionResult portions(
            @PathVariable long id,
            @RequestParam(required = false) Double scale,
            @RequestParam(required = false) Long anchorIngredientId,
            @RequestParam(required = false) Double anchorAmount,
            @RequestParam(required = false) String anchorUnit) {
        return calculator.portions(id, scale, anchorIngredientId, anchorAmount, anchorUnit);
    }
}
