package com.allergen_info_service.bff;

import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * The BFF's copy of RecipeCatalogue's {@code Unit} conversions — cross-service
 * code can't be shared, and the calculators need to turn a line's
 * {@code amount + unit} into grams (for nutrition) or a common base (for
 * scaling). Kept deliberately small; RecipeCatalogue's enum is the source of
 * truth for what units exist.
 */
final class Units {

    private Units() {
    }

    enum Dimension { MASS, VOLUME, COUNT, UNKNOWN }

    /** unit token -> [dimension, factor into the dimension's base (grams / millilitres / items)]. */
    private static final Map<String, Object[]> UNITS = Map.ofEntries(
            Map.entry("mg", new Object[]{Dimension.MASS, 0.001}),
            Map.entry("g", new Object[]{Dimension.MASS, 1.0}),
            Map.entry("kg", new Object[]{Dimension.MASS, 1000.0}),
            Map.entry("oz", new Object[]{Dimension.MASS, 28.349523125}),
            Map.entry("lb", new Object[]{Dimension.MASS, 453.59237}),
            Map.entry("ml", new Object[]{Dimension.VOLUME, 1.0}),
            Map.entry("l", new Object[]{Dimension.VOLUME, 1000.0}),
            Map.entry("tsp", new Object[]{Dimension.VOLUME, 4.92892159375}),
            Map.entry("tbsp", new Object[]{Dimension.VOLUME, 14.78676478125}),
            Map.entry("cup", new Object[]{Dimension.VOLUME, 236.5882365}),
            Map.entry("fl_oz", new Object[]{Dimension.VOLUME, 29.5735295625}),
            Map.entry("piece", new Object[]{Dimension.COUNT, 1.0}),
            Map.entry("clove", new Object[]{Dimension.COUNT, 1.0}),
            Map.entry("slice", new Object[]{Dimension.COUNT, 1.0}),
            Map.entry("pinch", new Object[]{Dimension.COUNT, 1.0}));

    static Dimension dimension(String unit) {
        if (unit == null) {
            return Dimension.UNKNOWN;
        }
        Object[] u = UNITS.get(unit.trim().toLowerCase(Locale.ROOT));
        return u == null ? Dimension.UNKNOWN : (Dimension) u[0];
    }

    /** {@code amount} in the unit's dimension base (grams for MASS, ml for VOLUME, items for COUNT). Empty if the unit is unknown. */
    static OptionalDouble toBase(Double amount, String unit) {
        if (amount == null || unit == null) {
            return OptionalDouble.empty();
        }
        Object[] u = UNITS.get(unit.trim().toLowerCase(Locale.ROOT));
        return u == null ? OptionalDouble.empty() : OptionalDouble.of(amount * (double) u[1]);
    }

    /** {@code amount} in grams — only for MASS units. Empty otherwise (volume/count aren't weighable without more data). */
    static OptionalDouble toGrams(Double amount, String unit) {
        return dimension(unit) == Dimension.MASS ? toBase(amount, unit) : OptionalDouble.empty();
    }
}
