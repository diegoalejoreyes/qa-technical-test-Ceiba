package com.ceiba.qa.core.models;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

/** Representa un producto del catalogo o del carrito de SauceDemo. */
public class Product {

    public static final Comparator<Product> BY_PRICE = Comparator.comparing(Product::getPrice);

    private final String name;
    private final BigDecimal price;

    public Product(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(name, product.name) && price.compareTo(product.price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}
