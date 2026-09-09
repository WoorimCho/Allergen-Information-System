package com.allergen_info_service.Models;

// === Phase 3 cutover (2026-09-08): RETIRED - kept commented, not deleted ===
// Food/Ingredient are now owned by IngredientCatalogue (unified Ingredient + tags).
// To restore: strip the leading '// ' from each line below, remove this banner.
//
// 
// import jakarta.persistence.*;
// 
// import java.util.Set;
// 
// // Marks class as a JPA entity
// @Entity
// public class Food {
//     //Defines primary key
//     @Id
//     //Auto generates primary key values
//     @GeneratedValue
//     private Long id;
// 
//     // Maps class field to table column
//     @Column(name = "name", nullable = false, unique = true)
//     private String name;
// 
//     // Defines relationship with specified table/entity
//     @OneToMany(mappedBy = "food")
//     private Set<Recipe> recipes;
// 
// 
//     public long getId(){
//         return id;
//     }
// 
//     public void setName(String name) {
//         this.name = name.trim();
//     }
//     public String getName() {
//         return name;
//     }
// 
//     public void setIngredientList(Set<Recipe> ingredientList) {
//         this.recipes = ingredientList;
//     }
// 
//     public void addIngredient(Recipe recipe){
//         recipes.add(recipe);
//     }
//     public void removeIngredient(Recipe recipe){    //May need to change this
//         recipes.remove(recipe);
//     }
// 
//     public boolean usesIngredient(Recipe recipe){
//         return recipes.contains(recipe);
//     }
// 
//     public Set<Recipe> getIngredientList() {
//         return recipes;
//     }
// }
