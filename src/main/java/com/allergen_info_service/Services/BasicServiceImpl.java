package com.allergen_info_service.Services;

// === Phase 3 cutover (2026-09-08): RETIRED - kept commented, not deleted ===
// Server-rendered CRUD over local Food/Ingredient/Recipe tables. The catalogues' REST APIs replace it; the monolith is now BFF + auth only.
// To restore: strip the leading '// ' from each line below, remove this banner.
//
// 
// import com.allergen_info_service.Models.Food;
// import com.allergen_info_service.Models.Ingredient;
// import com.allergen_info_service.Models.Recipe;
// import com.allergen_info_service.Repositorys.FoodRepository;
// import com.allergen_info_service.Repositorys.IngredientRepository;
// import com.allergen_info_service.Repositorys.RecipeRepository;
// 
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.client.RestClient;
// import org.springframework.web.client.RestTemplate;
// 
// // Services are called here, not in the controller
// 
// // defines that this is a service, so business logic is defined here and helps with readability
// @Service
// public class BasicServiceImpl implements BasicService {
// 
//     @Autowired
//     private FoodRepository foodRepository;
//     @Autowired
//     private IngredientRepository ingredientRepository;
//     @Autowired
//     private RecipeRepository recipeRepository;
// 
//     @Autowired
//     private GoodNightRestClientImpl goodNight;
// 
//     public String home(){
//         return "redirect:/";
//     }
// 
// 
// //    @PostMapping
// //    public ResponseEntity postController(@RequestBody Snack snack){
// //
// //        return ResponseEntity.ok(HttpStatus.OK);
// //    }
// 
//     @Override
//     public String newFoodForm(Model model){
//         goodNight.getNight();
//         model.addAttribute("food", new Food());
//         goodNight.getNight();
//         return "Food/newFood";
//     }
// 
//     @Override
//     public String newFood(Food food, Model model){
//         Food fod = new Food();
//         fod.setName(food.getName());
//         foodRepository.save(fod);
//         return "redirect:/allFood";
//     }
//     @Override
//     public String modifyFood (long id, Food food){
//         foodRepository.findById(id).map(
//                 existingFood -> {
//                     existingFood.setName(food.getName());
//                     foodRepository.save(existingFood);
//                     return null;    //new ResponseEntity<>(savedEntity, HttpStatus.OK); **Need to learn about this
//                 }
// 
//         );
//         return "redirect:/allFood";
//     }
// 
//     @Override
//     public String modifyFood(long id, Model model){
//         model.addAttribute("food", getFood(id));
//         return "Food/modifyFood";
//     }
// 
//     @Override
//     public String getFood(long id, Model model){
//         model.addAttribute("food", getFood(id));
//         return "Food/getFood";
//     }
//     @Override
//     public Food getFood(long id){
//         return foodRepository.findById(id).get();
//     }
// 
//     @Override
//     public String allFood(Model model){
//         model.addAttribute("foods", getAllFood());
//         return "Food/allFood";
//     }
//     @Override
//     public Iterable<Food> getAllFood(){
//         return foodRepository.findAll();
//     }
// 
//     @Override
//     public String deleteFoodForm(long id, Model model) {
//         model.addAttribute("food", getFood(id));
//         return "Food/deleteFood";
//     }
//     @Override
//     public String deleteFood(long id){
//         try{
//             foodRepository.delete(getFood(id));
//         }
//         catch(Exception e){
//             return "Error/DeleteForeignKeyF";
//         }
//         return "redirect:/allFood";
//     }
// /////////////////////////////////////////////////////////////////////////////////////////////////////
//     @Override
//     public String newIngredientForm(Model model) {
//         model.addAttribute("ingredient", new Ingredient());
//         return "Ingredient/newIngredient";
//     }
// 
//     @Override
//     public String newIngredient(Ingredient ingredient, Model model){
//         Ingredient newIngredient = new Ingredient();
//         newIngredient.setName(ingredient.getName());
//         ingredientRepository.save(newIngredient);
//         return "redirect:/allIngredients";
//     }
//     @Override
//     public String modifyIngredient(long id, Ingredient ingredient){
//         ingredientRepository.findById(id).map(
//                 existingIngredient-> {
//                     existingIngredient.setName(ingredient.getName());
//                     ingredientRepository.save(existingIngredient);
//                     return null;
//                 }
//         );
//         return "redirect:/allIngredients";
//     }
// 
//     @Override
//     public String modifyIngredient(long id, Model model){
//         model.addAttribute("ingredient", getIngredient(id));
//         return "Ingredient/modifyIngredient";
//     }
// 
//     @Override
//     public String getIngredient(long id, Model model) {
//         model.addAttribute("ingredient", getIngredient(id));
//         return "Ingredient/getIngredient";
//     }
// 
// 
//     @Override
//     public Ingredient getIngredient(long id){
//         return ingredientRepository.findById(id).get();
//     }
// 
// 
//     @Override
//     public String deleteIngredient(long id){
//         try{
//             ingredientRepository.delete(getIngredient(id));
//         }
//         catch(Exception e){
//             return "Error/DeleteForeignKeyI";
//         }
//         return "redirect:/allIngredients";
//     }
// 
// 
//     @Override
//     public String allIngredients(Model model) {
//         model.addAttribute("ingredients", getAllIngredients());
//         return "Ingredient/allIngredients";
//     }
// 
//     @Override
//     public Iterable<Ingredient> getAllIngredients() {
//         return ingredientRepository.findAll();
//     }
// 
//     @Override
//     public String deleteIngredientForm(long id, Model model) {
//         model.addAttribute("ingredient", getIngredient(id));
//         return "Ingredient/deleteIngredient";
//     }
// 
//     ///////////////////////////////////////////////////////////////////////////////////////////////////
//     @Override
//     public String newRecipeForm(Model model) {
//         model.addAttribute("recipe", new Recipe());
//         model.addAttribute("foods", getAllFood());
//         model.addAttribute("ingredients", getAllIngredients());
//         return "Recipe/newRecipe";
//     }
//     @Override
//     public String newRecipe(long foodId, long ingredientId, Model model) {
//         Recipe newRecipe = new Recipe();
//         newRecipe.setFood(foodRepository.findById(foodId).get());
//         newRecipe.setIngredient(ingredientRepository.findById(ingredientId).get());
//         recipeRepository.save(newRecipe);
//         return "redirect:/allRecipes";
//     }
// 
//     @Override
//     public String modifyRecipe(long index, long foodId, long ingredientId) {
//         recipeRepository.findById(index).map(
//                 existingRecipe-> {
//                     existingRecipe.setIngredient(ingredientRepository.findById(ingredientId).get());
//                     existingRecipe.setFood(foodRepository.findById(foodId).get());
//                     recipeRepository.save(existingRecipe);
//                     return null;
//                 }
//         );
//         return "redirect:/allRecipes";
//     }
// 
//     @Override
//     public String modifyRecipe(long index, Model model) {
//         model.addAttribute("recipe", getRecipe(index));
//         model.addAttribute("foods", getAllFood());
//         model.addAttribute("ingredients", getAllIngredients());
//         return "Recipe/modifyRecipe";
//     }
// 
//     @Override
//     public String getRecipe(long index, Model model) {
//         model.addAttribute("ingredient", getRecipe(index));
//         return "Recipe/getRecipe";
//     }
// 
//     @Override
//     public Recipe getRecipe(long index) {
//         return recipeRepository.findById(index).get();
//     }
// 
//     @Override
//     public String allRecipes(Model model) {
//         model.addAttribute("recipes", getAllRecipes());
//         return "Recipe/allRecipes";
//     }
//     @Override
//     public Iterable<Recipe> getAllRecipes() {
//         return recipeRepository.findAll();
//     }
//     @Override
//     public String deleteRecipe(long index) {
//         recipeRepository.delete(getRecipe(index));
//         return "redirect:/allRecipes";
//     }
// 
//     @Override
//     public String deleteRecipeForm(long index, Model model) {
//         model.addAttribute("recipe", getRecipe(index));
//         return "Recipe/deleteRecipe";
//     }
// }
