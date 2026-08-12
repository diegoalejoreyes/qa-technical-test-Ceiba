package com.ceiba.qa.ui.questions;

import com.ceiba.qa.ui.pages.CheckoutPage;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.questions.Text;
import net.serenitybdd.screenplay.targets.Target;

/** Mensajes que muestra la aplicacion (errores y confirmaciones). */
public class DisplayedMessage {

    private DisplayedMessage() {
    }

    public static Question<String> error() {
        return textOf(CheckoutPage.ERROR_MESSAGE);
    }

    public static Question<String> confirmationHeader() {
        return textOf(CheckoutPage.CONFIRMATION_HEADER);
    }

    public static Question<String> confirmationDetail() {
        return textOf(CheckoutPage.CONFIRMATION_TEXT);
    }

    private static Question<String> textOf(Target target) {
        return actor -> Text.of(target).answeredBy(actor).trim();
    }
}
