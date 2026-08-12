package com.ceiba.qa.api.models;

import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/** Genera datos de prueba dinámicos para las reservas (evita colisiones entre ejecuciones). */
public final class BookingFactory {

    private static final Faker FAKER = new Faker();

    private BookingFactory() {
    }

    public static Booking validBooking() {
        Booking booking = new Booking();
        booking.setFirstname(FAKER.name().firstName());
        booking.setLastname(FAKER.name().lastName());
        booking.setTotalprice(ThreadLocalRandom.current().nextInt(50, 900));
        booking.setDepositpaid(true);
        booking.setBookingdates(BookingDates.from(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5)));
        booking.setAdditionalneeds("Breakfast");
        return booking;
    }

    /** Reserva incompleta: sin campos obligatorios (escenario negativo). */
    public static Booking bookingWithoutMandatoryFields() {
        Booking booking = new Booking();
        booking.setFirstname(FAKER.name().firstName());
        return booking;
    }
}
