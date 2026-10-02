# Seneca HR Engine (High-Performance Multi-Tenant System)

Sistem transaksional *Human Resources* multi-tenant berbasis **Quarkus 3.40**, **Java 25**, **JDBI 3** (tanpa ORM/Hibernate), **PostgreSQL 18** dengan ekstensi **pgvector**, serta antarmuka web SSR bertema **PatternFly Enterprise Light Interface** (Qute Type-Safe Templates, Tailwind CSS v4, font Inter, Alpine.js, Line Awesome, dan HTMX).

---

## 1. Karakteristik & Hierarki Peran

Sistem menerapkan hierarki 4 peran (*4-Tier Roles*):
1. **`super` (`sysadmin`):** Akses tingkat tertinggi ke seluruh sistem. Hanya ada 1 akun global: `sysadmin@seneca.local`.
2. **`owner`:** Pemilik organisasi/tenant. Mengontrol data seluruh tenant yang dimilikinya, mengundang pegawai, serta mendefinisikan Admin Roles kustom.
3. **`admin`:** Pegawai yang diberikan izin administratif khusus oleh Owner (`EMPLOYEE_MANAGE`, `ATTENDANCE_VIEW`, dll.).
4. **`employee`:** Pegawai biasa dalam tenant. Hanya dapat mengakses data pribadi.

Setiap data root dalam organisasi wajib memiliki `tenant_id` (*Multi-Tenancy Rule*).

---

## 2. Akun Demo & Uji Coba Bawaan

Kata sandi bawaan untuk seluruh akun demo di bawah ini adalah: **`Admin@Seneca2026!`**

| Peran | Email | Kata Sandi | Portal Akses |
|---|---|---|---|
| **Super Admin** | `sysadmin@seneca.local` | `Admin@Seneca2026!` | `http://localhost:8698/super/dashboard` |
| **Tenant Owner** | `owner@seneca.local` | `Admin@Seneca2026!` | `http://localhost:8698/owner/dashboard` |

---

## 3. Prasyarat & Menjalankan Aplikasi

### 3.1 Basis Data PostgreSQL 18
Pastikan PostgreSQL berjalan dengan ekstensi `vector` (pgvector) terpasang:
```bash
# Target database: seneca_hr_new
docker exec -i postgres psql -U postgres -c "CREATE DATABASE seneca_hr_new;"
docker exec -i postgres psql -U postgres -d seneca_hr_new -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### 3.2 Menjalankan dalam Mode Pengembangan (Dev Mode)
Aplikasi berjalan pada port HTTP **8698**:
```bash
./mvnw quarkus:dev
```
Akses antarmuka web pada: **http://localhost:8698/login**

### 3.3 Menjalankan Test Suite Otomatis
```bash
./mvnw test
```

### 3.4 Mengemas Aplikasi (Packaging)
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

---

## 4. Konfigurasi (`application.properties`)

```properties
# Database Datasource
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/seneca_hr_new
quarkus.datasource.username=postgres
quarkus.datasource.password=admin12345

# Flyway Migrations
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.baseline-on-migrate=true

# HTTP Server Port
quarkus.http.port=8698
quarkus.http.auth.form.enabled=false
quarkus.rest-csrf.enabled=false

# Seneca Security & Session Token (JJWT)
seneca.auth.session-secret=9f8c6b7e5d4a3b2c1f0e9d8c7b6a5f4e3d2c1b0a9f8e7d6c5b4a3f2e1d0c9b8a
seneca.auth.session-timeout-minutes=1440
```

---

## 5. Keamanan Sesi & Proteksi Navigasi Riwayat (V0.0.2 Defense-in-Depth)

Sistem mengadopsi standar pertahanan berlapis (*Defense in Depth*) berpedoman pada OWASP & W3C:
1. **Strict No-Cache:** Header `Cache-Control: no-cache, no-store, must-revalidate, max-age=0`, `Pragma: no-cache`, dan `Expires: 0` pada seluruh halaman HTML dan rute terproteksi melalui `@ServerResponseFilter`.
2. **Standard Web Security Headers:** Injeksi otomatis `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, dan `Referrer-Policy: strict-origin-when-cross-origin`.
3. **Pembersihan Bersih & Cepat (Instant Clean Logout):** Endpoint `/logout` (mendukung `GET` & `POST`) menghapus cookie `seneca_session` dengan `Max-Age=0` dan tanggal kedaluwarsa lampau, tombol UI menggunakan `window.location.replace('/logout')` untuk menggantikan riwayat navigasi, serta menghindari delay cold-restart cache browser sehingga transisi berlangsung instan (< 100 ms).
4. **Proteksi BFCache & History Traversal:** Script guard di `base.html` mendeteksi event `pageshow` (`persisted` / `back_forward`), melakukan DOM blanking seketika untuk mencegah kebocoran visual data, dan memicu *server reload* yang langsung dicegat oleh `WebSessionFilter`.

---

## 6. Dokumentasi Arsitektur Terkait

- [ARCHITECTURE.md](ARCHITECTURE.md): Standar arsitektur perangkat lunak, ERD skema basis data, spesifikasi pgvector, dan keamanan sesi.
- [PROJECT_INDEX.md](PROJECT_INDEX.md): Direktori pemetaan menyeluruh seluruh file kode, entity records, DAOs, repositories, handlers, controllers, dan templates.
- [agent.md](agent.md): Panduan pengembangan kode bagi AI Agent / Engineer.
- [docs/DESIGN.md](docs/DESIGN.md): Pedoman desain PatternFly Enterprise Light Interface.
- [docs/todo/V0.0.1__module-user-dan-tenant.md](docs/todo/V0.0.1__module-user-dan-tenant.md): Spesifikasi modul User & Tenant V0.0.1.
