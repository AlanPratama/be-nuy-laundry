# Katalog Issue: Implementasi Role-Based Access Control (RBAC) & Otorisasi

Dokumen ini berisi spesifikasi teknis dan daftar issue/tiket kerja untuk mengimplementasikan **Role-Based Access Control (RBAC)** secara menyeluruh pada backend **BE Nuy Laundry**. Dibuat dengan bahasa **high-level, jelas, dan berorientasi aksi** agar mudah dieksekusi oleh *junior developer* maupun agen AI otomatis.

---

## 📌 Ringkasan Masalah & Analisis Teknis (Root Cause)

Sebelumnya, konfigurasi otorisasi peran (*role*) sempat dicoba pada [SecurityConfig.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/SecurityConfig.java), namun mengalami masalah akses (*blocked / unexpected error*). Berikut adalah akar penyebabnya berdasarkan audit kode:

1. **Endpoint User Terekspos Penuh Tanpa Proteksi**:
   Pada `SecurityConfig.java`, baris `requestMatchers("/api/v1/users/**").permitAll()` membuat seluruh operasi user (termasuk tambah user, ubah data, ganti password, dan hapus) bisa diakses publik tanpa login sama sekali.
2. **Ketiadaan Penanganan `AccessDeniedException`**:
   Ketika user dengan role yang tidak berhak mengakses suatu method beranotasi `@PreAuthorize`, Spring Security melemparkan `org.springframework.security.access.AccessDeniedException`. Karena [GlobalExceptionHandler.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/advices/GlobalExceptionHandler.java) belum menangkap exception tersebut secara spesifik, error tersebut tertangkap oleh handler umum `Exception.class` dan mengembalikan **HTTP 500 (Internal Server Error)**, bukan **HTTP 403 (Forbidden)** dengan format respons standar.
3. **Konflik Antara Filter Chain & Method Security**:
   Pencampuran antara aturan di `SecurityFilterChain` (`.requestMatchers(...).hasAnyRole(...)`) dan anotasi `@PreAuthorize` di controller tanpa pola yang konsisten membuat debugging sulit. Best practice di Spring Boot 3 adalah:
   - `SecurityConfig` mengatur perimeter dasar: endpoint mana yang **Public** (`permitAll()`) dan sisanya wajib login (`authenticated()`).
   - Otorisasi spesifik per peran ditangani menggunakan anotasi deklaratif `@PreAuthorize("hasRole('ADMIN')")` atau `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")` langsung pada level Controller method.
4. **Format Role Prefix**:
   Di [JwtAuthFilter.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/JwtAuthFilter.java), role dipetakan sebagai `ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, dan `ROLE_STAFF`. Anotasi `hasRole('ADMIN')` otomatis memeriksa prefix `ROLE_`, sehingga nama role yang dipassing di ekspresi SpEL **tidak boleh** menyertakan kata `ROLE_` secara manual.

---

## 👥 Matriks Hak Akses Peran Pengguna (RBAC Matrix)

Sistem memiliki 3 tingkat peran di enum [UserRole.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/database/entity/enumrated/UserRole.java):
1. **`SUPER_ADMIN`**: Pemilik bisnis / Administrator tertinggi. Memiliki seluruh akses sistem.
2. **`ADMIN`**: Pengelola operasional cabang & data master (termasuk manajemen user/karyawan dan paket).
3. **`STAFF`**: Kasir dan staf operasional harian. Fokus pada pelayanan pelanggan dan pencatatan transaksi.

| Modul / Fitur | Endpoint & HTTP Method | SUPER_ADMIN | ADMIN | STAFF | Keterangan Aturan |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **Autentikasi** | `POST /api/auth/login` | ✅ Publik | ✅ Publik | ✅ Publik | Bebas akses |
| | `POST /api/auth/refresh-token` | ✅ Publik | ✅ Publik | ✅ Publik | Bebas akses |
| | `POST /api/auth/logout` | ✅ | ✅ | ✅ | Wajib login (Bearer Token) |
| | `GET /api/auth/me` | ✅ | ✅ | ✅ | Wajib login (Bearer Token) |
| **Manajemen User (Operasi Ubah Data)** | `POST /api/v1/users` | ✅ | ✅ | ❌ (*403*) | **Khusus Admin & Super Admin** |
| | `PUT /api/v1/users/{id}` | ✅ | ✅ | ❌ (*403*) | **Khusus Admin & Super Admin** |
| | `DELETE /api/v1/users/{id}` | ✅ | ✅ | ❌ (*403*) | **Khusus Admin & Super Admin** |
| | `PATCH /api/v1/users/{id}` | ✅ | ✅ | ❌ (*403*) | **Khusus Admin & Super Admin** (Reset Password) |
| **Manajemen User (Melihat Data)** | `GET /api/v1/users` | ✅ | ✅ | ❌ (*403*) | Khusus Admin & Super Admin |
| | `GET /api/v1/users/{id}` | ✅ | ✅ | ❌ (*403*) | Khusus Admin & Super Admin |
| **Manajemen Paket** | `POST /api/v1/packages` | ✅ | ✅ | ❌ (*403*) | Khusus Admin & Super Admin |
| | `PUT /api/v1/packages/{id}` | ✅ | ✅ | ❌ (*403*) | Khusus Admin & Super Admin |
| | `DELETE /api/v1/packages/{id}` | ✅ | ✅ | ❌ (*403*) | Khusus Admin & Super Admin |
| | `GET /api/v1/packages/**` | ✅ | ✅ | ✅ | Semua staf bisa melihat paket |
| **Manajemen Benefit** | `POST /api/v1/benefits` | ✅ | ✅ | ❌ (*403*) | Khusus Admin & Super Admin |
| | `PUT /api/v1/benefits/{id}` | ✅ | ✅ | ❌ (*403*) | Khusus Admin & Super Admin |
| | `DELETE /api/v1/benefits/{id}` | ✅ | ✅ | ❌ (*403*) | Khusus Admin & Super Admin |
| | `GET /api/v1/benefits/**` | ✅ | ✅ | ✅ | Semua staf bisa melihat benefit |
| **Pelanggan (Customer)** | `CRUD /api/v1/customers/**` | ✅ | ✅ | ✅ | Seluruh staf operasional bisa akses |
| **Transaksi (Order)** | `CRUD /api/v1/transactions/**` | ✅ | ✅ | ✅ | Seluruh staf operasional bisa akses |
| **Detail Transaksi** | `DELETE /api/v1/transaction-details/**` | ✅ | ✅ | ✅ | Seluruh staf operasional bisa akses |
| **Berkas Transaksi** | `POST & DELETE /api/v1/transaction-files/**` | ✅ | ✅ | ✅ | Seluruh staf operasional bisa akses |
| **Actuator & Metrik** | `GET /actuator/health` | ✅ Publik | ✅ Publik | ✅ Publik | Monitoring liveness & readiness |
| | `GET /actuator/prometheus` | ✅ | ✅ | ❌ (*403*) | Hanya internal admin/monitoring |

---

## 🛠️ Daftar Tiket Tugas (Actionable Issues)

| Kode Tiket | Judul Tugas | Prioritas | Estimasi |
| :--- | :--- | :---: | :---: |
| **RBAC-01** | Penataan Perimeter Keamanan di `SecurityConfig` | Tinggi | 1-2 jam |
| **RBAC-02** | Penanganan Standar Respons 403 Forbidden & 401 Unauthorized | Tinggi | 1-2 jam |
| **RBAC-03** | Proteksi Otorisasi pada `UserController` (Operasi Data Khusus Admin) | **Kritis** | 2 jam |
| **RBAC-04** | Audit & Penegakan Anotasi `@PreAuthorize` pada Controller Lainnya | Sedang | 2 jam |
| **RBAC-05** | Penulisan Skenario Automated Test Otorisasi & Verifikasi Regresi | Tinggi | 3 jam |

---

### [RBAC-01] Penataan Perimeter Keamanan di `SecurityConfig`

**Tujuan**
Memastikan `SecurityConfig.java` hanya mengatur perimeter otorisasi dasar (jalur publik vs jalur privat) dan mendelegasikan izin spesifik role ke controller melalui method security.

**Langkah Pengerjaan**
1. Buka [SecurityConfig.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/SecurityConfig.java).
2. Hapus baris `.requestMatchers("/api/v1/users/**").permitAll()` karena endpoint user tidak boleh terbuka untuk umum.
3. Pastikan endpoint publik hanya:
   - `/api/auth/**` (login, refresh-token)
   - `/actuator/health`, `/actuator/health/**` (health check)
   - Jalur dokumentasi jika ada (misal `/swagger-ui/**`, `/v3/api-docs/**`)
4. Pastikan semua rute lainnya wajib terautentikasi: `.anyRequest().authenticated()`.
5. Pastikan anotasi `@EnableMethodSecurity(prePostEnabled = true)` aktif pada kelas `SecurityConfig`.

**Kriteria Lolos**
- Request ke `/api/v1/users` tanpa membawa Bearer Token langsung ditolak dengan HTTP 401/403 (tidak lagi 200).
- Request ke `/api/auth/login` tetap bisa diakses tanpa token.

---

### [RBAC-02] Penanganan Standar Respons 403 Forbidden & 401 Unauthorized

**Tujuan**
Menghilangkan error HTTP 500 saat role tidak cocok dan mengembalikan format JSON standar `ErrorResponse` sesuai arsitektur sistem.

**Langkah Pengerjaan**
1. Buka [GlobalExceptionHandler.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/advices/GlobalExceptionHandler.java).
2. Tambahkan handler khusus untuk `org.springframework.security.access.AccessDeniedException`:
   ```java
   @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
   public ResponseEntity<ErrorResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
       log.warn("[AccessDeniedException] Forbidden access: {}", ex.getMessage());
       return ResponseUtil.errorResponse("Anda tidak memiliki hak akses untuk fitur ini", HttpStatus.FORBIDDEN);
   }
   ```
3. Tambahkan custom `AuthenticationEntryPoint` dan `AccessDeniedHandler` pada konfigurasi HttpSecurity di `SecurityConfig.java` untuk menangani penolakan di level filter (misal token kadaluarsa atau format salah):
   ```java
   .exceptionHandling(ex -> ex
       .authenticationEntryPoint((request, response, authException) -> {
           response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
           response.setContentType(MediaType.APPLICATION_JSON_VALUE);
           response.getWriter().write("{\"status\":false,\"message\":\"Autentikasi gagal atau token tidak valid\",\"errors\":null,\"data\":null}");
       })
       .accessDeniedHandler((request, response, accessDeniedException) -> {
           response.setStatus(HttpServletResponse.SC_FORBIDDEN);
           response.setContentType(MediaType.APPLICATION_JSON_VALUE);
           response.getWriter().write("{\"status\":false,\"message\":\"Akses ditolak: role tidak memiliki izin\",\"errors\":null,\"data\":null}");
       })
   )
   ```

**Kriteria Lolos**
- Ketika pengguna dengan role `STAFF` memanggil endpoint khusus `ADMIN`, respons yang diterima berupa HTTP 403 Forbidden berformat JSON standar (bukan HTML Tomcat atau HTTP 500).

---

### [RBAC-03] Proteksi Otorisasi pada `UserController`

**Tujuan**
Mengunci semua operasi pembuatan, pengubahan, penghapusan, dan reset password user hanya untuk pengguna dengan role `ADMIN` atau `SUPER_ADMIN`.

**Langkah Pengerjaan**
1. Buka [UserController.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/UserController.java).
2. Berikan proteksi pada method yang **mengubah data** menggunakan `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`:
   - `create`: `POST /api/v1/users`
   - `update`: `PUT /api/v1/users/{id}`
   - `delete`: `DELETE /api/v1/users/{id}`
   - `resetPassword`: `PATCH /api/v1/users/{id}`
3. Berikan proteksi juga pada method yang **melihat daftar akun pegawai**:
   - `getAll`: `GET /api/v1/users` -> `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
   - `getById`: `GET /api/v1/users/{id}` -> `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`
   *(Catatan: Pegawai biasa dapat melihat profil dirinya sendiri melalui endpoint `GET /api/auth/me`).*

**Kriteria Lolos**
- Token dengan role `STAFF` mencoba `POST /api/v1/users` -> Ditolak **403 Forbidden**.
- Token dengan role `STAFF` mencoba `PATCH /api/v1/users/{id}` -> Ditolak **403 Forbidden**.
- Token dengan role `ADMIN` atau `SUPER_ADMIN` berhasil melakukan operasi CRUD user (HTTP 200/201).

---

### [RBAC-04] Audit & Penegakan Anotasi `@PreAuthorize` pada Controller Lainnya

**Tujuan**
Menyelaraskan controller lain agar sesuai dengan matriks bisnis tanpa memblokir alur operasional kasir/staf.

**Langkah Pengerjaan**
1. **PackageController** ([PackageController.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/PackageController.java)):
   - Verifikasi `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")` pada `create`, `update`, `delete`.
   - Pastikan method `getById`, `getAll`, dan `getByBenefitId` **TIDAK** diberi `@PreAuthorize` restriktif agar staf kasir dapat melihat daftar paket saat membuat transaksi.
2. **BenefitController** ([BenefitController.java](../../src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/BenefitController.java)):
   - Tambahkan `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")` pada method `create` (POST), `update` (PUT), dan `delete` (DELETE).
   - Biarkan method baca (`getAll`, `getById`) dapat diakses seluruh authenticated user.
3. **CustomerController, TransactionController, TransactionFileController**:
   - Biarkan terbuka untuk semua authenticated user (`STAFF`, `ADMIN`, `SUPER_ADMIN`) karena ini adalah fungsi inti operasional kasir.

**Kriteria Lolos**
- Staf kasir (`STAFF`) tetap bisa membuat transaksi dan membaca daftar paket tanpa kendala.
- Staf kasir (`STAFF`) ditolak saat mencoba menambah atau menghapus paket / benefit.

---

### [RBAC-05] Penulisan Skenario Automated Test Otorisasi & Verifikasi Regresi

**Tujuan**
Memvalidasi seluruh skenario positif dan negatif otorisasi peran secara otomatis dan memastikan tidak ada regresi pada tes yang sudah ada.

**Langkah Pengerjaan**
1. Perbarui runner pengujian [scripts/test_runner.py](../../scripts/test_runner.py) dengan menambahkan suite **TEST-07: Testing Hak Akses & RBAC**:
   - `TC-RBAC-01`: User role `STAFF` ditolak saat `POST /api/v1/users` (Harus 403 Forbidden).
   - `TC-RBAC-02`: User role `STAFF` ditolak saat `PUT /api/v1/users/{id}` (Harus 403 Forbidden).
   - `TC-RBAC-03`: User role `STAFF` ditolak saat `DELETE /api/v1/users/{id}` (Harus 403 Forbidden).
   - `TC-RBAC-04`: User role `STAFF` ditolak saat `PATCH /api/v1/users/{id}` (Harus 403 Forbidden).
   - `TC-RBAC-05`: User role `STAFF` ditolak saat `POST /api/v1/packages` (Harus 403 Forbidden).
   - `TC-RBAC-06`: User role `ADMIN` berhasil melakukan `POST /api/v1/users` (Harus 200 OK).
   - `TC-RBAC-07`: User role `STAFF` berhasil mengakses `GET /api/v1/packages` dan membuat transaksi (Harus 200 OK).
2. Jalankan pengujian unit test backend:
   ```bash
   ./mvnw test
   ```
   *Pastikan controller test yang memakai `@WebMvcTest` disesuaikan dengan `@WithMockUser(roles = "ADMIN")` bila perlu.*
3. Jalankan pengujian otomatis:
   ```bash
   python3 scripts/test_runner.py
   ```

**Kriteria Lolos**
- Seluruh skenario RBAC baru berstatus `[PASS]`.
- Seluruh unit test (230+) dan skenario API fungsional yang sudah ada tetap lulus 100%.

---

## 📋 Checklist Penerimaan (Definition of Done)
- [ ] Endpoint `/api/v1/users/**` tidak lagi bersifat `permitAll()`.
- [ ] Method mutasi data user (`POST`, `PUT`, `DELETE`, `PATCH`) dilindungi `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`.
- [ ] Exception `AccessDeniedException` mengembalikan status HTTP 403 dengan format JSON standar.
- [ ] User role `STAFF` tidak mengalami kendala saat mengakses fitur kasir (Customer, Transaction, Transaction Files, dan Read Packages).
- [ ] Seluruh unit test dan test runner API lulus 100%.
