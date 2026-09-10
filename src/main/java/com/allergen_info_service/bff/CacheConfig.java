package com.allergen_info_service.bff;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Turns on Spring's caching annotations. The only cache is
 * {@value #INGREDIENTS_BY_IDS}, backing {@link IngredientCatalogueClient#byIds}:
 * a composed recipe view plus the nutrition / calorie / portion calculators all
 * resolve the same set of line-ingredient ids from IngredientCatalogue, and the
 * portion slider re-requests on every drag. A short write-expiry (see
 * {@code spring.cache.caffeine.spec} in {@code application.properties}) collapses
 * those into one downstream call without serving stale ingredient data.
 */
@Configuration
@EnableCaching
class CacheConfig {

    static final String INGREDIENTS_BY_IDS = "ingredientsByIds";
}
