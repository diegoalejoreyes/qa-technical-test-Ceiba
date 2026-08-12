package com.ceiba.qa.ui.tasks;

import com.ceiba.qa.ui.pages.InventoryPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.SelectFromOptions;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static com.ceiba.qa.core.utils.Config.defaultWaitSeconds;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/** Ordena el catalogo usando el valor del option (az, za, lohi, hilo). */
public class SortTheCatalog implements Task {

    public static final String PRICE_LOW_TO_HIGH = "lohi";
    public static final String PRICE_HIGH_TO_LOW = "hilo";

    private final String optionValue;

    public SortTheCatalog(String optionValue) {
        this.optionValue = optionValue;
    }

    public static SortTheCatalog by(String optionValue) {
        return new SortTheCatalog(optionValue);
    }

    public static SortTheCatalog byPriceLowToHigh() {
        return new SortTheCatalog(PRICE_LOW_TO_HIGH);
    }

    @Override
    @Step("{0} ordena el catalogo con la opcion '#optionValue'")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                WaitUntil.the(InventoryPage.SORT_DROPDOWN, isVisible()).forNoMoreThan(defaultWaitSeconds()).seconds(),
                SelectFromOptions.byValue(optionValue).from(InventoryPage.SORT_DROPDOWN),
                WaitUntil.the(InventoryPage.ITEM_PRICES, isVisible()).forNoMoreThan(defaultWaitSeconds()).seconds()
        );
    }
}
