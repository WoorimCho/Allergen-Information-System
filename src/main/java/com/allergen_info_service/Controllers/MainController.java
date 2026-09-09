package com.allergen_info_service.Controllers;

// Phase 3 cutover: Food/Ingredient/Recipe CRUD retired (owned by the catalogue services now).
// import com.allergen_info_service.Models.Food;
// import com.allergen_info_service.Models.Ingredient;
// import com.allergen_info_service.Services.BasicServiceImpl;
import com.allergen_info_service.Services.GoodNightRestClientImpl;
import tools.jackson.databind.JsonNode;  // Jackson 3 (Boot 4 default)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.ui.Model;

/**
 * Connects the html requests with  the proper method calls which then call the methods from the necessary service(s)
 *      The html requests are redirected using the @RequestMapping annotation
 *      Specific GPPD requests are done with their specific mappings:
 *      @GetMapping
 *      @PostMapping
 *      @PutMapping
 *      @DeleteMapping
 * hiding the business logic and following the concept of abstraction
 *
 * Controller has any string outputs be interpretted as paths to html view that need to be rendered
 * Using RESTController would instead have the ouput be read as is
 *
 * to optionally choose what to get read as is or not, use ResponseBody annotation for the methods that would return
 * the raw data
 *
 * RequestBody annotation is used to...
 *
 * FIND OUT WHAT EACH ANNOTTATION DOES!!!!
 */
@Controller
//@RequestMapping(path = "/")
public class MainController {
    // Autowired injects the necessary dependencies allowing for the creation of objects without using "new"
    // Phase 3 cutover: BasicServiceImpl retired.
    // @Autowired
    // BasicServiceImpl service;
    @Autowired
    GoodNightRestClientImpl night;

    // Change such that it only calls service methods and doesn't have any business methods
    @ResponseBody
    @PostMapping("/response")
    public ResponseEntity<JsonNode> postController(/**Normally an @RequestBody is used here to
                                              send the desired information to the other service**/
                                         @RequestBody JsonNode rawJson){

        if (rawJson.has("name")) {
            String snackName = rawJson.get("name").asText();
            System.out.println("Snack's name: " + snackName);
        }
//        night.getSnack();
        return ResponseEntity.status(200).body(rawJson);
    }
//    @GetMapping("/request")
//    public ResponseEntity<JsonNode> postController(/**Normally an @RequestBody is used here to
//                                              send the desired information to the other service**/
//                                         @RequestBody JsonNode rawJson){
////        if (rawJson.has("name")) {
////            String snackName = rawJson.get("name").asText();
////            System.out.println("Snack's name: " + snackName);
////        }
//        return night.getSnack(rawJson);
//
////        return ResponseEntity.ok(night.getSnack(rawJson));
//    }

// Change such that it only calls service methods and doesn't have any business methods
    @GetMapping("/request")
    public ResponseEntity getter(/**Normally an @RequestBody is used here to
                                              send the desired information to the other service**/
                // Binds the HTTP request body to a java object, Commonly used with POST and PUT requests
                                         @RequestBody JsonNode rawJson){
//        if (rawJson.has("name")) {
//            String snackName = rawJson.get("name").asText();
//            System.out.println("Snack's name: " + snackName);
//        }
//        return night.getSnack(rawJson);
        String response = night.getSnack(rawJson);

        return ResponseEntity.ok(response);
    }

    @GetMapping({"/", "/home"})
    public String home(){
        return "home";
    }

    /* ===== Phase 3 cutover (2026-09-08): Food / Ingredient / Recipe CRUD retired =====
     * These routes rendered the server-side Thymeleaf CRUD over local tables. That
     * data is now owned by IngredientCatalogue and RecipeCatalogue; the monolith keeps
     * only the landing page, the GoodNight tester, and the /bff/** composition API.
     * Kept commented (not deleted) per request.

    @GetMapping("/newFood")
    public String newFoodForm(Model model) {
        return service.newFoodForm(model);
    }

    @PostMapping("/newFood")
    public String newFood(@ModelAttribute Food food, Model model){
        return service.newFood(food, model);
    }

    //Request Params reads the query parameters directly from the request URL. Used for optional or filtering inputs.
    @GetMapping(path="/getFood")
    public String getFood(@RequestParam long foodId, Model model){
        return service.getFood(foodId,model);
    }

    @GetMapping("/allFood")
    public String allFood(Model model){
        return service.allFood(model);
    }

    @GetMapping("/deleteFood")
    public String deleteFoodForm(@RequestParam long foodId, Model model) {
        return service.deleteFoodForm(foodId,model);
    }

    @DeleteMapping("/deleteFood")
    public String deleteFood(@RequestParam long foodId) {
        return service.deleteFood(foodId);
    }

    @GetMapping("/modifyFood")
    public String modifyFood(@RequestParam long foodId, Model model) {
        return service.modifyFood(foodId,model);
    }

    @PutMapping("/modifyFood")
    public String modifyFood(@RequestParam long foodId, @ModelAttribute Food food){
        return service.modifyFood(foodId, food);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/newIngredient")
    public String newIngredientForm(Model model) {
        return service.newIngredientForm(model);
    }

    @PostMapping("/newIngredient")
    public String newIngredient(@ModelAttribute Ingredient ingredient, Model model){
        return service.newIngredient(ingredient, model);
    }

    @GetMapping(path="/getIngredient")
    public String getIngredient(@RequestParam long ingredientId, Model model){
        return service.getIngredient(ingredientId,model);
    }

    @GetMapping("/allIngredients")
    public String allIngredients(Model model){
        return service.allIngredients(model);
    }

    @GetMapping("/deleteIngredient")
    public String deleteIngredientForm(@RequestParam long ingredientId, Model model) {
        return service.deleteIngredientForm(ingredientId, model);
    }

    @DeleteMapping("/deleteIngredient")
    public String deleteIngredient(@RequestParam long ingredientId) {
        return service.deleteIngredient(ingredientId);
    }

    @GetMapping("/modifyIngredient")
    public String modifyIngredient(@RequestParam long ingredientId, Model model) {
        return service.modifyIngredient(ingredientId,model);
    }

    @PutMapping("/modifyIngredient")
    public String modifyIngredient(@RequestParam long ingredientId, @ModelAttribute Ingredient ingredient){
        return service.modifyIngredient(ingredientId, ingredient);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @GetMapping("/newRecipe")
    public String newRecipeForm(Model model) {
        return service.newRecipeForm(model);
    }

    @PostMapping("/newRecipe")
    public String newRecipe(@RequestParam long foodId, @RequestParam long ingredientId, Model model){
        return service.newRecipe(foodId, ingredientId, model);
    }

    @GetMapping(path="getRecipe")
    public String getRecipe(@RequestParam long index, Model model){
        return service.getRecipe(index,model);
    }

    @GetMapping("/allRecipes")
    public String allRecipes(Model model){
        return service.allRecipes(model);
    }

    @GetMapping("/deleteRecipe")
    public String deleteRecipeForm(@RequestParam long index, Model model) {
        return service.deleteRecipeForm(index, model);
    }

    @DeleteMapping("/deleteRecipe")
    public String deleteRecipe(@RequestParam long index) {
        return service.deleteRecipe(index);
    }

    @GetMapping("/modifyRecipe")
    public String modifyRecipe(@RequestParam long index, Model model) {
        return service.modifyRecipe(index,model);
    }

    @PutMapping("/modifyRecipe")
    public String modifyRecipe(@RequestParam long index, @RequestParam long foodId, @RequestParam long ingredientId){
        return service.modifyRecipe(index, foodId, ingredientId);
    }
     * ===== end Phase 3 cutover block ===== */
}