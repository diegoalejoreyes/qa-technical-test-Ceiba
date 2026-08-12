package com.ceiba.qa.ui.pages;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class InventoryPage {

    public static final Target PRODUCTS_TITLE =
            Target.the("titulo de la pagina de productos").located(By.cssSelector(".title"));

    public static final Target SORT_DROPDOWN =
            Target.the("selector de ordenamiento").located(By.cssSelector(".product_sort_container"));

    public static final Target INVENTORY_ITEMS =
            Target.the("productos del catalogo").located(By.cssSelector(".inventory_item"));

    public static final Target ITEM_NAMES =
            Target.the("nombres de los productos").located(By.cssSelector(".inventory_item_name"));

    public static final Target ITEM_PRICES =
            Target.the("precios de los productos").located(By.cssSelector(".inventory_item_price"));

    /** Boton "Add to cart" del producto indicado (seleccion dinamica por nombre, no por posicion).
     *  * Se filtra por data-test='add-to-cart-...' y NO por la clase btn_inventory,
     *      * porque esa clase la comparte tambien el boton "Remove": al agregar un producto*/
    public static final Target ADD_TO_CART_BUTTON_FOR = Target
            .the("boton Add to cart del producto '{0}'")
            .locatedBy("//div[@class='inventory_item']"
                    + "[.//div[contains(@class,'inventory_item_name')][normalize-space()='{0}']]"
                    + "//button[starts-with(@data-test,'add-to-cart')]");

    public static final Target CART_BADGE =
            Target.the("contador del carrito").located(By.cssSelector(".shopping_cart_badge"));

    public static final Target CART_LINK =
            Target.the("icono del carrito").located(By.cssSelector(".shopping_cart_link"));

    private InventoryPage() {
    }
}
