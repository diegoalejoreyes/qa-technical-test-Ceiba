package com.ceiba.qa.api.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;


/**   * Runner de la suite de automatización de API (Restful Booker).
 *
 * Ver la nota sobre el filtro de tags en UiTestSuite. Ejemplos:
 *
 *   mvn clean verify -Dsuite=ApiTestSuite -Dtags="@smoke"
 *   mvn clean verify -Dsuite=ApiTestSuite -Dtags="@bug"    (evidencia del hallazgo API-002)
 */

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/api",
        glue = "com.ceiba.qa.api.stepdefinitions",
        snippets = CucumberOptions.SnippetType.CAMELCASE,
        tags = "not @bug",
        plugin = {"pretty"}
)

public class ApiTestSuite {
}
