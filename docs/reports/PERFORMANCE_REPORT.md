# Laporan Evaluasi Performa BE Nuy Laundry

## 1. Informasi Pengujian
- **Tanggal**: 24 Oktober 2026
- **Pelaksana**: Antigravity AI Engine (Pair Programming Engineer)
- **Branch / Komit**: `feat/performance-evaluation`
- **Lingkungan**:
  - Mesin: macOS (Apple Silicon / Darwin 24.6.0)
  - Java: OpenJDK 17 (Temurin 17.0.12)
  - Basis Data: PostgreSQL 14 (Host port 5432)
  - Cache: Redis 7 (Docker container `nuy-laundry-redis` :6379)
  - Object Storage: MinIO (Docker container `nuy-laundry-minio` :9000)
- **Volume Data Uji (Seeding PERF-00)**:
  - Users: 58
  - Customers: 2.004
  - Packages: 38
  - Benefits: 24
  - Transactions: 10.004
  - Transaction Details: 19.949
  - Transaction Files: 30
- **Alat Ukur**:
  - `k6` v2.3.0 (`/opt/homebrew/bin/k6`)
  - Spring Boot Actuator & Micrometer Prometheus
  - Hibernate Statistical Logging (`StatisticalLoggingSessionEventListener`)
  - Python HTTP Benchmark Harness (`scripts/perf/measure_baseline.py`)
  - PostgreSQL `EXPLAIN (ANALYZE, BUFFERS)`

---

## 2. Baseline Sebelum Optimasi (PERF-02)

Pengukuran dilakukan pada data skala produksi (10.000 transaksi dan 2.000 customer) dengan 50 iterasi per endpoint:

| Endpoint | Method | p50 (ms) | p95 (ms) | Query / Request | Catatan Bottleneck |
| :--- | :---: | ---: | ---: | :---: | :--- |
| `POST /api/auth/login` | POST | 100.27 | 121.44 | 2 | BCrypt hash work factor (normal untuk keamanan) |
| `GET /api/auth/me` | GET | 3.84 | 5.70 | 0 | Cache-aside Redis hit |
| `GET /api/v1/packages?size=20` | GET | 4.19 | 6.45 | 1 | Master data belum di-cache |
| `GET /api/v1/benefits?size=20` | GET | 4.19 | 6.30 | 1 | Master data belum di-cache |
| `GET /api/v1/customers?size=20` | GET | 6.12 | 27.73 | 2 | Count + select pagination |
| `GET /api/v1/customers/{id}` | GET | 18.44 | 33.80 | 1 | `@EntityGraph` menarik seluruh riwayat transaksi |
| `GET /api/v1/transactions?size=20` | GET | 44.55 | 63.23 | **80** | **N+1 Query parah** (lazy child collections) |
| `GET /api/v1/transactions?size=100` | GET | 187.44 | 254.64 | **341** | **N+1 Query bertambah proporsional terhadap size** |
| `GET /api/v1/transactions/{id}` | GET | 7.82 | 18.59 | 4 | Lazy fetch customer, user, details, files |
| `POST /api/v1/customers` | POST | 4.29 | 6.48 | 1 | Insert customer baru |

---

## 3. Hasil per Tiket Evaluasi

### [PERF-01] Observability: Actuator & Metrik
- **Masalah**: Aplikasi tidak memiliki endpoint metrik standar, metrik JVM, dan connection pool tidak terpantau.
- **Tindakan**:
  - Menambahkan dependensi `spring-boot-starter-actuator` dan `micrometer-registry-prometheus` pada [pom.xml](../../pom.xml).
  - Mengonfigurasi `/actuator/health` sebagai endpoint publik dan `/actuator/prometheus` serta `/actuator/metrics` terlindungi autentikasi JWT pada [SecurityConfig.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/SecurityConfig.java) dan [JwtAuthFilter.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/JwtAuthFilter.java).
  - Mengaktifkan distribusi persentil latensi HTTP (`0.5, 0.95, 0.99`).

### [PERF-03] Evaluasi JVM, Memori, GC & Container
- **Masalah**: `Dockerfile` sebelumnya menggunakan `ENTRYPOINT ["java", "-jar", "app.jar"]` tanpa konfigurasi memori container cgroup.
- **Tindakan**:
  - Memperbarui [Dockerfile](../../Dockerfile) dengan flag JVM produksi yang cgroup-aware:
    ```dockerfile
    ENTRYPOINT ["java", \
      "-XX:MaxRAMPercentage=75.0", \
      "-XX:InitialRAMPercentage=50.0", \
      "-XX:+UseG1GC", \
      "-XX:+ExitOnOutOfMemoryError", \
      "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/tmp", \
      "-jar", "app.jar"]
    ```
- **Hasil**:
  - Pemakaian heap saat idle: ~92 MB.
  - Pemakaian heap di bawah beban puncak (50 VU, 11.162 request): **228.6 MB** (sangat stabil dan efisien tanpa indikasi memory leak).
  - GC pause: rata-rata **4.53 ms per pause** (total 0.644s kumulatif dari 142 minor collections), nol pause > 200 ms.

### [PERF-04] Evaluasi Tomcat Thread & HikariCP Pool
- **Masalah**: Pool HikariCP default (10 koneksi) dan Tomcat thread default (200 thread) tidak seimbang, berisiko thread contention saat lonjakan traffic.
- **Tindakan**:
  - Menyetel pool size HikariCP menjadi 20 (`maximum-pool-size=20`, `minimum-idle=5`, `connection-timeout=5000ms`, `leak-detection-threshold=10000ms`) pada [application.properties](../../src/main/resources/application.properties).
  - Menyetel thread pool Tomcat: `threads.max=200`, `threads.min-spare=20`, `accept-count=100`, `connection-timeout=10s`.
- **Hasil**:
  - `hikaricp.connections.pending`: **0.0** (tidak ada antrean thread yang menunggu koneksi).
  - `hikaricp.connections.idle`: **20.0** (seluruh koneksi kembali ke pool dengan aman).

### [PERF-05] Audit N+1 Query & Strategi Fetch JPA
- **Masalah**: 
  - `TransactionMapper` memicu lazy loading individual untuk setiap transaksi dalam list. Untuk 20 transaksi dieksekusi 80 query; untuk 100 transaksi dieksekusi 341 query.
  - `CustomerJpaRepository` menggunakan `@EntityGraph` yang memuat seluruh transaksi customer saat mengambil detail profil.
- **Tindakan**:
  - Menghapus `@EntityGraph` berlebihan dari [CustomerJpaRepository.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/database/repository/CustomerJpaRepository.java).
  - Mengubah seluruh asosiasi `@ManyToOne` menjadi `fetch = FetchType.LAZY` di [TransactionEntity.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/database/entity/TransactionEntity.java) dan [TransactionDetailEntity.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/database/entity/TransactionDetailEntity.java).
  - Mengonfigurasi `spring.jpa.properties.hibernate.default_batch_fetch_size=50` pada [application.properties](../../src/main/resources/application.properties).
- **Hasil Perbandingan Query & Latensi (Re-Test Post-RBAC)**:
  | Endpoint | Query Sebelum | Query Sesudah | p95 Sebelum | p95 Sesudah | Peningkatan Latensi |
  | :--- | ---: | ---: | ---: | ---: | :---: |
  | `GET /api/v1/customers/{id}` | 1 (heavy join) | 1 (simple PK) | 33.80 ms | **35.14 ms** | Stabil (beban query berkurang drastis) |
  | `GET /api/v1/transactions?size=20` | **80** | **6** | 63.23 ms | **41.10 ms** | **+35.0% lebih cepat (N+1 teratasi)** |
  | `GET /api/v1/transactions?size=100` | **341** | **9** | 254.64 ms | **45.90 ms** | **+82.0% lebih cepat (N+1 teratasi)** |
  | `GET /api/v1/transactions/{id}` | 4 | **3** | 18.59 ms | **11.51 ms** | **+38.1% lebih cepat** |

### [PERF-06] Audit Index PostgreSQL
- **Masalah**: Kolom foreign key dan field filter tidak memiliki index pada basis data relasional PostgreSQL. Seluruh relasi transaksi melakukan sequential table scan.
- **Tindakan**:
  - Menambahkan changelog Liquibase [013-add-performance-indexes.yaml](../../src/main/resources/db/changelog/013-add-performance-indexes.yaml) dengan 10 index strategi tinggi:
    1. `idx_transactions_customer_id` (FK `transactions.customer_id`)
    2. `idx_transactions_user_id` (FK `transactions.user_id`)
    3. `idx_transactions_invoice_no` (Pencarian invoice unik)
    4. `idx_transactions_status_order_date` (Composite filter status & order_date)
    5. `idx_transaction_details_transaction_id` (FK detail ke transaksi)
    6. `idx_transaction_details_package_id` (FK paket)
    7. `idx_transaction_files_transaction_id` (FK file)
    8. `idx_user_sessions_user_id` (FK sesi)
    9. `idx_packages_benefits_pkg_bnf` (Composite relasi paket-benefit)
    10. `idx_customers_phone` (Filter pencarian nomor telepon)
- **Hasil `EXPLAIN ANALYZE`**:
  - Filter `transactions WHERE customer_id = ...`: Waktu eksekusi turun menjadi **0.550 ms** (`Bitmap Index Scan`).
  - Filter `transactions WHERE invoice_no = ...`: Waktu eksekusi turun menjadi **0.332 ms** (`Index Scan`).
  - Filter `transaction_details WHERE transaction_id = ...`: Waktu eksekusi turun menjadi **0.376 ms** (`Bitmap Index Scan`).

### [PERF-08] Evaluasi Redis: Session Cache & Master Data
- **Hasil Pengukuran**:
  - Total Keyspace Hits: **24.190** vs Misses: **9**.
  - Cache Hit Ratio Session: **99.96%** (Target ≥ 95% **tercapai**).
  - Rata-rata latensi round-trip Redis: **0.30 ms** (Target < 2 ms **tercapai**).
  - Seluruh kunci sesi di Redis memiliki TTL aktif (~604.800 detik / 7 hari).

### [PERF-09] Evaluasi Upload Berkas & Multipart Limit
- **Tindakan**:
  - Menyesuaikan konfigurasi multipart di [application.properties](../../src/main/resources/application.properties):
    - `spring.servlet.multipart.max-file-size=3MB` (batas per file tetap terjaga)
    - `spring.servlet.multipart.max-request-size=15MB` (mendukung multi-upload hingga 5 foto)
    - `spring.servlet.multipart.file-size-threshold=1MB` (file > 1MB di-buffer ke disk sementara untuk melindungi heap)

---

## 4. Hasil Load Test k6 (PERF-10)

Pengujian beban dilakukan menggunakan script [scripts/perf/load_test.js](../../scripts/perf/load_test.js) dengan skenario *Ramping VUs* (10 hingga 50 pengguna bersamaan) selama 3,5 menit:

| Skenario | VU Maks | Total Req | RPS | Read p95 (ms) | Write p95 (ms) | Error % | Lulus SLO? |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Smoke Test** | 2 | 157 | 5.14 | 38.34 | 28.10 | 0.00% | ✅ **LULUS** |
| **Load Test (Peak)** | 50 | 11.162 | 51.86 | 66.93 | 53.42 | 0.00% | ✅ **LULUS** |

- **Target SLO Read p95**: Target < 300 ms → **Hasil aktual: 66.93 ms** (jauh lebih cepat dari SLO).
- **Target SLO Write p95**: Target < 800 ms → **Hasil aktual: 53.42 ms** (jauh lebih cepat dari SLO).
- **Target Error Rate**: Target < 1.0% → **Hasil aktual: 0.00%** (14.609 / 14.609 assertion checks sukses 100%).
- **Distribusi Durasi HTTP Req (Overall)**: Med **11.80 ms**, p90 **37.53 ms**, p95 **66.28 ms**, Max **443.12 ms**.
- **Breaking Point**: Sistem sangat stabil di 50 VU (51.86 RPS) pada mesin pengujian tanpa kegagalan koneksi atau OOM.

---

## 5. Konfigurasi Final yang Diterapkan

### 1. `application.properties`
```properties
# Multipart Tuning (PERF-09)
spring.servlet.multipart.max-file-size=3MB
spring.servlet.multipart.max-request-size=15MB
spring.servlet.multipart.file-size-threshold=1MB

# Tomcat Threading & Timeouts (PERF-04)
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=20
server.tomcat.accept-count=100
server.tomcat.connection-timeout=10s

# HikariCP Connection Pool (PERF-04)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=5000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=10000

# Hibernate Batch Fetching to Eliminate N+1 (PERF-05)
spring.jpa.properties.hibernate.default_batch_fetch_size=50

# Actuator & Prometheus Metrics (PERF-01)
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.distribution.percentiles.http.server.requests=0.5,0.95,0.99
```

### 2. `Dockerfile` (PERF-03)
```dockerfile
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=50.0", \
  "-XX:+UseG1GC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/tmp", \
  "-jar", "app.jar"]
```

---

## 6. Temuan Tambahan (Technical Findings)

1. **FIND-PERF-01: Lazy Loading Proxy Null Identifier pada Mapper DTO**:
   - *Masalah*: Pemeriksaan `Hibernate.isInitialized(entity.getTransaction())` menyebabkan identifier proxy foreign key bernilai `null` saat method DTO mapper dipanggil di luar transaksi aktif.
   - *Solusi*: Mengakses `.getId()` langsung dari referensi entity (`if (entity.getTransaction() != null) model.setTransactionId(entity.getTransaction().getId())`) tanpa memaksa pemuatan seluruh entity.

---

## 7. Hasil Implementasi Optimasi Lanjutan (Fase 2)

Seluruh 3 rekomendasi arsitektural dari evaluasi awal telah berhasil diimplementasikan dan tervalidasi pada [05-PERFORMANCE_PHASE_2_ISSUES.md](../issues/05-PERFORMANCE_PHASE_2_ISSUES.md):

### [OPT-01 / PERF-07] Pemisahan I/O MinIO dari Transaksi Database
- **Tindakan**: Melepaskan `@Transactional` dari `createTransaction` pada [TransactionServiceImpl](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionServiceImpl.java) dan `uploadTransactionFile` pada [TransactionFileServiceImpl](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionFileServiceImpl.java).
- **Mekanisme Kompensasi**: Upload multipart berkas ke MinIO dilakukan terlebih dahulu secara independen. Koneksi database JDBC hanya diambil saat proses penyimpanan entitas. Jika persistensi DB gagal, mekanisme kompensasi otomatis menghapus file yang telah terunggah di MinIO via `fileUseCase.deleteObjects(...)`.
- **Hasil**: Mengeliminasi *idle-in-transaction*, menjaga `hikaricp.connections.pending = 0.0` dan membebaskan pool koneksi dari risiko *starvation*.

### [OPT-02] Implementasi Cache-Aside Redis untuk Master Data (Paket & Benefit)
- **Tindakan**: Menerapkan pola Cache-Aside pada [PackageServiceImpl](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/PackageServiceImpl.java) dan [BenefitServiceImpl](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/BenefitServiceImpl.java) dengan TTL 3.600 detik (1 jam) serta invalidasi otomatis (*cache eviction*) saat terjadi operasi tambah, ubah, atau hapus.
- **Hasil**: Panggilan `getPackageById` dan `getBenefitById` berulang mengembalikan data dari Redis dengan latensi round-trip < 1 ms tanpa membebani query ke database PostgreSQL.
- **Penyempurnaan Entitas**: Menambahkan `@Column(updatable = false)` pada field `createdAt` di [BaseEntity](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/database/entity/BaseEntity.java) untuk menjamin integritas timestamp saat update.

### [OPT-03] Ekstensi `pg_trgm` & Indeks GIN Pencarian Pelanggan
- **Tindakan**: Menambahkan migrasi Liquibase [014-add-trigram-gin-indexes.yaml](../../src/main/resources/db/changelog/014-add-trigram-gin-indexes.yaml) yang mengaktifkan ekstensi PostgreSQL `pg_trgm` dan membangun indeks GIN:
  - `idx_customers_name_trgm ON customers USING gin (lower(name) gin_trgm_ops)`
  - `idx_customers_address_trgm ON customers USING gin (lower(address) gin_trgm_ops)`
- **Hasil `EXPLAIN ANALYZE`**: Pencarian nama pelanggan dengan leading wildcard (`WHERE lower(name) LIKE '%budi%'`) menggunakan `Bitmap Index Scan` dengan execution time turun menjadi **0.337 ms** (jauh lebih cepat dibanding sequential scan).

---

## 8. Verifikasi Regresi Menyeluruh

- **Unit Test Suite**: **233 / 233 Lulus 100%** (`./mvnw clean test`, mencakup pengujian cache hit dan rollback MinIO)
- **Automated API Functional Test Suite**: **54 / 54 Lulus 100%** (`python3 scripts/test_runner.py` termasuk 8 skenario hak akses RBAC)
- **k6 Load Test Assertions (Fase 2)**: **14.220 / 14.220 Lulus 100%** (`load_test.js`, 10.860 request, 0.00% error rate, Read p95 127.04 ms, Write p95 94.21 ms)

