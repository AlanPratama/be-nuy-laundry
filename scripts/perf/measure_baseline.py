#!/usr/bin/env python3
"""
BE Nuy Laundry - Endpoint Baseline Benchmark Runner (PERF-02)
Measures p50, p90, p95, and p99 response times across standard API endpoints.
"""

import time
import json
import urllib.request
import urllib.error
import urllib.parse
import statistics

BASE_URL = "http://localhost:8080"
WARMUP_RUNS = 5
BENCHMARK_RUNS = 50

def request(method, path, headers=None, data=None):
    if headers is None:
        headers = {}
    url = f"{BASE_URL}{path}"
    payload = None
    if data is not None:
        if isinstance(data, dict):
            payload = json.dumps(data).encode("utf-8")
            headers["Content-Type"] = "application/json"
        elif isinstance(data, bytes):
            payload = data

    req = urllib.request.Request(url, data=payload, headers=headers, method=method)
    start = time.perf_counter()
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read().decode("utf-8")
            code = resp.status
            elapsed_ms = (time.perf_counter() - start) * 1000.0
            data_json = json.loads(body) if body else {}
            return code, elapsed_ms, data_json
    except urllib.error.HTTPError as e:
        elapsed_ms = (time.perf_counter() - start) * 1000.0
        return e.code, elapsed_ms, {}
    except Exception as ex:
        elapsed_ms = (time.perf_counter() - start) * 1000.0
        return 0, elapsed_ms, {"error": str(ex)}

def benchmark_endpoint(name, method, path, headers=None, data_generator=None):
    latencies = []
    # Warmup
    for _ in range(WARMUP_RUNS):
        payload = data_generator() if data_generator else None
        request(method, path, headers, payload)

    status_codes = []
    for _ in range(BENCHMARK_RUNS):
        payload = data_generator() if data_generator else None
        code, lat, _ = request(method, path, headers, payload)
        latencies.append(lat)
        status_codes.append(code)

    p50 = statistics.median(latencies)
    latencies.sort()
    p90 = latencies[int(len(latencies) * 0.90)]
    p95 = latencies[int(len(latencies) * 0.95)]
    p99 = latencies[int(len(latencies) * 0.99)]
    avg = statistics.mean(latencies)
    success_rate = (status_codes.count(200) / len(status_codes)) * 100

    return {
        "name": name,
        "method": method,
        "path": path,
        "p50": p50,
        "p90": p90,
        "p95": p95,
        "p99": p99,
        "avg": avg,
        "success": success_rate
    }

def main():
    print("=========================================================================")
    print("      BE NUY LAUNDRY - BASELINE PERFORMANCE BENCHMARK (PERF-02)         ")
    print("=========================================================================")

    # 1. Login to obtain access token
    code, lat, resp = request("POST", "/api/auth/login", data={"username": "perf_admin", "password": "Password123!"})
    if code != 200:
        print(f"[FAIL] Could not login with perf_admin! Code: {code}")
        return
    token = resp["data"]["accessToken"]
    auth_header = {"Authorization": f"Bearer {token}"}
    print(f"[INFO] Authenticated successfully with perf_admin. Token acquired.\n")

    # Fetch reference IDs
    _, _, cust_res = request("GET", "/api/v1/customers?page=0&size=1", headers=auth_header)
    cust_id = cust_res["data"]["content"][0]["id"]

    _, _, pkg_res = request("GET", "/api/v1/packages?page=0&size=1", headers=auth_header)
    pkg_id = pkg_res["data"]["content"][0]["id"]

    _, _, trx_res = request("GET", "/api/v1/transactions?page=0&size=1", headers=auth_header)
    trx_id = trx_res["data"]["content"][0]["id"]

    endpoints = [
        ("Login (BCrypt)", "POST", "/api/auth/login", None, lambda: {"username": "perf_admin", "password": "Password123!"}),
        ("Get Profile (/me)", "GET", "/api/auth/me", auth_header, None),
        ("List Packages (size=20)", "GET", "/api/v1/packages?page=0&size=20", auth_header, None),
        ("List Benefits (size=20)", "GET", "/api/v1/benefits?page=0&size=20", auth_header, None),
        ("List Customers (size=20)", "GET", "/api/v1/customers?page=0&size=20", auth_header, None),
        ("Get Customer Detail by ID", "GET", f"/api/v1/customers/{cust_id}", auth_header, None),
        ("List Transactions (size=20)", "GET", "/api/v1/transactions?page=0&size=20", auth_header, None),
        ("List Transactions (size=100)", "GET", "/api/v1/transactions?page=0&size=100", auth_header, None),
        ("Get Transaction Detail by ID", "GET", f"/api/v1/transactions/{trx_id}", auth_header, None),
        ("Create Customer", "POST", "/api/v1/customers", auth_header, lambda: {
            "name": f"Cust Benchmark {time.time_ns()}",
            "phone": f"0819{random.randint(10000000, 99999999)}",
            "address": "Jl. Benchmark No. 99"
        }),
    ]

    import random
    results = []
    for item in endpoints:
        print(f"Benchmarking: {item[0]} [{item[1]} {item[2]}]...")
        res = benchmark_endpoint(item[0], item[1], item[2], item[3], item[4])
        results.append(res)

    print("\n" + "=" * 95)
    print(f"{'Endpoint':<32} | {'p50 (ms)':<9} | {'p90 (ms)':<9} | {'p95 (ms)':<9} | {'p99 (ms)':<9} | {'Success':<7}")
    print("-" * 95)
    for r in results:
        print(f"{r['name']:<32} | {r['p50']:<9.2f} | {r['p90']:<9.2f} | {r['p95']:<9.2f} | {r['p99']:<9.2f} | {r['success']:<5.1f}%")
    print("=" * 95)

    import sys
    out_file = sys.argv[1] if len(sys.argv) > 1 else "scripts/perf/baseline_results.json"
    with open(out_file, "w") as f:
        json.dump(results, f, indent=2)
    print(f"\n[INFO] Results saved to {out_file}")

if __name__ == "__main__":
    main()
