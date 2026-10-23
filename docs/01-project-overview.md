# 01. Project Overview & Technology Stack

Dokumen ini menjelaskan latar belakang, tujuan fungsional sistem, serta katalog lengkap tumpukan teknologi (technology stack), pustaka (*libraries*), dan infrastruktur pendukung yang digunakan pada proyek **BE Nuy Laundry**.

---

## 1. Tujuan & Fungsi Utama Aplikasi

**BE Nuy Laundry** adalah sistem backend berbasis REST API yang dirancang untuk mengelola seluruh operasional bisnis usaha binatu (laundry). Sistem ini menyediakan fungsi pencatatan, pemantauan, dan manajemen data terpusat, antara lain:

1. **Manajemen Identitas & Pengguna (User Management)**:
   - Pengelolaan akun karyawan dengan role berbasis hak akses (`SUPER_ADMIN`, `ADMIN`, `STAFF`).
   - Autentikasi berbasis token JWT (Access Token dan Refresh Token).
   - Pengelolaan sesi pengguna (*user session tracking*) dengan penyimpanan di basis data dan Redis cache.
2. **Manajemen Data Master (Master Data)**:
   - Data Pelanggan (*Customers*): nama, nomor telepon, dan alamat domisili.
   - Paket Layanan (*Packages*): paket kiloan (`WEIGHT`) dan satuan (`PCS`), estimasi durasi pengerjaan (hari), harga per satuan, serta foto paket.
   - Manfaat Layanan (*Benefits*): benefit khusus yang melekat pada paket tertentu (misalnya gratis parfum premium, anti-bakteri).
3. **Manajemen Transaksi Order (Transaction Management)**:
   - Pembuatan pesanan laundry dengan kalkulasi otomatis total tagihan dan estimasi tanggal penyelesaian (*target date*).
   - Pembuatan nomor faktur (*invoice number*) otomatis dan unik berbasis pola hari, tanggal, dan acak (`TRX-[HARI]-[DDMMYYYY]-[RANDOM]`).
   - Pelacakan status pengerjaan: `IN_PROGRESS`, `DONE`, dan `CANCELED`.
4. **Manajemen Dokumentasi Berkas (File & Document Storage)**:
   - Penyimpanan bukti bayar (*payment receipt*), dokumen faktur (*invoice document*), foto kondisi pakaian sebelum dicuci (*condition before*), foto kondisi sesudah dicuci (*condition after*), serta berkas pendukung lainnya (*other files*) pada MinIO Object Storage (kompatibel S3).

---

## 2. Rincian Runtime & Framework Inti

| Komponen | Spesifikasi / Versi | Sumber Verifikasi | Keterangan |
| :--- | :--- | :--- | :--- |
| **Java Platform** | Java 17 (LTS) | [pom.xml](../pom.xml#L31), [Dockerfile](../Dockerfile#L1) | Runtime utama, kompatibel dengan Eclipse Temurin 17. |
| **Spring Boot** | 3.2.5 | [pom.xml](../pom.xml#L9) | Framework inti backend microservice/monolith. |
| **Spring Framework** | 6.1.x (transitive via Boot 3.2.5) | Dependency Tree | Core Inversion of Control & Spring Web MVC. |
| **Spring Security** | 6.2.x (transitive via Boot 3.2.5) | [pom.xml](../pom.xml#L40) | Pengamanan endpoint, otentikasi stateless JWT. |
| **Build Tool** | Apache Maven 3.9.x via Maven Wrapper (`mvnw`) | [.mvn/wrapper/maven-wrapper.properties](../.mvn/wrapper/maven-wrapper.properties) | Wrapper build tool tanpa perlu instalasi Maven lokal. |

---

## 3. Database & Object-Relational Mapping (ORM)

| Komponen | Implementasi / Versi | Sumber Verifikasi | Keterangan |
| :--- | :--- | :--- | :--- |
| **Relational Database** | PostgreSQL 16 (Alpine) | [docker-compose.yaml](../docker-compose.yaml#L2-L18) | Basis data relasional utama untuk persistensi data ACID. |
| **JDBC Driver** | `org.postgresql:postgresql` | [pom.xml](../pom.xml#L44-L47) | Driver komunikasi JDBC resmi PostgreSQL. |
| **ORM / JPA** | Hibernate ORM 6.4.x via Spring Data JPA | [pom.xml](../pom.xml#L36) | Abstraksi repository, JPA Specifications, dan entitas. |
| **Connection Pool** | HikariCP (Default Spring Boot) | Transitive via Starter Data JPA | Connection pool berkecepatan tinggi. |
| **Database Migration** | Liquibase Core (`org.liquibase:liquibase-core`) | [pom.xml](../pom.xml#L100-L103), [application.properties](../src/main/resources/application.properties#L30) | Versioning skema basis data berbasis changelog YAML (`db.changelog-master.yaml`). |

---

## 4. Cache, Storage & Infrastruktur Pendukung

| Layanan | Peran / Fungsi | Versi / Image | Konfigurasi Terkait | Status Implementasi |
| :--- | :--- | :--- | :--- | :--- |
| **Redis** | In-Memory Session Cache | `redis:7-alpine` (Port 6379) | [application.properties](../src/main/resources/application.properties#L37-L44) | **Aktif & Digunakan**. Menyimpan `UserSessionModel` dengan TTL 7 hari (`redis.ttl.session=604800`) untuk validasi token cepat. |
| **MinIO** | Object Storage (S3 Compatible) | `minio/minio:latest` (Port 9000 & 9001) | [application.properties](../src/main/resources/application.properties#L61-L66) | **Aktif & Digunakan**. Menyimpan berkas transaksi, foto profil, dan foto paket dalam bucket `nuy-laundry`. |
| **RabbitMQ** | Message Broker Asinkron | `rabbitmq:3.13-management-alpine` (Port 5672 & 15672) | [docker-compose.yaml](../docker-compose.yaml#L20-L36), [application.properties](../src/main/resources/application.properties#L46-L52) | **Belum Terintegrasi di Java**. Kontainer berjalan dan variabel konfigurasi disiapkan, namun modul `spring-boot-starter-amqp` belum ditambahkan ke `pom.xml`, dan belum ada kode producer/listener. |

> [!NOTE]
> **Fakta Terverifikasi vs Rencana Infrastruktur**:
> Sesuai Rule 10 [HELP.md](../HELP.md), RabbitMQ saat ini hanya ada pada level container dan properties. Kode bisnis saat ini berjalan secara sinkron (HTTP) tanpa *event publishing* antrian.

---

## 5. Pustaka Pihak Ketiga (Third-Party Libraries)

Berikut adalah daftar pustaka pihak ketiga yang didefinisikan secara eksplisit dalam [pom.xml](../pom.xml):

| Pustaka / Dependency | Artifact & Versi | Tujuan Penggunaan |
| :--- | :--- | :--- |
| **JJWT API & Impl** | `io.jsonwebtoken:jjwt-*` (`0.11.5`) | Pembuatan, parsing, dan validasi JSON Web Token dengan algoritma HMAC-SHA256 (`HS256`). |
| **MinIO Java SDK** | `io.minio:minio` (`8.5.7`) | Klien resmi MinIO untuk upload (`putObject`), download (`getObject`), dan hapus objek (`removeObjects`). |
| **Hibernate Validator** | `spring-boot-starter-validation` | Validasi anotasi pada request DTO (`@NotBlank`, `@NotNull`, `@Pattern`, `@Size`, dll). |
| **Jackson JSR310** | `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | Serialisasi dan deserialisasi tipe Java 8 Date/Time (`LocalDateTime`, `LocalDate`). |
| **Project Lombok** | `org.projectlombok:lombok` | Menghilangkan boilerplate code Java (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`). |
| **JaCoCo Plugin** | `org.jacoco:jacoco-maven-plugin` (`0.8.11`) | Analisis dan pengukuran cakupan unit test (Line & Branch coverage). |

---

## 6. Ringkasan Ketergantungan Eksternal Sistem

```mermaid
flowchart LR
    App[BE Nuy Laundry Application<br/>Spring Boot 3.2.5 on Java 17]
    
    App -->|JDBC :5432| PG[(PostgreSQL 16<br/>Database: nuy_laundry)]
    App -->|RESP :6379| Redis[(Redis 7<br/>Session Cache)]
    App -->|S3 REST API :9000| MinIO[(MinIO Object Storage<br/>Bucket: nuy-laundry)]
    App -.->|AMQP :5672 (Standby)| RabbitMQ[(RabbitMQ 3.13<br/>Message Broker)]
```
