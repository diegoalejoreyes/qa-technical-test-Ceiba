package com.ceiba.qa.api.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingDates {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String checkin;
    private String checkout;

    public BookingDates() {
    }

    public BookingDates(String checkin, String checkout) {
        this.checkin = checkin;
        this.checkout = checkout;
    }

    public static BookingDates from(LocalDate checkin, LocalDate checkout) {
        return new BookingDates(checkin.format(FORMAT), checkout.format(FORMAT));
    }

    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getCheckout() {
        return checkout;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }
}
