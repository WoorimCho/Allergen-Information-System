package com.allergen_info_service.bff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The calculators against stubbed RecipeCatalogue + IngredientCatalogue. No Spring
 * context — clients built from {@link MockRestServiceServer}-bound builders.
 */
class RecipeCalculatorServiceTest {

    private MockRestServiceServer recipeServer;
    private MockRestServiceServer ingredientServer;
    private RecipeCalculatorService calc;

    @BeforeEach
    void setUp() {
        RestClient.Builder recipeBuilder = RestClient.builder();
        RestClient.Builder ingredientBuilder = RestClient.builder();
        recipeServer = MockRestServiceServer.bindTo(recipeBuilder).build();
        ingredientServer = MockRestServiceServer.bindTo(ingredientBuilder).build();
        calc = new RecipeCalculatorService(
                new RecipeCatalogueClient(recipeBuilder.build()),
                new IngredientCatalogueClient(ingredientBuilder.build()));
    }

    private void stubRecipe(String json) {
        recipeServer.expect(requestTo("/api/recipes/1")).andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private void stubIngredients(String json) {
        ingredientServer.expect(requestTo(startsWith("/api/ingredients/by-ids?")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    // 4 lines: flour (mass, has nutrition), mystery juice (cup, no density -> not
    // weighable), salt (mass, no nutrition), #99 (unknown ingredient).
    private static final String RECIPE = """
            {"id":1,"name":"Test Loaf","creator":"anon","version":1,"steps":[],
             "ingredients":[
               {"ingredientId":10,"quantity":"200 g","amount":200,"unit":"g","optional":false,"replaceable":false,"replacements":[]},
               {"ingredientId":11,"quantity":"1 cup","amount":1,"unit":"cup","optional":false,"replaceable":false,"replacements":[]},
               {"ingredientId":12,"quantity":"5 g","amount":5,"unit":"g","optional":true,"replaceable":false,"replacements":[]},
               {"ingredientId":99,"quantity":"a bit","amount":10,"unit":"g","optional":false,"replaceable":false,"replacements":[]}],
             "tags":[]}""";

    private static final String INGREDIENTS = """
            [{"id":10,"name":"flour","tags":[],
              "nutrition":{"basisGrams":100,"kcal":364,"proteinG":10,"carbsG":76,"fatG":1,"sodiumMg":2}},
             {"id":11,"name":"mystery juice","tags":[],
              "nutrition":{"basisGrams":100,"kcal":61,"proteinG":3.2}},
             {"id":12,"name":"salt","tags":[]}]""";

    // a volume line whose ingredient carries an explicit density
    private static final String RECIPE_VOL = """
            {"id":1,"name":"Milk Bath","creator":"anon","version":1,"steps":[],
             "ingredients":[
               {"ingredientId":20,"quantity":"2 cup","amount":2,"unit":"cup","optional":false,"replaceable":false,"replacements":[]},
               {"ingredientId":10,"quantity":"200 g","amount":200,"unit":"g","optional":false,"replaceable":false,"replacements":[]}],
             "tags":[]}""";

    private static final String INGREDIENTS_VOL = """
            [{"id":20,"name":"whole milk","tags":[],"densityGPerMl":1.0,
              "nutrition":{"basisGrams":100,"kcal":50}},
             {"id":10,"name":"all-purpose flour","tags":[],
              "nutrition":{"basisGrams":100,"kcal":364}}]""";

    @Test
    void nutritionSumsWeighableLinesAndFlagsTheRest() {
        stubRecipe(RECIPE);
        stubIngredients(INGREDIENTS);

        RecipeCalculatorService.NutritionResult r = calc.nutrition(1, 2);

        // only flour (200 g, factor 2.0) is counted
        assertThat(r.total().kcal()).isEqualTo(728.0);          // 364 * 2
        assertThat(r.total().proteinG()).isEqualTo(20.0);
        assertThat(r.perServing().kcal()).isEqualTo(364.0);     // / 2 servings
        assertThat(r.servings()).isEqualTo(2);

        assertThat(r.lines()).hasSize(4);
        assertThat(r.lines().get(0).counted()).isTrue();
        assertThat(r.lines().get(0).grams()).isEqualTo(200.0);
        assertThat(r.lines().get(1).note()).contains("cup").contains("density");
        assertThat(r.lines().get(2).note()).contains("no nutrition data");
        assertThat(r.lines().get(3).note()).isEqualTo("unknown ingredient");
        assertThat(r.notCounted()).hasSize(3);

        recipeServer.verify();
        ingredientServer.verify();
    }

    @Test
    void volumeLineIsCountedWhenTheIngredientHasADensity() {
        stubRecipe(RECIPE_VOL);
        stubIngredients(INGREDIENTS_VOL);

        RecipeCalculatorService.NutritionResult r = calc.nutrition(1, 1);

        // 2 cups * 236.588 ml * 1.0 g/ml = 473.2 g of milk
        RecipeCalculatorService.LineNutrition milk = r.lines().get(0);
        assertThat(milk.counted()).isTrue();
        assertThat(milk.grams()).isEqualTo(473.2);
        assertThat(milk.millilitres()).isEqualTo(473.2);
        assertThat(milk.contribution().kcal()).isEqualTo(236.6);     // 473.2 * 50/100

        // the mass line gets a millilitre equivalent too (flour density ~0.53 from the common table)
        RecipeCalculatorService.LineNutrition flour = r.lines().get(1);
        assertThat(flour.grams()).isEqualTo(200.0);
        assertThat(flour.millilitres()).isNotNull();

        assertThat(r.total().kcal()).isEqualTo(965.0);               // round(236.588 + 728)
    }

    @Test
    void portionsCarriesScaledMassAndVolumeEquivalents() {
        stubRecipe(RECIPE_VOL);
        stubIngredients(INGREDIENTS_VOL);

        RecipeCalculatorService.PortionResult p = calc.portions(1, 2.0, null, null, null);

        RecipeCalculatorService.ScaledLine milk = p.lines().get(0);
        assertThat(milk.scaledAmount()).isEqualTo(4.0);              // 2 cup -> 4 cup
        assertThat(milk.scaledGrams()).isEqualTo(946.4);             // 473.2 g -> * 2
        assertThat(milk.scaledMillilitres()).isEqualTo(946.4);
    }

    @Test
    void caloriesIsTheKcalSlice() {
        stubRecipe(RECIPE);
        stubIngredients(INGREDIENTS);

        RecipeCalculatorService.CalorieResult c = calc.calories(1, 4);

        assertThat(c.totalKcal()).isEqualTo(728.0);
        assertThat(c.perServingKcal()).isEqualTo(182.0);        // 728 / 4
        assertThat(c.countedLines()).isEqualTo(1);
        assertThat(c.totalLines()).isEqualTo(4);
    }

    @Test
    void portionsScalesByExplicitFactor() {
        stubRecipe(RECIPE);
        stubIngredients(INGREDIENTS);

        RecipeCalculatorService.PortionResult p = calc.portions(1, 2.5, null, null, null);

        assertThat(p.scale()).isEqualTo(2.5);
        assertThat(p.lines().get(0).scaledAmount()).isEqualTo(500.0);   // 200 * 2.5
        assertThat(p.lines().get(0).scaled()).isTrue();
        assertThat(p.lines().get(1).scaledAmount()).isEqualTo(2.5);     // 1 cup * 2.5
    }

    @Test
    void portionsDerivesScaleFromAnAnchorLine() {
        stubRecipe(RECIPE);
        stubIngredients(INGREDIENTS);

        // "I want to use 500 g of flour" -> flour is 200 g in the recipe -> scale 2.5
        RecipeCalculatorService.PortionResult p = calc.portions(1, null, 10L, 500.0, "g");

        assertThat(p.scale()).isEqualTo(2.5);
        assertThat(p.lines().get(0).scaledAmount()).isEqualTo(500.0);
        assertThat(p.lines().get(1).scaledAmount()).isEqualTo(2.5);
    }

    @Test
    void portionsRejectsConflictingAndInvalidInput() {
        // portions() fetches the recipe before validating -> one stub per call, all set up first
        stubRecipe(RECIPE);
        stubRecipe(RECIPE);
        stubRecipe(RECIPE);

        assertThatThrownBy(() -> calc.portions(1, 2.0, 10L, 5.0, "g"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not both");
        assertThatThrownBy(() -> calc.portions(1, null, 11L, 5.0, "g"))   // milk line is a cup, anchor is g
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("different kind of unit");
        assertThatThrownBy(() -> calc.portions(1, -1.0, null, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("> 0");
    }

    @Test
    void unknownRecipeSurfacesAsNotFound() {
        recipeServer.expect(requestTo("/api/recipes/1")).andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> calc.nutrition(1, 1))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}
