# Pruebas de rendimiento (k6)

| Script | Propósito | Comando |
|---|---|---|
| `smoke-test.js` | Validación rápida del script y warm-up del servicio (1 VU, 30 s). | `k6 run performance/smoke-test.js` |
| `get-booking-load-test.js` | Prueba de carga exigida: 10 VUs, ramp-up 30 s, duración total 2 min. | `k6 run performance/get-booking-load-test.js` |

## Configuración implementada

```js
stages: [
  { duration: '30s',   target: 10 },  // ramp-up
  { duration: '1m30s', target: 10 },  // meseta (ventana válida de medición)
]
```

Total: **2 minutos exactos**, con think time de 1 s por iteración para simular usuarios reales.

## Warm-up

Restful Booker corre sobre infraestructura que suspende el servicio por inactividad. La
función `setup()` ejecuta `GET /ping` y `GET /booking` y espera 3 segundos antes de iniciar
la medición, para que el tiempo de arranque del servidor no contamine las latencias.

## Thresholds (criterios de aceptación automatizados)

| Métrica | Umbral |
|---|---|
| `http_req_failed` | < 1 % |
| `http_req_duration` avg | < 1000 ms |
| `http_req_duration` P90 | < 1500 ms |
| `http_req_duration` P95 | < 2000 ms |
| `checks` | > 99 % |

Si algún umbral no se cumple, k6 termina con código de salida 99 y el job del pipeline falla.

## Salidas

- `reports/performance/summary.json` — datos crudos de todas las métricas.
- `reports/performance/summary.html` — reporte visual.

El análisis y la respuesta formal están en [`../documentation/04-analisis-performance.md`](../documentation/04-analisis-performance.md).
