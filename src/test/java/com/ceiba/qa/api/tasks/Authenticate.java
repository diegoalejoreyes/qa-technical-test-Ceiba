package com.ceiba.qa.api.tasks;

import com.ceiba.qa.core.utils.Config;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

import java.util.HashMap;
import java.util.Map;

/** POST /auth : obtiene el token requerido por las operaciones PUT y DELETE. */
public class Authenticate implements Task {

    public static final String TOKEN = "token";

    public static Authenticate withValidCredentials() {
        return new Authenticate();
    }

    @Override
    @Step("{0} se autentica en la API")
    public <T extends Actor> void performAs(T actor) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", Config.bookerUser());
        credentials.put("password", Config.bookerPassword());

        actor.attemptsTo(
                Post.to("/auth").with(request -> request
                        .header("Content-Type", "application/json")
                        .body(credentials))
        );

        String token = LastResponse.received().answeredBy(actor).jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("No fue posible obtener el token de autenticación");
        }
        actor.remember(TOKEN, token);
    }
}
