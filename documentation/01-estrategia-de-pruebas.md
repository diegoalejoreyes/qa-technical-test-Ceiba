# Estrategia de pruebas

| Campo | Valor                                               |
|---|-----------------------------------------------------|
| Proyecto | Prueba técnica automatizacion QA – Ceiba            |
| Aplicaciones bajo prueba | SauceDemo (UI) · Restful Booker (API / Performance) |
| Versión | 1.0                                                 |
| Autor | Diego Alejandro reyes                               |
| Fecha | 11/08/2026                                          |

---

## 1. Objetivo de las pruebas

Verificar que los flujos críticos de compra de SauceDemo y el ciclo de vida del recurso
`booking` de Restful Booker se comportan de acuerdo con las reglas de negocio esperadas,
y dejar una base de automatización mantenible, ejecutable en CI y con reportería
consumible tanto por perfiles técnicos como de negocio.

Objetivos específicos:

1. Validar por código (no por inspección visual) la consistencia aritmética de la orden: `Subtotal + Impuesto = Total`.
2. Validar la integridad del carrito ante operaciones de agregado y eliminación.
3. Validar el CRUD completo de una reserva incluyendo autenticación, contrato de respuesta y limpieza de datos.
4. Determinar si `GET /booking` soporta la carga definida (10 VUs / ramp-up 30 s / 2 min).
5. Integrar todo lo anterior en un pipeline que se dispare con `push` y `pull_request`.

## 2. Alcance

### Dentro del alcance

| Área | Incluye |
|---|---|
| UI – SauceDemo | Login con `standard_user`, ordenamiento del catálogo por precio, agregado y eliminación dinámica de productos, contador del carrito, formulario de checkout (validaciones de campos obligatorios), cálculo de totales y confirmación de compra. |
| API – Restful Booker | `POST /auth`, `POST /booking`, `GET /booking/{id}`, `PUT /booking/{id}`, `DELETE /booking/{id}`. Códigos de estado, contrato de respuesta, campos obligatorios, coherencia entre datos enviados y recibidos, tiempos de respuesta y manejo de errores. |
| Performance | Prueba de carga sobre `GET /booking` con la configuración exigida. |
| Automatización | Suites UI y API en un único repositorio, ejecutables local y en CI, con reporte HTML publicado como artefacto. |

### Fuera del alcance

- Usuarios alternos de SauceDemo (`locked_out_user`, `problem_user`, `performance_glitch_user`, `error_user`, `visual_user`): se documentan como pruebas candidatas, pero no se automatizan en esta entrega.
- Pruebas de compatibilidad multi-navegador y responsive.
- Pruebas de seguridad (más allá de verificar que las operaciones `PUT`/`DELETE` exigen token).

## 3. Tipos de prueba considerados

| Tipo | Aplicación | Cómo se cubre |
|---|---|---|
| Funcional positiva | UI + API | Flujos 1 y 2, ciclo de vida CRUD. |
| Funcional negativa | UI + API | Checkout sin datos obligatorios, `PUT`/`DELETE` sin token, consulta de reserva inexistente, creación con payload incompleto. |
| De negocio / cálculo | UI | `Subtotal + Impuesto = Total` e impuesto = 8 % del subtotal, calculados con `BigDecimal`. |
| De integridad de datos | UI + API | Contenido del carrito tras eliminar, datos enviados vs. recibidos en la API. |
| De contrato | API | Validación contra JSON Schema (`schemas/*.json`). |
| De rendimiento (carga) | API | `GET /booking` con 10 VUs. |
| De regresión | UI + API | Conjunto etiquetado `@regresion`, ejecutable en cada `push`/`PR`. |
| Smoke | UI + API | Conjunto `@smoke`: ruta feliz mínima para validar que el ambiente responde. |

## 4. Matriz de riesgos

| ID | Riesgo | Prob. | Impacto | Exposición | Mitigación aplicada |
|---|---|---|---|---|---|
| R1 | Cálculo incorrecto de impuestos/total → cobro erróneo al cliente | Media | Alto | **Alta** | Validación aritmética en código con `BigDecimal` (no `double`), y verificación independiente del impuesto contra la regla del 8 %. |
| R2 | Precios y nombres de productos cambian → pruebas frágiles | Alta | Medio | **Alta** | Selección 100 % dinámica: los productos se resuelven leyendo el DOM y comparando precios; ningún nombre ni índice fijo en los escenarios. |
| R3 | Inestabilidad por sincronización (*flakiness*) | Alta | Medio | **Alta** | Esperas explícitas (`WaitUntil ... isVisible`); `Thread.sleep()` prohibido en el proyecto. |
| R4 | Ambiente compartido de Restful Booker: datos de terceros y reservas residuales | Alta | Medio | **Alta** | Cada escenario crea su propia reserva con datos aleatorios y la elimina en el `teardown`; el `bookingid` se usa de forma dinámica y nunca se hardcodea. |
| R5 | *Cold start* del servicio (dyno suspendido) contamina la medición de latencia | Alta | Medio | **Media** | Paso de *warm-up* antes de la prueba de carga y antes de las pruebas de API. |
| R6 | Indisponibilidad de la aplicación bajo prueba durante la ejecución del pipeline | Media | Alto | **Media** | Jobs independientes por suite, reintento manual del pipeline, reporte publicado siempre (`if: always()`). |
| R7 | El carrito conserva estado entre escenarios y produce falsos positivos | Media | Alto | **Media** | Navegador nuevo por escenario (`restart.browser.for.each = scenario`) y modo incógnito. |
| R8 | Ejecución en CI distinta a la local (headless, resolución, versión de Chrome) | Media | Medio | **Media** | Perfil `ci` en `serenity.conf` con `--headless=new` y resolución fija 1920x1080. |
| R9 | Falta de aserciones sobre el estado real (pruebas que "pasan" sin validar) | Baja | Alto | **Media** | Toda validación pasa por aserciones explícitas; los datos calculados se adjuntan al reporte de Serenity. |

## 5. Priorización

Se prioriza por **riesgo de negocio × frecuencia de uso**:

| Prioridad | Casos | Justificación |
|---|---|---|
| P1 – Crítica | Compra completa con validación `Subtotal + Impuesto = Total`; CRUD completo de la reserva | Afectan directamente dinero y datos del cliente. Un fallo aquí bloquea el negocio. |
| P2 – Alta | Contador del carrito y eliminación por precio; validaciones de campos obligatorios; `PUT`/`DELETE` sin token | Impacto funcional y de seguridad; alta frecuencia de uso. |
| P3 – Media | Ordenamiento del catálogo; consulta de reserva inexistente; contrato de respuesta | Afectan experiencia y consumo por parte de terceros. |
| P4 – Baja | Validación de formato de campos del checkout, mensajes de error uno a uno | Usabilidad; no bloquean la operación. |

## 6. Herramientas seleccionadas y justificación

| Necesidad | Herramienta | Por qué |
|---|---|---|
| Framework base y reportería | **Serenity BDD 5.x** | Genera *living documentation* en HTML con trazabilidad requisito → escenario → paso → evidencia, integra Selenium y Rest Assured bajo un mismo reporte, y trae esperas implícitas resilientes. Es el estándar más común en proyectos de la industria en Colombia. |
| Patrón de diseño UI | **Screenplay** | Frente a Page Object, favorece composición sobre herencia: Tasks reutilizables, Questions aisladas y clases pequeñas. Escala mejor cuando crece la suite y evita los *page objects* de 800 líneas. |
| Lenguaje de especificación | **Cucumber (Gherkin, español)** | Los escenarios quedan legibles para negocio y sirven de documentación viva; permite etiquetar (`@smoke`, `@regresion`, `@bug`) y filtrar por tags en el pipeline. |
| Automatización de navegador | **Selenium WebDriver** (vía Serenity) | Driver resuelto automáticamente por Selenium Manager, sin gestión manual de binarios. |
| Automatización de API | **Rest Assured** (`serenity-screenplay-rest`) | DSL específico para HTTP/JSON, validación de JSON Schema, y —clave— sus llamadas quedan registradas dentro **del mismo reporte Serenity** que la UI, cumpliendo el requisito de un único repositorio con reportería unificada. |
| Aserciones | **AssertJ** | Mensajes de fallo descriptivos y aserciones sobre colecciones (`containsExactlyInAnyOrder`, `isSortedAccordingTo`) que expresan directamente la regla de negocio. |
| Datos de prueba | **Datafaker** | Datos aleatorios por ejecución: evita colisiones en un ambiente compartido y descubre dependencias ocultas de datos fijos. |
| Pruebas de rendimiento | **k6** | Ver justificación ampliada abajo. |
| CI/CD | **GitHub Actions** | Nativo del repositorio, sin infraestructura adicional, con artefactos, *matrix*, ejecución por `push` y `pull_request` y resumen en la propia ejecución. |

### ¿Por qué k6 y no Serenity para la prueba de carga?

k6 se eligió sobre las alternativas por:

- **Modelo de VUs nativo**: `stages` expresa el ramp-up de 30 s y la duración de 2 min en tres líneas.
- **Thresholds**: convierten el análisis (P90, P95, tasa de error) en criterio de aceptación automatizado; si no se cumplen, el proceso termina con código 99 y el pipeline falla. Esto vuelve la prueba de rendimiento parte real del *quality gate*, no un reporte que alguien lee después.
- **Script como código**: JavaScript versionado y revisable en PR, a diferencia del XML de JMeter (difícil de revisar en un *diff*).
- **Huella liviana**: un binario, sin JVM ni GUI; ideal para un runner de CI.


## 7. Estrategia de automatización

**Principios de implementación:**

1. **Cero datos fijos.** Productos, precios y `bookingid` se resuelven en tiempo de ejecución. Si mañana SauceDemo cambia el catálogo o Restful Booker reinicia su base, las pruebas siguen pasando.
2. **Independencia entre escenarios.** Navegador nuevo por escenario, actor nuevo, datos propios. Cualquier escenario puede ejecutarse aislado y en cualquier orden.
3. **Limpieza de datos garantizada.** Hook `@After` que elimina la reserva creada aunque el escenario falle a mitad de camino.
4. **Esperas explícitas siempre.** `Thread.sleep()` está prohibido; se usa `WaitUntil(...).forNoMoreThan(n).seconds()`.
5. **Aserciones con intención.** Cada aserción declara la regla de negocio que valida (`.as("...")`), de modo que el mensaje de fallo explique el problema sin necesidad de leer el código.
6. **Separación por capas.** `pages` (localizadores) → `tasks` (acciones) → `questions` (consultas) → `stepdefinitions` (traducción Gherkin) → `features` (especificación). Ninguna capa salta a otra.
7. **Los hallazgos se automatizan.** El defecto conocido se convierte en un escenario etiquetado `@bug`, excluido de la ejecución bloqueante. El día que se corrija, el escenario pasa y se quita la etiqueta: la prueba es el criterio de cierre del defecto.

**Estrategia de ejecución por ambiente:**

| Trigger | Qué se ejecuta |
|---|---|
| `push` / `pull_request` | Suites completas de API y UI + prueba de carga + job informativo de hallazgos. |
| Ejecución manual (`workflow_dispatch`) | Igual, con la opción de volver bloqueante el job de hallazgos para evidenciar una ejecución fallida. |
| Local | `mvn clean verify` (todo) o por suite con `-Dit.test=`. |

## 8. Criterios de entrada y salida

### Criterios de entrada

- SauceDemo y Restful Booker accesibles y respondiendo (verificado con el *warm-up* / smoke).
- Credenciales `standard_user` y `admin/password123` vigentes.
- JDK 17, Maven 3.9+, Chrome y k6 instalados (o el runner de CI aprovisionado).
- Casos de prueba diseñados y priorizados (documento `02-diseno-casos-de-prueba.md`).

### Criterios de salida

- 100 % de los casos priorizados
- 100 % de los escenarios automatizados en verde, salvo los etiquetados `@bug` (que documentan defectos abiertos con su reporte asociado).
- Prueba de carga ejecutada y sus *thresholds* evaluados, con conclusión formal documentada.
- Pipeline ejecutándose correctamente en `push` y `pull_request`, con reportes publicados como artefactos.
- Hallazgos documentados con evidencia, severidad y pasos de reproducción.
- Sin defectos abiertos de severidad Crítica o Alta sin analizar y comunicar.

