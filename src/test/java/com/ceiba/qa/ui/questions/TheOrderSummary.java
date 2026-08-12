package com.ceiba.qa.ui.questions;

import com.ceiba.qa.core.models.OrderSummary;
import com.ceiba.qa.core.utils.Money;
import com.ceiba.qa.ui.pages.CheckoutPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.questions.Text;

/** Extrae Subtotal, Impuesto y Total de la pantalla Checkout: Overview. */
public class TheOrderSummary implements Question<OrderSummary> {

    public static Question<OrderSummary> displayed() {
        return new TheOrderSummary();
    }

    @Override
    public OrderSummary answeredBy(Actor actor) {
        return new OrderSummary(
                Money.parse(Text.of(CheckoutPage.SUBTOTAL_LABEL).answeredBy(actor)),
                Money.parse(Text.of(CheckoutPage.TAX_LABEL).answeredBy(actor)),
                Money.parse(Text.of(CheckoutPage.TOTAL_LABEL).answeredBy(actor))
        );
    }
}
