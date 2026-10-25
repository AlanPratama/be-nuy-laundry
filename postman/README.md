# BE Nuy Laundry - API Postman Collection & Environment

Katalog koleksi resmi Postman (Collection v2.1.0) untuk memudahkan pengujian manual, onboarding developer, dan pengujian otomatis CI/CD pada backend **BE Nuy Laundry**.

---

## 📦 Berkas Koleksi

1. **`be-nuy-laundry.postman_collection.json`**  
   Memuat seluruh request REST API yang dikelompokkan ke dalam 9 modul logis:
   - `01. Authentication & Profile`
   - `02. User Management (Admin Only)`
   - `03. Customer Management`
   - `04. Package Benefit Master`
   - `05. Laundry Package Master`
   - `06. Laundry Transactions`
   - `07. Transaction Details & Files`
   - `08. Health & Monitoring`
   - `09. Session Termination`
2. **`be-nuy-laundry.postman_environment.json`**  
   Variabel lingkungan terkonfigurasi untuk server lokal (`baseUrl: http://localhost:8080`) dengan kredensial default admin (`perf_admin`) dan staff (`perf_staff`).
3. **`test_assets/sample_image.png`**  
   File dummy gambar 1x1 pixel untuk pengujian form-data multipart.

---

## 🚀 Cara Menggunakan di Postman

1. Buka aplikasi **Postman** (Desktop atau Web).
2. Klik tombol **Import** di pojok kiri atas.
3. Pilih berkas:
   - `postman/be-nuy-laundry.postman_collection.json`
   - `postman/be-nuy-laundry.postman_environment.json`
4. Pilih environment aktif: **BE Nuy Laundry Local**.
5. Jalankan request secara berurutan atau gunakan **Collection Runner**:
   - Jalankan `01. Login as Admin` -> Token JWT otomatis tersimpan ke `adminToken`.
   - Jalankan `02. Login as Staff` -> Token JWT otomatis tersimpan ke `staffToken`.
   - Seluruh request berikutnya akan membaca token dan ID dinamis secara otomatis!

---

## 🤖 Eksekusi Otomatis via Newman CLI

Pastikan server backend Spring Boot telah aktif di port `8080`, lalu jalankan:

```bash
# Eksekusi seluruh koleksi API
npx newman run postman/be-nuy-laundry.postman_collection.json \
  -e postman/be-nuy-laundry.postman_environment.json

# Eksekusi dengan laporan detail
npx newman run postman/be-nuy-laundry.postman_collection.json \
  -e postman/be-nuy-laundry.postman_environment.json \
  --reporters cli
```
