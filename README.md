# BE Nuy Laundry (Backend System)

Backend RESTful API untuk sistem manajemen operasional laundry kiloan & satuan (**Nuy Laundry**), dibangun menggunakan arsitektur **Hexagonal (Ports & Adapters)** dengan standar enterprise berbasis **Java 17** dan **Spring Boot 3.2.5**.

---

## 🚀 Fitur Utama
- **Autentikasi & Otorisasi**: Berbasis JWT (Access Token & Refresh Token) dengan session blacklist & caching di Redis. Role: `SUPER_ADMIN`, `ADMIN`, `STAFF`.
- **Manajemen Pelanggan (Customer)**: Pengelolaan data pelanggan, riwayat nomor telepon, dan alamat penjemputan/pengantaran.
- **Katalog Paket & Layanan**: Manajemen paket laundry (kiloan/satuan), estimasi durasi pengerjaan, harga, serta integrasi keuntungan (*benefits*).
- **Transaksi & Detail Pesanan**: Pembuatan transaksi cerdas dengan auto-generate nomor invoice unik, kalkulasi otomatis harga & tanggal estimasi selesai, serta update status pesanan.
- **Manajemen Berkas & Bukti Transaksi**: Integrasi langsung dengan MinIO Object Storage (S3-compatible) untuk penyimpanan nota pembayaran, foto kondisi pakaian sebelum & sesudah dicuci, dan dokumen invoice.

---

## 🛠️ Tech Stack & Ekosistem
- **Core Runtime**: Java 17 (OpenJDK), Spring Boot 3.2.5
- **Database & Migration**: PostgreSQL 16, Spring Data JPA, Hibernate, Liquibase
- **Caching & Session**: Redis 7
- **Message Broker**: RabbitMQ
- **Object Storage**: MinIO (S3 API)
- **Testing**: JUnit 5, Mockito, AssertJ, JaCoCo, Python 3 Test Runner

---

## ⚡ Panduan Cepat Menjalankan Aplikasi (Quick Start)

### 1. Menjalankan Layanan Pendukung (Docker)
Pastikan Docker Daemon aktif, lalu jalankan seluruh kontainer pendukung:
```bash
docker compose up -d
```
Container yang akan berjalan:
- **PostgreSQL**: `localhost:5432` (DB: `nuy_laundry`)
- **Redis**: `localhost:6379`
- **RabbitMQ**: `localhost:5672` (Management UI: `http://localhost:15672`)
- **MinIO**: `localhost:9000` (Console UI: `http://localhost:9001`)

### 2. Menjalankan Aplikasi Backend
```bash
./mvnw spring-boot:run
```
Aplikasi backend akan aktif di: `http://localhost:8080`.

### 3. Menjalankan Pengujian

#### A. Unit Tests (Coverage JaCoCo 100%)
```bash
./mvnw clean test jacoco:report
```
Laporan coverage HTML tersedia di: `target/site/jacoco/index.html`.

#### B. Automated API Functional Tests (E2E)
Jalankan skrip runner Python saat backend aktif:
```bash
python3 scripts/test_runner.py
```

---

## 📚 Dokumentasi Lengkap Sistem (/docs)

Seluruh dokumentasi teknis, kamus data, diagram arsitektur, dan tiket pengembangan terpusat di folder **[`/docs`](./docs)**:

| Bagian | Dokumen | Ringkasan Isi |
| :--- | :--- | :--- |
| **Arsitektur & Domain** | [`docs/01-project-overview.md`](./docs/01-project-overview.md) | Ikhtisar proyek, tujuan bisnis, dan rincian teknologi. |
| | [`docs/02-architecture.md`](./docs/02-architecture.md) | Hexagonal Architecture, layer boundaries, dan diagram C4. |
| | [`docs/03-business-flow.md`](./docs/03-business-flow.md) | Alur transaksi, status pesanan, dan kalkulasi tagihan. |
| **Penyimpanan & Database** | [`docs/04-file-management.md`](./docs/04-file-management.md) | Alur upload/delete berkas MinIO dan validasi gambar. |
| | [`docs/05-database.md`](./docs/05-database.md) | Skema database, changelog Liquibase, ERD, dan kamus data. |
| **Implementasi Kode** | [`docs/06-codebase-structure.md`](./docs/06-codebase-structure.md) | Panduan struktur direktori, ports, adapters, dan mappers. |
| | [`docs/07-api-integration.md`](./docs/07-api-integration.md) | Katalog lengkap REST API endpoint, request DTO, dan response DTO. |
| | [`docs/08-setup-build-deployment.md`](./docs/08-setup-build-deployment.md) | Panduan setup lokal, konfigurasi env, dan Docker deployment. |
| | [`docs/09-security-operations.md`](./docs/09-security-operations.md) | Mekanisme keamanan Spring Security, filter JWT, dan Redis session. |
| | [`docs/10-testing-troubleshooting.md`](./docs/10-testing-troubleshooting.md) | Panduan unit test, mocking, dan troubleshooting error umum. |
| | [`docs/11-technical-findings.md`](./docs/11-technical-findings.md) | Hasil audit kode teknis, mitigasi bug, dan technical debt. |
| **Tiket Tugas (Issues)** | [`docs/issues/API_TESTING_ISSUES.md`](./docs/issues/API_TESTING_ISSUES.md) | Spesifikasi 46 test case pengujian API fungsional (TEST-01 s/d TEST-06). |
| | [`docs/issues/UNIT_TEST_ISSUES.md`](./docs/issues/UNIT_TEST_ISSUES.md) | Roadmap 15 tiket pembuatan unit test (UT-01 s/d UT-15) target 100% coverage. |
| | [`docs/issues/PERFORMANCE_EVALUATION_ISSUES.md`](./docs/issues/PERFORMANCE_EVALUATION_ISSUES.md) | Tiket evaluasi performa server, database N+1/indeks, cache, dan load testing (PERF-01 s/d PERF-06). |
| | [`docs/PROJECT_PLAN.md`](./docs/PROJECT_PLAN.md) | Roadmap prioritas fitur modul dan port service pendukung. |

👉 Lihat **[`docs/README.md`](./docs/README.md)** untuk indeks navigasi dokumentasi yang lebih mendalam.
