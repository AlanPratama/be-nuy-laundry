Tolong kamu buatkan dokumentasi mengenai projek ini agar junior developer atau ai selanjutnya dapat dengan mudah memahami konteks nya.

# Task: Technical Documentation & System Analysis

## 1. Objective

Buat dokumentasi teknis yang komprehensif mengenai project ini agar developer baru dapat memahami arsitektur, business flow, struktur kode, database, integrasi, serta proses menjalankan dan melakukan troubleshooting aplikasi.

Dokumentasi harus berdasarkan implementasi aktual pada repository, bukan asumsi atau best practice yang belum tentu diterapkan.

## 2. Scope of Documentation

### A. Project Overview & Technology Stack
Dokumentasikan:
- Tujuan dan fungsi utama aplikasi.
- Versi Java, JDK, Spring Boot, dan framework terkait.
- Build tools beserta versinya.
- Database, driver, ORM, connection pool, dan konfigurasi terkait.
- Message broker, cache, file storage, scheduler, dan teknologi pendukung lainnya jika digunakan.
- Library penting beserta fungsi dan versinya.
- Sistem eksternal yang terintegrasi dengan aplikasi.

### B. Architecture & System Flow
Jelaskan:
- Arsitektur sistem secara keseluruhan.
- Hubungan frontend, middleware, gateway, backend, database, dan sistem eksternal.
- Tanggung jawab setiap service atau module.
- Alur request dari awal sampai response.
- Komunikasi synchronous dan asynchronous.
- Authentication, authorization, logging, dan error handling.

Sertakan diagram arsitektur dan sequence diagram untuk alur utama.

### C. Business Flow
Dokumentasikan business flow untuk:
- User karyawan.
- User admin.
- Role pengguna lainnya jika tersedia.
- Proses input, submit, approval, rejection, dan perubahan status jika relevan.
- Hak akses setiap role.
- Validasi bisnis, kondisi gagal, dan mekanisme retry.

Gunakan flowchart atau sequence diagram agar mudah dipahami.

### D. File Upload & Download Flow
Jelaskan:
- Endpoint dan mekanisme upload.
- Struktur request, multipart field, DTO, dan validasi.
- Batas ukuran dan tipe file yang diperbolehkan.
- Lokasi penyimpanan file.
- Proses penyimpanan metadata dan relasi ke database.
- Mekanisme download, preview, dan penghapusan.
- Access control dan error handling.
- Penanganan file gagal diunggah atau penyimpanan gagal.

### E. Database & Data Dictionary
Dokumentasikan:
- Schema, tabel, view, sequence, dan stored procedure yang digunakan.
- Fungsi tabel dan kolom-kolom penting.
- Primary key, foreign key, index, dan relasi data.
- ERD jika memungkinkan.
- Query utama untuk business flow penting.
- Proses insert, update, delete, dan transaksi.
- Tabel konfigurasi dan audit log.

Prioritaskan objek database yang benar-benar digunakan oleh aplikasi.

### F. Codebase & Module Structure
Jelaskan:
- Struktur folder dan module.
- Fungsi controller, service, repository, entity, DTO, mapper, dan utility.
- Lokasi business logic utama.
- Dependency antar-module.
- Exception handling, validation, security, dan konfigurasi.
- Class atau method penting beserta tanggung jawabnya.

### G. API & External Integration
Buat inventaris endpoint yang mencakup:
- HTTP method dan URL.
- Fungsi endpoint.
- Request dan response.
- Authentication dan authorization.
- HTTP status dan error response.
- Service atau repository yang dipanggil.
- Integrasi eksternal, timeout, retry, dan circuit breaker jika digunakan.

Sertakan contoh request dan response dengan data dummy yang aman.

### H. Configuration, Build & Deployment
Jelaskan:
- Prasyarat software dan versi.
- Cara setup environment lokal.
- Cara build dan menjalankan aplikasi.
- Cara menjalankan unit test dan integration test.
- Environment profile yang tersedia.
- Konfigurasi yang wajib disediakan.
- CI/CD pipeline dan deployment flow jika dapat diakses.
- Health check, logging, dan troubleshooting.

Jangan mencantumkan nilai credential atau secret apa pun.

### I. Security & Operational Behavior
Jelaskan:
- Mekanisme authentication dan authorization.
- Pengelolaan secret dan environment variable.
- Logging, audit trail, dan correlation ID jika tersedia.
- Masking data sensitif.
- Transaction management.
- Concurrency, idempotency, scheduler, asynchronous processing, dan mekanisme retry jika diterapkan.

### J. Testing, Troubleshooting & Technical Findings
Dokumentasikan:
- Struktur dan cara menjalankan test.
- Business flow yang perlu diuji.
- Error umum beserta penyebab dan solusi yang sudah diverifikasi.
- Query atau kode yang berpotensi tidak efisien.
- Potensi bottleneck, risiko bug, dan security concern.
- Technical debt serta area yang perlu investigasi lanjutan.

Untuk setiap temuan, sertakan lokasi kode, bukti, dampak, tingkat prioritas, dan rekomendasi. Jangan melakukan perubahan kode atau database sebagai bagian dari tugas ini.

## 3. Rules & Constraints

1. Analisis repository dan konfigurasi aktual sebelum menulis dokumentasi.
2. Jangan mengarang versi teknologi, alur bisnis, relasi database, atau mekanisme yang belum terbukti.
3. Setiap informasi teknis penting harus dapat ditelusuri ke file, class, method, endpoint, atau konfigurasi terkait.
4. Bedakan fakta terverifikasi, asumsi, dan informasi yang belum diketahui.
5. Jangan menampilkan password, token, private key, credential, data pelanggan, atau informasi rahasia lainnya.
6. Jangan mengubah source code, dependency, konfigurasi aplikasi, atau database.
7. Jangan menjalankan operasi yang dapat mengubah data atau mengganggu environment.
8. Jika menemukan potensi bug atau masalah performa, catat sebagai temuan untuk direview, bukan langsung diperbaiki.
9. Gunakan bahasa Indonesia yang jelas dan konsisten. Istilah teknis boleh menggunakan bahasa Inggris.
10. Jangan hanya menjelaskan apa yang seharusnya dilakukan sistem; jelaskan apa yang benar-benar dilakukan implementasi saat ini.

## 4. Deliverables

Buat dokumentasi dalam folder `docs/` pada repository, dengan struktur awal:

- `docs/README.md` — indeks dokumentasi.
- `docs/01-project-overview.md`
- `docs/02-architecture.md`
- `docs/03-business-flow.md`
- `docs/04-file-management.md`
- `docs/05-database.md`
- `docs/06-codebase-structure.md`
- `docs/07-api-integration.md`
- `docs/08-setup-build-deployment.md`
- `docs/09-security-operations.md`
- `docs/10-testing-troubleshooting.md`
- `docs/11-technical-findings.md`

Sertakan diagram Mermaid apabila sesuai, terutama untuk arsitektur, sequence flow, business flow, dan relasi data.

Jika ada bagian yang tidak relevan atau tidak dapat diverifikasi, jelaskan alasannya. Jangan mengisi dokumentasi dengan informasi fiktif hanya untuk memenuhi daftar.

## 5. Definition of Done

Dokumentasi dinyatakan selesai apabila:

- Seluruh bagian yang relevan telah dibuat.
- Versi teknologi dan dependensi sudah diverifikasi dari sumber yang tepat.
- Business flow dan architecture flow sesuai dengan implementasi.
- Endpoint dan komponen utama dapat ditelusuri ke source code.
- Dokumentasi database tidak mengandung relasi atau query yang dikarang.
- Tidak ada secret atau data sensitif yang terekspos.
- Instruksi setup dan testing dapat diikuti atau keterbatasannya dijelaskan.
- Technical findings memiliki bukti dan rekomendasi yang jelas.
- Tidak ada perubahan fungsional pada aplikasi.
- Seluruh dokumen telah direview sebelum dinyatakan final.

## 6. Expected Outcome

Hasil akhir harus dapat digunakan oleh developer baru untuk:
1. Memahami sistem secara menyeluruh.
2. Menemukan lokasi implementasi suatu fitur.
3. Menelusuri request dari frontend hingga database atau sistem eksternal.
4. Menjalankan aplikasi dan test secara lokal.
5. Memahami struktur data dan business flow.
6. Mengidentifikasi risiko teknis dan area yang membutuhkan perbaikan.

Prioritaskan akurasi, keterlacakan, dan kemudahan pemeliharaan dokumentasi daripada panjang dokumen.