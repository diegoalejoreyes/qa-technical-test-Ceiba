package com.ceiba.qa.ui.questions;

import com.ceiba.qa.ui.pages.InventoryPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

/**
 * Contador del icono del carrito.
 * Si el badge no existe en el DOM, el valor de negocio es 0 (carrito vacio).
 */
public class CartItemCount implements Question<Integer> {

    public static Question<Integer> displayed() {
        return new CartItemCount();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        boolean badgeIsPresent = !InventoryPage.CART_BADGE.resolveAllFor(actor).isEmpty();
        if (!badgeIsPresent) {
            return 0;
        }
        return Integer.parseInt(InventoryPage.CART_BADGE.resolveFor(actor).getText().trim());
    }
}
