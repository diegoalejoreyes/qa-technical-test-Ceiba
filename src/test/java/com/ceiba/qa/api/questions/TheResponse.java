package com.ceiba.qa.api.questions;

import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

/** Atajos de lectura sobre la última respuesta HTTP recibida. */
public final class TheResponse {

    private TheResponse() {
    }

    public static Response receivedBy(Actor actor) {
        return LastResponse.received().answeredBy(actor);
    }

    public static Question<Integer> statusCode() {
        return actor -> receivedBy(actor).getStatusCode();
    }

    public static Question<Long> responseTimeInMillis() {
        return actor -> receivedBy(actor).getTime();
    }

    public static Question<String> body() {
        return actor -> receivedBy(actor).getBody().asString();
    }
}
