package com.ceiba.qa.ui.tasks;

import com.ceiba.qa.core.models.Product;
import com.ceiba.qa.ui.pages.CartPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static com.ceiba.qa.core.utils.Config.defaultWaitSeconds;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/** Elimina del carrito el producto indicado (identificado dinamicamente por precio). */
public class RemoveFromCart implements Task {

    private final Product product;

    public RemoveFromCart(Product product) {
        this.product = product;
    }

    public static RemoveFromCart theProduct(Product product) {
        return new RemoveFromCart(product);
    }

    @Override
    @Step("{0} elimina del carrito el producto #product")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                WaitUntil.the(CartPage.REMOVE_BUTTON_FOR.of(product.getName()), isVisible())
                        .forNoMoreThan(defaultWaitSeconds()).seconds(),
                Click.on(CartPage.REMOVE_BUTTON_FOR.of(product.getName()))
        );
    }
}
