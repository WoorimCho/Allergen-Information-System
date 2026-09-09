package com.allergen_info_service.Services;

// === Phase 3 cutover (2026-09-08): RETIRED - kept commented, not deleted ===
// Server-rendered CRUD over local Food/Ingredient/Recipe tables. The catalogues' REST APIs replace it; the monolith is now BFF + auth only.
// To restore: strip the leading '// ' from each line below, remove this banner.
//
// 
// import com.allergen_info_service.Models.Food;
// import com.allergen_info_service.Models.Ingredient;
// import com.allergen_info_service.Models.Recipe;
// import org.springframework.ui.Model;
// 
// public interface BasicService {
//     public abstract String home();
//     public abstract String newFoodForm(Model model);
//     public abstract String newFood(Food food, Model model);
//     public abstract String modifyFood (long id, Food food);
//     public abstract String modifyFood (long id, Model model);
//     public abstract String getFood(long id, Model model);
//     public abstract Food getFood(long id);
//     public abstract String allFood(Model model);
//     public abstract Iterable<Food> getAllFood();
//     public abstract String deleteFoodForm(long id, Model model);
//     public abstract String deleteFood(long id);   // Maybe change to boolean?
// 
// 
//     public abstract String newIngredientForm(Model model);
//     public abstract String newIngredient(Ingredient ingredient, Model model);
//     public abstract String modifyIngredient(long id, Ingredient ingredient);
//     public abstract String modifyIngredient(long id, Model model);
//     public abstract String getIngredient(long id, Model model);
//     public abstract Ingredient getIngredient(long id);
//     public abstract String allIngredients(Model model);
//     public abstract Iterable<Ingredient> getAllIngredients();
//     public abstract String deleteIngredientForm(long id, Model model);
//     public abstract String deleteIngredient(long id);
// 
// 
//     public abstract String newRecipeForm(Model model);
//     public abstract String newRecipe(long foodId, long ingredientId, Model model);
//     public abstract String modifyRecipe(long index, long foodId, long ingredientId);
//     public abstract String modifyRecipe(long index, Model model);
//     public abstract String getRecipe(long index, Model model);
//     public abstract Recipe getRecipe(long index);
//     public abstract String allRecipes(Model model);
//     public abstract Iterable<Recipe> getAllRecipes();
//     public abstract String deleteRecipeForm(long index, Model model);
//     public abstract String deleteRecipe(long index);
// }
