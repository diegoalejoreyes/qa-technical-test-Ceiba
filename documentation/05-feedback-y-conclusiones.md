# Feedback y conclusiones

## 1. ¿Cuál fue el principal riesgo identificado?

**La fragilidad de la automatización frente a datos que cambian** (riesgo R2 de la matriz),
y muy de cerca, **la ausencia de validaciones de negocio en la capa de servicio** (hallazgos
API-002, API-003 y UI-003).

El primero es un riesgo del *proyecto de automatización*: ambas aplicaciones exponen datos
que pueden variar (catálogo, precios, reservas creadas por terceros). Una suite construida
sobre nombres de productos, posiciones en el DOM o `bookingid` fijos habría pasado hoy y
fallado mañana, y una suite que falla por razones ajenas al producto pierde credibilidad y
termina siendo ignorada por el equipo. Se mitigó resolviendo todo dinámicamente: los
productos se seleccionan comparando precios leídos en tiempo de ejecución y el `bookingid`
se propaga desde la respuesta del `POST`.

El segundo es un riesgo del *producto*: se puede confirmar una orden vacía en la UI y crear
una reserva con fechas invertidas o recibir un `500` ante un error de validación en la API.
Son fallas de validación en el servidor, es decir, en el punto donde no hay *workaround*
posible para el consumidor.

## 2. ¿Qué pruebas considera prioritarias para una regresión?

La suite marcada `@regresion` (~11 escenarios, ejecución en pocos minutos), organizada así:

| Prioridad | Pruebas | Por qué en regresión |
|---|---|---|
| 1 | Cálculo de la orden: `Subtotal + Impuesto = Total` e impuesto = 8 % | Toca dinero. Cualquier cambio en catálogo, promociones o impuestos puede romperlo silenciosamente. |
| 2 | Ciclo de vida CRUD de la reserva (API) | Es el contrato central del servicio. Rápido, estable y con altísima cobertura de riesgo por minuto de ejecución. |
| 3 | Integridad del carrito (agregar 3 / eliminar el más caro / contador) | Alta frecuencia de uso y afectada por casi cualquier cambio de front. |
| 4 | Autorización: `PUT` y `DELETE` sin token → 403 | Regresión de seguridad: un fallo aquí es crítico y no se detecta a simple vista. |
| 5 | Campos obligatorios del checkout | Cubre las tres particiones con un solo `Esquema del escenario`; costo marginal casi nulo. |
| 6 | Login exitoso y confirmación de compra (smoke) | Puerta de entrada: si falla, el resto no vale la pena ejecutarse. |



## 3. ¿Qué pruebas adicionales automatizaría?

**En el corto plazo (siguiente iteración):**

1. **Los casos diseñados y no automatizados** de `02-diseno-casos-de-prueba.md`: login con credenciales inválidas, usuario bloqueado, acceso directo a rutas sin sesión, ordenamiento *high to low* y alfabético.
2. **Los escenarios que hoy documentan hallazgos** (UI-003, API-003, API-004), en cuanto el negocio defina el comportamiento correcto: cada uno se convierte en el criterio de cierre de su defecto.
3. **Validación de contrato completa por endpoint**, incluyendo códigos de error, y ejecutada contra el esquema publicado por el proveedor.
4. **`GET /booking` con filtros** (`firstname`, `lastname`, `checkin`, `checkout`), incluyendo combinaciones y valores límite de fecha.

**En el mediano plazo:**

5. **Pruebas de los usuarios especiales de SauceDemo** (`problem_user`, `performance_glitch_user`): están diseñados para exponer defectos de UI y de latencia, y son un excelente banco de pruebas de la robustez de la propia suite.
6. **Pruebas de accesibilidad** automatizadas (axe-core) sobre el flujo de compra: es la clase de defecto que nadie reporta y que tiene implicaciones legales.
7. **Pruebas de regresión visual** en el resumen de la orden, donde un desajuste de maquetación puede hacer ilegible un total.
8. **Pruebas de rendimiento sobre `POST /booking`** (escritura), que es donde suelen aparecer los cuellos de botella reales, no en la lectura.
9. **Pruebas de resiliencia**: comportamiento del front ante timeouts y errores 5xx del backend (con *mocks* o *proxy*).

## 4. ¿Qué oportunidades de mejora identifica en la solución desarrollada?


| # | Oportunidad                                       | Detalle                                                                                                                                                                                                  |
|---|---------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | **Ejecución en paralelo**                         | Las suites corren secuencialmente. Con Serenity y `cucumber.execution.parallel.enabled` el tiempo total bajaría significativamente; requiere revisar aislamiento de datos y sesiones antes de activarlo. |
| 2 | **Cobertura multi-navegador**                     | Solo se ejecuta en Chrome. Una *matrix* de GitHub Actions con Firefox y Edge aumentaría la cobertura real a costo casi nulo.                                                                             |
| 3 | **Gestión de credenciales**                       | Las credenciales están en `config.properties` porque son públicas y de demostración. En un proyecto real deben venir encriptadas.                                                                        |
| 4 | **Umbrales de performance basados en datos**      | Los *thresholds* de k6 son una propuesta técnica inicial; deberían derivarse de datos históricos de producción y acordarse formalmente con negocio.                                                      |
| 5 | **Métricas de la propia suite**                   | No se está midiendo la tasa de fallos intermitentes ni la duración por escenario a lo largo del tiempo; sin eso, la degradación de la suite pasa desapercibida.                                          |
| 6 | **Validación de contrato más estricta**           | Los JSON Schema validan estructura y tipos, pero no reglas semánticas (rangos, formatos de fecha coherentes). Un esquema más estricto detectaría el hallazgo API-003 automáticamente.                    |

## 5. Si estas pruebas fueran llevadas a un proyecto real, ¿qué mejoraría de la estrategia propuesta?

1. **Involucrar QA desde el refinamiento, no al final.** El aporte más grande no es automatizar rápido, sino evitar que el defecto se escriba: los criterios de aceptación en Gherkin deberían nacer en el refinamiento, acordados entre negocio, desarrollo y QA. La mayoría de los hallazgos de esta prueba (validaciones ausentes, códigos HTTP incorrectos) son decisiones de diseño que se debatieron —o no se debatieron— antes de programar.

2. **Invertir la pirámide hacia donde es barata.** Aquí solo hay caja negra. En un proyecto real exigiría pruebas unitarias y de integración como criterio de *Definition of Done* del equipo de desarrollo, y reservaría la automatización E2E para los flujos críticos de negocio. Una regla de negocio como el cálculo del impuesto debería estar cubierta por una prueba unitaria de milisegundos, no solo por una E2E de un minuto.

4. **Datos de prueba gestionados, no improvisados.** Definiría una estrategia formal: ambiente con datos semilla controlados, un servicio de creación de datos vía API, y aislamiento por ejecución. Es la principal fuente de inestabilidad en suites maduras y en este ejercicio solo se resolvió parcialmente.

6. **Calidad como métrica visible, no como reporte.** Publicaría un tablero con densidad de defectos por módulo, escape rate a producción, tiempo de ejecución de la suite y tasa de *flakiness*. Sin métricas, "calidad" es una opinión.

7. **Shift-right complementario.** Automatizar *health checks* y pruebas sintéticas en producción, y usar el monitoreo real (APM, logs) como fuente para priorizar dónde poner el esfuerzo de pruebas. Los umbrales de rendimiento deberían derivarse de esos datos, no proponerse desde cero.

9. **Mantenimiento presupuestado.** Una suite de automatización es software y tiene costo de mantenimiento. Dejaría explícito en la planeación el tiempo dedicado a refactor, revisión de *flakiness* y depuración de casos obsoletos; de lo contrario, la suite se degrada hasta que el equipo deja de confiar en ella.
