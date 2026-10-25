# 06 - Codebase & Module Structure

Dokumen ini menjelaskan struktur codebase, organisasi paket, tanggung jawab setiap modul/komponen, alur dependensi antar layer, konvensi penamaan, dan penanganan cross-cutting concerns pada proyek backend **Nuy Laundry**.

---

## 1. Ikhtisar Arsitektur Codebase (Hexagonal Architecture)

Proyek ini mengadopsi prinsip **Hexagonal Architecture (Ports and Adapters)** yang memisahkan kode menjadi layer-layer berorientasi domain:

```
src/main/java/com/be_nuy_laundry/be_nuy_laundry/
├── common/                  # Cross-cutting utilities, generic responses, custom exceptions
├── domain/                  # Pure enterprise business domain (models, enums, domain utils)
├── application/             # Application business logic & use-case orchestrators
│   ├── ports/in/            # Inbound / Primary ports (Service Interfaces)
│   ├── ports/out/           # Outbound / Secondary ports (SPI: Database & Storage interfaces)
│   └── services/            # Service implementations (Use-case handlers)
├── infrastructure/          # Adapters implementasi teknologi luar (Database, MinIO, Redis, Security)
│   ├── internal/            # Internal infra: DB (JPA, Liquibase), Redis, Spring Security
│   └── external/            # External infra: MinIO S3 SDK Storage Adapter
└── presentation/            # Entry points / Driving Adapters (REST Controllers, DTOs, Validations)
```

### Dependency Rule (Aturan Ketergantungan)
- **Domain** berada di inti terdalam dan tidak memiliki dependensi ke layer luar (tidak ada dependensi JPA/Spring Web di Domain Model).
- **Application Services** hanya bergantung pada Domain Model dan Ports (`in` & `out`).
- **Infrastructure** dan **Presentation** bertindak sebagai Adapter yang mengimplementasikan Port atau memanggil Inbound Port.

```mermaid
graph TD
    subgraph Presentation ["Presentation Layer (Inbound Adapters)"]
        Controllers["REST Controllers"]
        Advices["GlobalExceptionHandler"]
        DTOs["Request / Response DTOs"]
    end

    subgraph Application ["Application Layer (Use Cases)"]
        PortsIn["Inbound Ports (Service Interfaces)"]
        Services["Service Implementations"]
        PortsOut["Outbound Ports (Repository & Storage SPI)"]
    end

    subgraph Domain ["Domain Layer (Core Entities & Logic)"]
        Models["Domain Models"]
        Enums["Business Enums"]
        DomainUtils["DateUtil / ValueUtil"]
    end

    subgraph Infrastructure ["Infrastructure Layer (Outbound Adapters)"]
        DBAdapters["JPA Database Adapters"]
        MinioAdapter["MinIO S3 Adapter"]
        RedisCache["Redis Token Cache"]
        SecurityAdapters["JWT & Spring Security"]
    end

    Controllers -->|Calls| PortsIn
    PortsIn <|.. Services
    Services -->|Operates on| Models
    Services -->|Calls| PortsOut
    DBAdapters ..|> PortsOut
    MinioAdapter ..|> PortsOut
    SecurityAdapters -->|Authenticates| Services
```

---

## 2. Rincian Paket & Tanggung Jawab Modul

### A. Modul Presentation (`com.be_nuy_laundry.be_nuy_laundry.presentation`)
Menangani HTTP transport layer, serialisasi/deserialisasi JSON, validasi input HTTP, dan mapping exception ke response client.

| Paket | Komponen Penting | Tanggung Jawab & Implementasi |
| :--- | :--- | :--- |
| `controllers/` | `AuthController`<br>`CustomerController`<br>`PackageController`<br>`BenefitController`<br>`TransactionController`<br>`TransactionDetailController`<br>`TransactionFileController`<br>`UserController`<br>`DashboardController`<br>`SettingController`<br>`FileController` | Menerima HTTP Request, mengekstrak path/query parameter/body/multipart, memanggil Inbound Port (`*UseCase`), dan membungkus hasil dalam `ResponseEntity<ApiResponse<T>>`. |
| `dtos/request/` | `auth/LoginRequest`<br>`user/UserRequest`<br>`user/UserUpdateRequest`<br>`customer/CustomerRequest`<br>`pkg/PackageRequest`<br>`benefit/BenefitRequest`<br>`transaction/header/TransactionRequest`<br>`transaction/header/TransactionStatusUpdateRequest`<br>`transaction/header/TransactionPaymentStatusUpdateRequest`<br>`transaction/detail/TransactionDetailRequest`<br>`transaction/file/TransactionFileUploadRequest`<br>`setting/SettingRequest` | Kontrak payload JSON/multipart yang dikirim oleh client. Menggunakan anotasi Jakarta Validation (`@NotBlank`, `@NotNull`, `@Pattern`, `@ValidImage`). |
| `dtos/response/` | `auth/AuthResponse`<br>`transaction/header/TransactionResponse`<br>`transaction/file/TransactionFileResponse`<br>`dashboard/DashboardMetricsDTO`<br>`setting/SettingResponse` | DTO representasi data balikan khusus ke client untuk membatasi atau menyusun format view tertentu. |
| `validations/` | `@ValidImage`<br>`ImageValidator` | Custom constraint annotation dan validator untuk memeriksa `MultipartFile`: mime type (`image/jpeg`, `image/png`, `image/webp`) dan ukuran maksimum (3 MB). |
| `advices/` | `GlobalExceptionHandler` | `@RestControllerAdvice` yang menangkap exception (`MethodArgumentNotValidException`, `DataNotFoundException`, `BadRequestException`, `UnauthorizedException`, `Exception`) dan mengembalikan `ApiResponse<T>` dengan HTTP status code yang sesuai. |

---

### B. Modul Application (`com.be_nuy_laundry.be_nuy_laundry.application`)
Pusat orkestrasinya logika bisnis. Layer ini tidak terikat pada framework web atau teknologi database spesifik.

| Paket | Komponen Penting | Tanggung Jawab & Implementasi |
| :--- | :--- | :--- |
| `ports/in/` | `AuthUseCase`<br>`UserUseCase`<br>`CustomerUseCase`<br>`PackageUseCase`<br>`BenefitUseCase`<br>`TransactionUseCase`<br>`TransactionDetailUseCase`<br>`TransactionFileUseCase`<br>`DashboardUseCase`<br>`SettingUseCase`<br>`FileUseCase` | Interface kontrak use-case aplikasi yang diekspos ke presentation/controller layer. |
| `ports/out/` | `AuthRepositoryPort`<br>`UserRepositoryPort`<br>`CustomerRepositoryPort`<br>`PackageRepositoryPort`<br>`BenefitRepositoryPort`<br>`TransactionRepositoryPort`<br>`TransactionDetailRepositoryPort`<br>`TransactionFileRepositoryPort`<br>`SettingRepositoryPort`<br>`FileStoragePort` | Secondary Port (SPI) yang mendefinisikan operasi persistensi database dan penyimpanan file fisik yang harus disediakan oleh Infrastructure. |
| `services/` | `AuthServiceImpl`<br>`UserServiceImpl`<br>`CustomerServiceImpl`<br>`PackageServiceImpl`<br>`BenefitServiceImpl`<br>`TransactionServiceImpl`<br>`TransactionDetailServiceImpl`<br>`TransactionFileServiceImpl`<br>`DashboardServiceImpl`<br>`SettingServiceImpl`<br>`FileServiceImpl` | Implementasi use-case bisnis, pengelolaan transaksi declarative (`@Transactional`), validasi integritas entitas, kalkulasi total harga laundry, analitika metrik dashboard, pengaturan outlet, dan integrasi antar port. |

---

### C. Modul Domain (`com.be_nuy_laundry.be_nuy_laundry.domain`)
Entitas bisnis murni dan enum yang menjadi representasi domain model aplikasi.

| Paket | Komponen Penting | Tanggung Jawab & Implementasi |
| :--- | :--- | :--- |
| `model/` | `UserModel`<br>`UserSessionModel`<br>`CustomerModel`<br>`PackageModel`<br>`BenefitModel`<br>`TransactionModel`<br>`TransactionDetailModel`<br>`TransactionFileModel`<br>`SettingModel`<br>`BaseModel` | Plain Java Objects (POJO) beranotasi Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) tanpa anotasi `@Entity` JPA. |
| `enums/` | `UserRoleEnum`<br>`TransactionStatusEnum`<br>`TransactionPaymentStatusEnum`<br>`TransactionFileType` | Nilai konstan status bisnis (misal: role pengguna, siklus order laundry, jenis file attachment). |
| `utils/` | `DateUtil`<br>`ValueUtil` | Helper fungsi domain untuk manipulasi tanggal/waktu dan validasi null/empty check pada String, List, Map, dan Object. |

---

### D. Modul Infrastructure (`com.be_nuy_laundry.be_nuy_laundry.infrastructure`)
Implementasi teknis terhadap secondary ports (Database, Object Storage, Security, Cache).

#### 1. Internal Database (`infrastructure/internal/database`)
- **`entity/`**: Entitas JPA (`UserEntity`, `CustomerEntity`, `TransactionEntity`, `SettingEntity`, dll) yang memetakan tabel PostgreSQL via Hibernate. Turunan dari `BaseEntity` (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`).
- **`repository/`**: Spring Data JPA Repository (`JpaRepository`, `JpaSpecificationExecutor`).
- **`specifications/` & `filters/`**: Spesifikasi JPA dinamis (`CustomerSpecification`, `TransactionSpecification`, `UserSpecification`) yang membaca kriteria pencarian dari `CustomerFilter`, `TransactionFilter`, `UserFilter`.
- **`mapper/`**: MapStruct atau manual mappers (`UserMapper`, `TransactionMapper`, `CustomerMapper`, `PackageMapper`, `SettingMapper`, dll) yang melakukan konversi dua arah antara JPA Entity dan Domain Model.
- **`adapter/`**: Implementasi dari Outbound Port (contoh: `TransactionDatabaseAdapter` mengimplementasikan `TransactionRepositoryPort`). Adapter memanggil `TransactionRepository`, melakukan mapping Entity <-> Model via `TransactionMapper`, dan menangani query database.

#### 2. Internal Cache & Security (`infrastructure/internal/`)
- **`cache/RedisConfig`, `RedisCacheService`, & `SessionCacheService`**: Koneksi Redis Standalone via Spring Data Redis (Jedis/Lettuce), operasi `set`, `get`, `delete`, `deleteByPattern`, pengelolaan TTL sesi JWT, serta generic cache-aside data master (paket & benefit).
- **`security/SecurityConfig`**: Konfigurasi `SecurityFilterChain`, CSRF disabled, SessionManagement stateless, `requestMatchers` public vs protected, integrasi `CorsConfigurationSource`, serta kustomisasi `accessDeniedHandler` (HTTP 403) dan `authenticationEntryPoint` (HTTP 401).
- **`security/WebMvcCorsConfig`**: Konfigurasi global CORS `WebMvcConfigurer` yang mengizinkan origin frontend Vite/Nuxt (`localhost:5173`, `localhost:3000`), credentials, dan metode HTTP (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`).
- **`security/JwtAuthFilter`**: Filter per request (`OncePerRequestFilter`) yang mengekstrak `Bearer <token>`, memvalidasi signature & expiration via `JwtService`, serta memeriksa keaktifan sesi di database/Redis.
- **`security/JwtServiceImpl`**: Implementasi port untuk generate, parse, dan validasi token JWT HMAC-SHA256.

#### 3. External Object Storage (`infrastructure/external/minio`)
- **`MinioConfig`**: Inisialisasi bean singleton `MinioClient` berbasis endpoint, port, access key, dan secret key dari `application.properties`.
- **`MinioStorageAdapter`**: Mengimplementasikan `FileStoragePort` untuk operasi `upload`, `download`, `delete`, dan `preview` object pada bucket `nuy-laundry`.

---

### E. Modul Common (`com.be_nuy_laundry.be_nuy_laundry.common`)
Pustaka bantuan bersama yang digunakan secara luas oleh seluruh layer:

| Paket / Kelas | Fungsi & Tanggung Jawab |
| :--- | :--- |
| `response/ApiResponse<T>` | Standard response envelope berisi field: `code` (Integer), `status` (Boolean), `message` (String), `errors` (Object), dan `data` (T). |
| `response/PaginationResponse<T>` | Data wrapper khusus respons berhalaman: `content` (List), `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `isLastPage`. |
| `util/ResponseUtil` | Helper static factory untuk merangkum pembuatan `ApiResponse.success()`, `error()`, dan pagination response. |
| `util/PaginationUtil` | Helper untuk membangun objek `PageRequest` / `Pageable` dari query parameter request (`page`, `size`, `sort`). |
| `constant/CommonConstant` | Konstanta global sistem (format tanggal default, header HTTP, pesan standar). |
| `exception/` | Hirarki custom runtime exception: `BaseException`, `BadRequestException` (400), `UnauthorizedException` (401), `DataNotFoundException` (404), `ValidationException` (422). |

---

## 3. Detail Pola Desain (Design Patterns) yang Digunakan

1. **Hexagonal / Ports and Adapters Pattern**:
   - `TransactionServicePort` (Primary Port) diimplementasikan oleh `TransactionServiceImpl`.
   - `TransactionDatabasePort` (Secondary Port) didefinisikan di `application/ports/out` dan diimplementasikan oleh `TransactionDatabaseAdapter` di `infrastructure`.
   - Hasil: Service murni terisolasi dari Hibernate/Spring Data JPA. Jika database diganti MongoDB atau JDBC template, logika bisnis di `TransactionServiceImpl` tidak berubah sama sekali.

2. **Adapter Pattern**:
   - `MinioStorageAdapter` mengadaptasi API eksternal MinIO S3 SDK (`MinioClient.putObject`, `getObject`, `removeObject`) ke interface internal `FileStoragePort`.

3. **Specification Pattern (Dynamic Query Builder)**:
   - Digunakan pada `CustomerSpecification`, `TransactionSpecification`, dan `UserSpecification`.
   - Menggunakan `org.springframework.data.jpa.domain.Specification` untuk merangkai predicate SQL dinamis secara type-safe (`WHERE name ILIKE ... AND status = ...`).

4. **Data Transfer Object (DTO) & Mapper Pattern**:
   - DTO request dari presentation diubah menjadi Domain Model, lalu diteruskan ke Service.
   - Domain Model diubah menjadi Entity oleh Adapter sebelum disimpan ke database, dan dikonversi kembali saat query.

5. **Controller Advice Pattern (AOP Exception Handling)**:
   - `GlobalExceptionHandler` menangkap exception global secara deklaratif menggunakan `@ExceptionHandler`.

---

## 4. Aliran Dependensi dan Siklus Eksekusi Data

Diagram berikut menunjukkan bagaimana aliran data bergerak dari Controller hingga Database:

```
[ HTTP Request: JSON / Multipart ]
                │
                ▼
    [ Presentation: Controller ]
        - Validasi DTO (@Valid, @ValidImage)
        - Konversi Request DTO -> Domain Model
                │
                ▼ (Inbound Port: ServicePort)
    [ Application: ServiceImpl ]
        - @Transactional Boundary
        - Validasi Aturan Bisnis (Status order, kalkulasi harga, check availability)
        - Pemanggilan Port Outbound
                │
                ▼ (Outbound Port: DatabasePort / FileStoragePort)
    [ Infrastructure: Adapter ]
        - Model -> JPA Entity (via Mapper)
        - Spring Data JPA Repository (SQL execution via Hibernate)
        - Entity -> Model (via Mapper)
                │
                ▼ (Return Domain Model)
    [ Application: ServiceImpl ]
                │
                ▼ (Return Domain Model)
    [ Presentation: Controller ]
        - Bungkus Domain Model ke ApiResponse<T>
                │
                ▼
[ HTTP Response: Standard JSON 200/201/400/404 ]
```

---

## 5. Ringkasan Kelas & Metode Kritis

| Kelas | Metode Utama | Deskripsi Operasi |
| :--- | :--- | :--- |
| `AuthServiceImpl` | `login(LoginRequest)` | Verifikasi email, cek BCrypt password, generate JWT, simpan sesi ke database dan Redis. |
| `AuthServiceImpl` | `logout(token)` | Invalidasi token JWT dari header, update sesi database menjadi inactive, hapus sesi dari Redis. |
| `TransactionServiceImpl` | `createTransaction(request)` | Generate invoice `INV-YYYYMMDD-XXXX`, validasi customer & package, hitung subtotal & total, simpan transaksi & detail. |
| `TransactionServiceImpl` | `updateTransactionStatus(id, req)` | Validasi transisi status laundry (`WAITING` -> `PROCESS` -> `DONE` -> `DELIVERED`). |
| `TransactionFileServiceImpl`| `uploadTransactionFile(...)` | Validasi tipe file & ukuran, panggil `MinioStorageAdapter.uploadFile()`, simpan metadata file ke database. |
| `MinioStorageAdapter` | `uploadFile(file, path)` | Eksekusi stream `putObject` ke S3 bucket `nuy-laundry`. |
| `GlobalExceptionHandler` | `handleDataNotFoundException(...)` | Intercept 404 jika entitas tidak ditemukan di database. |
| `JwtAuthenticationFilter` | `doFilterInternal(...)` | Intercept setiap HTTP request, parse JWT Bearer, set `SecurityContextHolder`. |
