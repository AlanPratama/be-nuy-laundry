#!/usr/bin/env python3
import json
import subprocess
import time
import os
import sys

BASE_URL = "http://localhost:8080"
ASSETS_DIR = os.path.abspath("target/test_assets")
VALID_IMAGE = os.path.join(ASSETS_DIR, "valid_image.png")
INVALID_FILE = os.path.join(ASSETS_DIR, "invalid_file.txt")
OVERSIZED_FILE = os.path.join(ASSETS_DIR, "oversized.jpg")

# Test metrics
results = []

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    BOLD = '\033[1m'
    END = '\033[0m'

def log_test(test_id, name, passed, status_code, expected_status, details=""):
    results.append({
        "id": test_id,
        "name": name,
        "passed": passed,
        "status_code": status_code,
        "expected_status": expected_status,
        "details": details
    })
    badge = f"{Colors.GREEN}[PASS]{Colors.END}" if passed else f"{Colors.RED}[FAIL]{Colors.END}"
    print(f"  {badge} {Colors.BOLD}{test_id}{Colors.END}: {name} (HTTP {status_code}, expected {expected_status})")
    if not passed and details:
        print(f"         {Colors.RED}Details: {details}{Colors.END}")

def run_curl(method, endpoint, headers=None, data=None, multipart=None):
    url = f"{BASE_URL}{endpoint}"
    cmd = ["curl", "-s", "-w", "\n%{http_code}", "-X", method, url]

    if headers:
        for k, v in headers.items():
            cmd.extend(["-H", f"{k}: {v}"])

    if data is not None:
        if isinstance(data, dict):
            cmd.extend(["-H", "Content-Type: application/json", "-d", json.dumps(data)])
        else:
            cmd.extend(["-d", data])

    if multipart:
        for part_name, part_val in multipart.items():
            if isinstance(part_val, tuple):
                # (filepath, content_type)
                filepath, ct = part_val
                cmd.extend(["-F", f"{part_name}=@{filepath};type={ct}"])
            elif isinstance(part_val, list):
                for item in part_val:
                    if isinstance(item, tuple):
                        fp, ct = item
                        cmd.extend(["-F", f"{part_name}=@{fp};type={ct}"])
                    else:
                        cmd.extend(["-F", f"{part_name}=@{item}"])
            elif isinstance(part_val, str) and part_val.startswith("@"):
                cmd.extend(["-F", f"{part_name}={part_val}"])
            else:
                # String part, e.g. json payload
                cmd.extend(["-F", f"{part_name}={json.dumps(part_val)};type=application/json"])

    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    out = res.stdout.strip()
    if "\n" in out:
        body, http_code = out.rsplit("\n", 1)
    else:
        body = ""
        http_code = out

    parsed_json = None
    try:
        parsed_json = json.loads(body)
    except:
        parsed_json = None

    try:
        code_int = int(http_code)
    except:
        code_int = 0

    return code_int, parsed_json, body

def main():
    print(f"\n{Colors.BOLD}{Colors.BLUE}========================================================================={Colors.END}")
    print(f"{Colors.BOLD}{Colors.BLUE}   BE NUY LAUNDRY - AUTOMATED API FUNCTIONAL TEST SUITE (E2E)   {Colors.END}")
    print(f"{Colors.BOLD}{Colors.BLUE}========================================================================={Colors.END}\n")

    ts = int(time.time())
    admin_username = f"admin_test_{ts}"
    admin_password = "Password123!"

    # =========================================================================
    # PRE-SETUP: Buat Akun Admin untuk Testing
    # =========================================================================
    print(f"{Colors.BOLD}[SETUP] Membuat akun Admin untuk pengujian ({admin_username})...{Colors.END}")
    # Login dulu dengan bootstrap admin untuk mendapatkan token pembuatan user
    code_boot, res_boot, _ = run_curl("POST", "/api/auth/login", data={"username": "perf_admin", "password": "Password123!"})
    boot_header = {}
    if code_boot == 200 and res_boot and res_boot.get("data"):
        boot_header = {"Authorization": f"Bearer {res_boot['data']['accessToken']}"}

    setup_req = {
        "name": "Super Admin Test",
        "username": admin_username,
        "password": admin_password,
        "phone": "081234567890",
        "address": "Jakarta Barat",
        "role": "SUPER_ADMIN"
    }
    code, json_res, raw = run_curl("POST", "/api/v1/users", headers=boot_header, multipart={"request": setup_req})
    if code not in [200, 201]:
        print(f"{Colors.RED}Setup user gagal ({code}): {raw}{Colors.END}")
        sys.exit(1)
    admin_user_id = json_res.get("data", {}).get("id")
    print(f"  User Admin created successfully with ID: {admin_user_id}\n")

    # =========================================================================
    # TEST-01: AUTHENTICATION
    # =========================================================================
    print(f"{Colors.BOLD}{Colors.YELLOW}--- [TEST-01] Testing Fitur Autentikasi & Sesi ---{Colors.END}")

    # TC-AUTH-01: Login Berhasil
    login_req = {"username": admin_username, "password": admin_password, "deviceInfo": "Test Runner Mac"}
    code, res, _ = run_curl("POST", "/api/auth/login", data=login_req)
    access_token = res.get("data", {}).get("accessToken") if res and res.get("data") else None
    refresh_token = res.get("data", {}).get("refreshToken") if res and res.get("data") else None
    pass_01 = (code == 200 and access_token is not None and refresh_token is not None)
    log_test("TC-AUTH-01", "Login Berhasil (Happy Path)", pass_01, code, 200)

    # TC-AUTH-02: Login Gagal - Password Salah
    code, res, _ = run_curl("POST", "/api/auth/login", data={"username": admin_username, "password": "WrongPassword123!"})
    log_test("TC-AUTH-02", "Login Gagal - Password Salah", code == 403, code, 403)

    # TC-AUTH-03: Login Gagal - User Tidak Ditemukan
    code, res, _ = run_curl("POST", "/api/auth/login", data={"username": f"non_existent_{ts}", "password": "Password123!"})
    log_test("TC-AUTH-03", "Login Gagal - User Tidak Ditemukan", code == 404, code, 404)

    # TC-AUTH-04: Login Gagal - Request Kosong
    code, res, _ = run_curl("POST", "/api/auth/login", data={"username": "", "password": ""})
    log_test("TC-AUTH-04", "Login Gagal - Validasi Request Kosong", code == 400, code, 400)

    # TC-AUTH-05: Refresh Token Berhasil
    code, res, _ = run_curl("POST", "/api/auth/refresh-token", data={"refreshToken": refresh_token})
    new_access_token = res.get("data", {}).get("accessToken") if res and res.get("data") else None
    log_test("TC-AUTH-05", "Refresh Token Berhasil", (code == 200 and new_access_token is not None), code, 200)
    if new_access_token:
        access_token = new_access_token

    # TC-AUTH-06: Refresh Token Gagal - Token Palsu
    code, res, _ = run_curl("POST", "/api/auth/refresh-token", data={"refreshToken": "fake.refresh.token"})
    log_test("TC-AUTH-06", "Refresh Token Gagal - Token Palsu", code == 401, code, 401)

    # TC-AUTH-07: Akses Endpoint Terproteksi Tanpa Bearer Token
    code, res, _ = run_curl("GET", "/api/v1/customers")
    log_test("TC-AUTH-07", "Akses Protected Endpoint Tanpa Bearer Token", code in [401, 403], code, "401/403")

    # TC-AUTH-08: Ambil Profil Pengguna Terotentikasi (GET /api/auth/me)
    auth_header = {"Authorization": f"Bearer {access_token}"}
    code, res, _ = run_curl("GET", "/api/auth/me", headers=auth_header)
    retrieved_username = res.get("data", {}).get("username") if res and res.get("data") else None
    log_test("TC-AUTH-08", "Get Authenticated User (/api/auth/me)", (code == 200 and retrieved_username == admin_username), code, 200)

    # TC-AUTH-09: Logout Sesi
    # Kita login user temporary untuk ditest logout agar token admin tetap aktif
    temp_login_req = {"username": admin_username, "password": admin_password, "deviceInfo": "Logout Test"}
    _, temp_res, _ = run_curl("POST", "/api/auth/login", data=temp_login_req)
    temp_token = temp_res.get("data", {}).get("accessToken")
    code, _, _ = run_curl("POST", "/api/auth/logout", headers={"Authorization": f"Bearer {temp_token}"})
    # Verifikasi setelah logout harus 401
    verify_code, _, _ = run_curl("GET", "/api/auth/me", headers={"Authorization": f"Bearer {temp_token}"})
    log_test("TC-AUTH-09", "Logout Sesi & Verifikasi Inactive", (code == 200 and verify_code == 401), code, 200)

    # =========================================================================
    # TEST-02: CRUD CUSTOMER
    # =========================================================================
    print(f"\n{Colors.BOLD}{Colors.YELLOW}--- [TEST-02] Testing Fitur CRUD Customer ---{Colors.END}")

    # TC-CUST-01: Tambah Customer Baru Berhasil
    cust_req = {"name": f"Customer Test {ts}", "phone": "081234567890", "address": "Jl. Mawar No. 10"}
    code, res, _ = run_curl("POST", "/api/v1/customers", headers=auth_header, data=cust_req)
    customer_id = res.get("data", {}).get("id") if res and res.get("data") else None
    log_test("TC-CUST-01", "Tambah Customer Baru Berhasil", (code == 200 and customer_id is not None), code, 200)

    # TC-CUST-02: Tambah Customer Gagal - Nama Kosong
    code, res, _ = run_curl("POST", "/api/v1/customers", headers=auth_header, data={"name": "", "phone": "08123"})
    log_test("TC-CUST-02", "Tambah Customer Gagal - Nama Kosong", code == 400, code, 400)

    # TC-CUST-03: Ambil Data Customer by ID
    code, res, _ = run_curl("GET", f"/api/v1/customers/{customer_id}", headers=auth_header)
    log_test("TC-CUST-03", "Ambil Data Customer by ID", (code == 200 and res.get("data", {}).get("id") == customer_id), code, 200)

    # TC-CUST-04: Ambil Customer ID Tidak Ada
    code, res, _ = run_curl("GET", "/api/v1/customers/00000000-0000-0000-0000-000000000000", headers=auth_header)
    log_test("TC-CUST-04", "Ambil Customer - ID Tidak Ditemukan", code == 404, code, 404)

    # TC-CUST-05: Daftar Customer Berhalaman & Filter
    code, res, _ = run_curl("GET", f"/api/v1/customers?page=0&size=5&name=Customer", headers=auth_header)
    has_content = "content" in res.get("data", {}) if res and res.get("data") else False
    log_test("TC-CUST-05", "Daftar Customer Pagination & Filter", (code == 200 and has_content), code, 200)

    # TC-CUST-06: Update Customer
    cust_update = {"name": f"Customer Test Updated {ts}", "phone": "089999999999", "address": "Jl. Melati No. 99"}
    code, res, _ = run_curl("PUT", f"/api/v1/customers/{customer_id}", headers=auth_header, data=cust_update)
    log_test("TC-CUST-06", "Perbarui Data Customer (Update)", (code == 200 and res.get("data", {}).get("phone") == "089999999999"), code, 200)

    # TC-CUST-07: Hapus Customer
    # Kita buat customer dummy khusus untuk dihapus agar customer_id tetap bisa dipakai untuk transaksi
    _, c_del_res, _ = run_curl("POST", "/api/v1/customers", headers=auth_header, data={"name": "Temp Delete", "phone": "08111"})
    temp_cust_id = c_del_res.get("data", {}).get("id")
    code, _, _ = run_curl("DELETE", f"/api/v1/customers/{temp_cust_id}", headers=auth_header)
    verify_code, _, _ = run_curl("GET", f"/api/v1/customers/{temp_cust_id}", headers=auth_header)
    log_test("TC-CUST-07", "Hapus Data Customer (Delete & Verify 404)", (code == 200 and verify_code == 404), code, 200)

    # =========================================================================
    # TEST-03: CRUD USER & RESET PASSWORD
    # =========================================================================
    print(f"\n{Colors.BOLD}{Colors.YELLOW}--- [TEST-03] Testing Fitur CRUD User & Reset Password ---{Colors.END}")

    test_user_uname = f"staff_{ts}"
    user_req = {
        "name": "Staf Pengujian",
        "username": test_user_uname,
        "password": "Password123!",
        "phone": "081234445555",
        "address": "Bandung",
        "role": "STAFF"
    }

    # TC-USER-01: Tambah User Baru Berhasil dengan Foto (Multipart)
    code, res, _ = run_curl("POST", "/api/v1/users", headers=auth_header, multipart={
        "request": user_req,
        "file": (VALID_IMAGE, "image/png")
    })
    created_user_id = res.get("data", {}).get("id") if res and res.get("data") else None
    log_test("TC-USER-01", "Tambah User Baru dengan Foto (Multipart)", (code == 200 and created_user_id is not None), code, 200)

    # TC-USER-02: Tambah User Gagal - Password Lemah
    weak_user_req = {"name": "Weak", "username": f"weak_{ts}", "password": "123", "phone": "08123"}
    code, res, _ = run_curl("POST", "/api/v1/users", headers=auth_header, multipart={"request": weak_user_req})
    log_test("TC-USER-02", "Tambah User Gagal - Format Password Lemah", code == 400, code, 400)

    # TC-USER-03: Tambah User Gagal - Username Duplikat
    code, res, _ = run_curl("POST", "/api/v1/users", headers=auth_header, multipart={"request": user_req})
    log_test("TC-USER-03", "Tambah User Gagal - Username Duplikat", code == 409, code, 409)

    # TC-USER-04: Ambil User by ID
    code, res, _ = run_curl("GET", f"/api/v1/users/{created_user_id}", headers=auth_header)
    log_test("TC-USER-04", "Ambil Data User by ID", (code == 200 and res.get("data", {}).get("username") == test_user_uname), code, 200)

    # TC-USER-05: Update User Profile & Foto
    user_update_req = {
        "name": "Staf Pengujian Updated",
        "username": test_user_uname,
        "phone": "081299998888",
        "address": "Jakarta Baru",
        "role": "ADMIN"
    }
    code, res, _ = run_curl("PUT", f"/api/v1/users/{created_user_id}", headers=auth_header, multipart={
        "request": user_update_req,
        "file": (VALID_IMAGE, "image/png")
    })
    log_test("TC-USER-05", "Update User Profile & Ganti Foto", (code == 200 and res.get("data", {}).get("name") == "Staf Pengujian Updated"), code, 200)

    # TC-USER-06: Reset Password Berhasil
    reset_req = {"oldPassword": "Password123!", "newPassword": "PasswordBaru456@"}
    code, res, _ = run_curl("PATCH", f"/api/v1/users/{created_user_id}", headers=auth_header, data=reset_req)
    # Verifikasi login dengan password baru
    login_new_code, _, _ = run_curl("POST", "/api/auth/login", data={"username": test_user_uname, "password": "PasswordBaru456@"})
    log_test("TC-USER-06", "Reset Password Berhasil & Verifikasi Login", (code == 200 and login_new_code == 200), code, 200)

    # TC-USER-07: Reset Password Gagal - Password Lama Salah
    code, res, _ = run_curl("PATCH", f"/api/v1/users/{created_user_id}", headers=auth_header, data={"oldPassword": "WrongPassword999!", "newPassword": "AnotherPassword123!"})
    log_test("TC-USER-07", "Reset Password Gagal - Password Lama Salah", code == 403, code, 403)

    # TC-USER-08: Hapus User
    _, temp_u_res, _ = run_curl("POST", "/api/v1/users", headers=auth_header, multipart={"request": {"name": "Temp", "username": f"del_{ts}", "password": "Password123!", "phone": "08111"}})
    temp_u_id = temp_u_res.get("data", {}).get("id") if temp_u_res and temp_u_res.get("data") else None
    code, _, _ = run_curl("DELETE", f"/api/v1/users/{temp_u_id}", headers=auth_header)
    verify_u_code, _, _ = run_curl("GET", f"/api/v1/users/{temp_u_id}", headers=auth_header)
    log_test("TC-USER-08", "Hapus Data User (Delete & Verify 404)", (code == 200 and verify_u_code == 404), code, 200)

    # =========================================================================
    # TEST-04: CRUD BENEFIT
    # =========================================================================
    print(f"\n{Colors.BOLD}{Colors.YELLOW}--- [TEST-04] Testing Fitur CRUD Benefit ---{Colors.END}")

    benefit_name = f"Parfum Lavender Premium {ts}"
    # TC-BEN-01: Tambah Benefit Baru Berhasil
    code, res, _ = run_curl("POST", "/api/v1/benefits", headers=auth_header, data={"name": benefit_name})
    benefit_id = res.get("data", {}).get("id") if res and res.get("data") else None
    log_test("TC-BEN-01", "Tambah Benefit Baru Berhasil", (code == 200 and benefit_id is not None), code, 200)

    # TC-BEN-02: Tambah Benefit Gagal - Nama Duplikat
    code, res, _ = run_curl("POST", "/api/v1/benefits", headers=auth_header, data={"name": benefit_name})
    log_test("TC-BEN-02", "Tambah Benefit Gagal - Nama Duplikat", code == 409, code, 409)

    # TC-BEN-03: Tambah Benefit Gagal - Nama Kosong
    code, res, _ = run_curl("POST", "/api/v1/benefits", headers=auth_header, data={"name": ""})
    log_test("TC-BEN-03", "Tambah Benefit Gagal - Nama Kosong", code == 400, code, 400)

    # TC-BEN-04: Ambil Benefit by ID
    code, res, _ = run_curl("GET", f"/api/v1/benefits/{benefit_id}", headers=auth_header)
    log_test("TC-BEN-04", "Ambil Data Benefit by ID", (code == 200 and res.get("data", {}).get("id") == benefit_id), code, 200)

    # TC-BEN-05: Daftar Benefit dengan Filter Nama
    code, res, _ = run_curl("GET", f"/api/v1/benefits?name=Lavender", headers=auth_header)
    log_test("TC-BEN-05", "Daftar Benefit dengan Filter Nama", (code == 200 and "content" in res.get("data", {})), code, 200)

    # TC-BEN-06: Perbarui Benefit
    code, res, _ = run_curl("PUT", f"/api/v1/benefits/{benefit_id}", headers=auth_header, data={"name": f"{benefit_name} V2"})
    log_test("TC-BEN-06", "Perbarui Benefit (Update)", (code == 200 and res.get("data", {}).get("name") == f"{benefit_name} V2"), code, 200)

    # TC-BEN-07: Hapus Benefit
    _, b_temp_res, _ = run_curl("POST", "/api/v1/benefits", headers=auth_header, data={"name": f"Temp Benefit {ts}"})
    temp_b_id = b_temp_res.get("data", {}).get("id")
    code, _, _ = run_curl("DELETE", f"/api/v1/benefits/{temp_b_id}", headers=auth_header)
    verify_b_code, _, _ = run_curl("GET", f"/api/v1/benefits/{temp_b_id}", headers=auth_header)
    log_test("TC-BEN-07", "Hapus Benefit (Delete & Verify 404)", (code == 200 and verify_b_code == 404), code, 200)

    # =========================================================================
    # TEST-05: CRUD TRANSACTION & DETAIL
    # =========================================================================
    print(f"\n{Colors.BOLD}{Colors.YELLOW}--- [TEST-05] Testing Fitur Transaksi & Detail Pesanan ---{Colors.END}")

    # Buat Paket 1 & 2 untuk prasyarat transaksi
    pkg1_req = {
        "name": f"Cuci Kilat 1 Hari {ts}",
        "description": "Paket Cuci Kering Setrika 1 Hari",
        "type": "WEIGHT",
        "price": 10000,
        "daysEstimation": 1,
        "benefitIds": [benefit_id]
    }
    _, p1_res, _ = run_curl("POST", "/api/v1/packages", headers=auth_header, multipart={"request": pkg1_req})
    package_id_1 = p1_res.get("data", {}).get("id")

    pkg2_req = {
        "name": f"Bed Cover Satuan {ts}",
        "description": "Cuci Bed Cover Ukuran King",
        "type": "PCS",
        "price": 25000,
        "daysEstimation": 2,
        "benefitIds": [benefit_id]
    }
    _, p2_res, _ = run_curl("POST", "/api/v1/packages", headers=auth_header, multipart={"request": pkg2_req})
    package_id_2 = p2_res.get("data", {}).get("id")

    # TC-TRX-01: Buat Transaksi Baru Berhasil (Happy Path)
    # 2 Detail: Paket 1 (qty 3 * 10000 = 30000), Paket 2 (qty 2 * 25000 = 50000). Total: 80000.
    trx_req = {
        "customerId": customer_id,
        "userId": admin_user_id,
        "orderDate": "2026-10-25T10:00:00",
        "targetDate": "2026-10-28T10:00:00",
        "status": "IN_PROGRESS",
        "transactionDetails": [
            {"packageId": package_id_1, "quantity": 3, "orderDate": "2026-10-25T10:00:00"},
            {"packageId": package_id_2, "quantity": 2, "orderDate": "2026-10-25T10:00:00"}
        ]
    }
    code, res, _ = run_curl("POST", "/api/v1/transactions", headers=auth_header, multipart={
        "request": trx_req,
        "paymentReceiptFile": (VALID_IMAGE, "image/png"),
        "conditionBeforeFiles": [(VALID_IMAGE, "image/png")]
    })
    trx_data = res.get("data", {}) if res else {}
    transaction_id = trx_data.get("id")
    invoice_no = trx_data.get("invoiceNo")
    total_price = trx_data.get("totalPrice")
    details = trx_data.get("transactionDetails", [])
    
    pass_trx_01 = (code == 200 and transaction_id is not None and invoice_no is not None and total_price == 80000.0 and len(details) == 2)
    log_test("TC-TRX-01", "Buat Transaksi Baru (Kalkulasi Harga & Invoice Unik)", pass_trx_01, code, 200)

    # TC-TRX-02: Buat Transaksi Gagal - Customer Tidak Ada
    invalid_cust_trx = dict(trx_req)
    invalid_cust_trx["customerId"] = "00000000-0000-0000-0000-000000000000"
    code, res, _ = run_curl("POST", "/api/v1/transactions", headers=auth_header, multipart={"request": invalid_cust_trx})
    log_test("TC-TRX-02", "Buat Transaksi Gagal - Customer ID Tidak Ditemukan", code == 404, code, 404)

    # TC-TRX-03: Buat Transaksi Gagal - Tanggal Masa Lalu
    past_trx = dict(trx_req)
    past_trx["orderDate"] = "2020-01-01T00:00:00"
    code, res, _ = run_curl("POST", "/api/v1/transactions", headers=auth_header, multipart={"request": past_trx})
    log_test("TC-TRX-03", "Buat Transaksi Gagal - Tanggal Order Masa Lampau", code == 400, code, 400)

    # TC-TRX-04: Ambil Detail Transaksi by ID
    code, res, _ = run_curl("GET", f"/api/v1/transactions/{transaction_id}", headers=auth_header)
    log_test("TC-TRX-04", "Ambil Detail Transaksi by ID", (code == 200 and res.get("data", {}).get("id") == transaction_id), code, 200)

    # TC-TRX-05: Update Status Transaksi ke DONE
    update_trx_req = {
        "customerId": customer_id,
        "userId": admin_user_id,
        "orderDate": "2026-10-25T10:00:00",
        "targetDate": "2026-10-28T10:00:00",
        "status": "DONE",
        "transactionDetails": [
            {"id": details[0]["id"], "packageId": package_id_1, "quantity": 4, "orderDate": "2026-10-25T10:00:00"},
            {"id": details[1]["id"], "packageId": package_id_2, "quantity": 2, "orderDate": "2026-10-25T10:00:00"}
        ]
    }
    code, res, _ = run_curl("PUT", f"/api/v1/transactions/{transaction_id}", headers=auth_header, data=update_trx_req)
    # Total baru: (4 * 10000) + (2 * 25000) = 90000
    pass_trx_05 = (code == 200 and res.get("data", {}).get("status") == "DONE" and res.get("data", {}).get("totalPrice") == 90000.0)
    log_test("TC-TRX-05", "Update Status Pesanan ke DONE & Recalculate Harga", pass_trx_05, code, 200)

    # TC-TRX-06: Hapus Item Detail Transaksi (dari 2 item -> 1 item)
    detail_to_delete_id = details[0]["id"]
    code, _, _ = run_curl("DELETE", f"/api/v1/transaction-details/{detail_to_delete_id}", headers=auth_header)
    # Cek transaksi setelah dihapus 1 detail
    _, trx_after_del, _ = run_curl("GET", f"/api/v1/transactions/{transaction_id}", headers=auth_header)
    remaining_details = trx_after_del.get("data", {}).get("transactionDetails", [])
    log_test("TC-TRX-06", "Hapus 1 Item Detail Transaksi (Total Tagihan Berkurang)", (code == 200 and len(remaining_details) == 1), code, 200)

    # TC-TRX-07: Hapus Item Detail Terakhir Gagal - Melanggar Aturan Bisnis (Min 1 Detail)
    last_detail_id = remaining_details[0]["id"] if remaining_details else "dummy"
    code, res, _ = run_curl("DELETE", f"/api/v1/transaction-details/{last_detail_id}", headers=auth_header)
    log_test("TC-TRX-07", "Hapus Detail Terakhir Gagal (Proteksi Minimal 1 Detail)", code == 409, code, 409)

    # TC-TRX-08: Hapus Transaksi & Cascade MinIO
    # Buat transaksi terpisah untuk dihapus
    _, temp_trx_res, _ = run_curl("POST", "/api/v1/transactions", headers=auth_header, multipart={"request": trx_req})
    temp_trx_id = temp_trx_res.get("data", {}).get("id")
    code, _, _ = run_curl("DELETE", f"/api/v1/transactions/{temp_trx_id}", headers=auth_header)
    verify_trx_code, _, _ = run_curl("GET", f"/api/v1/transactions/{temp_trx_id}", headers=auth_header)
    log_test("TC-TRX-08", "Hapus Transaksi Utama & Bersihkan File (Delete & Verify 404)", (code == 200 and verify_trx_code == 404), code, 200)

    # =========================================================================
    # TEST-06: CRUD TRANSACTION FILE
    # =========================================================================
    print(f"\n{Colors.BOLD}{Colors.YELLOW}--- [TEST-06] Testing Fitur Berkas & Bukti Transaksi (MinIO) ---{Colors.END}")

    # TC-FILE-01: Upload Berkas Tambahan Berhasil
    file_upload_json = {"transactionId": transaction_id}
    code, res, _ = run_curl("POST", "/api/v1/transaction-files", headers=auth_header, multipart={
        "request": file_upload_json,
        "paymentReceiptFile": (VALID_IMAGE, "image/png"),
        "invoiceDocumentFile": (VALID_IMAGE, "image/png"),
        "conditionBeforeFiles": [(VALID_IMAGE, "image/png")],
        "conditionAfterFiles": [(VALID_IMAGE, "image/png")],
        "otherFiles": [(VALID_IMAGE, "image/png")]
    })
    uploaded_files = res.get("data", []) if res else []
    log_test("TC-FILE-01", "Upload Berkas Tambahan Transaksi ke MinIO (Happy Path)", (code == 200 and len(uploaded_files) > 0), code, 200)

    # TC-FILE-02: Upload Berkas Gagal - Transaksi ID Tidak Ada
    code, res, _ = run_curl("POST", "/api/v1/transaction-files", headers=auth_header, multipart={
        "request": {"transactionId": "00000000-0000-0000-0000-000000000000"},
        "paymentReceiptFile": (VALID_IMAGE, "image/png"),
        "invoiceDocumentFile": (VALID_IMAGE, "image/png"),
        "conditionBeforeFiles": [(VALID_IMAGE, "image/png")],
        "conditionAfterFiles": [(VALID_IMAGE, "image/png")],
        "otherFiles": [(VALID_IMAGE, "image/png")]
    })
    log_test("TC-FILE-02", "Upload Berkas Gagal - Transaksi ID Tidak Ditemukan", code == 404, code, 404)

    # TC-FILE-03: Upload Berkas Gagal - Format File Bukan Gambar (.txt)
    code, res, _ = run_curl("POST", "/api/v1/transaction-files", headers=auth_header, multipart={
        "request": file_upload_json,
        "paymentReceiptFile": (INVALID_FILE, "text/plain"),
        "invoiceDocumentFile": (VALID_IMAGE, "image/png"),
        "conditionBeforeFiles": [(VALID_IMAGE, "image/png")],
        "conditionAfterFiles": [(VALID_IMAGE, "image/png")],
        "otherFiles": [(VALID_IMAGE, "image/png")]
    })
    log_test("TC-FILE-03", "Upload Berkas Gagal - Format File Non-Image Ditolak (@ValidImage)", code == 400, code, 400)

    # TC-FILE-04: Upload Berkas Gagal - Ukuran File Melebihi 3MB
    code, res, _ = run_curl("POST", "/api/v1/transaction-files", headers=auth_header, multipart={
        "request": file_upload_json,
        "paymentReceiptFile": (OVERSIZED_FILE, "image/jpeg"),
        "invoiceDocumentFile": (VALID_IMAGE, "image/png"),
        "conditionBeforeFiles": [(VALID_IMAGE, "image/png")],
        "conditionAfterFiles": [(VALID_IMAGE, "image/png")],
        "otherFiles": [(VALID_IMAGE, "image/png")]
    })
    log_test("TC-FILE-04", "Upload Berkas Gagal - Ukuran Melebihi Batas 3MB", code == 400, code, 400)

    # TC-FILE-05: Hapus Satu Berkas Transaksi (Single Delete)
    if uploaded_files:
        file_to_del_id = uploaded_files[0]["id"]
        code, _, _ = run_curl("DELETE", f"/api/v1/transaction-files/{file_to_del_id}", headers=auth_header)
        log_test("TC-FILE-05", "Hapus Satu Berkas Transaksi (Single Delete dari DB & MinIO)", code == 200, code, 200)
    else:
        log_test("TC-FILE-05", "Hapus Satu Berkas Transaksi (Single Delete)", False, 0, 200, "Tidak ada file dari TC-FILE-01")

    # TC-FILE-06: Bulk Delete Sekumpulan Berkas Transaksi
    if len(uploaded_files) >= 3:
        bulk_ids = [uploaded_files[1]["id"], uploaded_files[2]["id"]]
        code, res, _ = run_curl("DELETE", "/api/v1/transaction-files/list", headers=auth_header, data={"transactionFileIds": bulk_ids})
        log_test("TC-FILE-06", "Hapus Sekumpulan Berkas Sekaligus (Bulk Delete)", code == 200, code, 200)
    else:
        log_test("TC-FILE-06", "Hapus Sekumpulan Berkas Sekaligus (Bulk Delete)", False, 0, 200, "Tidak cukup file untuk bulk delete")

    # TC-FILE-07: Hapus Berkas Gagal - File ID Tidak Ditemukan
    code, res, _ = run_curl("DELETE", "/api/v1/transaction-files/00000000-0000-0000-0000-000000000000", headers=auth_header)
    log_test("TC-FILE-07", "Hapus Berkas Gagal - File ID Tidak Ditemukan", code == 404, code, 404)

    # =========================================================================
    # TEST-07: ROLE-BASED ACCESS CONTROL (RBAC) & PERMISSIONS
    # =========================================================================
    print(f"\n{Colors.BOLD}{Colors.YELLOW}--- [TEST-07] Testing Hak Akses & RBAC (Role-Based Access Control) ---{Colors.END}")

    # Buat akun STAFF khusus untuk pengujian otorisasi
    staff_username = f"staff_rbac_{ts}"
    staff_password = "Password123!"
    code, staff_create_res, _ = run_curl("POST", "/api/v1/users", headers=auth_header, multipart={"request": {
        "name": "Staff RBAC Test",
        "username": staff_username,
        "password": staff_password,
        "phone": "081299998888",
        "address": "Jakarta Selatan",
        "role": "STAFF"
    }})
    staff_id = staff_create_res.get("data", {}).get("id") if staff_create_res and staff_create_res.get("data") else None

    # Login sebagai STAFF
    code, staff_login_res, _ = run_curl("POST", "/api/auth/login", data={"username": staff_username, "password": staff_password})
    staff_token = staff_login_res.get("data", {}).get("accessToken") if staff_login_res and staff_login_res.get("data") else None
    staff_header = {"Authorization": f"Bearer {staff_token}"} if staff_token else {}

    # TC-RBAC-01: User role STAFF ditolak saat POST /api/v1/users
    code, _, _ = run_curl("POST", "/api/v1/users", headers=staff_header, multipart={"request": {
        "name": "Hacker User", "username": f"hack_{ts}", "password": "Password123!", "phone": "081234567890", "address": "Jkt", "role": "ADMIN"
    }})
    log_test("TC-RBAC-01", "STAFF Ditolak Saat Tambah User Baru (Harus 403)", code == 403, code, 403)

    # TC-RBAC-02: User role STAFF ditolak saat PUT /api/v1/users/{id}
    code, _, _ = run_curl("PUT", f"/api/v1/users/{admin_user_id}", headers=staff_header, multipart={"request": {
        "name": "Updated By Staff", "username": admin_username, "phone": "081234567890", "address": "Jkt", "role": "ADMIN"
    }})
    log_test("TC-RBAC-02", "STAFF Ditolak Saat Update User (Harus 403)", code == 403, code, 403)

    # TC-RBAC-03: User role STAFF ditolak saat DELETE /api/v1/users/{id}
    code, _, _ = run_curl("DELETE", f"/api/v1/users/{admin_user_id}", headers=staff_header)
    log_test("TC-RBAC-03", "STAFF Ditolak Saat Hapus User (Harus 403)", code == 403, code, 403)

    # TC-RBAC-04: User role STAFF ditolak saat PATCH /api/v1/users/{id} (Reset Password)
    code, _, _ = run_curl("PATCH", f"/api/v1/users/{admin_user_id}", headers=staff_header, data={
        "oldPassword": "Password123!",
        "newPassword": "HackedPassword123!"
    })
    log_test("TC-RBAC-04", "STAFF Ditolak Saat Reset Password User (Harus 403)", code == 403, code, 403)

    # TC-RBAC-05: User role STAFF ditolak saat POST /api/v1/packages
    code, _, _ = run_curl("POST", "/api/v1/packages", headers=staff_header, multipart={"request": {
        "name": "Paket Ilegal", "description": "Deskripsi Paket", "price": 10000, "type": "WEIGHT", "daysEstimation": 2, "benefitIds": [benefit_id]
    }})
    log_test("TC-RBAC-05", "STAFF Ditolak Saat Tambah Paket Layanan (Harus 403)", code == 403, code, 403)

    # TC-RBAC-06: User role STAFF ditolak saat POST /api/v1/benefits
    code, _, _ = run_curl("POST", "/api/v1/benefits", headers=staff_header, data={"name": f"Benefit Ilegal {ts}"})
    log_test("TC-RBAC-06", "STAFF Ditolak Saat Tambah Benefit Layanan (Harus 403)", code == 403, code, 403)

    # TC-RBAC-07: User role ADMIN berhasil akses GET /api/v1/users
    code, _, _ = run_curl("GET", "/api/v1/users?page=0&size=5", headers=auth_header)
    log_test("TC-RBAC-07", "ADMIN Berhasil Melihat Daftar User (Harus 200)", code == 200, code, 200)

    # TC-RBAC-08: User role STAFF berhasil akses GET /api/v1/packages dan GET /api/v1/customers
    code_pkg, _, _ = run_curl("GET", "/api/v1/packages?page=0&size=5", headers=staff_header)
    code_cust, _, _ = run_curl("GET", "/api/v1/customers?page=0&size=5", headers=staff_header)
    pass_staff_read = (code_pkg == 200 and code_cust == 200)
    log_test("TC-RBAC-08", "STAFF Berhasil Melihat Paket & Pelanggan (Harus 200)", pass_staff_read, code_pkg, 200)

    # =========================================================================
    # SUMMARY REPORT
    # =========================================================================
    total_tests = len(results)
    passed_tests = sum(1 for r in results if r["passed"])
    failed_tests = total_tests - passed_tests

    print(f"\n{Colors.BOLD}{Colors.BLUE}========================================================================={Colors.END}")
    print(f"{Colors.BOLD}{Colors.BLUE}                       HASIL EKSEKUSI PENGUJIAN                          {Colors.END}")
    print(f"{Colors.BOLD}{Colors.BLUE}========================================================================={Colors.END}")
    print(f"Total Test Cases   : {total_tests}")
    print(f"Passed             : {Colors.GREEN}{passed_tests}{Colors.END}")
    print(f"Failed             : {Colors.RED if failed_tests > 0 else Colors.GREEN}{failed_tests}{Colors.END}")
    success_rate = (passed_tests / total_tests) * 100 if total_tests > 0 else 0
    print(f"Success Rate       : {Colors.BOLD}{success_rate:.2f}%{Colors.END}")
    print(f"{Colors.BOLD}{Colors.BLUE}========================================================================={Colors.END}\n")

    if failed_tests > 0:
        sys.exit(1)

if __name__ == "__main__":
    main()
