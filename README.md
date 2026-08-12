# Prueba técnica QA – Ceiba

Suite de automatización de pruebas **UI + API + Rendimiento** en un único repositorio,
construida con **Serenity BDD (Screenplay + Cucumber)**, **Rest Assured** y **k6**,
integrada en un pipeline de **GitHub Actions**.

| Capa | Aplicación bajo prueba | Herramienta |
|---|---|---|
| UI | [SauceDemo](https://www.saucedemo.com) | Serenity BDD + Screenplay + Selenium + Cucumber |
| API | [Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html) | Serenity BDD + Rest Assured (Screenplay REST) |
| Rendimiento | Restful Booker – `GET /booking` | k6 |
| CI/CD | – | GitHub Actions |

---

## 1. Descripción del proyecto

El proyecto automatiza y documenta:

- **Flujo 1 (UI):** login, ordenamiento del catálogo por precio, selección **dinámica** del producto de menor y mayor precio, checkout y validación por código de la fórmula `Subtotal + Impuesto = Total`.
- **Flujo 2 (UI):** agregado de 3 productos diferentes, validación del contador del carrito, eliminación del producto de mayor precio identificado dinámicamente y validación de los mensajes de error del checkout.
- **API:** ciclo de vida completo de una reserva (`POST` → `GET` → `PUT` → `GET` → `DELETE` → `GET`), con validación de código de estado, contrato (JSON Schema), campos obligatorios, datos enviados vs. recibidos, tiempo de respuesta y manejo de errores.
- **Rendimiento:** prueba de carga sobre `GET /booking` con 10 VUs, ramp-up de 30 s y 2 minutos de duración.
- **CI/CD:** ejecución automática en `push` y `pull_request`, con reportes publicados como artefactos.

La documentación de planeación, diseño, hallazgos y conclusiones está en [`documentation/`](documentation/).

## 2. Herramientas utilizadas y por qué

| Herramienta | Rol | Justificación resumida |
|---|---|---|
| **Serenity BDD 5.x** | Framework base y reportería | Reporte HTML con trazabilidad requisito → escenario → paso → evidencia; unifica UI y API en un solo reporte. |
| **Screenplay** | Patrón de diseño UI | Composición sobre herencia: Tasks y Questions reutilizables, clases pequeñas, mejor escalabilidad que Page Object. |
| **Cucumber (Gherkin en español)** | Especificación | Escenarios legibles por negocio y filtrables por tags (`@smoke`, `@regresion`, `@bug`). |
| **Selenium WebDriver** | Automatización de navegador | Estándar de industria; driver resuelto por Selenium Manager. |
| **Rest Assured** | Automatización de API | DSL para HTTP/JSON, validación de JSON Schema y trazas integradas al reporte Serenity. |
| **AssertJ** | Aserciones | Mensajes de fallo descriptivos y aserciones expresivas sobre colecciones. |
| **Datafaker** | Datos de prueba | Datos aleatorios por ejecución: evita colisiones en ambientes compartidos. |
| **k6** | Pruebas de carga | Modelo de VUs nativo, *thresholds* como criterio de aceptación automatizado, script como código y huella liviana en CI. |
| **GitHub Actions** | CI/CD | Nativo del repositorio, con artefactos y triggers de `push`/`pull_request`. |

> La justificación ampliada —incluida la comparación **k6 vs. JMeter** y por qué **no** se usó Serenity para la prueba de carga— está en [`documentation/01-estrategia-de-pruebas.md`](documentation/01-estrategia-de-pruebas.md#6-herramientas-seleccionadas-y-justificación).

## 3. Requisitos

| Requisito | Versión mínima | Verificar con |
|---|---|---|
| JDK | 17 | `java -version` |
| Maven | 3.9 | `mvn -v` |
| Google Chrome | Estable reciente | `google-chrome --version` |
| k6 | 0.50+ | `k6 version` |
| Git | – | `git --version` |

Instalación de k6:

```bash
# macOS
brew install k6
# Windows
choco install k6
# Linux (Debian/Ubuntu)
sudo gpg -k && sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update && sudo apt-get install k6
```

No se requiere descargar `chromedriver`: Selenium Manager lo resuelve automáticamente.

## 4. Instalación

```bash
git clone <URL-DEL-REPOSITORIO>
cd qa-technical-test
mvn -B clean install -DskipTests
```

## 5. Configuración

| Archivo | Qué configura |
|---|---|
| `src/test/resources/serenity.conf` | Driver, opciones de Chrome, perfiles `local` / `ci`, timeouts y opciones del reporte. |
| `src/test/resources/config.properties` | URLs, credenciales, SLA de tiempo de respuesta y esperas. |
| `pom.xml` | Versiones, suites (`it.test`), filtro de tags (`tags`) y perfil de entorno (`environment`). |

Cualquier propiedad de `config.properties` puede sobrescribirse por línea de comandos:

```bash
mvn clean verify -Drestfulbooker.url=https://mi-ambiente.com -Drestfulbooker.sla.responseTimeMs=5000
```

Perfiles de ejecución del navegador:

- `-Denvironment=local` *(por defecto)*: Chrome visible, útil para depurar.
- `-Denvironment=ci`: Chrome en modo `--headless=new`, resolución fija 1920x1080.

## 6. Ejecución

### Todo el proyecto (UI + API)

```bash
mvn clean verify
```

### Solo una suite

```bash
mvn clean verify -Dit.test=UiTestSuite      # solo UI
mvn clean verify -Dit.test=ApiTestSuite     # solo API
```

### Filtrando por tags

```bash
mvn clean verify -Dtags="@smoke"
mvn clean verify -Dtags="@regresion"
mvn clean verify -Dtags="@ui and @negativo"
mvn clean verify -Dtags="@bug"              # escenarios que documentan hallazgos (fallan a propósito)
```

> Por defecto se ejecuta `not @bug`: los escenarios que documentan defectos abiertos quedan
> fuera del *quality gate* y se ejecutan en un job informativo del pipeline.

### Pruebas de rendimiento

```bash
k6 run performance/smoke-test.js                 # validación rápida y warm-up
k6 run performance/get-booking-load-test.js      # prueba de carga (10 VUs / 30s ramp-up / 2 min)
```

### Ejecución en modo headless local

```bash
mvn clean verify -Denvironment=ci
```

## 7. Generación y ubicación de reportes

| Reporte | Ruta | Contenido |
|---|---|---|
| Serenity (HTML navegable) | `target/site/serenity/index.html` | Escenarios, pasos, capturas de pantalla ante fallos, peticiones y respuestas HTTP, datos adjuntos. |
| Serenity (página única) | `target/site/serenity/serenity-summary.html` | Versión de un solo archivo, fácil de adjuntar o compartir. |
| k6 (JSON) | `reports/performance/summary.json` | Datos crudos de todas las métricas. |
| k6 (HTML) | `reports/performance/summary.html` | Reporte visual de la prueba de carga. |

Regenerar el reporte agregado de Serenity sin volver a ejecutar las pruebas:

```bash
mvn serenity:aggregate
```

## 8. Pipeline CI/CD

Definido en [`.github/workflows/qa-pipeline.yml`](.github/workflows/qa-pipeline.yml).

**Triggers:** `push` y `pull_request` sobre cualquier rama, más ejecución manual.

**Jobs:**

| Job | Qué hace |
|---|---|
| `api-tests` | Obtiene el código, instala dependencias, ejecuta la suite de API y publica el reporte Serenity. |
| `ui-tests` | Instala Chrome y ejecuta la suite de UI en modo headless; publica el reporte Serenity. |
| `performance-tests` | Instala k6 y ejecuta la prueba de carga; publica los reportes JSON y HTML. |
| `known-issues` | Ejecuta los escenarios `@bug` que documentan hallazgos. Informativo por defecto. |
| `publish-results` | Consolida todos los artefactos, escribe el resumen de la ejecución y marca el pipeline como fallido si alguna suite falló. |

**Evidencia de ejecución exitosa y fallida**

- *Exitosa*: cualquier `push` con las suites en verde. Reportes en los artefactos de la ejecución.
- *Fallida*: ejecutar el workflow manualmente (**Actions → QA Automation Pipeline → Run workflow**) con `failure_demo = true`. El job `known-issues` deja de ser informativo y el pipeline falla mostrando el hallazgo API-002 como causa real, con su reporte publicado.

Las capturas de ambas ejecuciones se almacenan en [`evidence/pipeline/`](evidence/pipeline/).

## 9. Estructura del repositorio

```
qa-technical-test/
├── .github/workflows/qa-pipeline.yml     # Pipeline CI/CD
├── documentation/                        # Estrategia, casos, hallazgos, análisis, conclusiones
├── evidence/                             # Capturas de ejecución (UI, API, performance, pipeline)
├── performance/                          # Scripts de k6
├── reports/                              # Salida de k6 (generada)
├── src/test/java/com/ceiba/qa/
│   ├── core/                             # Modelos y utilidades compartidas (Money, Product, Config)
│   ├── ui/
│   │   ├── pages/                        # Localizadores (Targets)
│   │   ├── tasks/                        # Acciones del actor (Screenplay)
│   │   ├── questions/                    # Consultas sobre el estado de la UI
│   │   ├── stepdefinitions/              # Traducción Gherkin → Screenplay
│   │   └── runners/                      # UiTestSuite
│   └── api/
│       ├── models/                       # Booking, BookingDates, BookingFactory
│       ├── tasks/                        # Authenticate, Create/Get/Update/DeleteBooking
│       ├── questions/                    # Lectura de la última respuesta
│       ├── stepdefinitions/              # Steps + hooks (setup y teardown de datos)
│       └── runners/                      # ApiTestSuite
├── src/test/resources/
│   ├── features/ui/                      # flujo1_compra_dinamica · flujo2_carrito_validaciones
│   ├── features/api/                     # booking_lifecycle · booking_negative
│   ├── schemas/                          # JSON Schemas de validación de contrato
│   ├── serenity.conf                     # Configuración de Serenity y del driver
│   └── config.properties                 # URLs, credenciales y SLAs
└── pom.xml
```

## 10. Resultados

*(Completar tras la ejecución final.)*

| Suite | Escenarios | Pasaron | Fallaron | Duración |
|---|---|---|---|---|
| UI (SauceDemo) | `___` | `___` | `___` | `___` |
| API (Restful Booker) | `___` | `___` | `___` | `___` |
| Escenarios `@bug` (hallazgos) | 1 | 0 | 1 *(esperado)* | `___` |
| Performance (k6) | – | Thresholds: `___` | – | 2 min |

**Hallazgos:** 10 defectos documentados (2 de severidad Alta) — ver
[`documentation/03-reporte-de-hallazgos.md`](documentation/03-reporte-de-hallazgos.md).

## 11. Decisiones técnicas relevantes

1. **Screenplay en lugar de Page Object.** Los flujos comparten pasos (login, agregar al carrito, abrir el carrito). Screenplay permite componerlos como Tasks reutilizables sin cadenas de herencia entre páginas.

2. **Un solo repositorio, dos suites, un reporte.** UI y API comparten `pom.xml`, configuración y reportería, pero se ejecutan de forma independiente (`-Dit.test=`). Se cumple el requisito de convivencia sin acoplar las suites entre sí.

3. **`BigDecimal` y nunca `double` para dinero.** `0.1 + 0.2 != 0.3` en punto flotante. Validar un total de compra con `double` es un defecto esperando ocurrir; todos los cálculos usan `BigDecimal` con escala 2 y `RoundingMode.HALF_UP`.

4. **Selección 100 % dinámica.** Los productos se resuelven leyendo el DOM y comparando precios; el `bookingid` se propaga desde la respuesta del `POST`. No hay un solo nombre de producto ni id fijo en el código de pruebas.

5. **Cero `Thread.sleep()`.** Toda sincronización usa esperas explícitas de Serenity (`WaitUntil(...).forNoMoreThan(n).seconds()`), configurables desde `config.properties`.

6. **Independencia y limpieza garantizadas.** Navegador nuevo por escenario (`restart.browser.for.each = scenario`) y hook `@After` que elimina la reserva creada aunque el escenario falle.

7. **Los defectos conocidos se automatizan y se etiquetan.** El escenario que documenta el hallazgo API-002 está escrito contra el comportamiento **esperado** y etiquetado `@bug`, fuera del *quality gate*. Cuando el defecto se corrija, pasará a verde y bastará quitar la etiqueta: la prueba es el criterio de cierre del defecto.

8. **Aserción laxa y documentada para `DELETE`.** El ciclo de vida acepta 200/201/204 con un comentario explícito que remite al hallazgo API-001: la prueba no oculta la desviación, pero tampoco bloquea la validación del flujo completo por un código de estado no estándar del proveedor.

9. **Thresholds de k6 como criterio de salida.** El análisis de rendimiento no depende de que alguien lea un reporte: si P90, P95 o la tasa de error se degradan, k6 falla y el pipeline se detiene.

10. **Warm-up antes de medir.** Restful Booker se suspende por inactividad. Sin `GET /ping` previo, la primera latencia contaminaría tanto las aserciones de SLA como los percentiles de la prueba de carga.

11. **Configuración separada del código.** URLs, credenciales y SLAs viven en `config.properties`, sobrescribibles por `-D` desde CI. Cambiar de ambiente no requiere tocar una sola línea de Java.

## 12. Documentación

| Documento | Contenido |
|---|---|
| [`01-estrategia-de-pruebas.md`](documentation/01-estrategia-de-pruebas.md) | Alcance, objetivos, tipos de prueba, matriz de riesgos, priorización, herramientas, estrategia de automatización y criterios de entrada/salida. |
| [`02-diseno-casos-de-prueba.md`](documentation/02-diseno-casos-de-prueba.md) | Escenarios y casos (positivos, negativos, funcionales, de negocio), priorización para automatización con justificación y datos de prueba. |
| [`03-reporte-de-hallazgos.md`](documentation/03-reporte-de-hallazgos.md) | Defectos con ID, título, descripción, pasos, resultado esperado/actual, severidad, evidencia y ambiente. |
| [`04-analisis-performance.md`](documentation/04-analisis-performance.md) | Diseño de la prueba de carga, métricas, análisis y respuesta formal sobre la capacidad del endpoint. |
| [`05-feedback-y-conclusiones.md`](documentation/05-feedback-y-conclusiones.md) | Respuestas a las cinco preguntas de feedback. |
