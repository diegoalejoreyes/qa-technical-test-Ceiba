package com.ceiba.qa.ui.stepdefinitions;

import com.ceiba.qa.core.models.OrderSummary;
import com.ceiba.qa.core.models.Product;
import com.ceiba.qa.core.utils.Money;
import com.ceiba.qa.ui.pages.CheckoutPage;
import com.ceiba.qa.ui.questions.CatalogProducts;
import com.ceiba.qa.ui.questions.DisplayedMessage;
import com.ceiba.qa.ui.questions.TheOrderSummary;
import com.ceiba.qa.ui.tasks.AddToCart;
import com.ceiba.qa.ui.tasks.FillCustomerInformation;
import com.ceiba.qa.ui.tasks.FinishThePurchase;
import com.ceiba.qa.ui.tasks.OpenTheCart;
import com.ceiba.qa.ui.tasks.ProceedToCheckout;
import com.ceiba.qa.ui.tasks.SortTheCatalog;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import net.datafaker.Faker;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static org.assertj.core.api.Assertions.assertThat;

public class PurchaseStepDefinitions {

    private static final String SELECTED_PRODUCTS = "productosSeleccionados";
    private static final Faker FAKER = new Faker();

    @Cuando("ordena el catálogo por precio de menor a mayor")
    public void ordenaElCatalogoPorPrecioDeMenorAMayor() {
        theActorInTheSpotlight().attemptsTo(SortTheCatalog.byPriceLowToHigh());
    }

    @Cuando("agrega al carrito el producto de menor precio")
    public void agregaElProductoDeMenorPrecio() {
        Actor actor = theActorInTheSpotlight();
        Product cheapest = AddToCart.cheapestProductSeenBy(actor);
        actor.attemptsTo(AddToCart.theProduct(cheapest));
        remember(actor, cheapest);
    }

    @Cuando("agrega al carrito el producto de mayor precio")
    public void agregaElProductoDeMayorPrecio() {
        Actor actor = theActorInTheSpotlight();
        Product mostExpensive = AddToCart.mostExpensiveProductSeenBy(actor);
        actor.attemptsTo(AddToCart.theProduct(mostExpensive));
        remember(actor, mostExpensive);
    }

    @Cuando("completa el proceso de checkout")
    public void completaElProcesoDeCheckout() {
        theActorInTheSpotlight().attemptsTo(
                OpenTheCart.page(),
                ProceedToCheckout.stepOne(),
                FillCustomerInformation.with(
                        FAKER.name().firstName(),
                        FAKER.name().lastName(),
                        FAKER.address().zipCode())
        );
    }

    @Entonces("el subtotal debe corresponder a la suma de los precios de los productos agregados")
    public void elSubtotalCorrespondeALaSumaDeLosProductos() {
        Actor actor = theActorInTheSpotlight();
        OrderSummary summary = actor.asksFor(TheOrderSummary.displayed());
        BigDecimal expectedSubtotal = selectedProducts(actor).stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Serenity.recordReportData().withTitle("Productos seleccionados dinámicamente")
                .andContents(selectedProducts(actor).stream()
                        .map(Product::toString).collect(Collectors.joining("\n")));

        assertThat(summary.getSubtotal())
                .as("El subtotal mostrado debe ser la suma de los precios de los productos del carrito")
                .isEqualByComparingTo(Money.round(expectedSubtotal));
    }

    @Entonces("el total de la orden debe cumplir la fórmula Subtotal + Impuesto = Total")
    public void elTotalCumpleLaFormula() {
        OrderSummary summary = theActorInTheSpotlight().asksFor(TheOrderSummary.displayed());

        Serenity.recordReportData().withTitle("Validación de la fórmula Subtotal + Impuesto = Total")
                .andContents(summary.toString());

        assertThat(summary.getTotal())
                .as("Total mostrado vs Subtotal + Impuesto calculado en código [%s]", summary)
                .isEqualByComparingTo(summary.calculatedTotal());
    }

    @Entonces("el impuesto debe corresponder al {int}% del subtotal")
    public void elImpuestoCorrespondeAlPorcentaje(int percentage) {
        OrderSummary summary = theActorInTheSpotlight().asksFor(TheOrderSummary.displayed());
        BigDecimal expectedTax = Money.round(
                summary.getSubtotal().multiply(BigDecimal.valueOf(percentage)).divide(BigDecimal.valueOf(100)));

        assertThat(summary.getTax())
                .as("Impuesto mostrado (%s) vs impuesto esperado del %d%% (%s)",
                        summary.getTax(), percentage, expectedTax)
                .isEqualByComparingTo(expectedTax);
    }

    @Entonces("debe visualizar la confirmación de la compra")
    public void debeVisualizarLaConfirmacionDeLaCompra() {
        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(FinishThePurchase.now());

        assertThat(actor.asksFor(DisplayedMessage.confirmationHeader()))
                .as("Mensaje de confirmación de la compra")
                .containsIgnoringCase("Thank you for your order");
        assertThat(actor.asksFor(DisplayedMessage.confirmationDetail()))
                .as("Detalle de la confirmación de la compra")
                .isNotBlank();
    }

    @Entonces("los precios del catálogo deben estar ordenados de forma ascendente")
    public void losPreciosEstanOrdenadosDeFormaAscendente() {
        List<Product> catalog = theActorInTheSpotlight().asksFor(CatalogProducts.displayed());
        List<BigDecimal> prices = catalog.stream().map(Product::getPrice).collect(Collectors.toList());

        Serenity.recordReportData().withTitle("Precios leídos del catálogo").andContents(prices.toString());

        assertThat(prices)
                .as("El catálogo debe quedar ordenado de menor a mayor precio")
                .isSortedAccordingTo(Comparator.naturalOrder());
    }

    @SuppressWarnings("unchecked")
    private List<Product> selectedProducts(Actor actor) {
        Object stored = actor.recall(SELECTED_PRODUCTS);
        return stored == null ? new ArrayList<>() : (List<Product>) stored;
    }

    private void remember(Actor actor, Product product) {
        List<Product> products = selectedProducts(actor);
        products.add(product);
        actor.remember(SELECTED_PRODUCTS, products);
    }
}
