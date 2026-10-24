# Roadmap & Issue Breakdown: Unit Testing Suite (100% Coverage Target)

Dokumen ini berisi spesifikasi issue/tiket tugas pembuatan **Unit Test** untuk projek **BE Nuy Laundry**. Dibuat secara **high-level** agar mudah dieksekusi secara mandiri oleh *junior developer* atau *AI agent* (seperti model ringan/hemat biaya) tanpa kebingungan.

---

## Standar & Aturan Umum Pelaksanaan (Standard Guidelines)

1. **Framework & Tooling**:
   - Test Runner: **JUnit 5 (Jupiter)**
   - Mocking: **Mockito** (`@ExtendWith(MockitoExtension.class)`)
   - Web Testing: **Spring MockMvc** (`@WebMvcTest`) untuk layer Presentation/Controller
   - Assertion: **AssertJ** (`assertThat(...)`) dan **JUnit Assertions** (`assertThrows(...)`)
   - Coverage: **JaCoCo** dengan target **100% Line & Branch Coverage** pada business logic/services/utils/controllers.
2. **Naming Convention**:
   - Pola nama method test: `given<Kondisi>_when<Aksi>_then<Ekspektasi>()`
   - Contoh: `givenValidLoginRequest_whenLogin_thenReturnAuthResponseDTO()`
3. **Pola Penulisan**:
   - Struktur test wajib memisahkan blok **Given / Arrange**, **When / Act**, dan **Then / Assert**.
   - Setiap interaksi dengan mock wajib diverifikasi (`Mockito.verify(...)` atau `verifyNoMoreInteractions(...)`).
4. **Isolasi Unit Test**:
   - Jangan menyalakan context database riil (`@SpringBootTest` dilarang untuk unit test).
   - Seluruh dependency eksternal (Database, Redis, MinIO, Network) wajib di-mock.

---

## Daftar Tiket / Issues

| Issue ID | Komponen / Layer | Target File / Modul | Target Coverage | Status |
| :--- | :--- | :--- | :--- | :--- |
| **UT-01** | Foundation | Setup JaCoCo & Base Test Utility | Setup Tool | ✅ Completed |
| **UT-02** | Common & Domain Utils | `DateUtil`, `PageUtil`, `ValueUtil`, `ResponseUtil`, `TransactionNumberGenerator` | 100% | ✅ Completed |
| **UT-03** | Validation & Advices | `FileValidator`, `GlobalExceptionHandler` | 100% | ✅ Completed |
| **UT-04** | Security & Auth Filter | `JwtServiceImpl`, `JwtAuthFilter` | 100% | ✅ Completed |
| **UT-05** | Cache & File Storage | `RedisCacheService`, `SessionCacheService`, `MinioAdapter` | 100% | ✅ Completed |
| **UT-06** | Service: Auth | `AuthServiceImpl` | 100% | ✅ Completed |
| **UT-07** | Service: User | `UserServiceImpl` | 100% | ✅ Completed |
| **UT-08** | Service: Customer | `CustomerServiceImpl` | 100% | ✅ Completed |
| **UT-09** | Service: Package & Benefit | `PackageServiceImpl`, `BenefitServiceImpl` | 100% | ✅ Completed |
| **UT-10** | Service: Transaction & Detail | `TransactionServiceImpl`, `TransactionDetailServiceImpl` | 100% | ✅ Completed |
| **UT-11** | Service: Transaction File | `TransactionFileServiceImpl`, `FileServiceImpl` | 100% | ✅ Completed |
| **UT-12** | Presentation: Auth & User | `AuthController`, `UserController` | 100% | ✅ Completed |
| **UT-13** | Presentation: Master Data | `CustomerController`, `PackageController`, `BenefitController` | 100% | ✅ Completed |
| **UT-14** | Presentation: Transaction & Files | `TransactionController`, `TransactionDetailController`, `TransactionFileController`, `FileController` | 100% | ✅ Completed |
| **UT-15** | Infrastructure Mappers & Adapters | Database Entity-Domain Mappers & Repository Adapters | 100% | ✅ Completed |

---

### [UT-01] Setup JaCoCo Maven Plugin & Base Test Fixtures
- **Target**: `pom.xml`, test packages
- **Objektif**: Menyediakan alat ukur coverage otomatis serta helper fixture/data builder agar penulisan unit test seragam.
- **Tugas**:
  1. Tambahkan `jacoco-maven-plugin` ke dalam `pom.xml` dengan goal `prepare-agent` dan `report`.
  2. Tambahkan rule verifikasi coverage (Line & Branch coverage).
  3. Konfigurasi exclusion untuk file konfigurasi murni (`*Config.java`), `BeNuyLaundryApplication.java`, dan pure DTOs jika diperlukan.
  4. Buat base test fixture/builder helper untuk membuat dummy `UserModel`, `CustomerModel`, `PackageModel`, dan `TransactionModel`.
- **Acceptance Criteria**:
  - Perintah `./mvnw clean test jacoco:report` berhasil dijalankan dan menghasilkan laporan di `target/site/jacoco/index.html`.

---

### [UT-02] Unit Test: Common & Domain Utilities
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/common/util/DateUtil.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/common/util/PageUtil.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/common/util/ValueUtil.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/common/util/ResponseUtil.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/domain/utils/TransactionNumberGenerator.java`
- **File Test Baru**: `src/test/java/.../util/` (sesuai package masing-masing)
- **Skenario Pengujian (High Level)**:
  1. **DateUtil**:
     - Cek seluruh return hari Senin sampai Minggu (nilai integer 1-7).
     - Cek nilai di luar 1-7 (default case -> mengembalikan `"UNK"`).
  2. **PageUtil**:
     - Cek sorting `asc` dan `desc` (case-insensitive).
     - Cek properti `pageNumber`, `pageSize`, dan atribut `sort`.
  3. **ValueUtil**:
     - `hasValueString`: String valid, string kosong `""`, string spasi `"   "`, dan `null`.
     - `hasValueObject`: Objek ada vs `null`.
     - `hasValueList`: List berisi, list kosong (`isEmpty()`), dan list `null`.
     - `hasValueMultipartFile`: File ada isi vs file kosong (`isEmpty()`) vs `null`.
     - `orElse*` methods (`String`, `UserRole`, `Integer`, `LocalDateTime`): Mengembalikan nilai asal jika ada, mengembalikan nilai default jika kosong/null.
  4. **ResponseUtil**:
     - Format `success` default (HTTP 200) & custom HTTP status.
     - Format `successPage`: Semua metadata pagination ter-mapping akurat (`totalElements`, `totalPages`, `last`, dll).
     - Format `errorResponse` sederhana vs error response dengan payload errors.
  5. **TransactionNumberGenerator**:
     - Format invoice harus sesuai regex: `^TRX-[A-Z]{3}-\\d{8}-[A-Z0-9]{14}$`.
     - Keunikan: Dua invoice yang di-generate berturut-turut tidak boleh sama.
- **Acceptance Criteria**:
  - 100% Line & Branch Coverage pada ke-5 class utility di atas.

---

### [UT-03] Unit Test: Validation & Global Exception Handler
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/validations/FileValidator.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/advices/GlobalExceptionHandler.java`
- **Skenario Pengujian (High Level)**:
  1. **FileValidator**:
     - File valid tipe JPEG (`image/jpeg`) dan PNG (`image/png`).
     - File invalid (misal: `application/pdf`, `text/plain`).
     - File `null` atau kosong saat konfigurasi `required = true` (harus return `false` dan set error message).
     - File `null` atau kosong saat konfigurasi `required = false` (harus return `true`).
  2. **GlobalExceptionHandler**:
     - `handleValidationExceptions` (`MethodArgumentNotValidException`): Memetakan setiap field error ke format `Map<String, List<String>>` dan HTTP status 400 Bad Request.
     - `handleConstraintViolation`: Menangkap pesan violation pertama dan HTTP status 400.
     - `handleDataNotFound`: Return HTTP 404 Not Found dengan pesan error yang sesuai.
     - `handleConflict`: Return HTTP 409 Conflict.
     - `handleUnauthorized`: Return HTTP 401 Unauthorized.
     - `handleForbidden`: Return HTTP 403 Forbidden.
     - `handleMaxUploadSizeExceeded`: Return pesan batas ukuran file terlampaui.
     - `handleGeneralException`: Menangani `Exception.class` generik dan return HTTP 500 Internal Server Error.
- **Acceptance Criteria**:
  - Seluruh method handler dalam `GlobalExceptionHandler` dan kondisi percabangan di `FileValidator` tercover 100%.

---

### [UT-04] Unit Test: Security & JWT Service
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/JwtServiceImpl.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/JwtAuthFilter.java`
- **Dependencies di-mock**: `SessionCacheService`, `AuthRepositoryPort`, `FilterChain`, `HttpServletRequest`, `HttpServletResponse`.
- **Skenario Pengujian (High Level)**:
  1. **JwtServiceImpl**:
     - Generate access token & refresh token dari `UserModel` dan `UserSessionModel`.
     - Ekstraksi klaim: `extractUsername`, `extractRole`, `extractSessionId`.
     - Validasi token: Token valid (return `true`), token kedaluwarsa / expired (return `false`), token malformed / rusak (return `false`).
     - Pembersihan prefix `"Bearer "` vs token tanpa prefix vs token `null`.
  2. **JwtAuthFilter**:
     - Bypass filter: Path `/api/auth/*`, `/public/*`, `/swagger*` (harus `shouldNotFilter = true` dan langsung lanjut ke filter chain).
     - Missing header Authorization: Lanjut tanpa set authentication di SecurityContext.
     - Valid Token & Active Session: Berhasil set `UsernamePasswordAuthenticationToken` di `SecurityContextHolder`.
     - Invalid / Expired Token: Return HTTP 401 JSON Response.
     - Inactive / Revoked Session: Return HTTP 401 jika session di cache/DB tidak aktif.
     - Cache Hit vs Fallback DB: Mengambil session dari cache; jika cache miss, fallback ke database.
- **Acceptance Criteria**:
  - 100% coverage pada logika enkripsi/dekripsi klaim JWT dan alur filter request security.

---

### [UT-05] Unit Test: Cache & File Storage Adapter
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/cache/RedisCacheService.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/cache/SessionCacheService.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/external/minio/MinioAdapter.java`
- **Dependencies di-mock**: `RedisTemplate`, `ValueOperations`, `MinioClient`.
- **Skenario Pengujian (High Level)**:
  1. **RedisCacheService & SessionCacheService**:
     - `set`: Menyimpan data objek dengan key dan TTL yang ditentukan.
     - `get`: Berhasil mengambil data (cache hit) dan mengembalikan `Optional.empty()` saat cache miss / null.
     - `delete`: Menghapus cache berdasarkan key.
     - Error handling jika terjadi exception saat akses Redis.
  2. **MinioAdapter**:
     - `init`: Skenario bucket sudah ada vs bucket belum ada.
     - `upload`: Berhasil upload multipart file ke MinIO dan mengembalikan path file.
     - `download`: Berhasil membaca stream byte file.
     - `delete`: Berhasil menghapus file tunggal.
     - `deleteObjects`: Menghapus banyak file sekaligus, termasuk skenario terdapat sebagian file yang gagal dihapus (`MinIoRemoveObjectErrorResponse`).
     - Exception handling saat MinIO server throw MinioException.
- **Acceptance Criteria**:
  - 100% unit test coverage dengan seluruh panggilan ke Redis dan MinIO di-mock secara bersih.

---

### [UT-06] Unit Test: Service - Authentication (`AuthServiceImpl`)
- **Target File**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/AuthServiceImpl.java`
- **Dependencies di-mock**: `AuthRepositoryPort`, `UserUseCase`, `PasswordEncoder`, `JwtService`, `SessionCacheService`.
- **Skenario Pengujian (High Level)**:
  1. **login**:
     - **Happy Path**: Username & password cocok, user aktif -> menghasilkan access token & refresh token, menyimpan session ke DB dan Redis, return `AuthResponseDTO`.
     - **Wrong Password**: Password tidak cocok -> throw `ForbiddenException("Password tidak valid")`.
     - **Inactive User**: User dengan `isActive = false` -> throw `ForbiddenException("Akun tidak aktif")`.
     - **User Not Found**: User tidak ditemukan di sistem -> throw exception sesuai `userUseCase`.
  2. **refreshToken**:
     - **Happy Path**: Refresh token valid, session ditemukan dan aktif -> rotasi token baru, update expiration di DB dan Redis.
     - **Invalid / Expired Token**: Token tidak lolos validasi JWT -> throw `UnauthorizedException`.
     - **Session Not Found / Inactive**: Session tidak ada atau `isActive = false` -> throw `UnauthorizedException`.
  3. **logout**:
     - **Happy Path**: Menonaktifkan session di database (`isActive = false`) dan menghapus key dari Redis cache.
     - **Session Missing**: Penanganan saat session ID tidak ditemukan.
- **Acceptance Criteria**:
  - 100% Line & Branch Coverage pada `AuthServiceImpl`.

---

### [UT-07] Unit Test: Service - User Management (`UserServiceImpl`)
- **Target File**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/UserServiceImpl.java`
- **Dependencies di-mock**: `UserRepositoryPort`, `PasswordEncoder`, `FileUseCase`.
- **Skenario Pengujian (High Level)**:
  1. **createUser**:
     - **Happy Path (Tanpa Avatar)**: Username belum ada, password di-hash, simpan ke repo.
     - **Happy Path (Dengan Avatar)**: Upload avatar via `FileUseCase.upload()`, simpan file path ke user entity.
     - **Conflict**: Username sudah terdaftar -> throw `ConflictException("User sudah tersedia")`.
  2. **getUserById & getUserByUsername**:
     - User ditemukan -> return DTO / Domain Model.
     - User tidak ditemukan -> throw `DataNotFoundException`.
  3. **getAllUsers**:
     - Menguji pencarian dengan `UserFilter` dan `Pageable` -> return paginated list.
  4. **updateUser**:
     - Update nama/role/status dengan/tanpa upload avatar baru (menghapus avatar lama jika diganti).
     - User yang di-update tidak ditemukan -> throw `DataNotFoundException`.
  5. **resetPassword**:
     - Berhasil ubah password dengan hash baru.
     - User tidak ditemukan -> throw `DataNotFoundException`.
  6. **deleteUser**:
     - User ada -> repo `deleteById` terpanggil (dan hapus file avatar jika ada).
     - User tidak ada -> throw `DataNotFoundException`.
- **Acceptance Criteria**:
  - 100% Line & Branch Coverage pada `UserServiceImpl`.

---

### [UT-08] Unit Test: Service - Customer Management (`CustomerServiceImpl`)
- **Target File**: `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/CustomerServiceImpl.java`
- **Dependencies di-mock**: `CustomerRepositoryAdapter` (atau `CustomerRepositoryPort`).
- **Skenario Pengujian (High Level)**:
  1. **createCustomer**:
     - Menyimpan `CustomerModel` baru dan mengembalikan model yang tersimpan.
  2. **getCustomerById**:
     - ID ditemukan -> return `CustomerModel`.
     - ID tidak ditemukan -> throw `DataNotFoundException("Customer tidak ditemukan")`.
  3. **getAllCustomers**:
     - Filter dan pagination -> return `Page<CustomerModel>`.
  4. **updateCustomer**:
     - Update nama, telepon, dan alamat customer yang ada.
     - Customer tidak ditemukan -> throw `DataNotFoundException`.
  5. **deleteCustomer**:
     - Customer ada -> `deleteById` terpanggil.
     - Customer tidak ada -> throw `DataNotFoundException`.
- **Acceptance Criteria**:
  - 100% Line & Branch Coverage pada `CustomerServiceImpl`.

---

### [UT-09] Unit Test: Service - Package & Benefit Management
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/PackageServiceImpl.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/BenefitServiceImpl.java`
- **Dependencies di-mock**: `PackageRepositoryPort`, `BenefitRepositoryPort`, `FileUseCase`.
- **Skenario Pengujian (High Level)**:
  1. **PackageServiceImpl**:
     - `createPackage`: Sukses tanpa foto, sukses dengan foto (memanggil `fileUseCase.upload`), dan lempar `ConflictException` jika nama paket sudah ada.
     - `getPackageById`: Paket ditemukan vs lempar `DataNotFoundException`.
     - `getPackageByIds`: Batch lookup mapping ke `Map<String, PackageModel>`, cek kasus list kosong vs ada item.
     - `getAllPackages`: Filter & paginasi.
     - `updatePackage`: Update deskripsi, harga, tipe paket, dan update foto.
     - `deletePackage`: Hapus paket yang ada vs lempar `DataNotFoundException`.
  2. **BenefitServiceImpl**:
     - CRUD Benefit: Create benefit, get by ID (sukses vs not found), get all benefits, update benefit, dan delete benefit.
- **Acceptance Criteria**:
  - 100% Line & Branch Coverage pada `PackageServiceImpl` dan `BenefitServiceImpl`.

---

### [UT-10] Unit Test: Service - Transaction & Details
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionServiceImpl.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionDetailServiceImpl.java`
- **Dependencies di-mock**:
  - `TransactionRepositoryPort`, `PackageUseCase`, `CustomerUseCase`, `UserUseCase`, `TransactionFileUseCase`, `TransactionDetailUseCase`, `TransactionDetailRepositoryPort`.
- **Skenario Pengujian (High Level)**:
  1. **TransactionServiceImpl.createTransaction**:
     - **Happy Path**: Validasi customer sukses, validasi kasir/user sukses, paket-paket ditemukan, kalkulasi total harga akurat, nomor faktur otomatis terbentuk (`TRX-...`), status transaksi awal `PENDING`/`RECEIVED`, simpan detail & transaksi.
     - **Customer Not Found**: Lempar `DataNotFoundException`.
     - **User / Cashier Not Found**: Lempar `DataNotFoundException`.
     - **Package Missing**: Salah satu paket dalam detail tidak ada -> lempar `DataNotFoundException`.
  2. **TransactionServiceImpl (Query & Status Management)**:
     - `getTransactionById`: Transaksi ditemukan vs not found.
     - `getAllTransactions`: Filter berdasarkan status, tanggal, customer dengan pagination.
     - `updateStatus`: Perubahan status transaksi (misal: `PROCESS`, `READY_FOR_PICKUP`, `COMPLETED`, `CANCELLED`).
  3. **TransactionDetailServiceImpl**:
     - CRUD detail transaksi, kalkulasi subtotal harga per paket * kuantitas/berat (kg).
     - Validasi detail tidak ditemukan -> `DataNotFoundException`.
- **Acceptance Criteria**:
  - 100% Line & Branch Coverage pada alur kalkulasi harga, pembuatan transaksi, dan relasi detail.

---

### [UT-11] Unit Test: Service - Transaction File & MinIO Bridge
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionFileServiceImpl.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/FileServiceImpl.java`
- **Dependencies di-mock**: `FileStoragePort`, `FileUseCase`, `TransactionRepositoryPort`, `TransactionFileRepositoryPort`.
- **Skenario Pengujian (High Level)**:
  1. **TransactionFileServiceImpl**:
     - `getTransactionFileById`: ID ditemukan vs not found.
     - `getTransactionFileByIds`: Mengirim list ID -> sukses jika semua ada. Jika salah satu ID tidak ada di DB, lempar `DataNotFoundException("Beberapa file tidak ditemukan")`.
     - `uploadTransactionFile`: Upload bukti pakaian kotor / bukti serah terima (BEFORE / AFTER), validasi transaksi induk, simpan metadata ke DB.
     - `deleteTransactionFile`: Hapus file dari MinIO dan hapus record dari database.
  2. **FileServiceImpl**:
     - `upload`: Format nama file unik, delegasikan ke `FileStoragePort.upload()`.
     - `download`: Mengambil InputStream file dari storage port.
     - `delete`: Menghapus object dari storage port.
- **Acceptance Criteria**:
  - 100% Line & Branch Coverage pada logika upload, multi-file validation, dan penghapusan berkas.

---

### [UT-12] Unit Test: Controller - Auth & User
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/AuthController.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/UserController.java`
- **Teknologi**: `@WebMvcTest(controllers = {AuthController.class, UserController.class})` dengan `@MockBean` use-case terkait.
- **Skenario Pengujian (High Level)**:
  1. **AuthController**:
     - `POST /api/auth/login`: Request valid -> return 200 OK + payload `ApiResponse<AuthResponseDTO>`.
     - `POST /api/auth/login`: Request invalid (username kosong / password kosong) -> return 400 Bad Request.
     - `POST /api/auth/refresh-token`: Token valid -> return 200 OK.
  2. **UserController**:
     - `POST /api/users`: Create user multipart (JSON + file avatar) -> return 201/200.
     - `GET /api/users/{id}`: Return 200 OK saat ditemukan.
     - `GET /api/users`: Pagination dan filter parameter -> return 200 OK dengan format page.
     - `PUT /api/users/{id}`: Update user -> return 200 OK.
     - `PATCH /api/users/{id}/reset-password`: Reset password -> return 200 OK.
     - `DELETE /api/users/{id}`: Delete user -> return 200 OK.
- **Acceptance Criteria**:
  - 100% Controller methods teruji termasuk mapping URL, serialization response, dan binding validasi DTO.

---

### [UT-13] Unit Test: Controller - Customer, Package & Benefit
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/CustomerController.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/PackageController.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/BenefitController.java`
- **Teknologi**: `@WebMvcTest` dengan `@MockBean` `CustomerUseCase`, `PackageUseCase`, `BenefitUseCase`.
- **Skenario Pengujian (High Level)**:
  1. **CustomerController**:
     - CRUD endpoints (`POST /api/customers`, `GET /api/customers/{id}`, `GET /api/customers`, `PUT /api/customers/{id}`, `DELETE /api/customers/{id}`).
     - Validasi request body (`@Valid` nama customer, nomor telepon).
  2. **PackageController**:
     - Endpoints manajemen paket laundry (kiloan, satuan, bed cover).
     - Multipart form data request handling (deskripsi + upload gambar).
  3. **BenefitController**:
     - Endpoints CRUD benefit/fasilitas paket laundry.
- **Acceptance Criteria**:
  - 100% endpoint routing, status code, dan integrasi `@Valid` teruji.

---

### [UT-14] Unit Test: Controller - Transaction & Files
- **Target File**:
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/TransactionController.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/TransactionDetailController.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/TransactionFileController.java`
  - `src/main/java/com/be_nuy_laundry/be_nuy_laundry/presentation/controllers/FileController.java`
- **Skenario Pengujian (High Level)**:
  1. **TransactionController**:
     - `POST /api/transactions`: Buat pesanan laundry baru -> 201/200 OK.
     - `GET /api/transactions/{id}` & `GET /api/transactions`: Query filter & paging.
     - Endpoint update status laundry (Cuci, Kering, Setrika, Selesai, Diambil).
  2. **TransactionDetailController**:
     - Tambah & hapus item dalam rincian cucian.
  3. **TransactionFileController & FileController**:
     - Upload foto kondisi pakaian sebelum/sesudah dicuci.
     - Endpoint download/streaming file gambar.
     - Endpoint delete file bukti transaksi.
- **Acceptance Criteria**:
  - 100% endpoint transaksi dan file controller teruji via MockMvc.

---

### [UT-15] Unit Test: Infrastructure Mappers, Adapters & JPA Specifications
- **Target File**:
  - Mappers: `CustomerMapper`, `UserMapper`, `PackageMapper`, `BenefitMapper`, `TransactionMapper`, `TransactionDetailMapper`, `TransactionFileMapper`, `UserSessionMapper`.
  - Adapters: `CustomerRepositoryAdapter`, `UserRepositoryAdapter`, `PackageRepositoryAdapter`, `TransactionRepositoryAdapter`, dll.
  - Specifications: `CustomerSpecification`, `UserSpecification`, `TransactionSpecification`, dll.
- **Skenario Pengujian (High Level)**:
  1. **Mappers (Pure Unit Test)**:
     - `mapToEntity` & `mapToDomain`: Objek lengkap -> semua field ter-mapping identik.
     - Null input: Memastikan return `null` jika input null (mencegah NullPointerException).
     - List mapping: `mapToEntityList` & `mapToDomainList` saat list berisi data vs list kosong vs list null (harus return empty list).
  2. **Repository Adapters**:
     - Mock Spring Data JPA Repository (`JpaRepository`).
     - Menguji delegasi method: `save`, `findById`, `findAll(Specification, Pageable)`, `deleteById`.
  3. **JPA Specifications**:
     - Cek pembentukan `Predicate` saat kriteria pencarian diisi vs kriteria pencarian `null` (tidak menambahkan where clause).
- **Acceptance Criteria**:
  - 100% Line & Branch Coverage pada seluruh Mapper dan Adapter.

---

## Petunjuk Prompting untuk AI Agent / Junior Dev

Jika menugaskan tiket ke junior dev atau AI model yang lebih hemat (misal: GPT-4o-mini / Haiku), gunakan template prompt berikut:

> *"Kerjakan issue `[KODE_ISSUE]` dari file `UNIT_TEST_ISSUES.md`. Implementasikan unit test menggunakan JUnit 5 dan Mockito dengan pola Given-When-Then. Mock semua dependency eksternal. Pastikan seluruh skenario (Happy Path, Negative Cases, Edge/Boundary Cases) tercover sehingga mencapai 100% Line dan Branch Coverage pada file target."*
