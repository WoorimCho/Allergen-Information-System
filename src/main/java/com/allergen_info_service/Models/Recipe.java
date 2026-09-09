package com.allergen_info_service.Models;

// === Phase 3 cutover (2026-09-08): RETIRED - kept commented, not deleted ===
// Recipes are now owned by RecipeCatalogue (rich model: steps, tools, substitutes).
// To restore: strip the leading '// ' from each line below, remove this banner.
//
// 
// import jakarta.persistence.*;
// 
// @Entity
// public class Recipe {
//     @Id
//     @GeneratedValue
//     private Long id;
// 
//     //Change to many to many
//     @ManyToOne
//     @JoinColumn(name = "Food")
//     private Food food;
// 
//     //Change to many to many
//     @ManyToOne
//     @JoinColumn(name = "Ingredient")
//     private Ingredient ingredient;
// 
//     public long getId(){
//         return id;
//     }
// 
//     public Food getFood() {
//         return food;
//     }
// 
//     public Ingredient getIngredient() {
//         return ingredient;
//     }
// 
//     public void setFood(Food food) {
//         this.food = food;
//     }
// 
//     public void setIngredient(Ingredient ingredient) {
//         this.ingredient = ingredient;
//     }
// 
// }
