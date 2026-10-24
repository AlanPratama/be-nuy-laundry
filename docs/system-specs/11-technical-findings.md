# 11 - Technical Findings & Code Audit

Dokumen ini mencatat seluruh temuan teknis, potensi *bug*, celah keamanan (*security concern*), ketidakefisienan performa, konfigurasi mati (*dead config*), serta *technical debt* yang ditemukan selama audit mendalam terhadap basis kode backend **Nuy Laundry**.

Setiap temuan dilengkapi dengan **lokasi kode**, **bukti implementasi aktual**, **dampak**, **tingkat prioritas**, dan **rekomendasi perbaikan**.

---

## 1. Ringkasan Temuan (Findings Summary)

| ID | Kategori | Judul Temuan | Tingkat Prioritas | Status |
| :--- | :--- | :--- | :--- | :--- |
| **FIND-01** | Logic Bug | Logika Terbalik pada Validasi `invoiceNo` saat Upload File | **CRITICAL** | Terbuka |
| **FIND-02** | Security | Endpoint `/api/v1/users/**` Diberi Akses Terbuka (`permitAll`) | **HIGH** | Terbuka |
| **FIND-03** | API Routing | Mismatch Antara URI Template `{id}` dan `@PathVariable benefitId` | **MEDIUM** | Terbuka |
| **FIND-04** | Architecture | Transaksi Database Menahan Operasi I/O Upload MinIO S3 | **MEDIUM** | Terbuka |
| **FIND-05** | Reliability | Ketiadaan Idempotency Control pada Pembuatan Transaksi | **MEDIUM** | Terbuka |
| **FIND-06** | Dead Config | Konfigurasi RabbitMQ Aktif Tanpa Implementasi AMQP di Codebase | **LOW** | Terbuka |
| **FIND-07** | Database Schema | Inkonsistensi Naming Convention Kolom: camelCase vs snake_case | **LOW** | Terbuka |
| **FIND-08** | Code Hygiene | File Stub Kosong Tanpa Implementasi: `FileController.java` | **LOW** | Terbuka |

---

## 2. Rincian Temuan Teknis

---

### FIND-02: Endpoint User Diberi Akses Terbuka (`permitAll`) Tanpa Autentikasi
- **Lokasi Kode**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/SecurityConfig.java` (Baris 30).
- **Bukti Kode Aktual**:
  ```java
  auth.requestMatchers("/api/auth/**").permitAll()
      .requestMatchers("/api/v1/users/**").permitAll()
      .anyRequest().authenticated()
  ```
- **Analisis & Bukti**:
  Konfigurasi Spring Security secara eksplisit membebaskan seluruh rute `/api/v1/users/**` dari kewajiban login. Akibatnya, method `POST` (buat user), `GET` (daftar seluruh user), `PUT` (edit profil), `DELETE` (hapus user), dan `PATCH` (reset password user) dapat diakses oleh publik tanpa Bearer token.
- **Dampak**:
  Potensi kebocoran data pengguna (PII seperti nomor telepon dan alamat) serta risiko pengambilalihan akun atau perusakan data pengguna oleh pihak luar.
- **Tingkat Prioritas**: **HIGH**
- **Rekomendasi**:
  1. Hapus `.requestMatchers("/api/v1/users/**").permitAll()` dari `SecurityConfig.java`.
  2. Lindungi endpoint user dengan method security, misalnya:
     - `POST /api/v1/users` dan `DELETE /api/v1/users/{id}`: `@PreAuthorize("hasRole('SUPER_ADMIN')")`.
     - `GET /api/v1/users`: `@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")`.

---

### FIND-04: Transaksi Database Menahan Operasi I/O Upload MinIO S3
- **Lokasi Kode**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionFileServiceImpl.java` (Baris 71–85).
- **Bukti Kode Aktual**:
  ```java
  @Transactional
  public List<TransactionFileModel> uploadTransactionFile(...) {
      ...
      fileStoragePort.uploadFile(paymentReceiptFile, path); // Blocking S3 I/O
      ...
      transactionFileDatabasePort.save(fileModel);          // DB Write
  }
  ```
- **Analisis & Bukti**:
  Metode diberi anotasi `@Transactional`. Koneksi database (HikariCP connection pool) diambil sejak awal metode dan ditahan selama upload file fisik ke MinIO S3 berlangsung.
- **Dampak**:
  Jika client mengunggah banyak file gambar atau terjadi latensi jaringan menuju MinIO, koneksi database tertahan lebih lama dari seharusnya (*connection pool starvation*), mengurangi throughput keseluruhan API.
- **Tingkat Prioritas**: **MEDIUM**
- **Rekomendasi**:
  Pisahkan operasi upload file fisik ke MinIO di luar boundary `@Transactional`, atau simpan metadata terlebih dahulu dan lakukan upload fisik secara asinkron atau menggunakan direct S3 Presigned URL dari client.

---

### FIND-05: Ketiadaan Idempotency Control pada Pembuatan Transaksi
- **Lokasi Kode**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionServiceImpl.java`.
- **Analisis & Bukti**:
  Endpoint `POST /api/v1/transactions` tidak membaca header `Idempotency-Key` dan tidak memvalidasi duplikasi request dalam selang waktu singkat (*double submit*).
- **Dampak**:
  Jika kasir atau staf menekan tombol checkout dua kali berturut-turut karena koneksi internet lambat, sistem akan mencetak dua transaksi dan dua nomor invoice berbeda untuk pakaian yang sama.
- **Tingkat Prioritas**: **MEDIUM**
- **Rekomendasi**:
  Terapkan filter Idempotency berbasis Redis dengan menyimpan `Idempotency-Key` berumur pendek (misal: 60 detik) untuk menduplikasi respon tanpa memproses ulang transaksi.

---