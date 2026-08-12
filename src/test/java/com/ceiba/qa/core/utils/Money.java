package com.ceiba.qa.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidad para trabajar con valores monetarios.
 *
 * Se usa BigDecimal (nunca double) para evitar errores de precision en la
 * validacion de la formula Subtotal + Impuesto = Total.
 */
public final class Money {

    public static final int SCALE = 2;

    private Money() {
    }

    /** Convierte textos como "$29.99", "Item total: $129.94" o "Tax: $10.40" a BigDecimal. */
    public static BigDecimal parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("No se puede convertir a moneda un texto vacio");
        }
        String cleaned = rawText.replaceAll("[^0-9.,-]", "").replace(",", "");
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("No se encontro un valor numerico en: " + rawText);
        }
        return new BigDecimal(cleaned).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Comparacion por valor (evita el problema de equals() de BigDecimal con la escala). */
    public static boolean equal(BigDecimal a, BigDecimal b) {
        return round(a).compareTo(round(b)) == 0;
    }
}
