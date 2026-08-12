package com.ceiba.qa.ui.questions;

import com.ceiba.qa.core.models.Product;
import com.ceiba.qa.core.utils.Money;
import com.ceiba.qa.ui.pages.InventoryPage;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.questions.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lee dinamicamente el catalogo (nombre + precio) desde el DOM.
 * Nunca se usan nombres ni posiciones fijas en los escenarios.
 */
public class CatalogProducts implements Question<List<Product>> {

    public static Question<List<Product>> displayed() {
        return new CatalogProducts();
    }

    @Override
    public List<Product> answeredBy(Actor actor) {
        List<WebElementFacade> nameElements = InventoryPage.ITEM_NAMES.resolveAllFor(actor);
        List<WebElementFacade> priceElements = InventoryPage.ITEM_PRICES.resolveAllFor(actor);
//        List<String> names = Text.of(InventoryPage.ITEM_NAMES).asList().answeredBy(actor);
//        List<String> prices = Text.of(InventoryPage.ITEM_PRICES).asList().answeredBy(actor);

        // validacion para extraer el catalogo completo
        if (nameElements.size() != priceElements.size()) {
            throw new IllegalStateException(String.format(
                    "Inconsistencia en el catálogo: %d nombres vs %d precios",
                    nameElements.size(), priceElements.size()));
        }

        // validacion para asegurar que la lista no este vacia
        if (nameElements.isEmpty()) {
            throw new IllegalStateException(
                    "No se encontró ningún producto en el catálogo.");
        }

        List<Product> products = new ArrayList<>();
        for (int i = 0; i < nameElements.size(); i++) {
            products.add(new Product(
                    nameElements.get(i).getText().trim(),
                    Money.parse(priceElements.get(i).getText())));
        }
        return products;
    }
}
