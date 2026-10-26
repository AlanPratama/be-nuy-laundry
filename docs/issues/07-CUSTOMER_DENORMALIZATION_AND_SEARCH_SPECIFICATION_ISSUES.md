# Issue #07 (Backend): Denormalisasi Agregat Pelanggan, Optimasi Logika Transaksi, dan Spesifikasi Pencarian Fleksibel (Operator OR) - [RESOLVED]

> **Status:** ✅ **RESOLVED** (Selesai diimplementasikan dan dikompilasi dengan sukses)

## 📌 Deskripsi Masalah & Latar Belakang (High-Level)
1. **Beban Query Data Pelanggan (N+1 Problem)**:
   - Saat ini, data agregat pelanggan seperti total kilogram cucian, total pcs cucian, akumulasi transaksi, dan tanggal order terakhir masih dihitung secara dinamis atau bergantung pada join relasi transaksi.
   - Pada basis data berskala besar, ini menyebabkan latensi tinggi (*N+1 query problem*) pada endpoint `GET /api/v1/customers`.
2. **Keterbatasan Operasi Pencarian (Kondisi AND alih-alih OR)**:
   - Beberapa spesifikasi JPA Criteria menambahkan kondisi pencarian sebagai predikat `AND` kaku, sehingga jika pengguna memasukkan satu kata kunci pencarian, sistem memfilter data secara terlalu sempit.
   - Pencarian seharusnya menggunakan logika disjungsi (`OR`) pada beberapa kolom sekaligus:
     - **Paket**: Mencari kecocokan pada `name` **ATAU** `description`.
     - **Artikel**: Mencari kecocokan pada `title` **ATAU** `excerpt` **ATAU** `content` (tips & isi artikel).
     - **Transaksi**: Mencari kecocokan pada `invoiceNo` **ATAU** `customerName` **ATAU** `customerPhone`.
3. **Kebutuhan Fitur Pengambilan Parsial & Metrik Mandiri**:
   - Staf laundry membutuhkan kemampuan mencatat tanggal penyerahan barang (`deliveredDate`) per item detail pakaian (`transaction_details`), bukan hanya di header transaksi.
   - Endpoint metrik artikel (`/api/v1/articles/metrics`) diperlukan agar kartu statistik di dashboard tidak terdistorsi oleh filter paginasi.

---

## 🎯 Spesifikasi Kebutuhan & Solusi

### 1. Migrasi Skema Basis Data Pelanggan (Liquibase)
Buat berkas migrasi baru: `src/main/resources/db/changelog/019-add-customer-aggregated-stats.yaml` dan daftarkan ke `db.changelog-master.yaml`:
- **Kolom Baru pada Tabel `customers`**:
  - `total_cucian_kg DECIMAL(10, 2) DEFAULT 0.00 NOT NULL`: Akumulasi berat cucian kiloan (kg) yang telah selesai.
  - `total_cucian_satuan INT DEFAULT 0 NOT NULL`: Akumulasi jumlah cucian satuan (pcs) yang telah selesai.
  - `total_transaksi INT DEFAULT 0 NOT NULL`: Total frekuensi transaksi yang pernah dibuat oleh pelanggan.
  - `last_order TIMESTAMP WITH TIME ZONE`: Waktu transaksi terakhir dibuat/dipesan oleh pelanggan.

### 2. Penyesuaian Model Domain, Entity, dan Mapper Customer
- **`CustomerEntity.java`**:
  - Tambahkan properti: `totalCucianKg`, `totalCucianSatuan`, `totalTransaksi`, `lastOrder`.
  - Pastikan relasi ke `TransactionEntity` tidak di-fetch secara *eager* pada `getAll`.
- **`CustomerResponseDTO.java` & `CustomerMapper.java`**:
  - Map langsung kolom-kolom agregat tersebut ke response DTO. Respon `GET /api/v1/customers` menjadi sangat ringan O(1) tanpa perlu menghitung transaksi secara berulang.

### 3. Penyesuaian Logika Transaksi (`TransactionServiceImpl.java`)
- **Saat Transaksi Baru Dibuat (`createTransaction`)**:
  - Update `customer.totalTransaksi = customer.totalTransaksi + 1`.
  - Update `customer.lastOrder = LocalDateTime.now()`.
- **Saat Status Transaksi Berubah Menjadi `DONE`**:
  - Hitung total kuantitas kiloan dari `transactionDetails` bertipe `WEIGHT`, lalu tambahkan ke `customer.totalCucianKg`.
  - Hitung total kuantitas satuan dari `transactionDetails` bertipe `PCS`, lalu tambahkan ke `customer.totalCucianSatuan`.
- **Saat Status Transaksi Dibatalkan (`CANCELED`)**:
  - Jika sebelumnya transaksi sudah dihitung, lakukan penyesuaian/pengurangan kembali pada statistik pelanggan.

### 4. Perbaikan Spesifikasi Pencarian JPA (Operator OR)
Perbarui kelas-kelas spesifikasi agar menerapkan disjungsi (`criteriaBuilder.or(...)`):
1. **`PackageSpecification.java`**:
   - Jika keyword pencarian `search` diberikan:
     ```java
     String pattern = "%" + search.trim().toLowerCase() + "%";
     Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
     Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
     predicates.add(criteriaBuilder.or(nameMatch, descMatch));
     ```
2. **`ArticleSpecification.java`**:
   - Jika keyword pencarian `search` diberikan:
     ```java
     String pattern = "%" + search.trim().toLowerCase() + "%";
     Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
     Predicate excerptMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("excerpt")), pattern);
     Predicate contentMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), pattern);
     predicates.add(criteriaBuilder.or(titleMatch, excerptMatch, contentMatch));
     ```
3. **`TransactionSpecification.java`**:
   - Jika keyword pencarian `name` / keyword umum diberikan:
     ```java
     String pattern = "%" + search.trim().toLowerCase() + "%";
     Predicate invoiceMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("invoiceNo")), pattern);
     Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")), pattern);
     Predicate phoneMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("customerPhone")), pattern);
     predicates.add(criteriaBuilder.or(invoiceMatch, nameMatch, phoneMatch));
     ```

### 5. Dukungan Endpoint Tambahan
1. **Pengambilan Parsial Item Transaksi**:
   - `PATCH /api/v1/transaction-details/{id}/delivered`:
     - Mengisi `deliveredDate` pada entitas `TransactionDetailEntity` dengan waktu saat ini (`LocalDateTime.now()`).
     - Jika seluruh detail transaksi pada nomor invoice tersebut sudah memiliki `deliveredDate`, sistem otomatis mengubah status header transaksi menjadi `DONE`.
2. **Metrik Artikel Mandiri**:
   - `GET /api/v1/articles/metrics`:
     - Mengembalikan `ArticleMetricsDTO` yang berisi `{ totalArticles, publishedArticles, draftArticles }`.

---

## 📁 Berkas Terkait
- `src/main/resources/db/changelog/019-add-customer-aggregated-stats.yaml` *(Baru)*
- `src/main/resources/db/changelog/db.changelog-master.yaml`
- `src/main/java/.../infrastructure/internal/database/entity/CustomerEntity.java`
- `src/main/java/.../application/services/TransactionServiceImpl.java`
- `src/main/java/.../application/services/CustomerServiceImpl.java`
- `src/main/java/.../infrastructure/internal/database/specifications/PackageSpecification.java`
- `src/main/java/.../infrastructure/internal/database/specifications/ArticleSpecification.java`
- `src/main/java/.../infrastructure/internal/database/specifications/TransactionSpecification.java`
- `src/main/java/.../presentation/controllers/TransactionDetailController.java`
- `src/main/java/.../presentation/controllers/ArticleController.java`

---

## ✅ Kriteria Penerimaan (Acceptance Criteria)
- [ ] Database memiliki kolom `total_cucian_kg`, `total_cucian_satuan`, `total_transaksi`, dan `last_order` pada tabel `customers`.
- [ ] Endpoint `GET /api/v1/customers` merespons secara instan tanpa melakukan N+1 query ke tabel transaksi.
- [ ] Pembuatan dan penyelesaian transaksi otomatis meng-update kolom agregat pada pelanggan terkait.
- [ ] Pencarian paket mencocokkan nama ATAU deskripsi.
- [ ] Pencarian artikel mencocokkan judul ATAU kutipan ATAU isi konten tips.
- [ ] Pencarian transaksi mencocokkan nomor invoice ATAU nama pelanggan ATAU nomor telepon.
- [ ] Staf dapat meng-update `deliveredDate` per detail item via endpoint `PATCH /api/v1/transaction-details/{id}/delivered`.
- [ ] Endpoint `GET /api/v1/articles/metrics` mengembalikan hitungan agregat yang akurat.
