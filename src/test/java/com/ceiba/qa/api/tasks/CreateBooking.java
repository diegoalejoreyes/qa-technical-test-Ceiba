package com.ceiba.qa.api.tasks;

import com.ceiba.qa.api.models.Booking;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;

/** POST /booking */
public class CreateBooking implements Task {

    private final Booking booking;

    public CreateBooking(Booking booking) {
        this.booking = booking;
    }

    public static CreateBooking with(Booking booking) {
        return new CreateBooking(booking);
    }

    @Override
    @Step("{0} crea la reserva #booking")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Post.to("/booking").with(request -> request
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .body(booking))
        );
    }
}
