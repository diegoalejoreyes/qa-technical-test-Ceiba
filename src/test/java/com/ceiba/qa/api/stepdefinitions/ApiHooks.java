package com.ceiba.qa.api.stepdefinitions;

import com.ceiba.qa.api.questions.TheResponse;
import com.ceiba.qa.api.tasks.Authenticate;
import com.ceiba.qa.api.tasks.DeleteBooking;
import com.ceiba.qa.api.tasks.GetBooking;
import com.ceiba.qa.core.utils.Config;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Preparación y limpieza de datos de las pruebas de API.
 *
 * Teardown: si un escenario termina sin eliminar su reserva (por ejemplo, un fallo
 * intermedio), el hook la elimina para no dejar datos residuales en el ambiente.
 */
public class ApiHooks {

    public static final String BOOKING_ID = "bookingId";

    @Before(value = "@api", order = 10)
    public void prepareStage() {
        OnStage.setTheStage(Cast.whereEveryoneCan(CallAnApi.at(Config.bookerBaseUrl())));
    }

    @After(value = "@api", order = 20)
    public void cleanUpCreatedBookings() {
        Actor actor;
        try {
            actor = OnStage.theActorInTheSpotlight();
        } catch (RuntimeException noActorOnStage) {
            return;
        }

        Object bookingId = actor.recall(BOOKING_ID);
        if (bookingId == null) {
            return;
        }
        try {
            actor.attemptsTo(GetBooking.withId(bookingId));
            if (TheResponse.receivedBy(actor).getStatusCode() == 404) {
                return;
            }
            String token = actor.recall(Authenticate.TOKEN);
            if (token == null) {
                actor.attemptsTo(Authenticate.withValidCredentials());
                token = actor.recall(Authenticate.TOKEN);
            }
            actor.attemptsTo(DeleteBooking.withId(bookingId, token));
        } catch (RuntimeException e) {
            System.out.println("[teardown] No fue posible limpiar la reserva " + bookingId + ": " + e.getMessage());
        } finally {
            actor.forget(BOOKING_ID);
        }
    }

    @After(value = "@api", order = 1)
    public void clearTheStage() {
        OnStage.drawTheCurtain();
    }
}
