/**
 * Prueba de carga - GET /booking (Restful Booker)
 *
 * Configuración exigida por la prueba técnica:
 *   - 10 usuarios virtuales
 *   - Ramp-up de 30 segundos
 *   - Duración total de 2 minutos
 *
 * Ejecución:
 *   k6 run performance/get-booking-load-test.js
 *   k6 run -e BASE_URL=https://restful-booker.herokuapp.com performance/get-booking-load-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';

const BASE_URL = __ENV.BASE_URL || 'https://restful-booker.herokuapp.com';

// Métricas personalizadas para el análisis posterior
const getBookingDuration = new Trend('get_booking_duration', true);
const businessErrors = new Rate('business_errors');

export const options = {
  // 30s de ramp-up hasta 10 VUs + 90s sosteniendo la carga = 2 minutos exactos
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m30s', target: 10 },
  ],
  // Los thresholds convierten el análisis en un criterio de salida automatizable:
  // si no se cumplen, k6 termina con exit code 99 y el pipeline falla.
  thresholds: {
    http_req_failed: ['rate<0.01'],           // < 1% de errores
    http_req_duration: ['p(90)<1500', 'p(95)<2000', 'avg<1000'],
    business_errors: ['rate<0.01'],
    checks: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
  discardResponseBodies: false,
};

/**
 * Warm-up: Restful Booker corre sobre un dyno que se suspende por inactividad.
 * Sin este paso, las primeras peticiones incluirían el tiempo de arranque del
 * servidor y contaminarían las métricas de latencia.
 */
export function setup() {
  const ping = http.get(`${BASE_URL}/ping`, { tags: { name: 'warmup-ping' } });
  const warmup = http.get(`${BASE_URL}/booking`, { tags: { name: 'warmup-booking' } });
  console.log(`[warm-up] /ping -> ${ping.status} | /booking -> ${warmup.status} (${warmup.timings.duration.toFixed(0)} ms)`);
  sleep(3);
  return { startedAt: new Date().toISOString() };
}

export default function () {
  const response = http.get(`${BASE_URL}/booking`, {
    headers: { Accept: 'application/json' },
    tags: { name: 'GET /booking' },
  });

  getBookingDuration.add(response.timings.duration);

  const ok = check(response, {
    'status es 200': (r) => r.status === 200,
    'content-type es JSON': (r) => String(r.headers['Content-Type']).includes('application/json'),
    'el cuerpo es una lista de reservas': (r) => {
      try {
        return Array.isArray(r.json());
      } catch (e) {
        return false;
      }
    },
    'tiempo de respuesta < 2s': (r) => r.timings.duration < 2000,
  });

  businessErrors.add(!ok);

  // Think time: simula un usuario real y evita saturar artificialmente el servicio
  sleep(1);
}

export function handleSummary(data) {
  return {
    stdout: textSummary(data, { indent: '  ', enableColors: true }),
    'reports/performance/summary.json': JSON.stringify(data, null, 2),
    'reports/performance/summary.html': htmlReport(data, { title: 'GET /booking - Prueba de carga' }),
  };
}
