# 10 - Testing & Troubleshooting

Dokumen ini menjelaskan struktur pengujian otomatis (*unit & integration test*), panduan eksekusi pengujian, alur bisnis yang wajib diuji, serta katalog masalah operasional umum (*troubleshooting*) beserta solusi yang telah diverifikasi.

---

## 1. Struktur Test Suite (Testing Architecture)

Pengujian pada proyek **Nuy Laundry** dibangun menggunakan **JUnit 5 (Jupiter)**, **Mockito**, dan **Spring MVC Test (MockMvc)**. Seluruh layer aplikasi diuji secara komprehensif dengan pendekatan unit testing terisolasi:

```
src/test/java/com/be_nuy_laundry/be_nuy_laundry/
├── presentation/
│   ├── controllers/         # MockMvc testing untuk setiap REST controller
│   ├── advices/             # GlobalExceptionHandler mapping test
│   └── validations/         # Validator (@ValidImage, file type, max size)
├── application/
│   └── services/            # Unit testing use case bisnis & orkestrasi port
├── domain/
│   └── utils/               # Invoice number generator & domain logic
├── common/
│   └── util/                # DateUtil, ValueUtil, ResponseUtil, PageUtil test
└── infrastructure/
    ├── internal/database/   # JPA Adapters, Mappers, dynamic Specifications
    ├── internal/security/   # JwtAuthFilter, JwtService, SecurityConfig
    ├── internal/cache/      # RedisCacheService, SessionCacheService
    └── external/minio/      # MinioStorageAdapter S3 integration test
```

### Statistik Test Suite Aktual
- **Total Test Cases**: **220 tests**
- **Test Failures / Errors**: **0**
- **Skipped**: **0**
- **JaCoCo Instruction Coverage**: **99.98%** (100% pada Controller, Service, Mapper, Adapter, Utility, Security).

---

## 2. Cara Menjalankan Pengujian

### Eksekusi Seluruh Test
```bash
./mvnw clean test
```

### Eksekusi Kelas Pengujian Tertentu
```bash
# Menjalankan pengujian spesifik service transaksi
./mvnw test -Dtest=TransactionServiceImplTest

# Menjalankan seluruh pengujian pada controller
./mvnw test -Dtest=*ControllerTest
```

### Verifikasi Laporan Coverage JaCoCo
Setelah eksekusi perintah `./mvnw test`, file laporan HTML akan dibuat secara otomatis pada direktori:
```
target/site/jacoco/index.html
```

---

## 3. Alur Bisnis Kritis yang Wajib Diuji (Critical Test Matrix)

Saat melakukan penambahan fitur atau *refactoring*, pastikan skenario berikut selalu ter-cover:

| Alur Bisnis | Skenario Pengujian Kritis | Test Target |
| :--- | :--- | :--- |
| **Autentikasi & Sesi** | 1. Login sukses menghasilkan token & refreshToken valid.<br>2. Login gagal karena password salah (401).<br>3. Request ditolak jika sesi berstatus inactive / revoked.<br>4. Refresh token memperbarui masa berlaku sesi. | `AuthServiceImplTest`<br>`JwtAuthFilterTest` |
| **Pembuatan Transaksi** | 1. Validasi customerId & packageId wajib eksis.<br>2. Kalkulasi totalAmount = sum(qty * price).<br>3. Format invoice unik `INV-YYYYMMDD-XXXX`.<br>4. Upload foto awal pakaian (kondisi sebelum). | `TransactionServiceImplTest`<br>`TransactionControllerTest` |
| **Siklus Status Order** | 1. Transisi sah: `WAITING` -> `PROCESS` -> `DONE` -> `DELIVERED`.<br>2. Transisi ilegal (misal langsung `WAITING` ke `DELIVERED` atau mundur) harus melempar `BadRequestException`. | `TransactionServiceImplTest` |
| **Validasi File Attachment**| 1. Penolakan file > 3 MB (`MaxUploadSizeExceededException`).<br>2. Penolakan MIME type terlarang (misal `.exe`, `.pdf`).<br>3. Pembersihan metadata DB saat file fisik dihapus dari S3. | `FileValidatorTest`<br>`TransactionFileServiceImplTest` |

---

## 4. Panduan Troubleshooting Masalah Operasional

Berikut adalah daftar masalah operasional umum yang sering ditemui saat menjalankan aplikasi di lingkungan lokal atau server beserta langkah mitigasi yang telah terverifikasi:

### A. Masalah: `Connection to localhost:5432 refused`
- **Gejala**: Aplikasi gagal startup dengan pesan `org.postgresql.util.PSQLException: Connection to localhost:5432 refused`.
- **Akar Masalah**: Kontainer database PostgreSQL belum berjalan atau port `5432` terblokir/bentrok dengan PostgreSQL lokal di host.
- **Solusi**:
  1. Pastikan kontainer database berjalan:
     ```bash
     docker compose up -d postgres
     ```
  2. Periksa status port dan log container:
     ```bash
     docker compose logs -f postgres
     ```

---

### B. Masalah: `LiquibaseException: Waiting for changelog lock...`
- **Gejala**: Aplikasi hang saat startup pada tahap migrasi database dengan pesan log:
  ```
  Waiting for changelog lock....
  ```
- **Akar Masalah**: Aplikasi sebelumnya dimatikan secara mendadak (SIGKILL/Crash) saat proses migrasi Liquibase sedang berlangsung, sehingga flag kunci pada tabel `databasechangeloglock` masih bernilai `LOCKED = TRUE`.
- **Solusi**:
  Jalankan perintah SQL berikut pada database PostgreSQL untuk melepaskan kunci:
  ```sql
  UPDATE databasechangeloglock SET locked = FALSE, lockgranted = NULL, lockedby = NULL WHERE id = 1;
  ```

---

### C. Masalah: `Unable to connect to Redis on localhost:6379`
- **Gejala**: Request API yang membutuhkan autentikasi menghasilkan HTTP 500 atau timeout karena `RedisConnectionFailureException`.
- **Akar Masalah**: Layanan Redis cache mati atau konfigurasi host/port tidak sesuai.
- **Solusi**:
  1. Jalankan kembali container Redis:
     ```bash
     docker compose up -d redis
     ```
  2. Uji ping redis melalui CLI:
     ```bash
     docker exec -it nuy-laundry-redis redis-cli ping
     # Output harus: PONG
     ```

---

### D. Masalah: `MinioException / SdkClientException: Connection Refused`
- **Gejala**: Endpoint upload file mengembalikan error saat mencoba melakukan `putObject` atau inisialisasi bucket.
- **Akar Masalah**: Server MinIO belum siap menerima request HTTP pada port 9000.
- **Solusi**:
  1. Jalankan container MinIO:
     ```bash
     docker compose up -d minio
     ```
  2. Verifikasi endpoint web console:
     Buka browser pada `http://localhost:9001` untuk memastikan service MinIO aktif dan bucket `nuy-laundry` telah tersedia.

---

### E. Masalah: `MaxUploadSizeExceededException` (File Terlalu Besar)
- **Gejala**: Client menerima response HTTP 400 atau connection reset saat mengunggah foto laundry resolusi tinggi.
- **Akar Masalah**: Ukuran file melebihi batas 3 MB yang dikonfigurasi pada Spring Multipart Resolver dan anotasi `@ValidImage`.
- **Solusi**:
  1. Pastikan file gambar dikompresi di sisi frontend sebelum diunggah (< 3 MB).
  2. Jika batas ingin diperbesar, sesuaikan parameter pada `application.properties`:
     ```properties
     spring.servlet.multipart.max-file-size=5MB
     spring.servlet.multipart.max-request-size=5MB
     ```
     dan perbarui konstanta `MAX_FILE_SIZE` pada `ImageValidator.java`.

---

### F. Masalah: JWT Token Expired / Session Revoked
- **Gejala**: Request mengembalikan HTTP 401 Unauthorized dengan pesan `Session expired` atau `Session revoked`.
- **Akar Masalah**: Token sudah melewati masa berlaku atau status `is_active` record pada tabel `user_sessions` telah di-set `false` (logout).
- **Solusi**:
  Lakukan autentikasi ulang via `POST /api/auth/login` untuk mendapatkan token JWT baru, atau panggil `POST /api/auth/refresh-token`.
