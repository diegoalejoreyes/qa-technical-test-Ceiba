package com.ceiba.qa.ui.pages;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class CheckoutPage {

    // --- Step One: informacion del comprador ---
    public static final Target FIRST_NAME_FIELD =
            Target.the("campo First Name").located(By.id("first-name"));

    public static final Target LAST_NAME_FIELD =
            Target.the("campo Last Name").located(By.id("last-name"));

    public static final Target POSTAL_CODE_FIELD =
            Target.the("campo Zip/Postal Code").located(By.id("postal-code"));

    public static final Target CONTINUE_BUTTON =
            Target.the("boton Continue").located(By.id("continue"));

    public static final Target ERROR_MESSAGE =
            Target.the("mensaje de error del formulario").located(By.cssSelector("h3[data-test='error']"));

    // --- Step Two: resumen de la orden ---
    public static final Target SUBTOTAL_LABEL =
            Target.the("etiqueta Item total").located(By.cssSelector(".summary_subtotal_label"));

    public static final Target TAX_LABEL =
            Target.the("etiqueta Tax").located(By.cssSelector(".summary_tax_label"));

    public static final Target TOTAL_LABEL =
            Target.the("etiqueta Total").located(By.cssSelector(".summary_total_label"));

    public static final Target FINISH_BUTTON =
            Target.the("boton Finish").located(By.id("finish"));

    // --- Complete: confirmacion ---
    public static final Target CONFIRMATION_HEADER =
            Target.the("titulo de confirmacion de compra").located(By.cssSelector(".complete-header"));

    public static final Target CONFIRMATION_TEXT =
            Target.the("detalle de confirmacion de compra").located(By.cssSelector(".complete-text"));

    private CheckoutPage() {
    }
}
