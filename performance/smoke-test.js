/**
 * Smoke de performance (1 VU, 30s).
 * Sirve como validación rápida del script y como warm-up del servicio
 * antes de lanzar la prueba de carga real.
 *
 *   k6 run performance/smoke-test.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'https://restful-booker.herokuapp.com';

export const options = {
  vus: 1,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<3000'],
  },
};

export default function () {
  const response = http.get(`${BASE_URL}/booking`, { tags: { name: 'GET /booking' } });
  check(response, { 'status es 200': (r) => r.status === 200 });
  sleep(1);
}
