package com.ceiba.qa.api.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;

/** GET /booking/{id} : el id se resuelve dinámicamente en tiempo de ejecución. */
public class GetBooking implements Task {

    private final Object bookingId;

    public GetBooking(Object bookingId) {
        this.bookingId = bookingId;
    }

    public static GetBooking withId(Object bookingId) {
        return new GetBooking(bookingId);
    }

    @Override
    @Step("{0} consulta la reserva #bookingId")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Get.resource("/booking/" + bookingId)
                        .with(request -> request.header("Accept", "application/json"))
        );
    }
}
