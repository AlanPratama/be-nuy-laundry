# Roadmap & Issue Breakdown: Evaluasi Performa Server, Database, & Aplikasi (BE Nuy Laundry)

Dokumen ini berisi spesifikasi issue/tiket tugas untuk mengevaluasi dan mengoptimasi **performa server, basis data, dan aplikasi BE Nuy Laundry secara menyeluruh**.
Spesifikasi ini dirancang dengan bahasa **high-level, jelas, dan instruktif** agar mudah dipahami serta dapat dieksekusi secara mandiri oleh *junior developer* atau *AI agent* (seperti model ringan/hemat biaya).

---

## 🎯 Tujuan Evaluasi (Objectives)
1. Mengetahui batas kemampuan (*bottleneck*) sistem di bawah beban tinggi (*high concurrency*).
2. Memastikan utilisasi CPU, Memory JVM, dan Thread Pool berjalan efisien tanpa memory leak atau thrashing.
3. Mengidentifikasi dan mengeliminasi query database yang lambat (*slow queries*) serta masalah *N+1 query* pada Hibernate/JPA.
4. Memastikan efisiensi caching Redis dan throughput upload berkas ke MinIO Object Storage.
5. Menghasilkan laporan metrik performa (*baseline report*) beserta rekomendasi tuning konfigurasi.

---

## 📋 Ringkasan Tiket Evaluasi Performa

| Kode Issue | Area Fokus | Komponen Target | Metrik Utama yang Dinilai |
| :--- | :--- | :--- | :--- |
| **PERF-01** | JVM & Server Resource | Tomcat, JVM Heap/GC, HikariCP | CPU %, Heap Memory, Active Threads, Connection Pool Wait Time |
| **PERF-02** | Database & JPA/Hibernate | PostgreSQL, Spring Data JPA, Liquibase | N+1 Queries, Query Execution Time, Missing Indexes, Deadlocks |
| **PERF-03** | Caching & Session Store | Redis Cache & Session Storage | Cache Hit Ratio, Session Eviction, Redis Latency (<5ms) |
| **PERF-04** | File Storage & I/O | MinIO Object Storage, Multipart Handling | Upload Latency, Memory Spike during Upload, MinIO Throughput |
| **PERF-05** | Load Testing & Concurrency | Stress Test (k6 / Locust / JMeter) | Max RPS (Throughput), Latency P95 & P99, Error Rate (<1%) |
| **PERF-06** | Monitoring & Observability | Spring Boot Actuator, Micrometer | Health Indicators, Metrics Endpoint `/actuator/prometheus` |

---

## 🛠️ Panduan Eksekusi Tiket

---

### [PERF-01] Evaluasi Utilisasi Resource Server, JVM, & Thread Pool
- **Komponen Target**: Spring Boot Embedded Tomcat, JVM runtime (OpenJDK 17), HikariCP.
- **Tujuan**: Memastikan server tidak kehabisan thread atau memory saat menangani request paralel.
- **Langkah Tugas**:
  1. Periksa konfigurasi pool koneksi database di `application.properties`:
     - Default pool size HikariCP (apakah sudah sesuai untuk concurrency: misal `maximum-pool-size: 20`, `minimum-idle: 10`).
     - Pastikan `connection-timeout` tidak terlalu lama (misal 30000ms).
  2. Periksa alokasi JVM Heap Memory (`-Xms` dan `-Xmx`):
     - Catat konsumsi memory baseline saat aplikasi idle dan saat menangani beban.
     - Pantau apakah terjadi jeda Garbage Collection (GC Pause) yang signifikan.
  3. Konfigurasi worker threads Tomcat:
     - Tentukan batas `server.tomcat.threads.max` (default 200) dan `server.tomcat.threads.min-spare` (default 10).
- **Kriteria Lolos (Acceptance Criteria)**:
  - Tidak ada *Connection Timeout* dari HikariCP saat request simultan.
  - Penggunaan memori JVM stabil, tidak ada *OutOfMemoryError* (OOM).
  - Terdapat dokumentasi rekomendasi flag JVM di `docs/11-technical-findings.md`.

---

### [PERF-02] Evaluasi & Optimasi Query Database (JPA / Hibernate & Indexing)
- **Komponen Target**: PostgreSQL, Hibernate SQL execution, Liquibase changelog.
- **Tujuan**: Mencegah query database yang lambat dan mengeliminasi masalah *N+1 query*.
- **Langkah Tugas**:
  1. **Deteksi N+1 Query Problem**:
     - Cek endpoint `GET /api/v1/transactions` dan `GET /api/v1/transactions/{id}`.
     - Perhatikan apakah query ke tabel `transaction_details` dan `transaction_files` dieksekusi berulang-ulang untuk setiap row transaksi.
     - Jika ya, optimalkan query repository menggunakan `@EntityGraph` atau `JOIN FETCH` pada JPA Repository.
  2. **Verifikasi Indeks Database (PostgreSQL Indexing)**:
     - Periksa tabel: `transactions`, `transaction_details`, `transaction_files`, `users`, `customers`.
     - Pastikan kolom-kolom pencarian/filter dan foreign key memiliki index:
       - `transactions.invoice_no` (Unique index)
       - `transactions.customer_id`, `transactions.user_id` (Index foreign key)
       - `transaction_details.transaction_id`, `transaction_files.transaction_id` (Index foreign key)
       - `customers.phone`, `users.username` (Index pencarian)
  3. **Aktifkan Profiling SQL**:
     - Pantau log query di `application.properties` via `spring.jpa.show-sql=true`.
     - Pastikan tidak ada query berulang yang redundan pada satu siklus request.
- **Kriteria Lolos (Acceptance Criteria)**:
  - Query list transaksi mengambil relasi detail tanpa N+1 query terpisah.
  - Semua foreign key dan kolom filter sering pakai telah memiliki indeks di PostgreSQL.
  - Rata-rata response time query single-row di bawah 10ms.

---

### [PERF-03] Evaluasi Strategi Caching Redis & Session Store
- **Komponen Target**: `RedisCacheService`, Redis container (:6379), Session Token.
- **Tujuan**: Mengurangi beban query database dengan memaksimalkan penggunaan Redis cache.
- **Langkah Tugas**:
  1. **Evaluasi Session Token Caching**:
     - Cek alur autentikasi pada `JwtAuthFilter` dan `SessionCacheService`.
     - Pastikan setiap request protected memvalidasi token dari Redis tanpa melakukan query ulang ke tabel `users`.
  2. **Identifikasi Data Kandidat Cache**:
     - Master data yang jarang berubah (seperti daftar paket laundry `packages` dan `benefits`) sangat ideal untuk di-cache di Redis.
     - Buat mekanisme cache-aside menggunakan Spring Cache (`@Cacheable`, `@CacheEvict`) atau `RedisCacheService`.
  3. **Evaluasi TTL (Time-To-Live)**:
     - Pastikan semua key di Redis memiliki masa kedaluwarsa (TTL) agar memori Redis tidak membengkak tanpa batas.
- **Kriteria Lolos (Acceptance Criteria)**:
  - Validasi token autentikasi terlaksana melalui Redis dengan latensi < 5ms.
  - Data master yang di-cache langsung ter-invalidasi saat ada pembaruan data (Update/Delete).

---

### [PERF-04] Evaluasi Throughput Upload & Storage Berkas (MinIO)
- **Komponen Target**: `MinioAdapter`, `TransactionFileServiceImpl`, `MultipartFile`.
- **Tujuan**: Memastikan proses upload gambar (hingga 3MB per file) tidak membebani memori server dan throughput storage optimal.
- **Langkah Tugas**:
  1. **Ukur Latensi Upload**:
     - Ukur waktu eksekusi endpoint `POST /api/v1/transaction-files` untuk 1 file, 3 file, dan 5 file secara bersamaan.
  2. **Evaluasi Penggunaan Memori Saat Upload**:
     - Pastikan file ditransfer secara streaming ke MinIO tanpa membaca seluruh payload ke dalam byte array memory yang besar secara berulang.
  3. **Validasi Error Handling & Cleanup**:
     - Pastikan jika upload salah satu file gagal di tengah transaksi, tidak ada file yatim piatu (*orphaned objects*) yang tersisa di MinIO.
- **Kriteria Lolos (Acceptance Criteria)**:
  - Upload file 1MB-3MB ke MinIO selesai dalam waktu < 500ms (pada network lokal/Docker).
  - Memory heap server tidak mengalami lonjakan drastis (*spike*) saat beberapa upload berlangsung simultan.

---

### [PERF-05] Load Testing & Stress Testing (Pengujian Beban Concurrency)
- **Komponen Target**: Seluruh endpoint REST API utama.
- **Tool yang Direkomendasikan**: **k6**, **Apache JMeter**, atau **Locust**.
- **Tujuan**: Mengetahui batas maksimum request per detik (RPS) dan kestabilan sistem pada kondisi puncak.
- **Skenario Pengujian**:
  1. **Smoke Test**: 5 Virtual Users (VUs) selama 1 menit (memastikan skrip load test berjalan).
  2. **Load Test**: Ramp-up dari 10 hingga 50 VUs selama 5 menit:
     - 40% request `GET /api/v1/transactions` (Melihat daftar pesanan)
     - 30% request `GET /api/v1/customers` & `GET /api/v1/packages`
     - 20% request `POST /api/v1/transactions` (Membuat pesanan baru)
     - 10% request `POST /api/auth/login` & `GET /api/auth/me`
  3. **Stress Test**: Naikkan VUs hingga 100-200 VUs untuk melihat pada titik berapa response time mulai melambat (*breaking point*).
- **Target Metrik yang Harus Dicapai**:
  - **Latency p95**: < 300 ms (untuk read endpoint) dan < 800 ms (untuk write endpoint).
  - **Latency p99**: < 1000 ms.
  - **Error Rate**: < 1.0% (tidak ada 500 Internal Server Error akibat deadlock atau kehabisan koneksi).
  - **Throughput**: Stabil pada target minimum 100+ RPS.

---

### [PERF-06] Monitoring, Metrics, & Health Indicators
- **Komponen Target**: Spring Boot Actuator, Micrometer.
- **Tujuan**: Menyediakan visibilitas kondisi internal server secara real-time.
- **Langkah Tugas**:
  1. Aktifkan Spring Boot Actuator di `pom.xml`:
     ```xml
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-actuator</artifactId>
     </dependency>
     ```
  2. Konfigurasi endpoint di `application.properties`:
     ```properties
     management.endpoints.web.exposure.include=health,info,metrics,prometheus
     management.endpoint.health.show-details=when_authorized
     ```
  3. Verifikasi ketersediaan metrik performa:
     - `/actuator/health` (Status koneksi PostgreSQL, Redis, RabbitMQ, Disk Space).
     - `/actuator/metrics/jvm.memory.used`
     - `/actuator/metrics/http.server.requests`
     - `/actuator/metrics/hikaricp.connections.active`
- **Kriteria Lolos (Acceptance Criteria)**:
  - Endpoint `/actuator/health` mengembalikan status `"status": "UP"` lengkap dengan komponen pendukung.
  - Metrik HTTP latency dan koneksi pool dapat dipantau tanpa menurunkan performa aplikasi.

---

## 📝 Format Template Laporan Hasil Evaluasi (Bagi Developer / AI Pelaksana)

Setelah menyelesaikan pengujian di atas, pelaksana wajib menyusun ringkasan dengan format berikut:

```markdown
# Laporan Evaluasi Performa BE Nuy Laundry

- Tanggal Pengujian: [DD/MM/YYYY]
- Tool Pengujian: [k6 / JMeter / VisualVM]
- Lingkungan: [Local Docker / Staging]

### 1. Ringkasan Metrik
- Max Throughput: ... RPS
- Latency Rata-rata: ... ms
- Latency P95: ... ms
- Error Rate: ... %

### 2. Bottleneck yang Ditemukan
1. [Contoh: Query N+1 pada endpoint GET /api/v1/transactions]
2. [Contoh: Pool koneksi HikariCP penuh saat concurrency > 50 VUs]

### 3. Solusi & Perbaikan yang Telah Diterapkan
1. ...
2. ...

### 4. Rekomendasi Selanjutnya
1. ...
```
