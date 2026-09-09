package com.allergen_info_service.bff;

import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * The BFF's copy of RecipeCatalogue's {@code Unit} conversions — cross-service
 * code can't be shared, and the calculators need to turn a line's
 * {@code amount + unit} into grams (for nutrition) or a common base (for
 * scaling), and to show a volume↔mass equivalent. Kept deliberately small;
 * RecipeCatalogue's enum is the source of truth for what units exist.
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

    /** {@code amount} in grams — only for MASS units. Empty otherwise (volume/count aren't weighable without density). */
    static OptionalDouble toGrams(Double amount, String unit) {
        return toGrams(amount, unit, null);
    }

    /**
     * {@code amount} in grams. MASS units convert directly; VOLUME units need
     * {@code densityGPerMl} (grams per millilitre) and are otherwise empty;
     * COUNT / unknown units are always empty.
     */
    static OptionalDouble toGrams(Double amount, String unit, Double densityGPerMl) {
        Dimension d = dimension(unit);
        if (d == Dimension.MASS) {
            return toBase(amount, unit);
        }
        if (d == Dimension.VOLUME && densityGPerMl != null && densityGPerMl > 0) {
            OptionalDouble ml = toBase(amount, unit);
            return ml.isPresent() ? OptionalDouble.of(ml.getAsDouble() * densityGPerMl) : OptionalDouble.empty();
        }
        return OptionalDouble.empty();
    }

    /** Millilitres for {@code grams} of a substance with the given density. Empty without a usable density. */
    static OptionalDouble gramsToMillilitres(double grams, Double densityGPerMl) {
        return densityGPerMl != null && densityGPerMl > 0
                ? OptionalDouble.of(grams / densityGPerMl)
                : OptionalDouble.empty();
    }

    /**
     * A human volume string for {@code ml} — picks cups / tbsp / tsp / ml by
     * magnitude and rounds to a sensible precision. e.g. {@code 355.0 -> "1.5 cup"}.
     */
    static String prettyVolume(double ml) {
        if (ml >= 0.75 * 236.5882365) {
            return trim(ml / 236.5882365) + " cup";
        }
        if (ml >= 0.75 * 14.78676478125) {
            return trim(ml / 14.78676478125) + " tbsp";
        }
        if (ml >= 0.75 * 4.92892159375) {
            return trim(ml / 4.92892159375) + " tsp";
        }
        return trim(ml) + " ml";
    }

    /** A human mass string for {@code grams} — g up to 1 kg, then kg. */
    static String prettyMass(double grams) {
        return grams >= 1000 ? trim(grams / 1000.0) + " kg" : trim(grams) + " g";
    }

    private static String trim(double v) {
        double r = Math.round(v * 100.0) / 100.0;
        return r == Math.rint(r) ? Long.toString((long) r) : Double.toString(r);
    }
}
