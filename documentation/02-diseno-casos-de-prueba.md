# Diseño de escenarios y casos de prueba

**Convenciones**

- **Tipo**: P = positivo · N = negativo · F = funcional · B = regla de negocio · S = seguridad
- **Prioridad**: P1 (crítica) · P2 (alta) · P3 (media) · P4 (baja)
- **Auto**: ✅ automatizado en esta entrega · ⬜ diseñado, no automatizado (con justificación en §4)

Técnicas de diseño aplicadas: **partición de equivalencia** (campos del checkout: vacío / válido / inválido),
**valores límite** (contador del carrito 3 → 2, producto de menor y mayor precio), **transición de estados**
(ciclo de vida de la reserva: creada → consultada → actualizada → eliminada → inexistente) y
**tabla de decisión** (combinaciones de campos obligatorios del formulario de checkout).

---

## 1. UI – Flujo 1: selección dinámica y validación de compra

| ID       | Caso | Tipo | Prioridad | Precondición | Pasos | Resultado esperado | Auto |
|----------|---|---|---|---|---|---|---|
| UI-F1-01 | Compra del producto de menor y mayor precio validando `Subtotal + Impuesto = Total` | P/B | **P1** | Usuario `standard_user` válido; catálogo con ≥ 2 productos | 1. Iniciar sesión. 2. Ordenar por *Price (low to high)*. 3. Agregar el producto de menor precio. 4. Agregar el de mayor precio. 5. Completar el checkout. 6. Calcular en código `Subtotal + Impuesto`. | El total mostrado es exactamente igual a `Subtotal + Impuesto`, calculado con `BigDecimal` a 2 decimales. | ✅ |
| UI-F1-02 | El subtotal corresponde a la suma de los precios de los productos agregados | B | **P1** | Ídem | Leer los precios de los productos agregados y sumarlos; comparar con *Item total*. | `Item total` = suma de los precios unitarios de los productos del carrito. | ✅ |
| UI-F1-03 | El impuesto corresponde al 8 % del subtotal | B | **P1** | Ídem | Calcular `subtotal × 0.08` redondeado a 2 decimales; comparar con *Tax*. | El impuesto mostrado coincide con el 8 % del subtotal. | ✅ |
| UI-F1-04 | Confirmación de la compra | P/F | **P1** | Checkout completado | Clic en *Finish*. | Se muestra el mensaje de agradecimiento y el detalle del despacho; el carrito queda vacío. | ✅ |
| UI-F1-05 | El catálogo queda ordenado de menor a mayor precio | F | **P3** | Sesión iniciada | Seleccionar *Price (low to high)* y leer todos los precios. | Los precios están en orden ascendente. | ✅ |
| UI-F1-06 | El producto agregado desde el catálogo es el mismo que aparece en el carrito | F | **P2** | Sesión iniciada | Agregar producto y abrir el carrito. | Nombre y precio del carrito coinciden con los del catálogo. | ✅ (implícito en UI-F1-02) |
| UI-F1-07 | El ordenamiento seleccionado se conserva al volver del detalle del producto | F | P3 | Catálogo ordenado por precio | Ordenar, entrar al detalle de un producto, volver atrás. | El orden y el valor del selector se conservan. | ⬜ → ver hallazgo **UI-002** |

## 2. UI – Flujo 2: carrito y validaciones

| ID | Caso | Tipo | Prioridad | Precondición | Pasos | Resultado esperado | Auto |
|---|---|---|---|---|---|---|---|
| UI-F2-01 | El contador del carrito refleja 3 productos agregados | P/F | **P1** | Sesión iniciada, carrito vacío | Agregar 3 productos diferentes seleccionados dinámicamente. | El badge del carrito muestra `3`. | ✅ |
| UI-F2-02 | Eliminar el producto de mayor precio actualiza el contador a 2 | P/F | **P1** | 3 productos en el carrito | Identificar por precio el producto más caro del carrito y eliminarlo. | El badge muestra `2`. | ✅ |
| UI-F2-03 | El producto eliminado ya no está en el carrito | F | **P1** | Ídem | Leer el contenido del carrito. | El producto eliminado no aparece; los otros 2 permanecen intactos (nombre y precio). | ✅ |
| UI-F2-04 | No se puede continuar el checkout sin *First Name* | N | **P2** | Carrito con productos | Ir a checkout y presionar *Continue* con todos los campos vacíos. | Mensaje `Error: First Name is required` y el usuario permanece en el formulario. | ✅ |
| UI-F2-05 | No se puede continuar el checkout sin *Last Name* | N | **P2** | Ídem | Diligenciar solo *First Name* y continuar. | Mensaje `Error: Last Name is required`. | ✅ |
| UI-F2-06 | No se puede continuar el checkout sin *Postal Code* | N | **P2** | Ídem | Diligenciar nombre y apellido y continuar. | Mensaje `Error: Postal Code is required`. | ✅ |
| UI-F2-07 | El carrito se limpia al cerrar sesión | F | P2 | Productos en el carrito | Cerrar sesión y volver a iniciar sesión. | El carrito debería quedar vacío. | ⬜ → ver hallazgo **UI-001** |
| UI-F2-08 | No es posible iniciar el checkout con el carrito vacío | N/B | P2 | Carrito vacío | Abrir el carrito y presionar *Checkout*. | La aplicación debería impedir avanzar. | ⬜ → ver hallazgo **UI-003** |
| UI-F2-09 | El *Postal Code* rechaza valores no válidos | N | P4 | Carrito con productos | Ingresar caracteres especiales / cadena de 200 caracteres. | Debería validarse formato y longitud. | ⬜ → ver hallazgo **UI-004** |
| UI-F2-10 | Login con credenciales inválidas | N | P2 | – | Ingresar usuario/clave incorrectos. | Mensaje de error; no se accede al catálogo. | ⬜ |

## 3. API – Restful Booker

| ID     | Caso | Tipo | Prioridad | Endpoint | Resultado esperado | Auto |
|--------|---|---|---|---|---|---|
| API-01 | Autenticación con credenciales válidas | P/S | **P1** | `POST /auth` | 200 y token no vacío. | ✅ |
| API-02 | Crear reserva con datos válidos | P | **P1** | `POST /booking` | 200, `bookingid` entero positivo, cuerpo conforme al JSON Schema, datos recibidos = datos enviados. | ✅ |
| API-03 | Tiempo de respuesta de la creación dentro del SLA | F | **P2** | `POST /booking` | < 3000 ms (SLA funcional configurable). | ✅ |
| API-04 | Consultar la reserva por `bookingid` | P | **P1** | `GET /booking/{id}` | 200, estructura conforme al contrato, datos iguales a los enviados. | ✅ |
| API-05 | Actualizar un dato de la reserva | P | **P1** | `PUT /booking/{id}` | 200 y el campo actualizado se refleja en la siguiente consulta. | ✅ |
| API-06 | Los campos no actualizados permanecen sin cambios | B | **P2** | `GET /booking/{id}` | Solo cambia el campo enviado; el resto conserva su valor. | ✅ |
| API-07 | Eliminar la reserva | P | **P1** | `DELETE /booking/{id}` | Eliminación exitosa. | ✅ |
| API-08 | La reserva eliminada ya no está disponible | F | **P1** | `GET /booking/{id}` | 404 Not Found. | ✅ |
| API-09 | No se permite actualizar sin token | N/S | **P2** | `PUT /booking/{id}` | 403 Forbidden. | ✅ |
| API-10 | No se permite eliminar sin token | N/S | **P2** | `DELETE /booking/{id}` | 403 Forbidden. | ✅ |
| API-11 | Consultar una reserva inexistente | N | **P3** | `GET /booking/99999999` | 404 Not Found. | ✅ |
| API-12 | Crear reserva sin campos obligatorios | N | **P2** | `POST /booking` | Debería responder 400 Bad Request con mensaje descriptivo. | ✅ (etiquetado `@bug`) → hallazgo **API-002** |
| API-13 | El `DELETE` exitoso responde con un código semánticamente correcto | N/F | P3 | `DELETE /booking/{id}` | Debería responder 200 o 204. | ✅ (aserción laxa documentada) → hallazgo **API-001** |
| API-14 | Crear reserva con `checkout` anterior a `checkin` | N/B | P2 | `POST /booking` | Debería rechazarse la reserva. | ⬜ → hallazgo **API-003** |
| API-15 | Autenticación con credenciales inválidas | N/S | P2 | `POST /auth` | Debería responder 401 Unauthorized. | ⬜ → hallazgo **API-004** |
| API-16 | `GET /booking` retorna la lista de reservas | P | P3 | `GET /booking` | 200 y arreglo de objetos con `bookingid`. | ✅ (cubierto por la prueba de carga) |

## 4. Priorización para automatización y justificación

**Se automatizó primero (P1 y P2):**

| Caso | Por qué se automatiza |
|---|---|
| UI-F1-01 a UI-F1-04 (cálculo de la orden y confirmación) | Es el flujo que mueve dinero. Un error de un centavo en el impuesto es un defecto financiero, es imperceptible en una prueba manual y es exactamente el tipo de verificación en la que el código gana a un humano. Además es un cálculo determinístico: alta estabilidad y ROI inmediato. |
| UI-F2-01 a UI-F2-03 (integridad del carrito) | Alta frecuencia de uso, alto costo de fallo (el cliente compra lo que no quería) y validación repetitiva y aburrida para un tester manual: candidato ideal para automatizar. |
| UI-F2-04 a UI-F2-06 (campos obligatorios) | Se automatizan como `Esquema del escenario`: un solo bloque cubre las tres particiones. Costo marginal casi nulo por caso adicional. |
| API-01 a API-11 (ciclo de vida y seguridad) | Ejecución en segundos, sin fragilidad de UI, y cubren la regla de negocio en la capa donde vive. Es la capa que más veces se ejecutará en la vida del proyecto. |

**Se automatizó como documentación de defecto (`@bug`):** API-12. La prueba está escrita
contra el comportamiento **esperado**, no contra el actual, y se excluye del *quality gate*
mediante la etiqueta. Cuando el defecto se corrija, el escenario pasará a verde y bastará
quitar la etiqueta: la prueba se convierte en el criterio de cierre del defecto.

**No se automatizó (y por qué):**

| Caso | Justificación |
|---|---|
| UI-F1-07, UI-F1-08, UI-F2-09, UI-F2-10, UI-F2-11 | Prioridad P3/P4 en un ejercicio con tiempo acotado. El enunciado indica explícitamente que se valora el criterio y la calidad sobre la cantidad; automatizar casos de baja prioridad habría añadido tiempo de mantenimiento sin reducir riesgo. Quedan diseñados y listos para incorporarse. |
| UI-F2-07, UI-F2-08 | Están reportados como hallazgos. Automatizarlos hoy produciría fallos permanentes en el pipeline sin aportar información nueva; se automatizan cuando exista una decisión del negocio sobre el comportamiento correcto. |
| API-14 a API-16 | Requieren definición de negocio previa sobre cuál es la validación esperada. Sin criterio de aceptación acordado, la prueba automatizada codifica una suposición. |

## 5. Datos de prueba

| Dato | Estrategia | Detalle |
|---|---|---|
| Usuario de SauceDemo | Fijo (definido por el enunciado) | `standard_user` / `secret_sauce`, parametrizado en `config.properties`. |
| Productos del catálogo | **Dinámico** | Se leen del DOM y se seleccionan por precio (mínimo, máximo y muestra distribuida). Ningún nombre ni índice está escrito en el código. |
| Formulario de checkout | Aleatorio (Datafaker) | Nombre, apellido y código postal generados por ejecución. Los escenarios negativos usan las particiones vacías definidas en la tabla de ejemplos. |
| Reserva (Restful Booker) | Aleatorio (Datafaker) | Nombre, apellido, precio (50–900) y fechas relativas a `LocalDate.now()`. Evita colisiones en un ambiente compartido. |
| `bookingid` | **Dinámico** | Se obtiene de la respuesta del `POST` y se propaga a `GET`, `PUT` y `DELETE` a través de la memoria del actor. |
| Token de autenticación | Dinámico | Obtenido en `POST /auth` al inicio del escenario. |
| Limpieza | Automática | Hook `@After` que elimina la reserva creada, aun si el escenario falla. |
