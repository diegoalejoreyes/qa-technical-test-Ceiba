package com.ceiba.qa.ui.pages;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class LoginPage {

    public static final Target USERNAME_FIELD =
            Target.the("campo Username").located(By.id("user-name"));

    public static final Target PASSWORD_FIELD =
            Target.the("campo Password").located(By.id("password"));

    public static final Target LOGIN_BUTTON =
            Target.the("boton Login").located(By.id("login-button"));

    public static final Target ERROR_MESSAGE =
            Target.the("mensaje de error de login").located(By.cssSelector("h3[data-test='error']"));

    private LoginPage() {
    }
}
