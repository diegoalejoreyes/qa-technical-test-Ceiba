package com.ceiba.qa.api.stepdefinitions;

import com.ceiba.qa.api.models.Booking;
import com.ceiba.qa.api.models.BookingFactory;
import com.ceiba.qa.api.questions.TheResponse;
import com.ceiba.qa.api.tasks.Authenticate;
import com.ceiba.qa.api.tasks.CreateBooking;
import com.ceiba.qa.api.tasks.DeleteBooking;
import com.ceiba.qa.api.tasks.GetBooking;
import com.ceiba.qa.api.tasks.UpdateBooking;
import com.ceiba.qa.core.utils.Config;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.interactions.Get;

import static com.ceiba.qa.api.stepdefinitions.ApiHooks.BOOKING_ID;
import static net.serenitybdd.screenplay.actors.OnStage.theActorCalled;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingStepDefinitions {

    private static final String BOOKING_SENT = "reservaEnviada";

    @Dado("que {word} tiene acceso a la API de Restful Booker")
    public void tieneAccesoALaApi(String actorName) {
        Actor actor = theActorCalled(actorName);
        // Warm-up: despierta el dyno de Heroku antes de medir tiempos de respuesta.
        actor.attemptsTo(Get.resource("/ping"));
    }

    @Y("se autentica con credenciales válidas")
    public void seAutenticaConCredencialesValidas() {
        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(Authenticate.withValidCredentials());
        assertThat(actor.recall(Authenticate.TOKEN).toString())
                .as("Token devuelto por POST /auth")
                .isNotBlank();
    }

    @Cuando("crea una nueva reserva")
    public void creaUnaNuevaReserva() {
        Actor actor = theActorInTheSpotlight();
        Booking booking = BookingFactory.validBooking();
        actor.remember(BOOKING_SENT, booking);
        actor.attemptsTo(CreateBooking.with(booking));

        Serenity.recordReportData().withTitle("Reserva enviada").andContents(booking.toString());

        Integer bookingId = TheResponse.receivedBy(actor).jsonPath().getInt("bookingid");
        actor.remember(BOOKING_ID, bookingId);
    }

    @Cuando("consulta la reserva creada")
    public void consultaLaReservaCreada() {
        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(GetBooking.withId(actor.recall(BOOKING_ID)));
    }

    @Cuando("consulta la reserva con id {long}")
    public void consultaLaReservaConId(long bookingId) {
        theActorInTheSpotlight().attemptsTo(GetBooking.withId(bookingId));
    }

    @Cuando("actualiza el nombre de la reserva a {string}")
    public void actualizaElNombreDeLaReserva(String newFirstName) {
        Actor actor = theActorInTheSpotlight();
        Booking booking = actor.recall(BOOKING_SENT);
        booking.setFirstname(newFirstName);
        actor.remember(BOOKING_SENT, booking);
        actor.attemptsTo(UpdateBooking.withId(actor.recall(BOOKING_ID), booking, actor.recall(Authenticate.TOKEN)));
    }

    @Cuando("intenta actualizar la reserva sin token de autenticación")
    public void intentaActualizarSinToken() {
        Actor actor = theActorInTheSpotlight();
        Booking booking = actor.recall(BOOKING_SENT);
        booking.setFirstname("SinToken");
        actor.attemptsTo(UpdateBooking.withoutAuthentication(actor.recall(BOOKING_ID), booking));
    }

    @Cuando("intenta eliminar la reserva sin token de autenticación")
    public void intentaEliminarSinToken() {
        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(DeleteBooking.withId(actor.recall(BOOKING_ID), null));
    }

    @Cuando("intenta crear una reserva sin los campos obligatorios")
    public void intentaCrearReservaIncompleta() {
        Booking incomplete = BookingFactory.bookingWithoutMandatoryFields();
        Serenity.recordReportData().withTitle("Payload incompleto enviado").andContents(incomplete.toString());
        theActorInTheSpotlight().attemptsTo(CreateBooking.with(incomplete));
    }

    @Cuando("elimina la reserva")
    public void eliminaLaReserva() {
        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(DeleteBooking.withId(actor.recall(BOOKING_ID), actor.recall(Authenticate.TOKEN)));
    }

    // ------------------------------ Validaciones ------------------------------

    @Entonces("el código de respuesta debe ser {int}")
    public void elCodigoDeRespuestaDebeSer(int expectedStatus) {
        Actor actor = theActorInTheSpotlight();
        Serenity.recordReportData().withTitle("Respuesta recibida")
                .andContents(TheResponse.receivedBy(actor).getBody().asString());

        assertThat(actor.asksFor(TheResponse.statusCode()))
                .as("Código de estado HTTP")
                .isEqualTo(expectedStatus);
    }

    @Entonces("la eliminación debe ser exitosa")
    public void laEliminacionDebeSerExitosa() {
        int statusCode = theActorInTheSpotlight().asksFor(TheResponse.statusCode());
        // Restful Booker responde 201 (Created) ante un DELETE exitoso: ver hallazgo API-001.
        assertThat(statusCode)
                .as("Código de estado del DELETE (esperado 200/204 por semántica REST; la API responde 201)")
                .isIn(200, 201, 204);
    }

    @Y("la respuesta debe contener un bookingid válido")
    public void laRespuestaDebeContenerUnBookingIdValido() {
        assertThat((Integer) theActorInTheSpotlight().recall(BOOKING_ID))
                .as("bookingid generado por la API")
                .isNotNull()
                .isPositive();
    }

    @Y("la estructura de la respuesta debe cumplir el contrato {string}")
    public void laEstructuraDebeCumplirElContrato(String schemaPath) {
        TheResponse.receivedBy(theActorInTheSpotlight())
                .then()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }

    @Y("los datos de la reserva deben coincidir con los datos enviados")
    public void losDatosDebenCoincidirConLoEnviado() {
        Actor actor = theActorInTheSpotlight();
        Booking sent = actor.recall(BOOKING_SENT);
        Response response = TheResponse.receivedBy(actor);
        String prefix = response.jsonPath().get("booking") != null ? "booking." : "";

        assertThat(response.jsonPath().getString(prefix + "firstname")).as("firstname").isEqualTo(sent.getFirstname());
        assertThat(response.jsonPath().getString(prefix + "lastname")).as("lastname").isEqualTo(sent.getLastname());
        assertThat(response.jsonPath().getInt(prefix + "totalprice")).as("totalprice").isEqualTo(sent.getTotalprice());
        assertThat(response.jsonPath().getBoolean(prefix + "depositpaid")).as("depositpaid").isEqualTo(sent.getDepositpaid());
        assertThat(response.jsonPath().getString(prefix + "bookingdates.checkin"))
                .as("checkin").isEqualTo(sent.getBookingdates().getCheckin());
        assertThat(response.jsonPath().getString(prefix + "bookingdates.checkout"))
                .as("checkout").isEqualTo(sent.getBookingdates().getCheckout());
    }

    @Y("el tiempo de respuesta debe estar dentro del SLA definido")
    public void elTiempoDeRespuestaDebeEstarDentroDelSla() {
        long responseTime = theActorInTheSpotlight().asksFor(TheResponse.responseTimeInMillis());
        Serenity.recordReportData().withTitle("Tiempo de respuesta (ms)").andContents(String.valueOf(responseTime));

        assertThat(responseTime)
                .as("Tiempo de respuesta contra el SLA funcional de %d ms", Config.apiSlaMillis())
                .isLessThan(Config.apiSlaMillis());
    }

    @Entonces("el nombre de la reserva debe ser {string}")
    public void elNombreDeLaReservaDebeSer(String expectedName) {
        assertThat(TheResponse.receivedBy(theActorInTheSpotlight()).jsonPath().getString("firstname"))
                .as("firstname de la reserva consultada")
                .isEqualTo(expectedName);
    }

    @Y("los demás datos de la reserva no deben haber cambiado")
    public void losDemasDatosNoDebenHaberCambiado() {
        Actor actor = theActorInTheSpotlight();
        Booking sent = actor.recall(BOOKING_SENT);
        Response response = TheResponse.receivedBy(actor);

        assertThat(response.jsonPath().getString("lastname")).as("lastname").isEqualTo(sent.getLastname());
        assertThat(response.jsonPath().getInt("totalprice")).as("totalprice").isEqualTo(sent.getTotalprice());
        assertThat(response.jsonPath().getString("bookingdates.checkin"))
                .as("checkin").isEqualTo(sent.getBookingdates().getCheckin());
    }
}
