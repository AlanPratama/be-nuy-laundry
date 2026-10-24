# 09 - Security & Operational Behavior

Dokumen ini menjelaskan mekanisme keamanan sistem, autentikasi dan otorisasi, penanganan rahasia/kredensial, manajemen transaksi database, serta karakteristik operasional aktual pada backend **Nuy Laundry**.

---

## 1. Mekanisme Autentikasi (Authentication)

Aplikasi mengimplementasikan mekanisme autentikasi berbasis **JSON Web Token (JWT)** yang dikombinasikan dengan validasi sesi terdistribusi menggunakan **Redis** dan persistensi database PostgreSQL:

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Filter as JwtAuthFilter
    participant JwtSvc as JwtServiceImpl
    participant Redis as SessionCacheService (Redis)
    participant DB as UserSessionRepository (PostgreSQL)
    participant App as Secured Endpoint

    Client->>Filter: Request + Header "Authorization: Bearer <token>"
    Note over Filter: Lewati filter jika path /api/auth/**
    Filter->>JwtSvc: validateToken(token)
    alt Token Invalid / Expired
        JwtSvc-->>Filter: false
        Filter-->>Client: HTTP 401 Unauthorized ("Invalid token")
    end
    JwtSvc-->>Filter: true
    Filter->>JwtSvc: extractSessionId(token)
    Filter->>Redis: get(sessionId)
    alt Cache Miss di Redis
        Filter->>DB: findById(sessionId)
        alt Sesi Tidak Ada di DB
            DB-->>Filter: null
            Filter-->>Client: HTTP 401 Unauthorized ("Session not found")
        end
        Filter->>Redis: save(sessionModel)
    end
    alt Sesi Dicabut (isActive == false)
        Filter-->>Client: HTTP 401 Unauthorized ("Session revoked")
    else Sesi Kadaluwarsa (expiresAt < now)
        Filter-->>Client: HTTP 401 Unauthorized ("Session expired")
    end
    Filter->>JwtSvc: extractRole(token)
    Note over Filter: Set SecurityContextHolder (Username + ROLE_XXX)
    Filter->>App: Lanjutkan Chain
    App-->>Client: Response Data
```

### Karakteristik Token JWT
- **Algoritma**: HMAC-SHA256 (`HS256`) menggunakan kunci rahasia simetris 256-bit.
- **Klaim Token (Claims)**:
  - `sub`: Username pengguna.
  - `userId`: UUID pengguna.
  - `role`: Role pengguna (`SUPER_ADMIN`, `ADMIN`, `STAFF`).
  - `sessionId`: UUID record tabel `user_sessions`.
  - `iat`: Timestamp penerbitan token.
  - `exp`: Timestamp kedaluwarsa.
- **Masa Berlaku Default**:
  - Access Token: `900000000 ms` (~10.4 hari).
  - Refresh Token: `604800000000 ms` (~7000 hari).
  - Redis Sesi TTL: `604800 detik` (7 hari).

---

## 2. Mekanisme Otorisasi (Authorization)

Otorisasi dieksekusi secara ketat melalui dua lapis pertahanan (*defense-in-depth*):

### A. Lapis Pertama: SecurityFilterChain (`SecurityConfig.java`)
- Perimeter akses jaringan dikonfigurasi secara eksplisit:
  - `/api/auth/**` -> Diizinkan tanpa autentikasi (`permitAll()`), mencakup login dan refresh token.
  - `/actuator/health`, `/actuator/health/**` -> Diizinkan tanpa autentikasi (`permitAll()`) untuk *liveness* dan *readiness probes*.
  - `anyRequest().authenticated()` -> Seluruh rute lain (termasuk `/api/v1/users/**`, `/api/v1/customers/**`, `/api/v1/packages/**`, `/api/v1/transactions/**`) **wajib menyertakan Bearer token valid**.
- **Standarisasi Penanganan Error Autentikasi & Otorisasi**:
  - `authenticationEntryPoint`: Mengembalikan response JSON standar saat kredensial/token tidak valid atau tidak disediakan (**HTTP 401 Unauthorized**):
    ```json
    {"status":false,"message":"Autentikasi gagal atau token tidak valid","errors":null,"data":null}
    ```
  - `accessDeniedHandler`: Mengembalikan response JSON standar saat peran pengguna tidak memenuhi kriteria akses (**HTTP 403 Forbidden**):
    ```json
    {"status":false,"message":"Akses ditolak: role tidak memiliki izin","errors":null,"data":null}
    ```

### B. Lapis Kedua: Method-Level Security (`@PreAuthorize`)
Diaktifkan melalui `@EnableMethodSecurity` pada kelas konfigurasi. Menggunakan evaluasi ekspresi SpEL berbasis peran di mana nilai peran diekstrak dari klaim token JWT (dengan prefix `ROLE_` standar):

1. **Manajemen Pengguna (`UserController`)**:
   - Seluruh endpoint CRUD dan reset password user dibatasi hanya untuk `ADMIN` dan `SUPER_ADMIN`:
     - `POST /api/v1/users`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
     - `GET /api/v1/users/{id}`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
     - `GET /api/v1/users`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
     - `PUT /api/v1/users/{id}`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
     - `DELETE /api/v1/users/{id}`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
     - `PATCH /api/v1/users/{id}`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
2. **Master Paket Laundry (`PackageController`)**:
   - Operasi baca bersifat terbuka bagi seluruh staf terotentikasi.
   - Operasi mutasi data dibatasi untuk `ADMIN` dan `SUPER_ADMIN`:
     - `POST /api/v1/packages`: `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")`
     - `PUT /api/v1/packages/{id}`: `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")`
     - `DELETE /api/v1/packages/{id}`: `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")`
3. **Master Benefit Paket (`BenefitController`)**:
   - Operasi baca terbuka untuk seluruh staf terotentikasi.
   - Operasi mutasi data dibatasi untuk `ADMIN` dan `SUPER_ADMIN`:
     - `POST /api/v1/benefits`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
     - `PUT /api/v1/benefits/{id}`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
     - `DELETE /api/v1/benefits/{id}`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
4. **Operasional Transaksi & Pelanggan**:
   - Role `STAFF` diizinkan mengelola data transaksi (`/api/v1/transactions/**`), file bukti (`/api/v1/transaction-files/**`), dan pelanggan (`/api/v1/customers/**`).

---

## 3. Pengelolaan Secret & Environment Variables

- Seluruh nilai kredensial sensitif diabstraksikan ke dalam konfigurasi `application.properties` dengan sintaks substitusi environment variable `${VAR_NAME:default_value}`.
- Pada lingkungan produksi, variabel berikut **wajib di-inject** dari secret manager (Kubernetes Secrets, AWS Secrets Manager, atau Docker Secrets) dan tidak boleh mengandalkan nilai default:
  - `JWT_SECRET`: Kunci HMAC-SHA256 minimal 256-bit (32 karakter/64 hex characters).
  - `DB_USER` dan `DB_PASSWORD`: Kredensial PostgreSQL.
  - `MINIO_USERNAME` dan `MINIO_PASSWORD`: Kredensial S3 Object Storage.

---

## 4. Perlindungan Data Sensitif (Sensitive Data Masking)

1. **Password Hashing**:
   - Password disimpan menggunakan algoritma **BCrypt** dengan salted hash satu arah via bean `BCryptPasswordEncoder()`.
   - Password plaintext tidak pernah disimpan di database atau ditulis ke file log.

2. **Response DTO Projection**:
   - Domain Model `UserModel` yang memuat field `password` tidak pernah dikembalikan langsung ke presentation layer.
   - Endpoint mengembalikan `UserResponseDTO` yang secara eksplisit tidak menyertakan field `password`.

---

## 5. Manajemen Transaksi & I/O Decoupling

- Menggunakan Spring Declarative Transaction Management (`@Transactional`).
- **Pemisahan I/O Storage MinIO dari Transaksi Database**:
  - Untuk mencegah *connection pool starvation* (koneksi HikariCP tertahan selama transfer berkas fisik ke MinIO), operasi upload berkas fisik ke MinIO S3 dipisahkan di luar batasan `@Transactional` pada `TransactionServiceImpl` dan `TransactionFileServiceImpl`.
  - Berkas fisik diunggah terlebih dahulu ke MinIO sebelum koneksi transaksi database dibuka untuk menulis record `TransactionModel`, `TransactionDetailModel`, dan `TransactionFileModel`.
- **Compensating Rollback Pattern**:
  - Apabila terjadi runtime exception saat persistensi ke PostgreSQL (misal pelanggaran constraint DB atau database down), blok `catch` secara otomatis memicu penghapusan kompensasi (`fileUseCase.deleteObjects(...)`) atas seluruh file fisik yang sempat terunggah ke MinIO.
  - Pola ini menjamin tidak ada berkas yatim (*orphan files*) yang tertinggal di MinIO ketika transaksi database gagal.

---

## 6. Logging & Audit Trail

- **Logging**:
  - Framework: SLF4J dengan implementasi Logback bawaan Spring Boot.
  - Error pada autentikasi JWT dicatat dengan level `ERROR`: `log.error("JWT validation failed: {}", e.getMessage())`.
- **Audit Trail Kolom**:
  - Entitas JPA mewarisi `BaseEntity` yang dilengkapi anotasi Hibernate `@CreationTimestamp` (`createdAt`) dengan `@Column(updatable = false)` dan `@UpdateTimestamp` (`updatedAt`). Anotasi `updatable = false` menjamin nilai timestamp pembuatan awal tidak tertimpa nilai `null` saat mutasi data atau cache eviction.
  - *Catatan Faktual*: Belum terdapat pencatatan audit log aktor pengubah (`createdBy`, `updatedBy`) secara otomatis via `AuditorAware`, serta belum tersedia implementasi Correlation ID / MDC logging untuk tracing per request HTTP.

---

## 7. Analisis Perilaku Operasional: Asynchronous, Concurrency, & Idempotency

| Aspek Operasional | Status Implementasi Aktual | Analisis & Dampak |
| :--- | :--- | :--- |
| **Concurrency Control** | Pendelegasian ke Database MVCC | Tidak menggunakan optimistic locking (`@Version`) atau pessimistic locking. Operasi update status bergantung pada serialisasi database PostgreSQL. |
| **Idempotency** | Sebagian (Parsial) | Endpoint `POST /api/v1/transactions` tidak memiliki idempotency-key header. Pengiriman duplikat request akan membuat invoice baru yang berbeda. Namun nomor invoice dijamin unik melalui sequence database. |
| **MinIO I/O Decoupling** | **Terimplementasi** | Operasi I/O berkas MinIO dipisahkan dari transaksi database SQL, dilengkapi compensating rollback (`deleteObjects`) jika DB write gagal. Mencegah HikariCP starvation. |
| **Buffer Multipart I/O** | **Terkonfigurasi Optimal** | Konfigurasi `file-size-threshold: 1MB` dan batas maksimum request 15MB menjaga penggunaan memori JVM tetap terkendali. |
| **Scheduler (`@Scheduled`)** | **Tidak Diterapkan** | Tidak ada background scheduler untuk pembersihan file yatim (*orphan files*) atau sesi kedaluwarsa di PostgreSQL. |
| **Retry Mechanism** | **Tidak Diterapkan** | Kegagalan koneksi jaringan ke MinIO atau Redis akan langsung melempar exception ke client tanpa percobaan otomatis (*auto-retry*). |
