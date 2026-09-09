package com.allergen_info_service.bff;

import java.util.Locale;
import java.util.Map;

/**
 * A small fallback table of packed densities (g/ml) for everyday ingredients,
 * matched loosely by name. Used only when an ingredient carries no
 * {@code densityGPerMl} of its own — an explicit value on the ingredient always
 * wins. Approximate by nature (a "cup of flour" varies ±15% with how it's
 * scooped); good enough to offer a volume↔mass equivalent rather than "unknown".
 */
final class CommonDensities {

    private CommonDensities() {
    }

    /** substring (lower-case) -> g/ml. First match wins, so order longer/more-specific keys first. */
    private static final Map<String, Double> TABLE = Map.ofEntries(
            Map.entry("water", 1.00),
            Map.entry("milk", 1.03),
            Map.entry("buttermilk", 1.03),
            Map.entry("cream", 1.01),
            Map.entry("yogurt", 1.03),
            Map.entry("honey", 1.42),
            Map.entry("maple syrup", 1.33),
            Map.entry("molasses", 1.40),
            Map.entry("corn syrup", 1.38),
            Map.entry("vegetable oil", 0.92),
            Map.entry("olive oil", 0.91),
            Map.entry("oil", 0.92),
            Map.entry("melted butter", 0.91),
            Map.entry("butter", 0.96),
            Map.entry("brown sugar", 0.93),
            Map.entry("powdered sugar", 0.56),
            Map.entry("icing sugar", 0.56),
            Map.entry("granulated sugar", 0.85),
            Map.entry("sugar", 0.85),
            Map.entry("table salt", 1.22),
            Map.entry("salt", 1.22),
            Map.entry("bread flour", 0.55),
            Map.entry("whole wheat flour", 0.54),
            Map.entry("almond flour", 0.42),
            Map.entry("all-purpose flour", 0.53),
            Map.entry("all purpose flour", 0.53),
            Map.entry("flour", 0.53),
            Map.entry("cocoa powder", 0.51),
            Map.entry("baking soda", 0.92),
            Map.entry("baking powder", 0.90),
            Map.entry("cornstarch", 0.54),
            Map.entry("corn starch", 0.54),
            Map.entry("rolled oats", 0.36),
            Map.entry("oats", 0.41),
            Map.entry("rice", 0.85),
            Map.entry("ketchup", 1.14),
            Map.entry("mayonnaise", 0.91),
            Map.entry("peanut butter", 1.09));

    /** g/ml for the best loose name match, or {@code null} if nothing matches. */
    static Double lookup(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return null;
        }
        String n = ingredientName.toLowerCase(Locale.ROOT);
        Double best = null;
        int bestKeyLen = -1;
        for (Map.Entry<String, Double> e : TABLE.entrySet()) {
            if (n.contains(e.getKey()) && e.getKey().length() > bestKeyLen) {
                best = e.getValue();
                bestKeyLen = e.getKey().length();
            }
        }
        return best;
    }
}
