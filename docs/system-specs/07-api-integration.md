# 07 - API & External Integration

Dokumen ini menyajikan katalog lengkap seluruh REST API endpoint yang tersedia pada backend **Nuy Laundry**, format request dan response, mekanisme autentikasi/otorisasi aktual, serta status integrasi sistem eksternal.

---

## 1. Format Standar Komunikasi API

Seluruh endpoint pada aplikasi ini mengembalikan struktur JSON terstandarisasi melalui pembungkus `ApiResponse<T>`:

### Format Sukses Single Data
```json
{
  "code": 200,
  "status": true,
  "message": "SUCCESS",
  "errors": null,
  "data": {
    "id": "c1f71a74-95d9-482a-a9e9-74d618d36eb1"
  }
}
```

### Format Sukses Berhalaman (Pagination)
```json
{
  "code": 200,
  "status": true,
  "message": "SUCCESS",
  "errors": null,
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "isLastPage": false
  }
}
```

### Format Error / Validasi Gagal (HTTP 400 / 422)
```json
{
  "code": 400,
  "status": false,
  "message": "Validation failed",
  "errors": {
    "email": "Format email tidak valid",
    "password": "Password wajib diisi"
  },
  "data": null
}
```

---

## 2. Inventaris Lengkap Endpoint API

Berdasarkan konfigurasi aktual pada `SecurityConfig.java` dan anotasi method security `@PreAuthorize`:
- **Publik (`permitAll`)**: `/api/auth/**` (khususnya login & refresh-token) dan `/actuator/health`.
- **Perlu Autentikasi (`authenticated`)**: Seluruh endpoint aplikasi mewajibkan header `Authorization: Bearer <token>`.
- **Role-based (`ADMIN`, `SUPER_ADMIN`)**:
  - Seluruh manajemen pengguna: `POST`, `GET`, `PUT`, `DELETE`, `PATCH` pada `/api/v1/users/**`.
  - Operasi mutasi paket laundry: `POST`, `PUT`, `DELETE` pada `/api/v1/packages/**`.
  - Operasi mutasi benefit laundry: `POST`, `PUT`, `DELETE` pada `/api/v1/benefits/**`.
- **Role `STAFF`**: Dibatasi hanya untuk operasi operasional harian (Customers, Transactions, Transaction Files, dan Read-only Packages & Benefits). Mencoba mengakses endpoint mutasi master atau manajemen user akan menghasilkan **HTTP 403 Forbidden**.

| Modul | Method | URL Path | Role / Izin Akses | Fungsi & Deskripsi |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/login` | Publik | Otentikasi user & generate Bearer JWT |
| **Auth** | `POST` | `/api/auth/refresh-token` | Publik | Refresh token kedaluwarsa |
| **Auth** | `POST` | `/api/auth/logout` | Authenticated (Bearer) | Invalidation / cabut sesi aktif di DB & Redis |
| **Auth** | `GET` | `/api/auth/me`, `/api/auth/authenticated-user` | Authenticated (Bearer) | Ambil profil pengguna yang sedang login |
| **Customers**| `POST` | `/api/v1/customers` | Authenticated | Tambah data pelanggan baru |
| **Customers**| `GET` | `/api/v1/customers/{id}` | Authenticated | Ambil detail pelanggan berdasarkan ID |
| **Customers**| `GET` | `/api/v1/customers` | Authenticated | Ambil daftar pelanggan berhalaman & filter |
| **Customers**| `PUT` | `/api/v1/customers/{id}` | Authenticated | Perbarui data pelanggan |
| **Customers**| `DELETE` | `/api/v1/customers/{id}` | Authenticated | Hapus data pelanggan |
| **Packages** | `POST` | `/api/v1/packages` | `ADMIN`, `SUPER_ADMIN` | Tambah paket laundry (Multipart: request + file) |
| **Packages** | `GET` | `/api/v1/packages/{id}` | Authenticated | Ambil detail paket laundry |
| **Packages** | `GET` | `/api/v1/packages/{benefitId}/benefit` | Authenticated | Ambil paket berdasarkan ID benefit |
| **Packages** | `GET` | `/api/v1/packages` | Authenticated | Ambil daftar paket berhalaman & filter |
| **Packages** | `PUT` | `/api/v1/packages/{id}` | `ADMIN`, `SUPER_ADMIN` | Perbarui data paket laundry (Multipart) |
| **Packages** | `DELETE` | `/api/v1/packages/{id}` | `ADMIN`, `SUPER_ADMIN` | Hapus paket laundry |
| **Benefits** | `POST` | `/api/v1/benefits` | `ADMIN`, `SUPER_ADMIN` | Tambah master benefit baru |
| **Benefits** | `GET` | `/api/v1/benefits/{id}` | Authenticated | Ambil detail benefit |
| **Benefits** | `GET` | `/api/v1/benefits` | Authenticated | Ambil daftar benefit berhalaman & filter |
| **Benefits** | `PUT` | `/api/v1/benefits/{id}` | `ADMIN`, `SUPER_ADMIN` | Perbarui data benefit |
| **Benefits** | `DELETE` | `/api/v1/benefits/{id}` | `ADMIN`, `SUPER_ADMIN` | Hapus benefit |
| **Transactions**| `POST` | `/api/v1/transactions` | Authenticated | Buat transaksi laundry baru (Multipart: request + foto) |
| **Transactions**| `GET` | `/api/v1/transactions/{id}` | Authenticated | Ambil detail transaksi laundry & detail item |
| **Transactions**| `GET` | `/api/v1/transactions` | Authenticated | Ambil daftar transaksi berhalaman & filter |
| **Transactions**| `PUT` | `/api/v1/transactions/{id}` | Authenticated | Perbarui status transaksi laundry |
| **Transactions**| `DELETE` | `/api/v1/transactions/{id}` | Authenticated | Hapus data transaksi |
| **Trans. Details**| `DELETE`| `/api/v1/transaction-details/{id}`| Authenticated | Hapus satu baris detail item transaksi |
| **Trans. Files** | `POST` | `/api/v1/transaction-files` | Authenticated | Upload file/bukti tambahan transaksi |
| **Trans. Files** | `DELETE` | `/api/v1/transaction-files/{id}` | Authenticated | Hapus satu file transaksi berdasarkan ID |
| **Trans. Files** | `DELETE` | `/api/v1/transaction-files/list` | Authenticated | Hapus sekumpulan file transaksi sekaligus (bulk) |
| **Dashboard** | `GET` | `/api/v1/dashboard/metrics` | Authenticated | Ambil metrik overview, pendapatan harian, antrean express, & status mesin |
| **Settings** | `GET` | `/api/v1/outlet/profile` | Authenticated | Ambil data profil outlet & konfigurasi toko |
| **Settings** | `PUT` | `/api/v1/outlet/profile` | `ADMIN`, `SUPER_ADMIN` | Perbarui informasi profil outlet & operasional |
| **Settings** | `GET` | `/api/v1/settings` | `ADMIN`, `SUPER_ADMIN` | Ambil pengaturan key-value sistem |
| **Settings** | `POST` | `/api/v1/settings` | `ADMIN`, `SUPER_ADMIN` | Simpan / perbarui pengaturan key-value |
| **Articles** | `GET` | `/api/v1/articles` | Authenticated | Ambil daftar artikel berhalaman & filter (`search`, `category`, `isPublished`) |
| **Articles** | `GET` | `/api/v1/articles/{idOrSlug}` | Authenticated | Ambil detail artikel berdasarkan ID atau slug |
| **Articles** | `POST` | `/api/v1/articles` | `ADMIN`, `SUPER_ADMIN` | Buat artikel baru (Multipart: `request` JSON + `file` cover) |
| **Articles** | `PUT` | `/api/v1/articles/{id}` | `ADMIN`, `SUPER_ADMIN` | Perbarui artikel (Multipart: `request` JSON + `file` cover) |
| **Articles** | `DELETE` | `/api/v1/articles/{id}` | `ADMIN`, `SUPER_ADMIN` | Hapus artikel secara permanen beserta foto MinIO |
| **Articles** | `PATCH` | `/api/v1/articles/{id}/publish` | `ADMIN`, `SUPER_ADMIN` | Toggle status publikasi artikel (_Online_ / _Draft_) |
| **Articles** | `POST` | `/api/v1/articles/upload-image` | `ADMIN`, `SUPER_ADMIN` | Unggah gambar inline konten artikel ke MinIO |
| **Files** | `GET` | `/api/v1/files/**` | Authenticated | Streaming dan unduh berkas biner/gambar langsung dari MinIO Storage |
| **Users** | `POST` | `/api/v1/users` | `ADMIN`, `SUPER_ADMIN` | Registrasi user / staf baru (Multipart: request + file) |
| **Users** | `GET` | `/api/v1/users/{id}` | `ADMIN`, `SUPER_ADMIN` | Ambil data user berdasarkan ID |
| **Users** | `GET` | `/api/v1/users` | `ADMIN`, `SUPER_ADMIN` | Ambil daftar user berhalaman (filter: `name`, `role`, `username`, `phone`) |
| **Users** | `PUT` | `/api/v1/users/{id}` | `ADMIN`, `SUPER_ADMIN` | Perbarui profil user & status `isActive` (Multipart) |
| **Users** | `DELETE` | `/api/v1/users/{id}` | `ADMIN`, `SUPER_ADMIN` | Hapus user berdasarkan ID |
| **Users** | `PATCH` | `/api/v1/users/{id}` | `ADMIN`, `SUPER_ADMIN` | Reset password user |

---

## 3. Spesifikasi Detail Endpoint Kritis

### A. Autentikasi: `POST /api/auth/login`
- **Fungsi**: Memverifikasi credential user dan menerbitkan token JWT serta mencatat sesi di database & Redis.
- **Service/Repo Dipanggil**: `AuthServiceImpl`, `UserRepository`, `UserSessionRepository`, `RedisService`, `JwtUtil`.
- **Request Body (`application/json`)**:
```json
{
  "email": "admin@nuylaundry.com",
  "password": "Password123!"
}
```
- **Response Sukses (HTTP 200)**:
```json
{
  "code": 200,
  "status": true,
  "message": "SUCCESS",
  "errors": null,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_token_payload...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_refresh_payload..."
  }
}
```
- **Error Responses**:
  - `400 Bad Request`: Format JSON tidak valid atau email kosong.
  - `401 Unauthorized`: Email tidak terdaftar atau password salah.

---

### B. Transaksi: `POST /api/v1/transactions`
- **Fungsi**: Membuat order transaksi laundry lengkap beserta detail item dan foto kondisi barang awal.
- **Content-Type**: `multipart/form-data`
- **Bagian Multipart**:
  - `request` (`application/json`): DTO `TransactionRequestDTO`
  - `paymentReceiptFile` (`image/*`, opsional): Bukti bayar
  - `invoiceDocumentFile` (`image/*`, opsional): Dokumen fisik
  - `conditionBeforeFiles` (`List<image/*>`, opsional): Foto pakaian sebelum dicuci
  - `conditionAfterFiles` (`List<image/*>`, opsional): Foto pakaian setelah dicuci
  - `otherFiles` (`List<image/*>`, opsional): Dokumen pendukung lain
- **Contoh Payload Part `request`**:
```json
{
  "customerId": "8f9024f2-5ec8-452a-9e0c-99c0d12e98a1",
  "paymentStatus": "UNPAID",
  "description": "Pakaian dinas jangan gunakan pemutih",
  "transactionDetails": [
    {
      "packageId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "qty": 3.5,
      "deliveredDate": "2026-10-27 10:00:00"
    }
  ]
}
```
- **Service/Repo Dipanggil**: `TransactionServiceImpl`, `TransactionDatabaseAdapter`, `CustomerDatabaseAdapter`, `PackageDatabaseAdapter`, `MinioStorageAdapter`.
- **Response Sukses (HTTP 200)**:
```json
{
  "code": 200,
  "status": true,
  "message": "SUCCESS",
  "errors": null,
  "data": {
    "id": "e4b2d56a-7890-4abc-9def-0123456789ab",
    "invoiceNo": "INV-20261024-0001",
    "customerId": "8f9024f2-5ec8-452a-9e0c-99c0d12e98a1",
    "status": "WAITING",
    "paymentStatus": "UNPAID",
    "totalAmount": 70000.0,
    "transactionDate": "2026-10-24 01:00:00",
    "details": [
      {
        "id": "11111111-2222-3333-4444-555555555555",
        "packageId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "qty": 3.5,
        "price": 20000.0,
        "totalAmount": 70000.0
      }
    ],
    "files": []
  }
}
```

---

### C. Manajemen File: `POST /api/v1/transaction-files`
- **Fungsi**: Mengunggah file tambahan untuk transaksi yang sudah ada.
- **Content-Type**: `multipart/form-data`
- **Bagian Multipart**:
  - `request` (`application/json`): `{"invoiceNo": "INV-20261024-0001"}`
  - `paymentReceiptFile` (opsional)
  - `invoiceDocumentFile` (opsional)
  - `conditionBeforeFiles` (opsional)
  - `conditionAfterFiles` (opsional)
  - `otherFiles` (opsional)
- **Response Sukses (HTTP 200)**:
```json
{
  "code": 200,
  "status": true,
  "message": "SUCCESS",
  "errors": null,
  "data": [
    {
      "id": "f5e4d3c2-b1a0-9876-5432-10fedcba9876",
      "invoiceNo": "INV-20261024-0001",
      "fileName": "payment-receipt-20261024.jpg",
      "filePath": "transactions/payment-receipt-20261024.jpg",
      "fileType": "PAYMENT_RECEIPT"
    }
  ]
}
```
- **Catatan Investigasi**: Pada implementasi saat ini terdapat bug inversi logika pada validasi `invoiceNo`. Lihat dokumen 11.

---

### D. Status Transaksi: `PUT /api/v1/transactions/{id}`
- **Fungsi**: Memperbarui status pengerjaan atau status pembayaran laundry.
- **Request Body (`application/json`)**:
```json
{
  "status": "PROCESS",
  "paymentStatus": "PAID"
}
```
- **Aturan Transisi Status**:
  - `WAITING` -> `PROCESS` -> `DONE` -> `DELIVERED`. Transisi mundur atau langsung melompat dari `WAITING` ke `DELIVERED` akan ditolak dengan `400 Bad Request`.

---

## 4. Status Integrasi Sistem Eksternal

### A. MinIO S3 Object Storage
- **Protokol**: S3 Compatible REST API via MinIO Java SDK 8.5.7.
- **Host**: `localhost:9000` (atau `minio:9000` di dalam Docker network).
- **Bucket**: `nuy-laundry` (diperiksa/dibuat otomatis saat bootstrap via `makeBucket`).
- **Timeout**: Default OkHttp client timeout (10 detik connect, 10 detik write, 10 detik read).
- **Arsitektur I/O & Compensating Rollback**:
  - Operasi upload fisik ke MinIO dieksekusi di luar batasan `@Transactional` database (terpisah dari transaksi SQL) pada `TransactionServiceImpl` dan `TransactionFileServiceImpl`.
  - Jika terjadi kegagalan saat persistensi metadata ke PostgreSQL, sistem memicu *compensating rollback* dengan menghapus objek yang sempat diunggah ke MinIO via `fileUseCase.deleteObjects(...)` guna mencegah file yatim (*orphan files*).
  - Buffer memori multipart diatur dengan `file-size-threshold: 1MB` dan batas upload 15MB untuk menjaga efisiensi RAM server.

### B. Redis Cache (Sesi & Master Data)
- **Protokol**: Redis Serialization Protocol (RESP) via Spring Data Redis (Jedis/Lettuce).
- **Host**: `localhost:6379` (atau `redis:6379`).
- **Cakupan & Key Format**:
  1. **User Session**: Menyimpan sesi token aktif dengan key format `session:<userId>` atau `token:<jwt>` (TTL default 7 hari / sinkron masa berlaku JWT).
  2. **Package Master Cache**: Menyimpan detail dan daftar paket laundry dengan key `package:<id>`, `package:benefit:<benefitId>`, dan `package:list:*` (TTL 3600 detik). Otomatis di-evict saat ada mutasi paket.
  3. **Benefit Master Cache**: Menyimpan detail dan daftar benefit dengan key `benefit:<id>` dan `benefit:list:*` (TTL 3600 detik). Otomatis di-evict saat ada mutasi benefit.
- **Pattern Eviction**: Mendukung pembersihan massal via `RedisCacheService.deleteByPattern(pattern)`.

### C. RabbitMQ Message Broker
- **Status Faktual**: **Tidak Terintegrasi pada Source Code**.
- **Bukti**: Layanan RabbitMQ dikonfigurasi pada `docker-compose.yaml` (port `5672`/`15672`) dan tercantum pada `application.properties`, namun dependensi `spring-boot-starter-amqp` tidak ada di `pom.xml`, serta tidak ada kelas producer, listener, queue, atau exchange di dalam codebase.
