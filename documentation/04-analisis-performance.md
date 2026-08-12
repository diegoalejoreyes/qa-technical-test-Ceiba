# Análisis de la prueba de rendimiento – `GET /booking`

## 1. Diseño de la prueba

| Parámetro | Valor | Justificación |
|---|---|---|
| Tipo de prueba | Carga (*load test*) | Se busca conocer el comportamiento del servicio bajo una carga esperada y sostenida, no encontrar su punto de quiebre. |
| Endpoint | `GET /booking` | Es una lectura, el tipo de operación más frecuente en una API de reservas. |
| Usuarios virtuales | 10 | Definido por la prueba tecnica |
| Ramp-up | 30 s (0 → 10 VUs) | Evita el pico artificial de un arranque simultáneo y permite observar cómo escala el servicio de forma progresiva. |
| Duración total | 2 min (30 s de rampa + 1 min 30 s en meseta) | La meseta es la ventana de medición válida: la rampa incluye VUs que aún no están activos. |
| Think time | 1 s entre iteraciones | Aproxima el comportamiento de un usuario real; sin él se mediría un *stress test* encubierto y no la carga definida. |
| Warm-up | `GET /ping` + `GET /booking` + 3 s antes de iniciar | El servicio se suspende por inactividad; sin el *warm-up*, la primera latencia (varios segundos) distorsionaría el promedio y los percentiles. |
| Herramienta | k6 | Ver justificación en `01-estrategia-de-pruebas.md` |

## 2. Criterios de aceptación (*thresholds*)

Los criterios se declararon **dentro del script**, de modo que la evaluación es automática y
el pipeline falla si no se cumplen (k6 termina con código de salida 99):

| Métrica | Umbral | Razón |
|---|---|---|
| `http_req_failed` | < 1 % | Una API de lectura debe ser prácticamente libre de error bajo carga normal. |
| `http_req_duration` avg | < 1000 ms | Percepción de fluidez para un consumidor de servicio. |
| `http_req_duration` P90 | < 1500 ms | El 90 % de los usuarios debe estar dentro de un tiempo aceptable. |
| `http_req_duration` P95 | < 2000 ms | Controla la cola de la distribución: es donde vive la mala experiencia. |
| `checks` | > 99 % | Además de responder rápido, la respuesta debe ser correcta (200, JSON, arreglo de reservas). |

**Por qué percentiles y no solo promedio:** el promedio esconde los casos malos. Con 100
peticiones de 200 ms y 5 de 8 s, el promedio sigue siendo "bueno" mientras 5 usuarios tienen
una experiencia pésima. P90 y P95 son los que revelan ese comportamiento.

## 3. Resultados obtenidos

**Ejecución:** duración real 2 min 06 s · 756 iteraciones completas · 0 interrumpidas · 10 VUs máximos.

**Warm-up registrado:** `/ping -> 201 | /booking -> 200 (321 ms)`. El servicio ya estaba
despierto al iniciar la medición, por lo que ninguna latencia de arranque contamina los resultados.

| Métrica | Valor obtenido | Umbral | ¿Cumple? |
|---|---|---|---|
| Peticiones totales (`http_reqs`) | 758 | – | – |
| Throughput | 6.01 req/s | – | – |
| Tiempo de respuesta promedio | 390.71 ms | < 1000 ms | ✅ |
| Mediana (P50) | 326.33 ms | – | – |
| P90 | 558.32 ms | < 1500 ms | ✅ |
| P95 | 647.18 ms | < 2000 ms | ✅ |
| P99 | 1.30 s | – | – |
| Mínimo / Máximo | 109.29 ms / 1.50 s | – | – |
| Porcentaje de errores (`http_req_failed`) | 0.00 % (0 de 758) | < 1 % | ✅ |
| Checks exitosos | 100 % (3024 de 3024) | > 99 % | ✅ |
| Errores de negocio (`business_errors`) | 0.00 % | < 1 % | ✅ |
| VUs máximos alcanzados | 10 | 10 | ✅ |

**Todos los thresholds se cumplieron.** k6 finalizó con código de salida 0.

### Desglose del tiempo de respuesta

Descomponer la latencia permite identificar dónde se consume el tiempo:

| Fase | Promedio | % del total | Interpretación |
|---|---|---|---|
| `http_req_waiting` (TTFB) | 292.42 ms | ~75 % | Tiempo de procesamiento del servidor. Es el componente dominante. |
| `http_req_receiving` | 98.28 ms | ~25 % | Descarga del cuerpo. Elevado porque el endpoint devuelve la lista completa de reservas (50 MB transferidos en total). |
| `http_req_sending` | 1.99 µs | ~0 % | Despreciable: las peticiones GET no llevan cuerpo. |
| `http_req_connecting` | 3.05 ms (mediana 0 s) | ~0 % | La mediana en 0 s confirma reutilización de conexiones (*keep-alive*). |
| `http_req_tls_handshaking` | 4.16 ms (mediana 0 s) | ~0 % | Solo se paga en el establecimiento inicial de cada conexión. |


### Comportamiento del servicio durante la prueba

- **Estabilidad durante la rampa.** El mínimo (109 ms) y la mediana (326 ms) permanecen muy próximos, y la mediana casi coincide con la latencia observada en el warm-up (321 ms). Esto indica que la latencia base **no se degradó** conforme se incorporaban usuarios virtuales: con 10 VUs el servicio opera lejos de su punto de saturación.
- **Distribución homogénea.** La diferencia entre P50 (326 ms) y P95 (647 ms) es de apenas 321 ms, y entre P90 y P95 de 89 ms. Una distribución tan compacta descarta contención de recursos o encolamiento de peticiones.
- **Cola superior acotada.** El P99 (1.30 s) y el máximo (1.50 s) se separan del resto de la distribución, pero afectan a menos del 1 % de las peticiones. La métrica `http_req_blocked` con P99 de 325 ms sugiere que estos casos corresponden al establecimiento inicial de conexiones y negociación TLS, no a lentitud del procesamiento del servidor.
- **Throughput coherente con el diseño.** Los 6.01 req/s son consistentes con 10 VUs y una duración de iteración promedio de 1.4 s (que incluye el *think time* de 1 s): `10 / 1.4 ≈ 7 req/s` teóricos. El throughput está limitado por el diseño de la prueba, no por la capacidad del servidor.
- **Ninguna iteración interrumpida.** Las 756 iteraciones se completaron; no hubo *timeouts* ni conexiones abortadas.

## 4. Respuesta formal: ¿el endpoint soporta adecuadamente la carga definida?

**Sí, el endpoint `GET /booking` soporta adecuadamente la carga definida de 10 usuarios virtuales, con margen de holgura.**.

**Justificación, con base en los datos:**

1. **Disponibilidad total.** Se ejecutaron 758 peticiones sin un solo fallo (0.00 % de error) y con el 100 % de los 3024 checks funcionales exitosos. Este es el criterio de mayor peso: un servicio rápido que falla no soporta la carga. Aquí el servicio respondió correctamente en todos los casos, tanto en código de estado como en estructura de la respuesta.
2. **Latencia dentro de los criterios acordados, con margen amplio.** El promedio de 390.71 ms representa el 39 % del umbral de 1000 ms; el P90 de 558.32 ms, el 37 % de su umbral; y el P95 de 647.18 ms, el 32 % del suyo. No se trata de un cumplimiento marginal: los tres indicadores están en torno a un tercio de su límite, lo que deja espacio para absorber picos de carga sin incumplir el SLA
3. **Experiencia consistente entre usuarios.** La brecha entre la mediana (326 ms) y el P95 (647 ms) es de 321 ms. Este es el indicador clave de consistencia: una brecha amplia revelaría que un subconjunto de usuarios recibe un servicio notablemente peor que el resto. Con esta distribución, prácticamente todos los usuarios obtienen una experiencia equivalente.
4. **Ausencia de degradación al escalar.** Durante los 30 segundos de rampa la latencia se mantuvo estable en torno a la línea base medida en el warm-up (321 ms). Si el servicio estuviera operando cerca de su capacidad, la latencia habría crecido de forma proporcional al número de usuarios activos. No ocurrió, lo que indica que 10 VUs está muy por debajo del punto de saturación.
5. **El tiempo se consume donde es esperable.** El 75 % de la latencia corresponde al procesamiento del servidor (`http_req_waiting`) y el 25 % a la transferencia del cuerpo de la respuesta. No hay tiempo perdido en conexiones ni en negociación TLS durante el régimen estable, lo que confirma una configuración de red correcta.

**Conclusión y recomendaciones:**

- **Alcance de la conclusión.** Este resultado responde por el escenario definido (10 VUs). **No permite concluir que el endpoint soporte carga productiva.** Para determinar la capacidad real se requiere una prueba de escalabilidad incremental (10 → 25 → 50 → 100 VUs) que identifique el punto de quiebre.
- 10 VUs es una carga baja. Aun con resultado favorable, **no puede concluirse que el endpoint soporte carga productiva**: la prueba responde por el escenario definido, no por otro mayor.
- Se recomienda ejecutar una prueba de escalabilidad incremental (10 → 25 → 50 → 100 VUs) para identificar el punto de quiebre, y una prueba de resistencia (*soak*, 1 h) para detectar fugas de memoria o degradación progresiva.
- El servicio corre sobre infraestructura de demostración compartida; los resultados **no son extrapolables** a un ambiente productivo con recursos dedicados. Toda conclusión debe leerse con esa salvedad.
- Se recomienda incorporar percentiles P99 al monitoreo y establecer un SLA formal acordado con negocio: hoy los umbrales son una propuesta técnica, no un compromiso contractual.


## 5. Cómo reproducir la prueba

```bash
# Prueba de carga completa (10 VUs / ramp-up 30s / 2 min)
k6 run performance/get-booking-load-test.js

# Smoke rápido de validación del script
k6 run performance/smoke-test.js
```

# Las reportes y evidencias de ejecucion (json y html) se encuentran en reports/performance/

Artefactos generados: `reports/performance/summary.json` (datos crudos para análisis) y
`reports/performance/summary.html` (reporte visual). Ambos se publican como artefactos del
pipeline en el job *Performance test (k6 - GET /booking)*.
