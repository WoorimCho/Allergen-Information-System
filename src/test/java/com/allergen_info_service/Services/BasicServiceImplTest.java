package com.allergen_info_service.Services;

import com.allergen_info_service.Models.Food;
import com.allergen_info_service.Models.Ingredient;
import com.allergen_info_service.Models.Recipe;
import com.allergen_info_service.Repositorys.FoodRepository;
import com.allergen_info_service.Repositorys.IngredientRepository;
import com.allergen_info_service.Repositorys.RecipeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicServiceImplTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private Model model;

    @InjectMocks
    private BasicServiceImpl service;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getFood() {
        Food food = new Food();
        food.setName("Pizza");

        when(foodRepository.findById(1L)).thenReturn(Optional.of(food));

        Food result = service.getFood(1L);

        assertEquals("Pizza", result.getName());
    }

    @Test
    void testGetFood() {
        Food food = new Food();
        food.setName("Noodles");
        when(foodRepository.findById(2L)).thenReturn(Optional.of(food));

        String view = service.getFood(2L, model);

        assertEquals("Food/getFood", view);
        verify(model).addAttribute("food", food);
    }

    @Test
    void allFood() {
        List<Food> foods = List.of(new Food(), new Food());
        when(foodRepository.findAll()).thenReturn(foods);

        String view = service.allFood(model);

        assertEquals("Food/allFood", view);
        verify(model).addAttribute("foods", foods);
    }

    @Test
    void getAllFood() {
        List<Food> foods = List.of(new Food());
        when(foodRepository.findAll()).thenReturn(foods);

        Iterable<Food> result = service.getAllFood();

        assertSame(foods, result);
    }

    @Test
    void getIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Milk");
        when(ingredientRepository.findById(10L)).thenReturn(Optional.of(ingredient));

        Ingredient result = service.getIngredient(10L);

        assertEquals("Milk", result.getName());
    }

    @Test
    void testGetIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Egg");
        when(ingredientRepository.findById(11L)).thenReturn(Optional.of(ingredient));

        String view = service.getIngredient(11L, model);

        assertEquals("Ingredient/getIngredient", view);
        verify(model).addAttribute("ingredient", ingredient);
    }

    @Test
    void getAllIngredients() {
        List<Ingredient> ingredients = List.of(new Ingredient());
        when(ingredientRepository.findAll()).thenReturn(ingredients);

        Iterable<Ingredient> result = service.getAllIngredients();

        assertSame(ingredients, result);
    }

    @Test
    void getRecipe() {
        Recipe recipe = new Recipe();
        when(recipeRepository.findById(99L)).thenReturn(Optional.of(recipe));

        Recipe result = service.getRecipe(99L);

        assertSame(recipe, result);
    }

    @Test
    void testGetRecipe() {
        Recipe recipe = new Recipe();
        when(recipeRepository.findById(20L)).thenReturn(Optional.of(recipe));

        String view = service.getRecipe(20L, model);

        assertEquals("Recipe/getRecipe", view);
        verify(model).addAttribute("ingredient", recipe);
    }

    @Test
    void allRecipes() {
        List<Recipe> recipes = List.of(new Recipe());
        when(recipeRepository.findAll()).thenReturn(recipes);

        String view = service.allRecipes(model);

        assertEquals("Recipe/allRecipes", view);
        verify(model).addAttribute("recipes", recipes);
    }

    @Test
    void getAllRecipes() {
        List<Recipe> recipes = List.of(new Recipe());
        when(recipeRepository.findAll()).thenReturn(recipes);

        Iterable<Recipe> result = service.getAllRecipes();

        assertSame(recipes, result);
    }

    @Test
    void newFoodSavesAndRedirects() {
        Food input = new Food();
        input.setName("  Bread  ");

        String view = service.newFood(input, model);

        assertEquals("redirect:/allFood", view);
        verify(foodRepository).save(any(Food.class));
    }

    @Test
    void deleteFoodReturnsErrorViewWhenDeleteFails() {
        Food food = new Food();
        food.setName("Cheese");
        when(foodRepository.findById(5L)).thenReturn(Optional.of(food));
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("fk"))
                .when(foodRepository).delete(eq(food));

        String view = service.deleteFood(5L);

        assertEquals("Error/DeleteForeignKeyF", view);
    }
}