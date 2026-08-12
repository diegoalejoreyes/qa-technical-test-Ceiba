package com.ceiba.qa.ui.stepdefinitions;

import com.ceiba.qa.core.models.Product;
import com.ceiba.qa.ui.pages.CheckoutPage;
import com.ceiba.qa.ui.questions.CartItemCount;
import com.ceiba.qa.ui.questions.CartProducts;
import com.ceiba.qa.ui.questions.CatalogProducts;
import com.ceiba.qa.ui.questions.DisplayedMessage;
import com.ceiba.qa.ui.tasks.AddToCart;
import com.ceiba.qa.ui.tasks.FillCustomerInformation;
import com.ceiba.qa.ui.tasks.OpenTheCart;
import com.ceiba.qa.ui.tasks.ProceedToCheckout;
import com.ceiba.qa.ui.tasks.RemoveFromCart;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static org.assertj.core.api.Assertions.assertThat;

public class CartStepDefinitions {

    private static final String PRODUCTS_IN_CART = "productosEnElCarrito";
    private static final String REMOVED_PRODUCT = "productoEliminado";

    /**
     * Selecciona N productos DIFERENTES de forma dinámica.
     * Estrategia: se ordenan los productos del catálogo por precio y se toma una
     * muestra distribuida (más barato, intermedios y más caro). Así se garantiza
     * que exista un "producto de mayor precio" inequívoco para el resto del flujo,
     * sin depender de nombres ni posiciones fijas del DOM.
     */
    @Cuando("agrega {int} productos diferentes al carrito")
    public void agregaProductosDiferentesAlCarrito(int quantity) {
        Actor actor = theActorInTheSpotlight();
        List<Product> catalog = actor.asksFor(CatalogProducts.displayed()).stream()
                .sorted(Product.BY_PRICE)
                .collect(Collectors.toList());

        assertThat(catalog.size())
                .as("El catálogo debe tener al menos %d productos para ejecutar el escenario", quantity)
                .isGreaterThanOrEqualTo(quantity);

        List<Product> selected = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            int index = (quantity == 1) ? 0 : (int) Math.round((double) i * (catalog.size() - 1) / (quantity - 1));
            selected.add(catalog.get(index));
        }

        selected.forEach(product -> actor.attemptsTo(AddToCart.theProduct(product)));
        actor.remember(PRODUCTS_IN_CART, selected);

        Serenity.recordReportData().withTitle("Productos seleccionados dinámicamente")
                .andContents(selected.stream().map(Product::toString).collect(Collectors.joining("\n")));
    }

    @Cuando("abre el carrito de compras")
    public void abreElCarritoDeCompras() {
        theActorInTheSpotlight().attemptsTo(OpenTheCart.page());
    }

    @Cuando("continúa al formulario de checkout")
    public void continuaAlFormularioDeCheckout() {
        theActorInTheSpotlight().attemptsTo(ProceedToCheckout.stepOne());
    }

    @Cuando("elimina del carrito el producto de mayor precio")
    public void eliminaElProductoDeMayorPrecio() {
        Actor actor = theActorInTheSpotlight();
        Product mostExpensive = actor.asksFor(CartProducts.displayed()).stream()
                .max(Product.BY_PRICE)
                .orElseThrow(() -> new IllegalStateException("El carrito está vacío"));

        actor.attemptsTo(RemoveFromCart.theProduct(mostExpensive));
        actor.remember(REMOVED_PRODUCT, mostExpensive);

        Serenity.recordReportData().withTitle("Producto eliminado (mayor precio)")
                .andContents(mostExpensive.toString());
    }

    @Cuando("intenta continuar con nombre {string}, apellido {string} y código postal {string}")
    public void intentaContinuarConDatos(String firstName, String lastName, String postalCode) {
        theActorInTheSpotlight().attemptsTo(
                FillCustomerInformation.with(firstName, lastName, postalCode)
        );
    }

    @Entonces("el contador del carrito debe ser {int}")
    public void elContadorDelCarritoDebeSer(int expected) {
        assertThat(theActorInTheSpotlight().asksFor(CartItemCount.displayed()))
                .as("Contador visible en el icono del carrito")
                .isEqualTo(expected);
    }

    @Entonces("el producto eliminado ya no debe estar en el carrito")
    public void elProductoEliminadoNoDebeEstarEnElCarrito() {
        Actor actor = theActorInTheSpotlight();
        Product removed = (Product) actor.recall(REMOVED_PRODUCT);

        assertThat(actor.asksFor(CartProducts.displayed()))
                .as("El producto eliminado no debe seguir en el carrito")
                .doesNotContain(removed);
    }

    @Entonces("los productos restantes deben ser los originalmente agregados")
    @SuppressWarnings("unchecked")
    public void losProductosRestantesSonLosEsperados() {
        Actor actor = theActorInTheSpotlight();
        List<Product> expected = new ArrayList<>((List<Product>) actor.recall(PRODUCTS_IN_CART));
        expected.remove((Product) actor.recall(REMOVED_PRODUCT));

        assertThat(actor.asksFor(CartProducts.displayed()))
                .as("Contenido del carrito después de la eliminación")
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Entonces("debe visualizar el mensaje de error {string}")
    public void debeVisualizarElMensajeDeError(String expectedMessage) {
        assertThat(theActorInTheSpotlight().asksFor(DisplayedMessage.error()))
                .as("Mensaje de error mostrado por la aplicación")
                .isEqualTo(expectedMessage);
    }

    @Entonces("debe permanecer en el formulario de información del comprador")
    public void debePermanecerEnElFormulario() {
        assertThat(CheckoutPage.CONTINUE_BUTTON.resolveFor(theActorInTheSpotlight()).isVisible())
                .as("El usuario no debe avanzar al resumen de la orden")
                .isTrue();
    }
}
