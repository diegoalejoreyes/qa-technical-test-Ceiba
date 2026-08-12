# Reporte de hallazgos

## Ambiente de pruebas

| Componente | Detalle |
|---|---|
| Aplicación UI | SauceDemo – https://www.saucedemo.com |
| Aplicación API | Restful Booker – https://restful-booker.herokuapp.com |
| Navegador | Google Chrome *(versión)* – modo headless en CI, headed en local |
| Sistema operativo | *(SO local)* / `ubuntu-latest` en GitHub Actions |
| Usuario | `standard_user` (UI) · `admin` (API) |
| Framework | Serenity BDD 5.x + Screenplay + Cucumber + Rest Assured · k6 para carga |
| Fecha de ejecución | *(fecha)* |

## Resumen

| ID | Título | Severidad | Prioridad | Estado |
|---|---|---|---|---|
| UI-001 | El carrito conserva los productos después de cerrar sesión | Media | Media | Abierto |
| UI-002 | El ordenamiento del catálogo se pierde al regresar del detalle de un producto | Baja | Baja | Abierto |
| UI-003 | Es posible completar una orden con el carrito vacío | **Alta** | Alta | Abierto |
| UI-004 | El formulario de checkout no valida formato ni longitud de sus campos | Baja | Baja | Abierto |
| UI-005 | Los errores de campos obligatorios se muestran de uno en uno | Baja | Baja | Abierto |
| API-001 | `DELETE /booking/{id}` responde `201 Created` en lugar de `200`/`204` | Baja | Media | Abierto |
| API-002 | `POST /booking` sin campos obligatorios responde `500` en lugar de `400` | **Alta** | Alta | Abierto |
| API-003 | La API acepta reservas con `checkout` anterior a `checkin` | Media | Media | Abierto |
| API-004 | `POST /auth` con credenciales inválidas responde `200` en lugar de `401` | Media | Alta | Abierto |

**Criterio de severidad utilizado:** *Crítica* = bloquea la operación o produce pérdida de
datos/dinero · *Alta* = afecta una regla de negocio o expone comportamiento inseguro ·
*Media* = comportamiento incorrecto con *workaround* · *Baja* = usabilidad o desviación de estándar.

---

## UI-001 · El carrito conserva los productos después de cerrar sesión

| Campo | Detalle |
|---|---|
| **Descripción** | Los productos agregados al carrito permanecen después de cerrar sesión. Al volver a iniciar sesión con el mismo usuario, el carrito conserva el contenido de la sesión anterior, incluido el contador del icono. |
| **Pasos para reproducir** | 1. Iniciar sesión con `standard_user`. 2. Agregar 2 productos al carrito. 3. Abrir el menú lateral y seleccionar *Logout*. 4. Iniciar sesión nuevamente con el mismo usuario. 5. Observar el contador del carrito. |
| **Resultado esperado** | Al cerrar sesión se termina el contexto del usuario: el carrito debe quedar vacío o, si el negocio decide persistirlo, debe informarse al usuario. |
| **Resultado actual** | El contador del carrito conserva el valor previo y los productos siguen dentro. |
| **Severidad / Prioridad** | Media / Media |
| **Impacto de negocio** | En un equipo compartido, el siguiente usuario hereda un carrito ajeno y puede comprar productos que no seleccionó. |
| **Evidencia** | `evidence/ui/UI-001-carrito-persistente.png` |
| **Ambiente** | Ver sección *Ambiente de pruebas* |
| **Caso relacionado** | UI-F2-07 |

## UI-002 · El ordenamiento del catálogo se pierde al regresar del detalle de un producto

| Campo | Detalle |
|---|---|
| **Descripción** | Tras ordenar el catálogo por *Price (low to high)* y entrar al detalle de un producto, al regresar con *Back to products* el catálogo vuelve al orden por defecto, quedando inconsistente con la última selección del usuario. |
| **Pasos para reproducir** | 1. Iniciar sesión. 2. Ordenar por *Price (low to high)*. 3. Entrar al detalle de cualquier producto. 4. Presionar *Back to products*. 5. Comparar el orden mostrado con el criterio seleccionado. |
| **Resultado esperado** | El criterio de ordenamiento se conserva durante la navegación dentro del catálogo. |
| **Resultado actual** | El catálogo se re-renderiza con el orden por defecto. |
| **Severidad / Prioridad** | Baja / Baja |
| **Impacto de negocio** | Fricción en la experiencia de compra: el usuario debe volver a ordenar en cada regreso. Riesgo adicional para la automatización, que debe re-aplicar el orden. |
| **Evidencia** | `evidence/ui/UI-002-orden-perdido.png` |
| **Caso relacionado** | UI-F1-08 |

## UI-003 · Es posible completar una orden con el carrito vacío

| Campo | Detalle |
|---|---|
| **Descripción** | La aplicación permite iniciar y completar el flujo de checkout sin ningún producto en el carrito, generando una orden confirmada con total `$0.00`. |
| **Pasos para reproducir** | 1. Iniciar sesión con el carrito vacío. 2. Abrir el carrito. 3. Presionar *Checkout*. 4. Diligenciar nombre, apellido y código postal. 5. Presionar *Continue* y luego *Finish*. |
| **Resultado esperado** | El botón *Checkout* debe estar deshabilitado o mostrar un mensaje indicando que el carrito está vacío. No debe generarse una orden. |
| **Resultado actual** | Se muestra el resumen con `Item total: $0.00` y se confirma la orden con el mensaje de agradecimiento. |
| **Severidad / Prioridad** | **Alta** / Alta |
| **Impacto de negocio** | Genera órdenes sin contenido: ruido en el sistema de fulfillment, métricas de venta distorsionadas y posible costo operativo de procesar órdenes vacías. |
| **Evidencia** | `evidence/ui/UI-003-orden-vacia.png` |
| **Caso relacionado** | UI-F2-08 |

## UI-004 · El formulario de checkout no valida formato ni longitud de sus campos

| Campo | Detalle |
|---|---|
| **Descripción** | Los campos *First Name*, *Last Name* y *Zip/Postal Code* solo validan que no estén vacíos. Aceptan caracteres especiales, números, cadenas extremadamente largas y valores sin sentido para un código postal. |
| **Pasos para reproducir** | 1. Ir al checkout con productos en el carrito. 2. Ingresar `!!!###@@@` en *First Name*, una cadena de 300 caracteres en *Last Name* y `-----` en *Zip/Postal Code*. 3. Presionar *Continue*. |
| **Resultado esperado** | Validación de formato y longitud máxima por campo, con mensajes específicos. |
| **Resultado actual** | El formulario acepta los valores y avanza al resumen de la orden. |
| **Severidad / Prioridad** | Baja / Baja |
| **Impacto de negocio** | Datos de despacho inválidos que llegan al proceso logístico; potencial vector de inyección si el dato se propaga sin sanitizar. |
| **Evidencia** | `evidence/ui/UI-004-sin-validacion-formato.png` |
| **Caso relacionado** | UI-F2-09 |

## UI-005 · Los errores de campos obligatorios se muestran de uno en uno

| Campo | Detalle |
|---|---|
| **Descripción** | Al enviar el formulario de checkout vacío solo se informa el primer campo faltante. El usuario debe realizar tres intentos para conocer los tres campos requeridos. |
| **Pasos para reproducir** | 1. Ir al checkout. 2. Presionar *Continue* con los tres campos vacíos. 3. Diligenciar solo *First Name* y volver a presionar *Continue*. 4. Repetir. |
| **Resultado esperado** | Todos los campos obligatorios faltantes se marcan simultáneamente. |
| **Resultado actual** | Se muestra un único mensaje por intento (`Error: First Name is required`, luego `Error: Last Name is required`, luego `Error: Postal Code is required`). |
| **Severidad / Prioridad** | Baja / Baja |
| **Impacto de negocio** | Fricción en el paso final del embudo de compra, justo donde el abandono es más costoso. |
| **Evidencia** | `evidence/ui/UI-005-errores-secuenciales.png` |
| **Casos relacionados** | UI-F2-04, UI-F2-05, UI-F2-06 |

---

## API-001 · `DELETE /booking/{id}` responde `201 Created` en lugar de `200`/`204`

| Campo | Detalle |
|---|---|
| **Descripción** | La eliminación exitosa de una reserva devuelve `201 Created` con el cuerpo `Created`, código que semánticamente indica creación de un recurso. |
| **Pasos para reproducir** | 1. `POST /auth` para obtener el token. 2. `POST /booking` y capturar el `bookingid`. 3. `DELETE /booking/{bookingid}` con `Cookie: token=<token>`. 4. Observar el código de estado. |
| **Resultado esperado** | `200 OK` o `204 No Content`, conforme a RFC 9110. |
| **Resultado actual** | `201 Created`. |
| **Severidad / Prioridad** | Baja / Media |
| **Impacto de negocio** | Los clientes que implementen manejo estándar de códigos HTTP (o generación automática desde OpenAPI) interpretarán mal la respuesta; obliga a código defensivo en cada consumidor. |
| **Evidencia** | Reporte Serenity, escenario *Ciclo de vida completo (CRUD) de una reserva*, paso `la eliminación debe ser exitosa` · `evidence/api/API-001-delete-201.png` |
| **Caso relacionado** | API-13 |

## API-002 · `POST /booking` sin campos obligatorios responde `500` en lugar de `400`

| Campo | Detalle |
|---|---|
| **Descripción** | Al enviar un payload sin los campos obligatorios (`lastname`, `totalprice`, `depositpaid`, `bookingdates`), la API responde con un error interno del servidor en vez de un error de validación del cliente. |
| **Pasos para reproducir** | 1. `POST /booking` con `Content-Type: application/json` y cuerpo `{"firstname":"Ana"}`. 2. Observar el código de estado y el cuerpo. |
| **Resultado esperado** | `400 Bad Request` con un mensaje que indique qué campos faltan. |
| **Resultado actual** | `500 Internal Server Error` con un mensaje genérico. |
| **Severidad / Prioridad** | **Alta** / Alta |
| **Impacto de negocio** | Un error de validación se presenta como falla del servidor: dispara alertas de monitoreo falsas, impide que el consumidor distinguir entre "mi petición está mal" y "el servicio está caído", y expone detalles internos. |
| **Evidencia** | Escenario automatizado etiquetado `@bug` en `booking_negative.feature`; artefacto `serenity-report-known-issues` del pipeline · `evidence/api/API-002-500-vs-400.png` |
| **Caso relacionado** | API-12 |

## API-003 · La API acepta reservas con `checkout` anterior a `checkin`

| Campo | Detalle |
|---|---|
| **Descripción** | Es posible crear una reserva cuya fecha de salida es anterior a la de entrada. La API la persiste sin validación. |
| **Pasos para reproducir** | 1. `POST /booking` con `bookingdates: {"checkin":"2026-12-10","checkout":"2026-12-01"}`. 2. Observar la respuesta. 3. Consultar la reserva por `GET`. |
| **Resultado esperado** | `400 Bad Request` indicando que `checkout` debe ser posterior a `checkin`. |
| **Resultado actual** | `200 OK`; la reserva se crea y se puede consultar con fechas inválidas. |
| **Severidad / Prioridad** | Media / Media |
| **Impacto de negocio** | Datos inconsistentes en el sistema de reservas: cálculos de noches negativos, reportes de ocupación errados y reservas imposibles de operar. |
| **Evidencia** | `evidence/api/API-003-fechas-invertidas.png` |
| **Caso relacionado** | API-14 |

## API-004 · `POST /auth` con credenciales inválidas responde `200` en lugar de `401`

| Campo | Detalle |
|---|---|
| **Descripción** | Ante credenciales incorrectas, el endpoint de autenticación responde con código de éxito y un cuerpo que indica el fallo (`{"reason":"Bad credentials"}`), en lugar de un código de error de autenticación. |
| **Pasos para reproducir** | 1. `POST /auth` con `{"username":"admin","password":"clave-incorrecta"}`. 2. Observar el código de estado. |
| **Resultado esperado** | `401 Unauthorized`. |
| **Resultado actual** | `200 OK` con `reason` en el cuerpo y sin token. |
| **Severidad / Prioridad** | Media / Alta |
| **Impacto de negocio** | Los consumidores que validan únicamente el código de estado darán por exitosa una autenticación fallida y continuarán el flujo sin token, produciendo errores tardíos y difíciles de diagnosticar. |
| **Evidencia** | `evidence/api/API-004-auth-200.png` |
| **Caso relacionado** | API-15 |
