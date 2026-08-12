package com.ceiba.qa.ui.stepdefinitions;

import com.ceiba.qa.core.utils.Config;
import com.ceiba.qa.ui.pages.InventoryPage;
import com.ceiba.qa.ui.tasks.Login;
import com.ceiba.qa.ui.tasks.OpenTheApplication;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Y;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.actors.OnStage.theActorCalled;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static org.assertj.core.api.Assertions.assertThat;

public class LoginStepDefinitions {

    @Dado("que {word} está en la página de login de SauceDemo")
    public void queElActorEstaEnLaPaginaDeLogin(String actorName) {
        theActorCalled(actorName).attemptsTo(OpenTheApplication.onTheLoginPage());
    }

    @Cuando("inicia sesión con el usuario estándar")
    @Y("ha iniciado sesión con el usuario estándar")
    public void iniciaSesionConElUsuarioEstandar() {
        theActorInTheSpotlight().attemptsTo(
                Login.withCredentials(Config.standardUser(), Config.standardPassword())
        );
        assertThat(InventoryPage.PRODUCTS_TITLE.resolveFor(OnStage.theActorInTheSpotlight()).getText())
                .as("El usuario debe quedar en el catálogo de productos")
                .isEqualToIgnoringCase("Products");
    }
}
