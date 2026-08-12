package com.ceiba.qa.ui.stepdefinitions;

import io.cucumber.java.Before;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;

/**
 * Prepara el escenario Screenplay.
 * OnlineCast asigna a cada actor su propio navegador y lo cierra al terminar,
 * garantizando escenarios independientes.
 */
public class UiHooks {

    @Before(value = "@ui", order = 10)
    public void prepareStage() {
        OnStage.setTheStage(new OnlineCast());
    }
}
