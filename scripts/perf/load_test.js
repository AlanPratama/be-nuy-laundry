import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const USER = __ENV.PERF_USER || 'perf_admin';
const PASS = __ENV.PERF_PASS || 'Password123!';

export const options = {
  scenarios: {
    // Skenario 1: Smoke Test (validasi cepat)
    smoke: {
      executor: 'constant-vus',
      vus: 2,
      duration: '30s',
      startTime: '0s',
      tags: { scenario: 'smoke' },
    },
    // Skenario 2: Load Test (beban bertahap 10 -> 30 -> 50 VU)
    load: {
      executor: 'ramping-vus',
      startTime: '35s',
      stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 30 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 0 },
      ],
      tags: { scenario: 'load' },
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'], // error < 1%
    'http_req_duration{type:read}': ['p(95)<300'], // read p95 < 300ms
    'http_req_duration{type:write}': ['p(95)<800'], // write p95 < 800ms
  },
};

export function setup() {
  const res = http.post(
    `${BASE}/api/auth/login`,
    JSON.stringify({ username: USER, password: PASS }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  const body = res.json();
  if (res.status !== 200 || !body || !body.data || !body.data.accessToken) {
    throw new Error(`Setup failed: Unable to login with ${USER}. Response: ${res.body}`);
  }

  return { token: body.data.accessToken };
}

export default function (data) {
  const h = {
    headers: {
      Authorization: `Bearer ${data.token}`,
      'Content-Type': 'application/json',
    },
  };

  const r = Math.random();

  if (r < 0.40) {
    // 40% Read Transactions list
    const res = http.get(`${BASE}/api/v1/transactions?page=0&size=20`, {
      ...h,
      tags: { type: 'read', endpoint: 'list_transactions' },
    });
    check(res, {
      'status is 200 (transactions)': (r) => r.status === 200,
      'has content': (r) => r.json('data.content') !== undefined,
    });
  } else if (r < 0.70) {
    // 30% Read Customers & Packages
    const custRes = http.get(`${BASE}/api/v1/customers?page=0&size=20`, {
      ...h,
      tags: { type: 'read', endpoint: 'list_customers' },
    });
    check(custRes, { 'status is 200 (customers)': (r) => r.status === 200 });

    const pkgRes = http.get(`${BASE}/api/v1/packages?page=0&size=20`, {
      ...h,
      tags: { type: 'read', endpoint: 'list_packages' },
    });
    check(pkgRes, { 'status is 200 (packages)': (r) => r.status === 200 });
  } else if (r < 0.85) {
    // 15% Create Customer (Write)
    const randomPhone = `0819${Math.floor(10000000 + Math.random() * 90000000)}`;
    const payload = JSON.stringify({
      name: `K6 Customer ${Date.now()}`,
      phone: randomPhone,
      address: 'Jl. Uji Beban k6 No. 10',
    });

    const createRes = http.post(`${BASE}/api/v1/customers`, payload, {
      ...h,
      tags: { type: 'write', endpoint: 'create_customer' },
    });
    check(createRes, {
      'status is 200 or 201 (create customer)': (r) => r.status === 200 || r.status === 201,
    });
  } else {
    // 15% User Profile (/me)
    const meRes = http.get(`${BASE}/api/auth/me`, {
      ...h,
      tags: { type: 'read', endpoint: 'get_me' },
    });
    check(meRes, { 'status is 200 (me)': (r) => r.status === 200 });
  }

  sleep(0.5);
}
