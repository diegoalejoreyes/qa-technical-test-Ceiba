package com.ceiba.qa.ui.pages;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class CartPage {

    public static final Target CART_ITEMS =
            Target.the("items del carrito").located(By.cssSelector(".cart_item"));

    public static final Target CART_ITEM_NAMES =
            Target.the("nombres de los items del carrito").located(By.cssSelector(".cart_item .inventory_item_name"));

    public static final Target CART_ITEM_PRICES =
            Target.the("precios de los items del carrito").located(By.cssSelector(".cart_item .inventory_item_price"));

    public static final Target REMOVE_BUTTON_FOR = Target
            .the("boton Remove del producto '{0}'")
            .locatedBy("//div[@class='cart_item']"
                    + "[.//div[contains(@class,'inventory_item_name')][normalize-space()='{0}']]"
                    + "//button[starts-with(@data-test,'remove')]");

    public static final Target CHECKOUT_BUTTON =
            Target.the("boton Checkout").located(By.id("checkout"));

    public static final Target CONTINUE_SHOPPING_BUTTON =
            Target.the("boton Continue Shopping").located(By.id("continue-shopping"));

    private CartPage() {
    }
}
