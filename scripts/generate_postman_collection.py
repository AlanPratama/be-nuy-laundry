#!/usr/bin/env python3
"""
Script to generate standard Postman Collection v2.1.0 and Environment JSON
for BE Nuy Laundry backend API.
"""

import os
import json
import uuid

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
POSTMAN_DIR = os.path.join(BASE_DIR, "postman")
ASSETS_DIR = os.path.join(POSTMAN_DIR, "test_assets")

def ensure_dirs():
    os.makedirs(ASSETS_DIR, exist_ok=True)
    # create a dummy 1x1 transparent png if not exists
    png_path = os.path.join(ASSETS_DIR, "sample_image.png")
    if not os.path.exists(png_path):
        import base64
        dummy_png = base64.b64decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        )
        with open(png_path, "wb") as f:
            f.write(dummy_png)

def generate_environment():
    env = {
        "id": str(uuid.uuid4()),
        "name": "BE Nuy Laundry Local",
        "values": [
            {"key": "baseUrl", "value": "http://localhost:8080", "type": "default", "enabled": True},
            {"key": "adminUsername", "value": "perf_admin", "type": "default", "enabled": True},
            {"key": "adminPassword", "value": "Password123!", "type": "secret", "enabled": True},
            {"key": "staffUsername", "value": "perf_staff", "type": "default", "enabled": True},
            {"key": "staffPassword", "value": "Password123!", "type": "secret", "enabled": True},
            {"key": "adminToken", "value": "", "type": "secret", "enabled": True},
            {"key": "staffToken", "value": "", "type": "secret", "enabled": True},
            {"key": "adminRefreshToken", "value": "", "type": "secret", "enabled": True},
            {"key": "adminUserId", "value": "", "type": "default", "enabled": True},
            {"key": "currentUserId", "value": "", "type": "default", "enabled": True},
            {"key": "customerId", "value": "", "type": "default", "enabled": True},
            {"key": "packageId", "value": "", "type": "default", "enabled": True},
            {"key": "benefitId", "value": "", "type": "default", "enabled": True},
            {"key": "transactionId", "value": "", "type": "default", "enabled": True},
            {"key": "invoiceNo", "value": "", "type": "default", "enabled": True},
            {"key": "transactionFileId", "value": "", "type": "default", "enabled": True},
            {"key": "transactionDetailId", "value": "", "type": "default", "enabled": True},
            {"key": "transactionDetailId2", "value": "", "type": "default", "enabled": True}
        ],
        "_postman_variable_scope": "environment",
        "_postman_exported_at": "2026-10-24T12:00:00.000Z",
        "_postman_exported_using": "Postman/10.0.0"
    }
    env_file = os.path.join(POSTMAN_DIR, "be-nuy-laundry.postman_environment.json")
    with open(env_file, "w") as f:
        json.dump(env, f, indent=2)
    print(f"Environment created: {env_file}")

def make_req(name, method, url_path, query_params=None, headers=None, body=None, auth_bearer=None, tests=None, description=""):
    req_headers = []
    if headers:
        for k, v in headers.items():
            req_headers.append({"key": k, "value": v, "type": "text"})
    
    url_obj = {
        "raw": "{{baseUrl}}" + url_path,
        "host": ["{{baseUrl}}"],
        "path": [p for p in url_path.split("/") if p]
    }
    if query_params:
        url_obj["query"] = [{"key": k, "value": v} for k, v in query_params.items()]
        raw_query = "&".join([f"{k}={v}" for k, v in query_params.items()])
        url_obj["raw"] += "?" + raw_query
    
    req_item = {
        "name": name,
        "request": {
            "method": method,
            "header": req_headers,
            "url": url_obj,
            "description": description
        },
        "response": []
    }
    
    if auth_bearer:
        req_item["request"]["auth"] = {
            "type": "bearer",
            "bearer": [{"key": "token", "value": auth_bearer, "type": "string"}]
        }
    
    if body:
        req_item["request"]["body"] = body

    if tests:
        req_item["event"] = [
            {
                "listen": "test",
                "script": {
                    "type": "text/javascript",
                    "exec": tests
                }
            }
        ]
    return req_item

def build_collection():
    items = []

    # -------------------------------------------------------------
    # FOLDER 01: Authentication & Profile
    # -------------------------------------------------------------
    auth_items = []

    # Login as Admin
    login_admin_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Admin token captured successfully', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data).to.have.property('accessToken');",
        "    pm.environment.set('adminToken', res.data.accessToken);",
        "    if (res.data.refreshToken) {",
        "        pm.environment.set('adminRefreshToken', res.data.refreshToken);",
        "    }",
        "});"
    ]
    auth_items.append(make_req(
        name="01. Login as Admin",
        method="POST",
        url_path="/api/auth/login",
        headers={"Content-Type": "application/json"},
        body={"mode": "raw", "raw": json.dumps({"username": "{{adminUsername}}", "password": "{{adminPassword}}"}, indent=2)},
        tests=login_admin_tests,
        description="Otentikasi pengguna admin dan otomatis simpan Bearer JWT ke variabel {{adminToken}}."
    ))

    # Login as Staff
    login_staff_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Staff token captured successfully', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data).to.have.property('accessToken');",
        "    pm.environment.set('staffToken', res.data.accessToken);",
        "});"
    ]
    auth_items.append(make_req(
        name="02. Login as Staff",
        method="POST",
        url_path="/api/auth/login",
        headers={"Content-Type": "application/json"},
        body={"mode": "raw", "raw": json.dumps({"username": "{{staffUsername}}", "password": "{{staffPassword}}"}, indent=2)},
        tests=login_staff_tests,
        description="Otentikasi staf operasional dan otomatis simpan Bearer JWT ke variabel {{staffToken}}."
    ))

    # Get Current Profile
    me_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Profile corresponds to logged in user', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data).to.have.property('username');",
        "    pm.expect(res.data.username).to.eql(pm.environment.get('adminUsername'));",
        "    pm.environment.set('adminUserId', res.data.id);",
        "});"
    ]
    auth_items.append(make_req(
        name="03. Get Current Profile (Me)",
        method="GET",
        url_path="/api/auth/me",
        auth_bearer="{{adminToken}}",
        tests=me_tests,
        description="Mendapatkan profil pengguna yang sedang login dan menyimpan UUID pengguna ke {{adminUserId}}."
    ))

    # Refresh Token
    refresh_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Token refreshed successfully', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data).to.have.property('accessToken');",
        "    pm.environment.set('adminToken', res.data.accessToken);",
        "});"
    ]
    auth_items.append(make_req(
        name="04. Refresh Token",
        method="POST",
        url_path="/api/auth/refresh-token",
        headers={"Content-Type": "application/json"},
        body={"mode": "raw", "raw": json.dumps({"refreshToken": "{{adminRefreshToken}}"}, indent=2)},
        tests=refresh_tests,
        description="Menerbitkan token akses baru menggunakan refresh token aktif."
    ))

    # RBAC Check: Unauthenticated Profile Access (401)
    unauth_tests = [
        "pm.test('Status code is 401 Unauthorized', function () {",
        "    pm.response.to.have.status(401);",
        "});",
        "pm.test('Error response status is false or FAILED', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.status).to.be.oneOf([false, 'FAILED']);",
        "});"
    ]
    auth_items.append(make_req(
        name="05. [Negative Test] Unauthenticated Profile Access (401)",
        method="GET",
        url_path="/api/auth/me",
        tests=unauth_tests,
        description="Memvalidasi bahwa akses ke endpoint privat tanpa header Authorization ditolak dengan HTTP 401."
    ))

    items.append({"name": "01. Authentication & Profile", "item": auth_items})

    # -------------------------------------------------------------
    # FOLDER 02: User Management (Admin Only)
    # -------------------------------------------------------------
    user_items = []

    create_user_payload = {
        "name": "Staf Operasional Demo",
        "username": "staf_demo_postman",
        "password": "Password123!",
        "phone": "081298765432",
        "address": "Jl. Melati No. 12 Jakarta",
        "role": "STAFF"
    }
    create_user_tests = [
        "pm.test('Status code is 200 or 201', function () {",
        "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
        "});",
        "pm.test('User created and ID stored in environment', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.be.a('string');",
        "    pm.environment.set('currentUserId', res.data.id);",
        "});"
    ]
    user_items.append(make_req(
        name="01. Create New Staff User",
        method="POST",
        url_path="/api/v1/users",
        auth_bearer="{{adminToken}}",
        body={
            "mode": "formdata",
            "formdata": [
                {"key": "request", "value": json.dumps(create_user_payload), "type": "text", "contentType": "application/json"}
            ]
        },
        tests=create_user_tests,
        description="Membuat staf pengguna baru via multipart/form-data (khusus Admin/Super Admin)."
    ))

    get_users_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Users list pagination structure valid', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.content).to.be.an('array');",
        "    pm.expect(res.data).to.have.property('totalElements');",
        "});"
    ]
    user_items.append(make_req(
        name="02. Get All Users (Paginated & Filter)",
        method="GET",
        url_path="/api/v1/users",
        query_params={"page": "0", "size": "10", "sortBy": "createdAt", "direction": "desc"},
        auth_bearer="{{adminToken}}",
        tests=get_users_tests,
        description="Mengambil daftar seluruh user dengan paginasi dan filter nama."
    ))

    get_user_by_id_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Returned user matches ID', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.eql(pm.environment.get('currentUserId'));",
        "});"
    ]
    user_items.append(make_req(
        name="03. Get User By ID",
        method="GET",
        url_path="/api/v1/users/{{currentUserId}}",
        auth_bearer="{{adminToken}}",
        tests=get_user_by_id_tests,
        description="Mengambil detail user berdasarkan UUID."
    ))

    update_user_payload = {
        "name": "Staf Operasional Diperbarui",
        "username": "staf_demo_postman",
        "phone": "081298765499",
        "address": "Jl. Melati No. 15 Jakarta",
        "role": "STAFF"
    }
    update_user_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('User name updated correctly', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.name).to.eql('Staf Operasional Diperbarui');",
        "});"
    ]
    user_items.append(make_req(
        name="04. Update User Profile",
        method="PUT",
        url_path="/api/v1/users/{{currentUserId}}",
        auth_bearer="{{adminToken}}",
        body={
            "mode": "formdata",
            "formdata": [
                {"key": "request", "value": json.dumps(update_user_payload), "type": "text", "contentType": "application/json"}
            ]
        },
        tests=update_user_tests,
        description="Memperbarui profil pengguna (nama, no telepon, alamat)."
    ))

    reset_pass_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Password reset success', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.message).to.be.a('string');",
        "});"
    ]
    user_items.append(make_req(
        name="05. Reset User Password",
        method="PATCH",
        url_path="/api/v1/users/{{currentUserId}}",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{adminToken}}",
        body={"mode": "raw", "raw": json.dumps({"oldPassword": "Password123!", "newPassword": "PasswordBaru456@"}, indent=2)},
        tests=reset_pass_tests,
        description="Mereset password user oleh administrator."
    ))

    # RBAC Test: Staff Create User (Forbidden 403)
    rbac_user_tests = [
        "pm.test('Status code is 403 Forbidden for Staff', function () {",
        "    pm.response.to.have.status(403);",
        "});",
        "pm.test('Access denied response structure is standard', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.status).to.be.oneOf([false, 'FAILED']);",
        "    pm.expect(res.message).to.be.a('string');",
        "});"
    ]
    user_items.append(make_req(
        name="06. [RBAC Check] Staff Create User (Forbidden 403)",
        method="POST",
        url_path="/api/v1/users",
        auth_bearer="{{staffToken}}",
        body={
            "mode": "formdata",
            "formdata": [
                {"key": "request", "value": json.dumps(create_user_payload), "type": "text", "contentType": "application/json"}
            ]
        },
        tests=rbac_user_tests,
        description="Memverifikasi bahwa role STAFF ditolak (HTTP 403) saat mencoba membuat pengguna."
    ))

    delete_user_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('User deletion success', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.message).to.be.a('string');",
        "});"
    ]
    user_items.append(make_req(
        name="07. Delete User",
        method="DELETE",
        url_path="/api/v1/users/{{currentUserId}}",
        auth_bearer="{{adminToken}}",
        tests=delete_user_tests,
        description="Menghapus data user staf."
    ))

    items.append({"name": "02. User Management (Admin Only)", "item": user_items})

    # -------------------------------------------------------------
    # FOLDER 03: Customer Management
    # -------------------------------------------------------------
    customer_items = []

    cust_payload = {
        "name": "Budi Santoso",
        "phone": "081234567890",
        "address": "Jl. Kemang Raya No. 45 Jakarta Selatan"
    }
    create_cust_tests = [
        "pm.test('Status code is 200 or 201', function () {",
        "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
        "});",
        "pm.test('Customer ID stored in environment', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.be.a('string');",
        "    pm.environment.set('customerId', res.data.id);",
        "});"
    ]
    customer_items.append(make_req(
        name="01. Create Customer",
        method="POST",
        url_path="/api/v1/customers",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{staffToken}}",
        body={"mode": "raw", "raw": json.dumps(cust_payload, indent=2)},
        tests=create_cust_tests,
        description="Mendaftarkan pelanggan laundry baru (dapat dilakukan oleh Staff)."
    ))

    get_cust_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Customer list returned', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.content).to.be.an('array');",
        "});"
    ]
    customer_items.append(make_req(
        name="02. Get All Customers (Paginated & Filter)",
        method="GET",
        url_path="/api/v1/customers",
        query_params={"page": "0", "size": "10", "sortBy": "createdAt", "direction": "desc", "name": "Budi"},
        auth_bearer="{{staffToken}}",
        tests=get_cust_tests,
        description="Mendapatkan daftar pelanggan dengan pencarian nama (didukung GIN trigram index)."
    ))

    get_cust_id_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Customer detail matches ID', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.eql(pm.environment.get('customerId'));",
        "});"
    ]
    customer_items.append(make_req(
        name="03. Get Customer By ID",
        method="GET",
        url_path="/api/v1/customers/{{customerId}}",
        auth_bearer="{{staffToken}}",
        tests=get_cust_id_tests,
        description="Mengambil detail profil pelanggan berdasarkan UUID."
    ))

    update_cust_payload = {
        "name": "Budi Santoso Diperbarui",
        "phone": "081234567899",
        "address": "Jl. Kemang Raya No. 46 Jakarta Selatan"
    }
    update_cust_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Customer updated successfully', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.phone).to.eql('081234567899');",
        "});"
    ]
    customer_items.append(make_req(
        name="04. Update Customer",
        method="PUT",
        url_path="/api/v1/customers/{{customerId}}",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{staffToken}}",
        body={"mode": "raw", "raw": json.dumps(update_cust_payload, indent=2)},
        tests=update_cust_tests,
        description="Memperbarui data kontak dan alamat pelanggan."
    ))

    items.append({"name": "03. Customer Management", "item": customer_items})

    # -------------------------------------------------------------
    # FOLDER 04: Package Benefit Master
    # -------------------------------------------------------------
    benefit_items = []

    benefit_payload = {
        "name": "Pewangi Premium 7 Hari"
    }
    create_benefit_tests = [
        "pm.test('Status code is 200 or 201', function () {",
        "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
        "});",
        "pm.test('Benefit ID stored in environment', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.be.a('string');",
        "    pm.environment.set('benefitId', res.data.id);",
        "});"
    ]
    benefit_items.append(make_req(
        name="01. Create Benefit (Admin)",
        method="POST",
        url_path="/api/v1/benefits",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{adminToken}}",
        body={"mode": "raw", "raw": json.dumps(benefit_payload, indent=2)},
        tests=create_benefit_tests,
        description="Menambahkan master benefit layanan laundry (khusus Admin)."
    ))

    get_benefits_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Benefits list returned', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.content).to.be.an('array');",
        "});"
    ]
    benefit_items.append(make_req(
        name="02. Get All Benefits (Cache-Aside)",
        method="GET",
        url_path="/api/v1/benefits",
        query_params={"page": "0", "size": "10", "sortBy": "createdAt", "direction": "desc"},
        auth_bearer="{{staffToken}}",
        tests=get_benefits_tests,
        description="Melihat seluruh daftar benefit (tersedia untuk seluruh staf, di-cache di Redis)."
    ))

    get_benefit_id_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Benefit detail matches ID', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.eql(pm.environment.get('benefitId'));",
        "});"
    ]
    benefit_items.append(make_req(
        name="03. Get Benefit By ID",
        method="GET",
        url_path="/api/v1/benefits/{{benefitId}}",
        auth_bearer="{{staffToken}}",
        tests=get_benefit_id_tests,
        description="Mengambil detail master benefit berdasarkan ID."
    ))

    update_benefit_payload = {
        "name": "Pewangi Premium 10 Hari (Updated)"
    }
    update_benefit_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Benefit updated and cache evicted', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.name).to.include('10 Hari');",
        "});"
    ]
    benefit_items.append(make_req(
        name="04. Update Benefit (Admin)",
        method="PUT",
        url_path="/api/v1/benefits/{{benefitId}}",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{adminToken}}",
        body={"mode": "raw", "raw": json.dumps(update_benefit_payload, indent=2)},
        tests=update_benefit_tests,
        description="Memperbarui data benefit dan memicu otomatis evict Redis cache."
    ))

    rbac_benefit_tests = [
        "pm.test('Status code is 403 Forbidden for Staff', function () {",
        "    pm.response.to.have.status(403);",
        "});",
        "pm.test('Access denied response structure is standard', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.status).to.be.oneOf([false, 'FAILED']);",
        "    pm.expect(res.message).to.be.a('string');",
        "});"
    ]
    benefit_items.append(make_req(
        name="05. [RBAC Check] Staff Mutasi Benefit (Forbidden 403)",
        method="POST",
        url_path="/api/v1/benefits",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{staffToken}}",
        body={"mode": "raw", "raw": json.dumps({"name": "Illegal Benefit"}, indent=2)},
        tests=rbac_benefit_tests,
        description="Memverifikasi bahwa peran STAFF dilarang membuat atau mengubah benefit master."
    ))

    items.append({"name": "04. Package Benefit Master", "item": benefit_items})

    # -------------------------------------------------------------
    # FOLDER 05: Laundry Package Master
    # -------------------------------------------------------------
    package_items = []

    package_payload = {
        "name": "Cuci Komplit Kilat 1 Hari",
        "price": 15000,
        "description": "Cuci bersih, setrika uap, dan packaging higienis",
        "type": "WEIGHT",
        "daysEstimation": 2,
        "benefitIds": ["{{benefitId}}"]
    }
    create_pkg_tests = [
        "pm.test('Status code is 200 or 201', function () {",
        "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
        "});",
        "pm.test('Package ID stored in environment', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.be.a('string');",
        "    pm.environment.set('packageId', res.data.id);",
        "});"
    ]
    package_items.append(make_req(
        name="01. Create Package (Admin)",
        method="POST",
        url_path="/api/v1/packages",
        auth_bearer="{{adminToken}}",
        body={
            "mode": "formdata",
            "formdata": [
                {"key": "request", "value": json.dumps(package_payload), "type": "text", "contentType": "application/json"}
            ]
        },
        tests=create_pkg_tests,
        description="Menambahkan paket laundry baru dengan relasi benefit dan upload gambar ikon."
    ))

    get_pkgs_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Packages list returned with benefits', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.content).to.be.an('array');",
        "});"
    ]
    package_items.append(make_req(
        name="02. Get All Packages (Paginated & Cached)",
        method="GET",
        url_path="/api/v1/packages",
        query_params={"page": "0", "size": "10", "sortBy": "createdAt", "direction": "desc"},
        auth_bearer="{{staffToken}}",
        tests=get_pkgs_tests,
        description="Melihat seluruh paket laundry (di-cache di Redis, query bebas N+1)."
    ))

    get_pkg_id_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Package detail matches ID', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.eql(pm.environment.get('packageId'));",
        "});"
    ]
    package_items.append(make_req(
        name="03. Get Package By ID",
        method="GET",
        url_path="/api/v1/packages/{{packageId}}",
        auth_bearer="{{staffToken}}",
        tests=get_pkg_id_tests,
        description="Mengambil detail paket laundry berdasarkan UUID."
    ))

    get_pkg_by_benefit_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Packages with benefit returned', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data).to.be.an('array');",
        "});"
    ]
    package_items.append(make_req(
        name="04. Get Package By Benefit ID",
        method="GET",
        url_path="/api/v1/packages/{{benefitId}}/benefit",
        auth_bearer="{{staffToken}}",
        tests=get_pkg_by_benefit_tests,
        description="Mengambil daftar paket yang memiliki relasi benefit tertentu."
    ))

    update_pkg_payload = {
        "name": "Cuci Komplit Kilat 1 Hari (Updated)",
        "price": 16000,
        "description": "Cuci bersih, setrika uap, dan packaging higienis tahan lama",
        "type": "WEIGHT",
        "daysEstimation": 2,
        "benefitIds": ["{{benefitId}}"]
    }
    update_pkg_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Package price updated and cache evicted', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.price).to.eql(16000);",
        "});"
    ]
    package_items.append(make_req(
        name="05. Update Package (Admin)",
        method="PUT",
        url_path="/api/v1/packages/{{packageId}}",
        auth_bearer="{{adminToken}}",
        body={
            "mode": "formdata",
            "formdata": [
                {"key": "request", "value": json.dumps(update_pkg_payload), "type": "text", "contentType": "application/json"}
            ]
        },
        tests=update_pkg_tests,
        description="Memperbarui harga dan deskripsi paket laundry (khusus Admin)."
    ))

    rbac_pkg_tests = [
        "pm.test('Status code is 403 Forbidden for Staff', function () {",
        "    pm.response.to.have.status(403);",
        "});",
        "pm.test('Access denied response structure is standard', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.status).to.be.oneOf([false, 'FAILED']);",
        "    pm.expect(res.message).to.be.a('string');",
        "});"
    ]
    package_items.append(make_req(
        name="06. [RBAC Check] Staff Create Package (Forbidden 403)",
        method="POST",
        url_path="/api/v1/packages",
        auth_bearer="{{staffToken}}",
        body={
            "mode": "formdata",
            "formdata": [
                {"key": "request", "value": json.dumps(package_payload), "type": "text", "contentType": "application/json"}
            ]
        },
        tests=rbac_pkg_tests,
        description="Memverifikasi bahwa peran STAFF dilarang membuat paket laundry baru."
    ))

    items.append({"name": "05. Laundry Package Master", "item": package_items})

    # -------------------------------------------------------------
    # FOLDER 06: Laundry Transactions
    # -------------------------------------------------------------
    tx_items = []

    tx_payload = {
        "customerId": "{{customerId}}",
        "userId": "{{adminUserId}}",
        "orderDate": "2026-10-25T10:00:00",
        "targetDate": "2026-10-28T10:00:00",
        "status": "IN_PROGRESS",
        "transactionDetails": [
            {
                "packageId": "{{packageId}}",
                "quantity": 3,
                "orderDate": "2026-10-25T10:00:00"
            },
            {
                "packageId": "{{packageId}}",
                "quantity": 2,
                "orderDate": "2026-10-25T10:00:00"
            }
        ]
    }
    create_tx_tests = [
        "pm.test('Status code is 200 or 201', function () {",
        "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
        "});",
        "pm.test('Transaction created and IDs stored', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.be.a('string');",
        "    pm.environment.set('transactionId', res.data.id);",
        "    pm.environment.set('invoiceNo', res.data.invoiceNo);",
        "    if (res.data.transactionDetails && res.data.transactionDetails.length > 0) {",
        "        pm.environment.set('transactionDetailId', res.data.transactionDetails[0].id);",
        "        if (res.data.transactionDetails.length > 1) {",
        "            pm.environment.set('transactionDetailId2', res.data.transactionDetails[1].id);",
        "        }",
        "    }",
        "});"
    ]
    tx_items.append(make_req(
        name="01. Create Laundry Order",
        method="POST",
        url_path="/api/v1/transactions",
        auth_bearer="{{staffToken}}",
        body={
            "mode": "formdata",
            "formdata": [
                {"key": "request", "value": json.dumps(tx_payload), "type": "text", "contentType": "application/json"}
            ]
        },
        tests=create_tx_tests,
        description="Membuat transaksi order laundry baru dengan 2 detail paket (MinIO I/O Decoupled, kalkulasi harga otomatis)."
    ))

    get_txs_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Transactions list paginated correctly', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.content).to.be.an('array');",
        "});"
    ]
    tx_items.append(make_req(
        name="02. Get All Transactions (Paginated)",
        method="GET",
        url_path="/api/v1/transactions",
        query_params={"page": "0", "size": "10", "sortBy": "createdAt", "direction": "desc"},
        auth_bearer="{{staffToken}}",
        tests=get_txs_tests,
        description="Mengambil daftar transaksi laundry dengan filter dan pengurutan berindeks B-Tree."
    ))

    get_tx_id_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Transaction detail matches ID', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.id).to.eql(pm.environment.get('transactionId'));",
        "});"
    ]
    tx_items.append(make_req(
        name="03. Get Transaction By ID",
        method="GET",
        url_path="/api/v1/transactions/{{transactionId}}",
        auth_bearer="{{staffToken}}",
        tests=get_tx_id_tests,
        description="Mengambil detail transaksi lengkap dengan baris detail item dan relasi berkas."
    ))

    update_tx_status_payload = {
        "customerId": "{{customerId}}",
        "userId": "{{adminUserId}}",
        "orderDate": "2026-10-25T10:00:00",
        "targetDate": "2026-10-28T10:00:00",
        "status": "DONE",
        "transactionDetails": [
            {
                "id": "{{transactionDetailId}}",
                "packageId": "{{packageId}}",
                "quantity": 3,
                "orderDate": "2026-10-25T10:00:00"
            },
            {
                "id": "{{transactionDetailId2}}",
                "packageId": "{{packageId}}",
                "quantity": 2,
                "orderDate": "2026-10-25T10:00:00"
            }
        ]
    }
    update_tx_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Transaction status updated to DONE', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.status).to.eql('DONE');",
        "});"
    ]
    tx_items.append(make_req(
        name="04. Update Transaction Status (Done)",
        method="PUT",
        url_path="/api/v1/transactions/{{transactionId}}",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{staffToken}}",
        body={"mode": "raw", "raw": json.dumps(update_tx_status_payload, indent=2)},
        tests=update_tx_tests,
        description="Memperbarui status pengerjaan cucian (IN_PROGRESS -> DONE)."
    ))

    patch_status_payload = {
        "status": "DONE"
    }
    patch_status_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Transaction status updated via dedicated endpoint', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.status).to.eql('DONE');",
        "});"
    ]
    tx_items.append(make_req(
        name="05. Update Transaction Status Only (PATCH Dedicated)",
        method="PATCH",
        url_path="/api/v1/transactions/{{transactionId}}/status",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{staffToken}}",
        body={"mode": "raw", "raw": json.dumps(patch_status_payload, indent=2)},
        tests=patch_status_tests,
        description="Endpoint khusus untuk langsung memperbarui status transaksi tanpa roundtrip fetch rincian."
    ))

    items.append({"name": "06. Laundry Transactions", "item": tx_items})

    # -------------------------------------------------------------
    # FOLDER 07: Transaction Details & Files
    # -------------------------------------------------------------
    file_items = []

    upload_file_payload = {
        "transactionId": "{{transactionId}}"
    }
    upload_file_tests = [
        "pm.test('Status code is 200 or 201', function () {",
        "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
        "});",
        "pm.test('File upload response valid', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data).to.be.an('array');",
        "});"
    ]
    file_items.append(make_req(
        name="01. Upload Additional Transaction Files",
        method="POST",
        url_path="/api/v1/transaction-files",
        auth_bearer="{{staffToken}}",
        body={
            "mode": "formdata",
            "formdata": [
                {"key": "request", "value": json.dumps(upload_file_payload), "type": "text", "contentType": "application/json"}
            ]
        },
        tests=upload_file_tests,
        description="Mengunggah foto kondisi pakaian setelah dicuci (MinIO I/O Decoupled)."
    ))

    del_detail_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Transaction detail item deleted', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.message).to.be.a('string');",
        "});"
    ]
    file_items.append(make_req(
        name="02. Delete Transaction Detail Item",
        method="DELETE",
        url_path="/api/v1/transaction-details/{{transactionDetailId}}",
        auth_bearer="{{staffToken}}",
        tests=del_detail_tests,
        description="Menghapus satu baris item paket laundry dari rincian order transaksi."
    ))

    del_tx_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Transaction record deleted', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.message).to.be.a('string');",
        "});"
    ]
    file_items.append(make_req(
        name="03. Delete Transaction",
        method="DELETE",
        url_path="/api/v1/transactions/{{transactionId}}",
        auth_bearer="{{staffToken}}",
        tests=del_tx_tests,
        description="Menghapus order transaksi laundry (soft/hard delete)."
    ))

    view_file_tests = [
        "pm.test('Status code is 200 or 404', function () {",
        "    pm.expect(pm.response.code).to.be.oneOf([200, 404]);",
        "});"
    ]
    file_items.append(make_req(
        name="04. View Uploaded File via Proxy",
        method="GET",
        url_path="/api/v1/files/view",
        query_params={"path": "sample.jpg"},
        tests=view_file_tests,
        description="Mengunduh dan merender file gambar/dokumen dari MinIO storage via proxy backend."
    ))

    items.append({"name": "07. Transaction Details & Files", "item": file_items})

    # -------------------------------------------------------------
    # FOLDER 08: Dashboard & Metrics
    # -------------------------------------------------------------
    dashboard_items = []
    dashboard_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Metrics object contains revenue and orders', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.todayRevenue).to.not.be.undefined;",
        "    pm.expect(res.data.activeOrdersCount).to.be.a('number');",
        "});"
    ]
    dashboard_items.append(make_req(
        name="01. Get Dashboard Metrics",
        method="GET",
        url_path="/api/v1/dashboard/metrics",
        auth_bearer="{{staffToken}}",
        tests=dashboard_tests,
        description="Mengambil statistik pendapatan hari ini, berat cucian, antrian ekspres, dan status mesin."
    ))
    items.append({"name": "08. Dashboard & Metrics", "item": dashboard_items})

    # -------------------------------------------------------------
    # FOLDER 09: Outlet Profile & Settings
    # -------------------------------------------------------------
    setting_items = []
    setting_payload = {
        "outletName": "Nuy Laundry Central",
        "address": "Jl. Mawar No. 12, Jakarta",
        "phone": "081234567890",
        "openHour": "07:00",
        "closeHour": "21:00",
        "freeRadiusKm": 3,
        "extraCostPerKm": 2500,
        "receiptFooterNote": "Terima kasih telah mempercayakan cucian Anda kepada kami."
    }
    setting_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Setting data matches payload', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.data.outletName).to.eql('Nuy Laundry Central');",
        "});"
    ]
    setting_items.append(make_req(
        name="01. Get Outlet Settings",
        method="GET",
        url_path="/api/v1/outlet/profile",
        tests=setting_tests,
        description="Mengambil profil outlet laundry (alamat, jam operasional, radius antar)."
    ))
    setting_items.append(make_req(
        name="02. Update Outlet Settings (Admin)",
        method="PUT",
        url_path="/api/v1/outlet/profile",
        headers={"Content-Type": "application/json"},
        auth_bearer="{{adminToken}}",
        body={"mode": "raw", "raw": json.dumps(setting_payload, indent=2)},
        tests=setting_tests,
        description="Memperbarui profil dan konfigurasi outlet laundry."
    ))
    items.append({"name": "09. Outlet Profile & Settings", "item": setting_items})

    # -------------------------------------------------------------
    # FOLDER 10: Health & Monitoring
    # -------------------------------------------------------------
    health_items = []
    health_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Application is UP', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.status).to.eql('UP');",
        "});"
    ]
    health_items.append(make_req(
        name="01. Health Check Liveness",
        method="GET",
        url_path="/actuator/health",
        tests=health_tests,
        description="Monitoring status kesehatan aplikasi backend Spring Boot (Liveness & Readiness probe)."
    ))
    items.append({"name": "10. Health & Monitoring", "item": health_items})

    # -------------------------------------------------------------
    # FOLDER 11: Session Termination
    # -------------------------------------------------------------
    logout_items = []
    logout_tests = [
        "pm.test('Status code is 200', function () {",
        "    pm.response.to.have.status(200);",
        "});",
        "pm.test('Logout session revoked', function () {",
        "    var res = pm.response.json();",
        "    pm.expect(res.message).to.be.a('string');",
        "});"
    ]
    logout_items.append(make_req(
        name="01. Logout Active Admin Session",
        method="POST",
        url_path="/api/auth/logout",
        auth_bearer="{{adminToken}}",
        tests=logout_tests,
        description="Mencabut sesi aktif pengguna dari Redis cache dan database PostgreSQL."
    ))
    items.append({"name": "09. Session Termination", "item": logout_items})

    # Root Collection Schema
    collection = {
        "info": {
            "_postman_id": str(uuid.uuid4()),
            "name": "BE Nuy Laundry API Collection",
            "description": "Koleksi resmi REST API backend Nuy Laundry lengkap dengan otomatisasi token, alur transaksi, file multipart, RBAC authorization, dan monitoring.",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": items
    }

    col_file = os.path.join(POSTMAN_DIR, "be-nuy-laundry.postman_collection.json")
    with open(col_file, "w") as f:
        json.dump(collection, f, indent=2)
    print(f"Collection created: {col_file}")

def generate_readme():
    readme_content = """# BE Nuy Laundry - API Postman Collection & Environment

Katalog koleksi resmi Postman (Collection v2.1.0) untuk memudahkan pengujian manual, onboarding developer, dan pengujian otomatis CI/CD pada backend **BE Nuy Laundry**.

---

## 📦 Berkas Koleksi

1. **`be-nuy-laundry.postman_collection.json`**  
   Memuat seluruh request REST API yang dikelompokkan ke dalam 9 modul logis:
   - `01. Authentication & Profile`
   - `02. User Management (Admin Only)`
   - `03. Customer Management`
   - `04. Package Benefit Master`
   - `05. Laundry Package Master`
   - `06. Laundry Transactions`
   - `07. Transaction Details & Files`
   - `08. Health & Monitoring`
   - `09. Session Termination`
2. **`be-nuy-laundry.postman_environment.json`**  
   Variabel lingkungan terkonfigurasi untuk server lokal (`baseUrl: http://localhost:8080`) dengan kredensial default admin (`perf_admin`) dan staff (`perf_staff`).
3. **`test_assets/sample_image.png`**  
   File dummy gambar 1x1 pixel untuk pengujian form-data multipart.

---

## 🚀 Cara Menggunakan di Postman

1. Buka aplikasi **Postman** (Desktop atau Web).
2. Klik tombol **Import** di pojok kiri atas.
3. Pilih berkas:
   - `postman/be-nuy-laundry.postman_collection.json`
   - `postman/be-nuy-laundry.postman_environment.json`
4. Pilih environment aktif: **BE Nuy Laundry Local**.
5. Jalankan request secara berurutan atau gunakan **Collection Runner**:
   - Jalankan `01. Login as Admin` -> Token JWT otomatis tersimpan ke `adminToken`.
   - Jalankan `02. Login as Staff` -> Token JWT otomatis tersimpan ke `staffToken`.
   - Seluruh request berikutnya akan membaca token dan ID dinamis secara otomatis!

---

## 🤖 Eksekusi Otomatis via Newman CLI

Pastikan server backend Spring Boot telah aktif di port `8080`, lalu jalankan:

```bash
# Eksekusi seluruh koleksi API
npx newman run postman/be-nuy-laundry.postman_collection.json \\
  -e postman/be-nuy-laundry.postman_environment.json

# Eksekusi dengan laporan detail
npx newman run postman/be-nuy-laundry.postman_collection.json \\
  -e postman/be-nuy-laundry.postman_environment.json \\
  --reporters cli
```
"""
    readme_file = os.path.join(POSTMAN_DIR, "README.md")
    with open(readme_file, "w") as f:
        f.write(readme_content)
    print(f"README created: {readme_file}")

if __name__ == "__main__":
    ensure_dirs()
    generate_environment()
    build_collection()
    generate_readme()
    print("All Postman collection artifacts successfully generated!")
