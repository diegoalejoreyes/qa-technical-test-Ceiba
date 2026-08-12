package com.ceiba.qa.ui.tasks;

import com.ceiba.qa.ui.pages.CheckoutPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;

import java.util.ArrayList;
import java.util.List;

/**
 * Diligencia el formulario de checkout.
 * Los campos vacios ("") simplemente no se diligencian, lo que permite reutilizar
 * la misma Task para escenarios negativos de campos obligatorios.
 */
public class FillCustomerInformation implements Task {

    private final String firstName;
    private final String lastName;
    private final String postalCode;

    public FillCustomerInformation(String firstName, String lastName, String postalCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.postalCode = postalCode;
    }

    public static FillCustomerInformation with(String firstName, String lastName, String postalCode) {
        return new FillCustomerInformation(firstName, lastName, postalCode);
    }

    public static FillCustomerInformation leavingAllFieldsEmpty() {
        return new FillCustomerInformation("", "", "");
    }

    @Override
    @Step("{0} diligencia el formulario de checkout y continua")
    public <T extends Actor> void performAs(T actor) {
        List<Performable> steps = new ArrayList<>();
        if (!firstName.isEmpty()) {
            steps.add(Enter.theValue(firstName).into(CheckoutPage.FIRST_NAME_FIELD));
        }
        if (!lastName.isEmpty()) {
            steps.add(Enter.theValue(lastName).into(CheckoutPage.LAST_NAME_FIELD));
        }
        if (!postalCode.isEmpty()) {
            steps.add(Enter.theValue(postalCode).into(CheckoutPage.POSTAL_CODE_FIELD));
        }
        steps.add(Click.on(CheckoutPage.CONTINUE_BUTTON));

        actor.attemptsTo(steps.toArray(new Performable[0]));
    }
}
