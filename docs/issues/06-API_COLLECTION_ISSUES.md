# Katalog Issue: Pembuatan JSON Collection API (Postman Collection & Environment)

Dokumen ini berisi spesifikasi teknis dan daftar issue/tiket kerja terstruktur untuk membuat **JSON Collection API** resmi (format Postman Collection v2.1.0 & Postman Environment) pada backend **BE Nuy Laundry**. Dibuat dengan bahasa **high-level, jelas, rinci, dan berorientasi aksi** agar mudah dieksekusi oleh *junior developer* maupun agen AI otomatis.

---

## 📌 1. Latar Belakang & Tujuan Bisnis

Saat ini, backend **BE Nuy Laundry** telah memiliki 54 automated integration test cases dan 233 unit test cases, namun tim pengembang dan QA belum memiliki **API JSON Collection resmi** (Postman / Insomnia / Bruno format) yang tersimpan di repositori. 

### Kebutuhan Utama:
1. **Developer & QA Onboarding**: Memudahkan pengembang frontend, mobile app, dan QA engineer untuk langsung menguji seluruh endpoint tanpa harus menyusun request dari nol.
2. **Otomatisasi Alur Token & Variabel**: Pengujian endpoint sering terhambat karena token JWT harus disalin manual. Collection harus memiliki test script otomatis yang menyimpan token login ke environment variable (`adminToken`, `staffToken`, `superAdminToken`).
3. **Standardisasi Request Multipart**: Modul User, Package, Transaction, dan Transaction Files menggunakan format `multipart/form-data` dengan percampuran bagian payload JSON (`request`) dan file biner (`file`). Konfigurasi ini harus disiapkan dengan benar agar tidak terjadi error HTTP 400/415.
4. **Verifikasi Hak Akses (RBAC)**: Menyertakan skenario pengujian role `STAFF` vs `ADMIN` (memvalidasi respons sukses HTTP 200/201 vs penolakan HTTP 403 Forbidden).
5. **Eksekusi Otomatis CI/CD (Newman)**: File collection JSON harus valid dan kompatibel untuk dieksekusi secara otomatis menggunakan Newman CLI (`newman run`).

---

## 📁 2. Spesifikasi Berkas & Standar Penamaan

Seluruh file koleksi disimpan di dalam folder direktori `postman/` pada root proyek:

```
be-nuy-laundry/
├── postman/
│   ├── be-nuy-laundry.postman_collection.json    # Berkas utama seluruh request (Schema v2.1.0)
│   ├── be-nuy-laundry.postman_environment.json   # Environment variables lokal (localhost:8080)
│   └── README.md                                 # Panduan impor dan cara menjalankan via Newman
```

---

## ⚙️ 3. Spesifikasi Postman Environment

Environment `be-nuy-laundry.postman_environment.json` wajib memuat variabel-variabel kunci berikut:

| Nama Variabel | Tipe Data | Nilai Default (Local) | Deskripsi / Penggunaan |
| :--- | :--- | :--- | :--- |
| `baseUrl` | String | `http://localhost:8080` | Host dan port server backend |
| `adminEmail` | String | `admin@nuylaundry.com` | Email kredensial default admin |
| `adminPassword` | Secret | `Password123!` | Password default akun admin |
| `staffEmail` | String | `staff@nuylaundry.com` | Email kredensial default staf |
| `staffPassword` | Secret | `Password123!` | Password default akun staf |
| `adminToken` | Secret | *(Kosong - diisi script auto-login)* | Bearer JWT untuk role `ADMIN` |
| `staffToken` | Secret | *(Kosong - diisi script auto-login)* | Bearer JWT untuk role `STAFF` |
| `currentUserId` | String | *(Diisi otomatis saat create user)* | ID user aktif untuk pengujian |
| `customerId` | String | *(Diisi otomatis saat create customer)* | ID customer untuk pembuatan transaksi |
| `packageId` | String | *(Diisi otomatis saat create package)* | ID paket laundry untuk transaksi |
| `benefitId` | String | *(Diisi otomatis saat create benefit)* | ID benefit untuk relasi paket |
| `transactionId` | String | *(Diisi otomatis saat create transaksi)* | ID transaksi laundry |
| `invoiceNo` | String | *(Diisi otomatis saat create transaksi)* | Nomor invoice (e.g. `INV-20261024-0001`) |
| `transactionFileId` | String | *(Diisi otomatis saat upload file)* | ID record file lampiran transaksi |

---

## 🗂️ 4. Struktur Folder & Cakupan Endpoint API

Koleksi harus dikelompokkan ke dalam 8 folder logis:

```
be-nuy-laundry.postman_collection.json
├── 01. Authentication & Profile
├── 02. User Management (Admin Only)
├── 03. Customer Management
├── 04. Laundry Package Master
├── 05. Package Benefit Master
├── 06. Laundry Transactions
├── 07. Transaction Details & Files
└── 08. Health & Monitoring
```

### Matriks Endpoint Wajib:

| Folder Modul | Nama Request di Collection | Method | Endpoint Path | Authorization Header | Keterangan Request Body |
| :--- | :--- | :---: | :--- | :--- | :--- |
| **01. Auth** | Login as Admin | `POST` | `/api/auth/login` | None | JSON `adminEmail` & `adminPassword` (Auto-save `adminToken`) |
| | Login as Staff | `POST` | `/api/auth/login` | None | JSON `staffEmail` & `staffPassword` (Auto-save `staffToken`) |
| | Refresh Token | `POST` | `/api/auth/refresh-token` | None | JSON `refreshToken` |
| | Get Current Profile (Me) | `GET` | `/api/auth/me` | Bearer `{{adminToken}}` | None |
| | Logout Active Session | `POST` | `/api/auth/logout` | Bearer `{{adminToken}}` | None |
| **02. Users** | Create New User / Staff | `POST` | `/api/v1/users` | Bearer `{{adminToken}}` | `multipart/form-data`: `request` (JSON) + `file` (Image) |
| *(Admin)* | Get All Users (Paginated) | `GET` | `/api/v1/users?page=0&size=10` | Bearer `{{adminToken}}` | Query params: `page`, `size`, `name` |
| | Get User By ID | `GET` | `/api/v1/users/{{currentUserId}}` | Bearer `{{adminToken}}` | Path variable `id` |
| | Update User Profile | `PUT` | `/api/v1/users/{{currentUserId}}` | Bearer `{{adminToken}}` | `multipart/form-data`: `request` (JSON) + `file` (Image) |
| | Reset User Password | `PATCH` | `/api/v1/users/{{currentUserId}}` | Bearer `{{adminToken}}` | JSON `newPassword`, `confirmPassword` |
| | Delete User | `DELETE`| `/api/v1/users/{{currentUserId}}` | Bearer `{{adminToken}}` | Path variable `id` |
| | *[RBAC Check]* Staff Create User (Forbidden) | `POST` | `/api/v1/users` | Bearer `{{staffToken}}` | Ekspektasi status **HTTP 403 Forbidden** |
| **03. Customers**| Create Customer | `POST` | `/api/v1/customers` | Bearer `{{staffToken}}` | JSON `name`, `phoneNumber`, `address` |
| | Get All Customers | `GET` | `/api/v1/customers?page=0&size=10` | Bearer `{{staffToken}}` | Query params: `page`, `size`, `name` |
| | Get Customer By ID | `GET` | `/api/v1/customers/{{customerId}}` | Bearer `{{staffToken}}` | Path variable `id` |
| | Update Customer | `PUT` | `/api/v1/customers/{{customerId}}` | Bearer `{{staffToken}}` | JSON `name`, `phoneNumber`, `address` |
| | Delete Customer | `DELETE`| `/api/v1/customers/{{customerId}}` | Bearer `{{staffToken}}` | Path variable `id` |
| **04. Packages** | Create Package | `POST` | `/api/v1/packages` | Bearer `{{adminToken}}` | `multipart/form-data`: `request` (JSON) + `file` (Image) |
| | Get All Packages | `GET` | `/api/v1/packages?page=0&size=10` | Bearer `{{staffToken}}` | Query params: `page`, `size`, `name`, `type` |
| | Get Package By ID | `GET` | `/api/v1/packages/{{packageId}}` | Bearer `{{staffToken}}` | Path variable `id` |
| | Get Package By Benefit ID | `GET` | `/api/v1/packages/{{benefitId}}/benefit` | Bearer `{{staffToken}}` | Path variable `benefitId` |
| | Update Package | `PUT` | `/api/v1/packages/{{packageId}}` | Bearer `{{adminToken}}` | `multipart/form-data`: `request` (JSON) + `file` (Image) |
| | Delete Package | `DELETE`| `/api/v1/packages/{{packageId}}` | Bearer `{{adminToken}}` | Path variable `id` |
| | *[RBAC Check]* Staff Create Package (Forbidden) | `POST` | `/api/v1/packages` | Bearer `{{staffToken}}` | Ekspektasi status **HTTP 403 Forbidden** |
| **05. Benefits** | Create Benefit | `POST` | `/api/v1/benefits` | Bearer `{{adminToken}}` | JSON `name`, `description` |
| | Get All Benefits | `GET` | `/api/v1/benefits?page=0&size=10` | Bearer `{{staffToken}}` | Query params: `page`, `size`, `name` |
| | Get Benefit By ID | `GET` | `/api/v1/benefits/{{benefitId}}` | Bearer `{{staffToken}}` | Path variable `id` |
| | Update Benefit | `PUT` | `/api/v1/benefits/{{benefitId}}` | Bearer `{{adminToken}}` | JSON `name`, `description` |
| | Delete Benefit | `DELETE`| `/api/v1/benefits/{{benefitId}}` | Bearer `{{adminToken}}` | Path variable `id` |
| | *[RBAC Check]* Staff Mutasi Benefit (Forbidden) | `POST` | `/api/v1/benefits` | Bearer `{{staffToken}}` | Ekspektasi status **HTTP 403 Forbidden** |
| **06. Transactions**| Create Transaction | `POST` | `/api/v1/transactions` | Bearer `{{staffToken}}` | `multipart/form-data`: `request` (JSON order) + foto fisik |
| | Get All Transactions | `GET` | `/api/v1/transactions?page=0&size=10`| Bearer `{{staffToken}}` | Query params: `status`, `paymentStatus`, `invoiceNo` |
| | Get Transaction By ID | `GET` | `/api/v1/transactions/{{transactionId}}`| Bearer `{{staffToken}}` | Path variable `id` |
| | Update Transaction Status | `PUT` | `/api/v1/transactions/{{transactionId}}`| Bearer `{{staffToken}}` | JSON `status` (PROCESS/DONE), `paymentStatus` |
| | Delete Transaction | `DELETE`| `/api/v1/transactions/{{transactionId}}`| Bearer `{{staffToken}}` | Path variable `id` |
| **07. Trans. Files**| Upload Transaction Files | `POST` | `/api/v1/transaction-files` | Bearer `{{staffToken}}` | `multipart/form-data`: `request` (JSON) + foto |
| | Delete Transaction File By ID | `DELETE`| `/api/v1/transaction-files/{{transactionFileId}}`| Bearer `{{staffToken}}` | Path variable `id` |
| | Bulk Delete Transaction Files | `DELETE`| `/api/v1/transaction-files/list` | Bearer `{{staffToken}}` | JSON `{"ids": ["{{transactionFileId}}"]}` |
| | Delete Transaction Detail Item | `DELETE`| `/api/v1/transaction-details/{{transactionDetailId}}`| Bearer `{{staffToken}}` | Path variable `id` |
| **08. Monitoring**| Health Check | `GET` | `/actuator/health` | None | Monitoring liveness status |

---

## 🛠️ 5. Daftar Tiket Tugas (Actionable Issues)

| Kode Tiket | Judul Tugas | Tingkat Prioritas | Estimasi |
| :--- | :--- | :---: | :---: |
| **COL-01** | Inisialisasi Repositori Direktori `postman/` & Skema Environment JSON | Sedang | 1 jam |
| **COL-02** | Implementasi Skrip Otomatisasi Token & Post-Response Extraction | **Tinggi** | 2 jam |
| **COL-03** | Penyusunan Koleksi Modul Auth, Users, Customers, Packages, & Benefits | **Tinggi** | 3 jam |
| **COL-04** | Penyusunan Koleksi Modul Transaksi & Konfigurasi Multipart Attachment | **Kritis** | 3 jam |
| **COL-05** | Penulisan Test Assertions Standard & Verifikasi Validasi CLI Newman | **Tinggi** | 2 jam |

---

### [COL-01] Inisialisasi Repositori Direktori `postman/` & Skema Environment JSON

#### 🎯 Tujuan
Menyiapkan direktori penyimpanan `postman/` pada root proyek beserta file environment JSON awal `be-nuy-laundry.postman_environment.json`.

#### 📋 Langkah Pengerjaan
1. Buat folder `postman/` di root proyek.
2. Buat file `postman/be-nuy-laundry.postman_environment.json` yang mematuhi skema Postman Environment format v2.1.0:
   - Sertakan variabel default `baseUrl`: `http://localhost:8080`.
   - Sertakan variabel kredensial default admin: `admin@nuylaundry.com` / `Password123!`.
   - Sertakan variabel kredensial default staf: `staff@nuylaundry.com` / `Password123!`.
   - Siapkan variabel kosong untuk `adminToken`, `staffToken`, `customerId`, `packageId`, `benefitId`, `transactionId`, `invoiceNo`.
3. Buat file `postman/README.md` yang memuat panduan cara mengimpor koleksi ke Postman Desktop / Web serta cara menjalankan pengujian dengan Newman.

#### ✅ Kriteria Lolos (Definition of Done)
- [x] File `postman/be-nuy-laundry.postman_environment.json` valid secara sintaks JSON.
- [x] Berhasil diimpor ke aplikasi Postman tanpa peringatan schema mismatch.

---

### [COL-02] Implementasi Skrip Otomatisasi Token & Post-Response Extraction

#### 🎯 Tujuan
Memastikan pengguna tidak perlu menyalin token JWT atau ID entitas secara manual saat menguji alur API berurutan.

#### 📋 Langkah Pengerjaan
1. Pada request `Login as Admin`, tambahkan script di tab **Tests** untuk mengekstrak token respons:
   ```javascript
   if (pm.response.code === 200) {
       var jsonData = pm.response.json();
       pm.environment.set("adminToken", jsonData.data.token);
       if (jsonData.data.refreshToken) {
           pm.environment.set("adminRefreshToken", jsonData.data.refreshToken);
       }
       console.log("Admin token captured successfully.");
   }
   ```
2. Pada request `Login as Staff`, tambahkan script serupa untuk mengeset `pm.environment.set("staffToken", jsonData.data.token)`.
3. Pada setiap request pembuat data (`Create Customer`, `Create Package`, `Create Benefit`, `Create Transaction`), tambahkan script otomatisasi untuk menyimpan ID entitas baru ke environment:
   ```javascript
   if (pm.response.code === 200 || pm.response.code === 201) {
       var res = pm.response.json();
       if (res.data && res.data.id) {
           pm.environment.set("customerId", res.data.id);
       }
       if (res.data && res.data.invoiceNo) {
           pm.environment.set("invoiceNo", res.data.invoiceNo);
       }
   }
   ```
4. Di level root Collection, tambahkan konfigurasi Authorization tipe `Bearer Token` dengan nilai `{{adminToken}}` (dapat di-override di level request untuk pengujian role staf).

#### ✅ Kriteria Lolos (Definition of Done)
- [x] Menjalankan request `Login as Admin` otomatis mengisi variabel `adminToken` di environment.
- [x] Menjalankan request berikutnya otomatis menggunakan token yang tersimpan tanpa konfigurasi manual.

---

### [COL-03] Penyusunan Koleksi Modul Auth, Users, Customers, Packages, & Benefits

#### 🎯 Tujuan
Menyusun seluruh request untuk data master dan entitas bisnis dasar lengkap dengan payload contoh, query parameter, dan pengujian otorisasi peran (RBAC).

#### 📋 Langkah Pengerjaan
1. **Modul 01. Authentication & Profile**:
   - `Login as Admin`, `Login as Staff`, `Refresh Token`, `Get Current Profile (Me)`, `Logout`.
2. **Modul 02. User Management**:
   - `Create User`: Method `POST /api/v1/users`, body `form-data`. Key `request` bertipe Text dengan Content-Type `application/json` (contoh payload JSON staf baru). Key `file` bertipe File.
   - `Get All Users`: `GET /api/v1/users?page=0&size=10&sortBy=createdAt&direction=desc`.
   - `Get User By ID`: `GET /api/v1/users/{{currentUserId}}`.
   - `Update User`: `PUT /api/v1/users/{{currentUserId}}` (Multipart).
   - `Reset Password`: `PATCH /api/v1/users/{{currentUserId}}`.
   - `Delete User`: `DELETE /api/v1/users/{{currentUserId}}`.
   - `Staff Create User (Negative RBAC Test)`: Kirim request pembuatan user menggunakan token `{{staffToken}}`.
3. **Modul 03. Customer Management**:
   - CRUD lengkap untuk `/api/v1/customers/**` dengan token staf/admin.
4. **Modul 04. Laundry Package Master**:
   - `Create Package`: Method `POST /api/v1/packages`, body `form-data`. Key `request` (JSON: nama paket, harga per kg, deskripsi, tipe, benefitIds) dan key `file` (gambar paket).
   - `Get Packages`, `Get Package by ID`, `Get Package by Benefit ID`, `Update Package`, `Delete Package`.
   - `Staff Create Package (Negative RBAC Test)`: Menggunakan token staf, ekspektasi status 403.
5. **Modul 05. Package Benefit Master**:
   - CRUD lengkap untuk `/api/v1/benefits/**`. Mutasi diproteksi token admin, pembacaan dapat menggunakan token staf.

#### ✅ Kriteria Lolos (Definition of Done)
- [x] Seluruh endpoint CRUD modul 01 s/d 05 terdaftar dengan method dan URL yang sesuai.
- [x] Request negatif role staf mengembalikan status HTTP 403 Forbidden dengan pesan standar: `"Akses ditolak: role tidak memiliki izin"`.

---

### [COL-04] Penyusunan Koleksi Modul Transaksi & Konfigurasi Multipart Attachment

#### 🎯 Tujuan
Menyusun alur transaksi laundry lengkap dari pembuatan order, pelampiran berkas fisik foto pakaian, pembaruan status pengerjaan, hingga penghapusan berkas lampiran.

#### 📋 Langkah Pengerjaan
1. **Modul 06. Laundry Transactions**:
   - `Create Transaction`: Method `POST /api/v1/transactions`.
     - Body `multipart/form-data`:
       - Part `request` (`application/json`):
         ```json
         {
           "customerId": "{{customerId}}",
           "paymentStatus": "UNPAID",
           "description": "Pakaian dinas jangan pakai pemutih",
           "transactionDetails": [
             {
               "packageId": "{{packageId}}",
               "qty": 3.5,
               "deliveredDate": "2026-10-30 10:00:00"
             }
           ]
         }
         ```
       - Part `conditionBeforeFiles`: Tipe File (opsional).
       - Part `paymentReceiptFile`: Tipe File (opsional).
       - Part `invoiceDocumentFile`: Tipe File (opsional).
   - `Get All Transactions`: `GET /api/v1/transactions?page=0&size=10&sortBy=createdAt&direction=desc`.
   - `Get Transaction By ID`: `GET /api/v1/transactions/{{transactionId}}`.
   - `Update Transaction Status`: `PUT /api/v1/transactions/{{transactionId}}` dengan payload `{"status":"PROCESS","paymentStatus":"PAID"}`.
   - `Delete Transaction`: `DELETE /api/v1/transactions/{{transactionId}}`.
2. **Modul 07. Transaction Details & Files**:
   - `Upload Transaction Files`: `POST /api/v1/transaction-files`.
     - Body `multipart/form-data`: Part `request` (`{"transactionId": "{{transactionId}}"}`) dan file lampiran bukti pengerjaan (`conditionAfterFiles`).
   - `Delete File By ID`: `DELETE /api/v1/transaction-files/{{transactionFileId}}`.
   - `Bulk Delete Files`: `DELETE /api/v1/transaction-files/list` dengan body JSON `{"ids": ["{{transactionFileId}}"]}`.
   - `Delete Detail Item`: `DELETE /api/v1/transaction-details/{{transactionDetailId}}`.
3. **Modul 08. Health & Monitoring**:
   - `Health Check`: `GET /actuator/health` (Tanpa otorisasi).

#### ✅ Kriteria Lolos (Definition of Done)
- [x] Alur pembuatan transaksi hingga update status pengerjaan dapat dieksekusi secara mulus menggunakan variabel dinamis.
- [x] Part `request` pada request multipart diatur dengan eksplisit header `Content-Type: application/json`.

---

### [COL-05] Penulisan Test Assertions Standard & Verifikasi Validasi CLI Newman

#### 🎯 Tujuan
Menambahkan pengujian otomatis (*test assertions*) pada setiap request di collection untuk memvalidasi kode status HTTP, struktur schema response `ApiResponse<T>`, dan mengeksekusinya via Newman CLI.

#### 📋 Langkah Pengerjaan
1. Tambahkan assertions standar di tab **Tests** untuk request sukses (HTTP 200 / 201):
   ```javascript
   pm.test("Status code is 200 or 201", function () {
       pm.expect(pm.response.code).to.be.oneOf([200, 201]);
   });

   pm.test("Response envelope is standard ApiResponse", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData).to.have.property("status");
       pm.expect(jsonData.status).to.be.true;
       pm.expect(jsonData).to.have.property("code");
       pm.expect(jsonData).to.have.property("message");
   });
   ```
2. Tambahkan assertions khusus untuk request negatif RBAC (HTTP 403 Forbidden):
   ```javascript
   pm.test("Status code is 403 Forbidden for Staff", function () {
       pm.response.to.have.status(403);
   });

   pm.test("Access denied message is standard", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData.status).to.be.false;
       pm.expect(jsonData.message).to.include("Akses ditolak");
   });
   ```
3. Eksekusi pengujian menggunakan Newman CLI di terminal untuk memastikan tidak ada syntax error:
   ```bash
   npx newman run postman/be-nuy-laundry.postman_collection.json \
     -e postman/be-nuy-laundry.postman_environment.json \
     --bail
   ```

#### ✅ Kriteria Lolos (Definition of Done)
- [x] Seluruh request di dalam collection memiliki minimal 2 test assertions (status code dan envelope structure).
- [x] Newman runner berhasil menjalankan koleksi terhadap server lokal dengan status **All Tests Passed (0 Failures)**.

---

## 🚀 Panduan Eksekusi untuk Pengembang / AI Agent

1. **Urutan Pengerjaan**:
   - Jalankan tugas secara sekuensial dari **COL-01** sampai **COL-05**.
2. **Pustaka Pendukung**:
   - Gunakan format JSON Postman v2.1.0 resmi (`https://schema.getpostman.com/json/collection/v2.1.0/collection.json`).
   - Gunakan generator skrip Python atau tool pembuat JSON terprogram untuk merakit `be-nuy-laundry.postman_collection.json` secara presisi dan bebas dari syntax error manual.
3. **Validasi Akhir**:
   - Pastikan server backend sedang aktif pada profil default / dev (`http://localhost:8080`).
   - Eksekusi Newman runner untuk memvalidasi kelayakan berkas koleksi sebelum mengajukan Pull Request.
