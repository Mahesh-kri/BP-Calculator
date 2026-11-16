import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:5000';

const READINGS = [
  { systolic: '120', diastolic: '80', category: 'Ideal Blood Pressure' },
  { systolic: '85', diastolic: '55', category: 'Low Blood Pressure' },
  { systolic: '130', diastolic: '85', category: 'Pre-High Blood Pressure' },
  { systolic: '140', diastolic: '90', category: 'High Blood Pressure' },
];

export const options = {
  scenarios: {
    browse_and_calculate: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 10 },
        { duration: '1m', target: 10 },
        { duration: '20s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    checks: ['rate>0.99'],
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1500', 'p(99)<3000'],
  },
};

export default function () {
  const home = http.get(`${BASE_URL}/`);
  check(home, {
    'home page returns 200': (r) => r.status === 200,
    'home page renders the form': (r) => r.body.includes('id="bpForm"'),
  });

  const reading = READINGS[Math.floor(Math.random() * READINGS.length)];
  const result = http.post(`${BASE_URL}/`, {
    systolic: reading.systolic,
    diastolic: reading.diastolic,
  });
  check(result, {
    'calculation returns 200': (r) => r.status === 200,
    [`shows "${reading.category}"`]: (r) => r.body.includes(reading.category),
  });

  const health = http.get(`${BASE_URL}/actuator/health`);
  check(health, {
    'health endpoint is UP': (r) => r.status === 200 && r.body.includes('"UP"'),
  });

  sleep(Math.random() + 0.5);
}
