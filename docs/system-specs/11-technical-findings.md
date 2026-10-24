# 11 - Technical Findings & Code Audit

Dokumen ini mencatat seluruh temuan teknis, potensi *bug*, celah keamanan (*security concern*), ketidakefisienan performa, konfigurasi mati (*dead config*), serta *technical debt* yang ditemukan selama audit mendalam terhadap basis kode backend **Nuy Laundry**, beserta status dan rincian resolusinya.

Setiap temuan dilengkapi dengan **lokasi kode**, **analisis akar masalah**, **dampak**, **tingkat prioritas**, dan **status resolusi / perbaikan terverifikasi**.

---

## 1. Ringkasan Temuan (Findings Summary)

| ID | Kategori | Judul Temuan | Tingkat Prioritas | Status |
| :--- | :--- | :--- | :--- | :--- |
| **FIND-01** | Logic Bug | Logika Terbalik pada Validasi `invoiceNo` saat Upload File | **CRITICAL** | **RESOLVED** |
| **FIND-02** | Security | Endpoint `/api/v1/users/**` Diberi Akses Terbuka (`permitAll`) | **HIGH** | **RESOLVED** |
| **FIND-03** | API Routing | Mismatch Antara URI Template `{id}` dan `@PathVariable benefitId` | **MEDIUM** | **RESOLVED** |
| **FIND-04** | Architecture | Transaksi Database Menahan Operasi I/O Upload MinIO S3 | **MEDIUM** | **RESOLVED** |
| **FIND-05** | Reliability | Ketiadaan Idempotency Control pada Pembuatan Transaksi | **MEDIUM** | Terbuka |
| **FIND-06** | Dead Config | Konfigurasi RabbitMQ Aktif Tanpa Implementasi AMQP di Codebase | **LOW** | Terbuka |
| **FIND-07** | Database Schema | Inkonsistensi Naming Convention Kolom: camelCase vs snake_case | **LOW** | Terbuka |
| **FIND-08** | Code Hygiene | File Stub Kosong Tanpa Implementasi: `FileController.java` | **LOW** | Terbuka |
| **FIND-09** | Performance | N+1 Query & Unindexed Sorting pada Pagination Transaksi/Pelanggan | **HIGH** | **RESOLVED** |
| **FIND-10** | Performance | Latensi Tinggi Table Scan pada Filter Pencarian Pelanggan (`LIKE '%...'`) | **HIGH** | **RESOLVED** |
| **FIND-11** | Security | Ketiadaan Handler Standar untuk `AccessDeniedException` (HTTP 403) | **MEDIUM** | **RESOLVED** |

---

## 2. Rincian Temuan & Catatan Resolusi

---

### FIND-01: Logika Terbalik pada Validasi `invoiceNo` saat Upload File
- **Lokasi Kode**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionFileServiceImpl.java`.
- **Analisis & Masalah Asli**:
  Sebelumnya pengecekan kondisi string `invoiceNo` mengalami inversi logika sehingga invoice yang sah justru melempar eksepsi.
- **Resolusi (RESOLVED)**:
  Kondisi diperbaiki menjadi `if (!ValueUtil.hasValueString(invoiceNo)) throw new NotFoundException("Invoice not found");`. Operasi upload file hanya berlanjut jika invoice ditemukan di database.

---

### FIND-02: Endpoint User Diberi Akses Terbuka (`permitAll`) Tanpa Autentikasi
- **Lokasi Kode**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/SecurityConfig.java` dan `UserController.java`.
- **Analisis & Masalah Asli**:
  Konfigurasi awal Spring Security secara eksplisit membebaskan seluruh rute `/api/v1/users/**` dari kewajiban login via `.requestMatchers("/api/v1/users/**").permitAll()`. Akibatnya, seluruh aksi mutasi user dan reset password dapat diakses oleh publik tanpa token.
- **Resolusi (RESOLVED)**:
  1. Aturan `.permitAll()` untuk `/api/v1/users/**` dihapus dari `SecurityConfig.java`. Hanya rute `/api/auth/**` dan `/actuator/health` yang diizinkan publik.
  2. Seluruh method pada `UserController` diproteksi menggunakan `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`.
  3. Akses dari pengguna ber-role `STAFF` ditolak dengan response **HTTP 403 Forbidden**.

---

### FIND-03: Mismatch Antara URI Template `{id}` dan `@PathVariable benefitId`
- **Lokasi Kode**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/PackageController.java`.
- **Analisis & Masalah Asli**:
  Anotasi mapping menggunakan `@GetMapping("/{id}/benefit")`, namun parameter method diekstrak via `@PathVariable String benefitId`. Hal ini menyebabkan Spring MVC melempar `IllegalStateException` saat aplikasi dijalankan tanpa compiler flag `-parameters`.
- **Resolusi (RESOLVED)**:
  URI template disinkronkan menjadi `@GetMapping("/{benefitId}/benefit") public ResponseEntity<ApiResponse<List<PackageModel>>> getByBenefitId(@PathVariable String benefitId)`.

---

### FIND-04: Transaksi Database Menahan Operasi I/O Upload MinIO S3
- **Lokasi Kode**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionServiceImpl.java` & `TransactionFileServiceImpl.java`.
- **Analisis & Masalah Asli**:
  Operasi upload berkas fisik ke MinIO dieksekusi di dalam metode bertanda `@Transactional`. Akibatnya, koneksi pool database HikariCP tertahan selama transfer berkas multi-part melalui jaringan, memicu *connection pool starvation* di bawah beban tinggi (*idle-in-transaction*).
- **Resolusi (RESOLVED)**:
  1. I/O upload fisik ke MinIO dipisahkan di luar batasan `@Transactional` database. Seluruh file fisik diunggah terlebih dahulu sebelum koneksi database dibuka untuk menulis metadata.
  2. Diterapkan pola *compensating rollback*: jika proses persistensi ke database melempar runtime exception, blok `catch` memanggil `fileUseCase.deleteObjects(...)` untuk membersihkan seluruh file yang sempat terunggah ke MinIO.

---

### FIND-05: Ketiadaan Idempotency Control pada Pembuatan Transaksi
- **Lokasi Kode**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionServiceImpl.java`.
- **Analisis**:
  Endpoint `POST /api/v1/transactions` tidak membaca header `Idempotency-Key` dan tidak memvalidasi duplikasi request dalam selang waktu singkat (*double submit*).
- **Dampak**:
  Pengiriman request ganda berisiko mencetak invoice baru dengan detail yang identik.
- **Tingkat Prioritas**: **MEDIUM**
- **Status**: Terbuka (Rekomendasi: Filter idempotency berbasis token Redis TTL 60s).

---

### FIND-06: Konfigurasi RabbitMQ Aktif Tanpa Implementasi AMQP di Codebase
- **Lokasi**: `docker-compose.yaml` dan `application.properties`.
- **Analisis**:
  Konfigurasi RabbitMQ aktif dan container dijalankan, namun tidak ada dependensi `spring-boot-starter-amqp` maupun kelas queue/listener di codebase.
- **Status**: Terbuka (Dead Configuration).

---

### FIND-07: Inkonsistensi Naming Convention Kolom Database
- **Lokasi**: Tabel PostgreSQL `packages`, `users`, `transactions`.
- **Analisis**:
  Sebagian kolom menggunakan `camelCase` (misal `packageId`, `createdAt`), sementara standar PostgreSQL adalah `snake_case`.
- **Status**: Terbuka (Technical debt kosmetik / Liquibase migrations).

---

### FIND-08: File Stub Kosong Tanpa Implementasi: `FileController.java`
- **Lokasi**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/FileController.java`.
- **Analisis**:
  Kelas controller kosong tanpa handler endpoint aktif.
- **Status**: Terbuka (Code hygiene cleanup).

---

### FIND-09: N+1 Query & Unindexed Sorting pada Pagination Transaksi & Pelanggan
- **Lokasi**: `PackageDatabaseAdapter.java`, `CustomerDatabaseAdapter.java`, `db.changelog-013-add-performance-indexes.yaml`.
- **Analisis & Masalah Asli**:
  Pemanggilan daftar paket dan detail transaksi memicu query bertubi-tubi (*N+1 problem*) untuk memuat relasi benefit, serta ketiadaan indeks pada kolom `created_at` menyebabkan sorting sequential scan saat data bertumbuh besar.
- **Resolusi (RESOLVED)**:
  1. Mengoptimalkan query JPA menggunakan JOIN FETCH untuk relasi benefit.
  2. Menambahkan indeks B-Tree pada kolom sorting `transactions(created_at DESC)` dan `customers(created_at DESC)` via changelog 013.

---

### FIND-10: Latensi Tinggi Table Scan pada Filter Pencarian Pelanggan (`LIKE '%...'`)
- **Lokasi**: `CustomerDatabaseAdapter.java` dan `db/changelog/014-add-trigram-gin-indexes.yaml`.
- **Analisis & Masalah Asli**:
  Query pencarian pelanggan dengan wildcard awalan (`LIKE '%budi%'`) tidak dapat memanfaatkan indeks B-Tree biasa, sehingga PostgreSQL selalu melakukan full sequential table scan yang membebani CPU.
- **Resolusi (RESOLVED)**:
  1. Mengaktifkan ekstensi PostgreSQL `pg_trgm` via Liquibase changelog 014.
  2. Membuat 2 indeks GIN trigram: `idx_customers_name_trgm` dan `idx_customers_address_trgm`.
  3. Evaluasi EXPLAIN ANALYZE membuktikan query berpindah ke **Bitmap Index Scan** dengan waktu eksekusi turun drastis menjadi **0.337 ms**.

---

### FIND-11: Ketiadaan Handler Standar untuk `AccessDeniedException` (HTTP 403)
- **Lokasi**: `SecurityConfig.java`.
- **Analisis & Masalah Asli**:
  Ketika otorisasi method security `@PreAuthorize` ditolak, Spring Security melempar `AccessDeniedException` yang dikonversi oleh server menjadi response error default Tomcat / Whitelabel HTML, melanggar struktur kontrak `ApiResponse<T>`.
- **Resolusi (RESOLVED)**:
  Mengonfigurasi custom `accessDeniedHandler` dan `authenticationEntryPoint` di dalam `SecurityConfig.java` yang secara eksplisit menulis response JSON terstandarisasi dengan status **HTTP 403 Forbidden** dan **HTTP 401 Unauthorized**.