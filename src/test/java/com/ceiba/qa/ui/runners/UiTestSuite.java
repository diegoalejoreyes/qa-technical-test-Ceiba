package com.ceiba.qa.ui.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;

import org.junit.runner.RunWith;

/**  * FILTRO DE TAGS
 * --------------
 * El valor de 'tags' es el filtro por defecto. Cucumber 7 permite sobrescribirlo
 * desde la línea de comandos, y esa propiedad tiene precedencia sobre esta
 * anotación:
 *
 *   mvn clean verify -Dsuite=UiTestSuite -Dtags="@smoke"
 *   mvn clean verify -Dsuite=UiTestSuite -Dtags="@ui and @negativo"
 *
 * Tags disponibles en el proyecto:
 *   @ui @flujo1 @flujo2        -> por capa y por flujo
 *   @smoke                     -> ruta feliz mínima
 *   @regresion                 -> suite de regresión
 *   @critico                   -> casos P1
 *   @funcional @negativo       -> por tipo de prueba
 *   @bug                       -> escenarios que documentan hallazgos (excluidos por defecto)
 *   */


@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/ui",
        glue = "com.ceiba.qa.ui.stepdefinitions",
        snippets = CucumberOptions.SnippetType.CAMELCASE,
        tags = "not @bug"
//        plugin = {"pretty"}
)

public class UiTestSuite {
}
