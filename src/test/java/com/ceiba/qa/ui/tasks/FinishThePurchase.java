package com.ceiba.qa.ui.tasks;

import com.ceiba.qa.ui.pages.CheckoutPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static com.ceiba.qa.core.utils.Config.defaultWaitSeconds;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class FinishThePurchase implements Task {

    public static FinishThePurchase now() {
        return new FinishThePurchase();
    }

    @Override
    @Step("{0} finaliza la compra")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                WaitUntil.the(CheckoutPage.FINISH_BUTTON, isVisible()).forNoMoreThan(defaultWaitSeconds()).seconds(),
                Click.on(CheckoutPage.FINISH_BUTTON),
                WaitUntil.the(CheckoutPage.CONFIRMATION_HEADER, isVisible())
                        .forNoMoreThan(defaultWaitSeconds()).seconds()
        );
    }
}
