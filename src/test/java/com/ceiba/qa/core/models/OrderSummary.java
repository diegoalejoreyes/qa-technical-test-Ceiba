package com.ceiba.qa.core.models;

import com.ceiba.qa.core.utils.Money;

import java.math.BigDecimal;

/** Resumen economico de la pagina Checkout: Overview de SauceDemo. */
public class OrderSummary {

    /** Tasa de impuesto declarada por el negocio para SauceDemo. */
    public static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal total;

    public OrderSummary(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
        this.subtotal = Money.round(subtotal);
        this.tax = Money.round(tax);
        this.total = Money.round(total);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /** Formula exigida por la prueba: Subtotal + Impuesto = Total. */
    public BigDecimal calculatedTotal() {
        return Money.round(subtotal.add(tax));
    }

    public boolean totalIsConsistent() {
        return Money.equal(calculatedTotal(), total);
    }

    /** Impuesto teorico segun la regla de negocio (8% del subtotal). */
    public BigDecimal expectedTax() {
        return Money.round(subtotal.multiply(TAX_RATE));
    }

    public boolean taxIsConsistent() {
        return Money.equal(expectedTax(), tax);
    }

    @Override
    public String toString() {
        return String.format("Subtotal=%s | Tax=%s | Total=%s | Subtotal+Tax=%s",
                subtotal, tax, total, calculatedTotal());
    }
}
