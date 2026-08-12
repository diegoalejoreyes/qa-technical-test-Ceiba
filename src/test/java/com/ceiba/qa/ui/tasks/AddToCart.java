package com.ceiba.qa.ui.tasks;

import com.ceiba.qa.core.models.Product;
import com.ceiba.qa.ui.pages.InventoryPage;
import com.ceiba.qa.ui.questions.CatalogProducts;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.waits.WaitUntil;

import java.util.List;

import static com.ceiba.qa.core.utils.Config.defaultWaitSeconds;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/**
 * Agrega al carrito un producto identificado dinamicamente por su nombre real,
 * obtenido previamente de la lectura del catalogo (nunca por indice fijo).
 */
public class AddToCart implements Task {

    private final Product product;

    public AddToCart(Product product) {
        this.product = product;
    }

    public static AddToCart theProduct(Product product) {
        return new AddToCart(product);
    }

    /** Utilidad para resolver el producto mas barato del catalogo visible. */
    public static Product cheapestProductSeenBy(Actor actor) {
        return catalogOf(actor).stream().min(Product.BY_PRICE)
                .orElseThrow(() -> new IllegalStateException("El catalogo esta vacio"));
    }

    /** Utilidad para resolver el producto mas caro del catalogo visible. */
    public static Product mostExpensiveProductSeenBy(Actor actor) {
        return catalogOf(actor).stream().max(Product.BY_PRICE)
                .orElseThrow(() -> new IllegalStateException("El catalogo esta vacio"));
    }
    /**
     * Lee el catalogo y verifica que tenga suficientes productos para poder
     * distinguir un minimo de un maximo y validar el 2do flujo. Se corrige error de cuando devuelve un solo
     * elemento, "el mas barato" y "el mas caro" los toma como el mismo producto
     */
//    private static List<Product> catalogOf(Actor actor) {
//        return actor.asksFor(CatalogProducts.displayed());
//    }
    private static List<Product> catalogOf(Actor actor) {
        List<Product> catalog = actor.asksFor(CatalogProducts.displayed());
        if (catalog.size() < 2) {
            throw new IllegalStateException(
                    "Se leyeron " + catalog.size() + " producto(s) del catalogo. "
                            + "Se requieren al menos 2 para diferenciar el de menor y el de mayor precio. "
                            + "Revisar el localizador de productos.");
        }
        return catalog;
    }

    @Override
    @Step("{0} agrega al carrito el producto #product")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                WaitUntil.the(InventoryPage.ADD_TO_CART_BUTTON_FOR.of(product.getName()), isVisible())
                        .forNoMoreThan(defaultWaitSeconds()).seconds(),
                Click.on(InventoryPage.ADD_TO_CART_BUTTON_FOR.of(product.getName()))
        );
    }
}
