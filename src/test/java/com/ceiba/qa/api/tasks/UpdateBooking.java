package com.ceiba.qa.api.tasks;

import com.ceiba.qa.api.models.Booking;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Put;

/** PUT /booking/{id} : requiere el token obtenido en /auth. */
public class UpdateBooking implements Task {

    private final Object bookingId;
    private final Booking booking;
    private final String token;

    public UpdateBooking(Object bookingId, Booking booking, String token) {
        this.bookingId = bookingId;
        this.booking = booking;
        this.token = token;
    }

    public static UpdateBooking withId(Object bookingId, Booking booking, String token) {
        return new UpdateBooking(bookingId, booking, token);
    }

    /** Variante para escenarios negativos: sin token de autenticación. */
    public static UpdateBooking withoutAuthentication(Object bookingId, Booking booking) {
        return new UpdateBooking(bookingId, booking, null);
    }

    @Override
    @Step("{0} actualiza la reserva #bookingId")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Put.to("/booking/" + bookingId).with(request -> {
                    request.header("Content-Type", "application/json").header("Accept", "application/json");
                    if (token != null) {
                        request.cookie("token", token);
                    }
                    return request.body(booking);
                })
        );
    }
}
