package com.ceiba.qa.ui.tasks;

import com.ceiba.qa.core.utils.Config;
import com.ceiba.qa.ui.pages.LoginPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static com.ceiba.qa.core.utils.Config.defaultWaitSeconds;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class OpenTheApplication implements Task {

    public static OpenTheApplication onTheLoginPage() {
        return new OpenTheApplication();
    }

    @Override
    @Step("{0} abre la aplicacion SauceDemo")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Open.url(Config.sauceDemoUrl()),
                WaitUntil.the(LoginPage.LOGIN_BUTTON, isVisible()).forNoMoreThan(defaultWaitSeconds()).seconds()
        );
    }
}
