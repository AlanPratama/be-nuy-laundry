# 03. Alur Bisnis (Business Flow)

Dokumen ini menjelaskan alur operasional bisnis binatu (*laundry*), peran pengguna (*user roles*), hierarki otorisasi, serta siklus hidup pemrosesan pesanan dari awal hingga selesai pada sistem **BE Nuy Laundry**.

---

## 1. Peran Pengguna (User Roles) & Matriks Hak Akses

Sistem mendefinisikan tiga peran pengguna melalui enum [UserRole](../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/database/entity/enumrated/UserRole.java):

| Fitur / Modul | SUPER_ADMIN | ADMIN | STAFF (Karyawan/Kasir) |
| :--- | :---: | :---: | :---: |
| **Login & Refresh Token** | ✅ | ✅ | ✅ |
| **Pencatatan Pelanggan Baru (Customer CRUD)** | ✅ | ✅ | ✅ |
| **Pencarian & Riwayat Pelanggan** | ✅ | ✅ | ✅ |
| **Pembuatan Transaksi Order Baru** | ✅ | ✅ | ✅ |
| **Update Transaksi & Status Cucian** | ✅ | ✅ | ✅ |
| **Upload Dokumentasi Berkas Transaksi** | ✅ | ✅ | ✅ |
| **Hapus Transaksi & Berkas Transaksi** | ✅ | ✅ | ✅ |
| **Hapus Item Detail Transaksi (Min. 1 Item)**| ✅ | ✅ | ✅ |
| **Manajemen Paket Layanan (Create/Update/Delete Package)** | ✅ | ✅ | ❌ (*403 Forbidden*) |
| **Melihat Daftar Paket Layanan (Read Package)** | ✅ | ✅ | ✅ |
| **Manajemen Benefit Layanan (Benefit CRUD)** | ✅ | ✅ | ✅ |
| **Manajemen Pengguna (User CRUD & Reset Password)** | ✅ | ✅ | ⚠️ *(Lihat Temuan Teknis)* |

> [!WARNING]
> **Catatan Otorisasi Endpoint User**:
> Sesuai konfigurasi aktual di [SecurityConfig.java](../src/main/java/com/be_nuy_laundry/be_nuy_laundry/infrastructure/internal/security/SecurityConfig.java#L30), path `/api/v1/users/**` saat ini terekspos publik (`permitAll()`). Pembatasan role hanya diterapkan secara deklaratif pada `PackageController` menggunakan `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")`.

---

## 2. Alur Autentikasi & Manajemen Sesi

```mermaid
sequenceDiagram
    autonumber
    actor User as Pengguna (Staff / Admin)
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthServiceImpl
    participant UserRepo as UserRepositoryPort
    participant Cache as SessionCacheService (Redis)
    participant DB as PostgreSQL (user_sessions)

    User->>AuthCtrl: POST /api/auth/login {username, password, deviceInfo}
    AuthCtrl->>AuthSvc: login(request)
    AuthSvc->>UserRepo: findByUsername(username)
    alt User Tidak Ditemukan / Password Salah
        AuthSvc-->>User: 401 Unauthorized ("Username atau password salah")
    else User Tidak Aktif (isActive == false)
        AuthSvc-->>User: 401 Unauthorized ("Akun anda telah dinonaktifkan")
    else Kredensial Valid
        AuthSvc->>AuthSvc: Generate UUID sessionId
        AuthSvc->>DB: Simpan UserSessionEntity (active = true, expiresAt = +7 Hari)
        AuthSvc->>Cache: Simpan UserSessionModel ke Redis (TTL 604800s)
        AuthSvc->>AuthSvc: Generate Access Token (JWT) & Refresh Token (JWT)
        AuthSvc-->>User: 200 OK {accessToken, refreshToken, deviceInfo}
    end
```

---

## 3. Siklus Hidup Transaksi Order (Order Lifecycle)

Transaksi order mencatat pesanan laundry dari pelanggan dan diproses dengan tahapan berikut:

```mermaid
stateDiagram-v2
    [*] --> IN_PROGRESS: Kasir Input Order (POST /api/v1/transactions)
    
    state IN_PROGRESS {
        [*] --> Penimbangan_Pencatatan
        Penimbangan_Pencatatan --> Upload_Foto_Kondisi_Awal
        Upload_Foto_Kondisi_Awal --> Pencucian_Pengeringan_Setrika
        Pencucian_Pengeringan_Setrika --> Upload_Foto_Kondisi_Selesai
    }
    
    IN_PROGRESS --> DONE: Cucian Selesai & Diserahkan (PUT /api/v1/transactions/{id})
    IN_PROGRESS --> CANCELED: Pesanan Dibatalkan Kasir/Pelanggan (PUT /api/v1/transactions/{id})
    
    DONE --> [*]
    CANCELED --> [*]
```

---

## 4. Alur Bisnis Pembuatan Transaksi Baru (Create Transaction)

```mermaid
flowchart TD
    Start([Mulai Transaksi Laundry]) --> InputData[Kasir Menerima Pakaian & Pilih Customer]
    InputData --> CheckCust{Customer Terdaftar?}
    CheckCust -- Tidak --> RegCust[Buat Customer Baru<br/>POST /api/v1/customers]
    RegCust --> ChoosePkg[Pilih Paket Layanan & Input Qty]
    CheckCust -- Ya --> ChoosePkg
    
    ChoosePkg --> CalcPrice[Sistem Kalkulasi:<br/>totalPackagePrice = qty * price<br/>totalPrice = sum all packages]
    CalcPrice --> CalcDate[Sistem Kalkulasi Estimasi:<br/>targetDate = orderDate + daysEstimation<br/>trxTargetDate = max targetDate]
    CalcDate --> GenInvoice[Sistem Generate Nomor Invoice Unik:<br/>TRX-HARI-DDMMYYYY-RANDOM16]
    
    GenInvoice --> CheckFiles{Ada Berkas Dilampirkan?<br/>Nota / Kondisi Sebelum / Invoice}
    CheckFiles -- Ya --> UploadMinIO[Upload Berkas ke MinIO Storage<br/>Folder: transaction/...]
    UploadMinIO --> SaveDB[Simpan Transaksi & Detail ke PostgreSQL<br/>Status: IN_PROGRESS]
    CheckFiles -- Tidak --> SaveDB
    
    SaveDB --> Output[Kembalikan TransactionResponseDTO & Invoice No]
    Output --> Selesai([Selesai])
```

### Logika Bisnis Penting pada Transaksi:
1. **Nomor Invoice Otomatis**:
   - Dibuat menggunakan kelas [TransactionNumberGenerator](../src/main/java/com/be_nuy_laundry/be_nuy_laundry/domain/utils/TransactionNumberGenerator.java).
   - Format: `TRX-[HARI_ID]-[TANGGAL]-[ALPHANUMERIC_16]`. Contoh: `TRX-SEN-23102026-A1B2C3D4E5F6G7H8`.
2. **Kalkulasi Estimasi Tanggal Selesai (`targetDate`)**:
   - Masing-masing detail memiliki estimasi: `orderDate + package.daysEstimation`.
   - Estimasi keseluruhan transaksi diambil dari nilai maksimum (`maxEstimationDate`) dari seluruh paket yang diambil di dalam pesanan tersebut, atau ditentukan manual oleh kasir.
3. **Integritas Detail Transaksi**:
   - Transaksi tidak boleh kosong. Pada [TransactionDetailServiceImpl.java](../src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionDetailServiceImpl.java#L54), penghapusan item detail pesanan memiliki proteksi:
     ```java
     if (existingTransactionModel.getTransactionDetails().size() <= 1)
         throw new ConflictException("Minimal harus ada 1 transaksi detail");
     ```
   - Total tagihan transaksi otomatis disesuaikan secara real-time saat salah satu item detail dihapus: `totalPrice = totalPrice - deletedDetail.totalPrice`.
4. **Pembersihan Berkas Otomatis saat Transaksi Dihapus**:
   - Pada [TransactionServiceImpl.java](../src/main/java/com/be_nuy_laundry/be_nuy_laundry/application/services/TransactionServiceImpl.java#L175-L186), jika transaksi dihapus, sistem secara otomatis melacak seluruh berkas MinIO yang terasosiasi dan menghapusnya dari MinIO Storage sebelum menghapus rekaman di database PostgreSQL.
