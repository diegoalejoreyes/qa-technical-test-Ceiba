package com.ceiba.qa.api.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Delete;

/** DELETE /booking/{id} : requiere el token obtenido en /auth. */
public class DeleteBooking implements Task {

    private final Object bookingId;
    private final String token;

    public DeleteBooking(Object bookingId, String token) {
        this.bookingId = bookingId;
        this.token = token;
    }

    public static DeleteBooking withId(Object bookingId, String token) {
        return new DeleteBooking(bookingId, token);
    }

    @Override
    @Step("{0} elimina la reserva #bookingId")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Delete.from("/booking/" + bookingId).with(request -> {
                    if (token != null) {
                        request.cookie("token", token);
                    }
                    return request.header("Content-Type", "application/json");
                })
        );
    }
}
