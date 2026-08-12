package com.ceiba.qa.ui.questions;

import com.ceiba.qa.core.models.Product;
import com.ceiba.qa.core.utils.Money;
import com.ceiba.qa.ui.pages.CartPage;
import com.ceiba.qa.ui.pages.InventoryPage;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.questions.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Lee dinamicamente los productos que estan en el carrito. */
public class CartProducts implements Question<List<Product>> {

    public static Question<List<Product>> displayed() {
        return new CartProducts();
    }

    @Override
    public List<Product> answeredBy(Actor actor) {
//        List<String> names = Text.of(CartPage.CART_ITEM_NAMES).asList().answeredBy(actor);
//        List<String> prices = Text.of(CartPage.CART_ITEM_PRICES).asList().answeredBy(actor);
        List<WebElementFacade> nameElements = CartPage.CART_ITEM_NAMES.resolveAllFor(actor);
        List<WebElementFacade> priceElements = CartPage.CART_ITEM_PRICES.resolveAllFor(actor);

        List<Product> products = new ArrayList<>();
        for (int i = 0; i < nameElements.size(); i++) {
            products.add(new Product(
                    nameElements.get(i).getText().trim(),
                    Money.parse(priceElements.get(i).getText())));
        }
        return products;
    }
}
