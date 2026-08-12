package com.ceiba.qa.ui.tasks;

import com.ceiba.qa.ui.pages.CartPage;
import com.ceiba.qa.ui.pages.CheckoutPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static com.ceiba.qa.core.utils.Config.defaultWaitSeconds;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class ProceedToCheckout implements Task {

    public static ProceedToCheckout stepOne() {
        return new ProceedToCheckout();
    }

    @Override
    @Step("{0} continua al formulario de checkout")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Click.on(CartPage.CHECKOUT_BUTTON),
                WaitUntil.the(CheckoutPage.CONTINUE_BUTTON, isVisible()).forNoMoreThan(defaultWaitSeconds()).seconds()
        );
    }
}
