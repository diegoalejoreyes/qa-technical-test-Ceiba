package com.ceiba.qa.ui.tasks;

import com.ceiba.qa.ui.pages.CartPage;
import com.ceiba.qa.ui.pages.InventoryPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static com.ceiba.qa.core.utils.Config.defaultWaitSeconds;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class OpenTheCart implements Task {

    public static OpenTheCart page() {
        return new OpenTheCart();
    }

    @Override
    @Step("{0} abre el carrito de compras")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Click.on(InventoryPage.CART_LINK),
                WaitUntil.the(CartPage.CHECKOUT_BUTTON, isVisible()).forNoMoreThan(defaultWaitSeconds()).seconds()
        );
    }
}
