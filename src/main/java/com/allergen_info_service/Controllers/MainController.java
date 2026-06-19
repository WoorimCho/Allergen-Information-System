package com.allergen_info_service.Controllers;

import com.allergen_info_service.Models.Food;
import com.allergen_info_service.Models.Ingredient;
import com.allergen_info_service.Services.BasicServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.ui.Model;


@Controller
//@RequestMapping(path = "/")
public class MainController {
    @Autowired
    BasicServiceImpl service;

    @GetMapping({"/", "/home"})
    public String home(){
        return "home";
    }

    @GetMapping("/newFood")
    public String newFoodForm(Model model) {
        return service.newFoodForm(model);
    }

    @PostMapping("/newFood")
    public String newFood(@ModelAttribute Food food, Model model){
        return service.newFood(food, model);
    }

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
}