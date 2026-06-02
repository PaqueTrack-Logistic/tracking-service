export const BASE_URL = "http://paquetrack-app:8080/api/v1/tracking";

export const THRESHOLDS = {
  http_req_duration: ["p(95)<500", "p(99)<1000"],
  http_req_failed:   ["rate<0.01"],
  http_reqs:         ["rate>20"],
};

export const JSON_HEADERS = {
  "Content-Type": "application/json",
  "Accept":       "application/json",
};