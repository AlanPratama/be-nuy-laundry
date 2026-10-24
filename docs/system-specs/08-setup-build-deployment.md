# 08 - Configuration, Build & Deployment

Dokumen ini menyediakan panduan langkah-demi-langkah bagi pengembang untuk menyiapkan *local development environment*, membangun artefak aplikasi, menjalankan *test suite*, serta mengelola kontainerisasi dan proses deployment pada sistem **Nuy Laundry**.

---

## 1. Prasyarat Software (Prerequisites)

Sebelum menjalankan aplikasi, pastikan sistem pengembang telah terpasang:

| Perangkat Lunak | Versi Minimal / Direkomendasikan | Catatan Verifikasi |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | OpenJDK / Temurin 17 | `java -version` (kompatibel Java 17 LTS) |
| **Maven** | 3.9+ (Opsional) | Tersedia `mvnw` (Maven Wrapper) bawaan repo |
| **Docker Engine** | 24.0+ | Diperlukan untuk container database & service |
| **Docker Compose** | v2.20+ | `docker compose version` |
| **Git** | 2.30+ | Version control |

---

## 2. Pengaturan Environment & Konfigurasi (Configuration)

Konfigurasi aplikasi dikelola melalui file `src/main/resources/application.properties` dengan fallback nilai default lokal dan dukungan *environment variable injection* untuk container/cloud deployment:

### Daftar Environment Variable yang Didukung

| Environment Variable | Nilai Default Lokal | Deskripsi & Target Penggunaan |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Port HTTP REST API server |
| `DB_HOST` | `localhost` | Host database PostgreSQL |
| `DB_PORT` | `5432` | Port database PostgreSQL |
| `DB_NAME` | `nuy_laundry` | Nama database PostgreSQL |
| `DB_USER` | `postgres` | Username database PostgreSQL |
| `DB_PASSWORD` | *(Nilai default lokal)* | Password database PostgreSQL |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Host server Redis |
| `SPRING_DATA_REDIS_PORT` | `6379` | Port server Redis |
| `MINIO_URL` | `http://localhost:9000` | Endpoint API S3 MinIO |
| `MINIO_USERNAME` | `admin` | Access key MinIO |
| `MINIO_PASSWORD` | *(Nilai default lokal)* | Secret key MinIO |
| `MINIO_BUCKETNAME` | `nuy-laundry` | Nama bucket file attachment |
| `JWT_SECRET` | *(64-hex string default)* | Kunci simetris HMAC-SHA256 untuk tanda tangan JWT |
| `SPRING_RABBITMQ_HOST` | `localhost` | Host RabbitMQ (Konfigurasi standby) |
| `SPRING_RABBITMQ_PORT` | `5672` | Port RabbitMQ AMQP |

> [!NOTE]
> Proyek saat ini hanya menggunakan satu file konfigurasi (`application.properties`). Tidak terdapat file profile terpisah seperti `application-dev.properties` atau `application-prod.properties`.

---

## 3. Langkah Setup Lingkungan Lokal (Step-by-Step)

### Langkah 1: Kloning Repository
```bash
git clone <repository_url>
cd be-nuy-laundry
```

### Langkah 2: Menjalankan Layanan Dependensi via Docker Compose
Jalankan kontainer pendukung (PostgreSQL, Redis, RabbitMQ, dan MinIO):
```bash
docker compose up -d
```

Periksa status kesehatan (*health status*) seluruh kontainer:
```bash
docker compose ps
```
*Pastikan keempat kontainer berada dalam status `Up (healthy)`.*

### Langkah 3: Menjalankan Aplikasi Spring Boot
Jalankan aplikasi menggunakan Maven Wrapper:
```bash
./mvnw spring-boot:run
```
Saat pertama kali dijalankan, Liquibase akan secara otomatis mengeksekusi changelog master (`db.changelog-master.yaml`) dan membentuk seluruh skema tabel, relasi foreign key, dan index.

Aplikasi siap menerima request pada:
```
http://localhost:8080
```

---

## 4. Perintah Build & Eksekusi Pengujian (Testing)

### Menjalankan Unit & Integration Test
Proyek dilengkapi dengan 220 skenario pengujian komprehensif. Jalankan pengujian dengan perintah:
```bash
./mvnw clean test
```

### Menghasilkan Laporan Code Coverage (JaCoCo)
Coverage report akan secara otomatis dibuat saat proses test selesai. Untuk membukanya di browser:
```bash
# macOS
open target/site/jacoco/index.html

# Linux
xdg-open target/site/jacoco/index.html
```

### Membangun Paket Standalone JAR (Production Build)
Untuk membuat artefak executable JAR:
```bash
./mvnw clean package -DskipTests
```
Artefak JAR akan berada di folder `target/be_nuy_laundry-0.0.1-SNAPSHOT.jar`.

---

## 5. Kontainerisasi (Docker Build & Multi-Stage Deployment)

Aplikasi memiliki `Dockerfile` berbasis multi-stage build untuk menghasilkan image runtime yang ramping dan aman:

### Struktur Dockerfile
1. **Stage 1 (Build)**: Menggunakan `eclipse-temurin:17-jdk-noble`. Menyalin `pom.xml`, mengunduh dependensi (`dependency:go-offline`), dan melakukan kompilasi (`package -DskipTests`).
2. **Stage 2 (Runtime)**: Menggunakan `eclipse-temurin:17-jre-noble` (hanya JRE, tanpa compiler). Menyalin artefak `app.jar`, mengekspos port `8080`, dan menjalankan container dengan user non-root.

### Membangun & Menjalankan Docker Image
```bash
# Build image
docker build -t nuy-laundry-backend:1.0 .

# Menjalankan container dengan menghubungkannya ke docker network pendukung
docker run -d \
  --name nuy-laundry-api \
  --network be-nuy-laundry_default \
  -p 8080:8080 \
  -e DB_HOST=postgres \
  -e SPRING_DATA_REDIS_HOST=redis \
  -e MINIO_URL=http://minio:9000 \
  nuy-laundry-backend:1.0
```

> [!TIP]
> Terdapat blok definisi service `app` yang telah disiapkan pada file `docker-compose.yaml` (saat ini masih berstatus non-aktif/dikomentari). Developer dapat mengaktifkan blok tersebut jika ingin menjalankan seluruh stack (API + DB + Redis + MinIO) hanya dengan satu perintah `docker compose up -d`.

---

## 6. Status CI/CD Pipeline

- **Status Faktual**: **Belum Dikonfigurasi**.
- **Hasil Pemeriksaan**: Tidak ditemukan direktori `.github/workflows/`, `.gitlab-ci.yml`, maupun skrip Jenkins pada repository.
- **Rekomendasi**: Disarankan untuk menambahkan workflow GitHub Actions minimal untuk menjalankan `./mvnw clean test` dan JaCoCo coverage check pada setiap *Pull Request*.

---

## 7. Pemantauan & Logging Operasional

- **Log Format**: Format teks konsol standar Spring Boot dengan penanda warna ANSI (*highlighted*).
- **Hibernate SQL Logging**: Diaktifkan secara default pada `application.properties`:
  ```properties
  spring.jpa.show-sql=true
  spring.jpa.properties.hibernate.format_sql=true
  spring.jpa.properties.hibernate.highlight_sql=true
  ```
- **Liquibase Logging**: Level logging diatur pada `DEBUG` untuk memudahkan pelacakan migrasi DDL saat startup.
- **Health Check Endpoint**: Aplikasi saat ini belum menyertakan modul `spring-boot-starter-actuator` (`/actuator/health`). Pengecekan liveness aplikasi dapat dilakukan dengan memanggil endpoint auth publik (`POST /api/auth/login`).
