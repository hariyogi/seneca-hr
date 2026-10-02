# Panduan Agent: Quarkus & JDBI High-Performance Engine (Seneca HR)

Anda adalah seorang Principal Software Architect, Backend Engineer dan Frontend Engineer ahli yang terspesialisasi dalam Java, Quarkus, serta sistem transaksional berperforma tinggi (*high-throughput*). Ikuti seluruh instruksi dan konvensi ini secara ketat pada setiap tugas pengembangan dan pemeliharaan codebase Seneca HR.

---

## 1. Arsitektur Inti & Filosofi
* **Pemisahan Tanggung Jawab & Struktur Berlapis (*Layering & Separation of Concerns*):**
  - `Endpoint / Web Controller` (`tech.harlabs.web.controller.*`): Mengelola input HTTP, form submission, status code, dan mendelegasikan pemrosesan ke Handler. **Dilarang keras** menaruh logika bisnis atau query SQL langsung di Controller.
  - `Service / Handler` (`tech.harlabs.handler.*`): Menampung aturan bisnis (*business rules*), orkestrasi lintas domain/tabel, integrasi token sesi (JJWT), verifikasi kata sandi (Quarkus Security `BcryptUtil`), dan mengontrol batas transaksi (*transaction boundary*).
  - `Repository` (`tech.harlabs.repo.jdbi.*.*Repos`): Lapisan abstraksi data akses bean `@ApplicationScoped` yang mengorkestrasikan JDBI `withExtension`, `useExtension`, atau handle transaksi.
  - `DAO (JDBI SqlObject)` (`tech.harlabs.repo.jdbi.*.*Dao`): Interface JDBI 3 SqlObject untuk deklarasi query SQL statis (`@SqlQuery`, `@SqlUpdate`).
  - `Record / Model / Entity` (`tech.harlabs.repo.jdbi.*.*Ent`): Java Record immutable dengan konstanta string kolom untuk integritas kompilasi.
* **Hierarki Peran 4-Tingkat (*4-Tier Roles*):**
  - `super -> owner -> admin -> employee`.
  - `super`: Akses ke seluruh sistem global. Hanya ada tepat satu akun bernama `sysadmin`.
  - `owner`: Pengguna pemilik tenant/perusahaan. Dapat melihat dan mengelola seluruh data tenant miliknya, mengundang pegawai, dan mendefinisikan admin roles.
  - `admin`: Pegawai yang diberikan izin administratif oleh Owner, dibatasi oleh daftar izin (*permissions*).
  - `employee`: Pegawai biasa dalam suatu tenant. Hanya dapat mengakses data dirinya sendiri.
* **Aturan Multi-Tenancy (Root Table Rule):**
  - Setiap tabel data root (tabel operasional tenant) **WAJIB** menyertakan kolom `tenant_id UUID REFERENCES tenants(id)`.
  - Seorang user dapat tergabung dalam lebih dari satu tenant melalui tabel `tenant_members`.
* **Referensi Arsitektur:** Selalu ikuti seluruh standar database, schema, dan entity yang didefinisikan dalam `ARCHITECTURE.md`.
* **Indeks Komponen:** Gunakan `PROJECT_INDEX.md` untuk melacak pemetaan lokasi package, DTO, Entity, DAO, Repository, dan Handler.
* **Design Frontend:** Lihat di `docs/DESIGN.md` untuk panduan sistem desain PatternFly Enterprise Light Mode (https://www.patternfly.org/) dengan tema default terang yang nyaman di mata (anti-glare `bg-slate-100`), seluruh tipografi dan tulisan disatukan menggunakan font **Inter** resmi dari Google Fonts, posisi main view terpusat di tengah (`mx-auto`), dan aksen ungu terpercaya (`#6d28d9` / `purple-700`).

---

## 2. Tech Stack & Konvensi
* **Runtime & Bahasa:** Java 25.
  - Prioritaskan Java `record`, pattern matching untuk `switch` dan `instanceof`, multiline text blocks (`"""`), serta Virtual Threads jika relevan.
* **Framework:** Quarkus REST (RESTEasy Reactive) dengan Jackson (`quarkus-rest-jackson`).
* **Web UI Layer (SSR Monolith Terintegrasi):**
  - **Port Layanan:** Port HTTP default adalah **8698** (`quarkus.http.port=8698`).
  - **Quarkus Web Bundler:** Mengemas static bundle `app.js` dan `app.css` secara otomatis dari `src/main/resources/web/`.
  - **Dependencies Frontend (mvnpm):** Dideklarasikan di `pom.xml` dengan `<scope>provided</scope>` (HTMX 2.0.4, Alpine.js 3.14.8, Hyperscript 0.9.14, Floating UI DOM 1.6.13 via `org.mvnpm.at.floating-ui:dom`, Line Awesome 1.3.0).
  - **Styling & Layout:** Tailwind CSS v4 dengan 100% font **Inter** di seluruh tulisan/elemen UI (`body, input, button, select, textarea, optgroup`), proteksi khusus icon font Line Awesome (`.la, .las, .lar, .lab { font-family: 'Line Awesome Free' !important; }`), kanvas Slate 100 (`#f1f5f9`) yang menyejukkan mata, main view terpusat di tengah (`w-full max-w-7xl mx-auto`), navigasi samping collapsible bebas flickering (`components/sidenav.html` dengan View Transitions, localStorage state persistence, dan active item non-reload), header terpadu dengan aksi profil & logout di pojok kanan atas (`components/header.html`), dan komponen dropdown modern (`components/dropdown.html` / `modernDropdown`).
  - **Templating & Type-Safe Templates:** Wajib menggunakan **Type-Safe Templates (`@CheckedTemplate` static class)** sesuai panduan resmi Quarkus Qute dengan dependensi `io.quarkus:quarkus-rest-qute`. Dilarang menggunakan injeksi runtime `@Inject Template` / `@Location` biasa agar seluruh ekspresi variabel dan parameter divalidasi pada saat build/kompilasi (`CheckedTemplate.HYPHENATED_ELEMENT_NAME`).
  - **Web Security & Session (Defense-in-Depth):** Menggunakan `WebSessionFilter` yang mengombinasikan `@ServerRequestFilter(preMatching = true)` untuk autentikasi/otorisasi rute (`/super/**`, `/owner/**`) serta `@ServerResponseFilter` untuk menyematkan HTTP Security Headers (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`) dan strict no-cache (`Cache-Control: no-cache, no-store, must-revalidate, max-age=0`, `Pragma: no-cache`, `Expires: 0`). Alur logout melakukan pembersihan sesi instan (< 100 ms) via invalidasi cookie kedaluwarsa lampau dan history replacement tanpa cold-restart cache browser. Pada lapisan UI (`base.html`), dipasang guard siklus hidup `pageshow` dengan DOM blanking dan server reload otomatis untuk mencegah eksploitasi Back-Forward Cache (bfcache) dan history traversal.
  - **In-Process Controller:** Web Controller (`tech.harlabs.web.controller.*`) menginjeksi Service/Handler dan Repository CDI secara langsung (tanpa overhead HTTP/REST internal).
* **Basis Data & Ekstensi:** PostgreSQL 18+ (driver `io.quarkus:quarkus-jdbc-postgresql`, `org.jdbi:jdbi3-postgres`, Flyway PostgreSQL).
  - Target database default: `seneca_hr_new`.
  - **Biometrik Vektor Wajah (pgvector):** Menggunakan tipe native `vector(512)` via ekstensi `pgvector`.
* **Flyway Migrations:**
  - **Aturan Ketat:** Satu file migrasi untuk satu tabel (`V1__create_users.sql`, `V2__create_tenants.sql`, dst.).
* **Persistensi (Dilarang Menggunakan Hibernate / JPA / Panache):**
  - Murni menggunakan **JDBI 3 Core**, **JDBI 3 SqlObject**, dan **JDBI 3 Postgres Plugin**.
  - **Entity/Tabel Database:** Wajib menggunakan Java `record` dengan konstanta compile-time (`public static final String TABLE_NAME`, `ID`, `FIELDS`, `BINDER`).
  - **Larangan String.join() pada Konstanta Anotasi:** Dilarang menggunakan `String.join()` pada konstanta yang digunakan dalam anotasi `@SqlQuery` / `@SqlUpdate`, karena anotasi Java memerlukan *compile-time constant expression* (gunakan operator `+`).
* **Strategi Primary Key & Tipe Data ID:**
  - **Tabel Transaksional & Domain:** Wajib bertipe `java.util.UUID` sequential v7 (GUID) yang digenerate menggunakan library `com.github.f4b6a3:uuid-creator` melalui utilitas `RepoUtil.generateId()`.
* **CDI Scoping & Dependency Injection:**
  - JDBI Instance: Diproduksi sebagai `@Singleton` bean via `JdbiProvider.java` (`@Produces Jdbi`).
  - Service/Handler: `@ApplicationScoped`.
  - Repository: `@ApplicationScoped`.

---

## 3. Aturan JDBI: Kinerja & Larangan OnDemand
* **DILARANG KERAS MENGGUNAKAN `jdbi.onDemand(...)`:**
  - `jdbi.onDemand(...)` dilarang untuk semua DAO di codebase ini guna mencegah connection pool churn, proxy lifecycle overhead, dan leak koneksi.
* **Standar Eksekusi DAO SqlObject:**
  - Gunakan `jdbi.withExtension(Dao.class, dao -> dao.method(...))` untuk operasi baca (*read*) yang mengembalikan hasil.
  - Gunakan `jdbi.useExtension(Dao.class, dao -> dao.method(...))` untuk operasi mutasi (*write*) void.
  - Di dalam transaksi aktif atau blok handle, attach DAO langsung melalui `handle.attach(Dao.class)`.
* **Kapan Menggunakan SqlObject vs Fluent JDBI:**
  - **Gunakan SqlObject DAO:** Untuk seluruh query CRUD standar, single lookup, query statis, dan pagination terstandar.
  - **Gunakan Fluent JDBI (`jdbi.withHandle`, `handle.select`, `WhereQuery`, `DbUtil.wrapPaged`):** Khusus untuk query dinamis yang memiliki banyak klausa `WHERE` kondisional opsional (contoh: `UserInfoRepos.findFilter`).

---

## 4. Transaksi & Batasan Konkurensi
* **Batas Transaksi (*Transaction Boundary*):**
  - Setiap operasi mutasi data (`INSERT`, `UPDATE`, `DELETE`) yang memengaruhi beberapa tabel atau beberapa baris data **WAJIB** dijalankan di dalam blok transaksi.
  - Gunakan `jdbi.useTransaction(handle -> { ... })` atau `jdbi.inTransaction(handle -> { ... })` di dalam lapisan Service layer atau Repository.
  - Hindari operasi I/O jaringan yang lambat (seperti HTTP call ke vendor eksternal atau pengiriman SMTP email) di dalam blok database transaction yang aktif; kirim email/notifikasi di luar blok transaksi.

---

## 5. Dokumentasi & Gaya Penulisan Kode
* **Komentar Langkah Demi Langkah Wajib (*Inline Comments*):**
  - Selalu sertakan komentar sebaris bertahap untuk logika yang kompleks:
    1. Parsing, validasi, dan penyiapan payload token/kredensial.
    2. Enkripsi/dekripsi JWE dan penandatanganan JWS.
    3. Hashing kredensial dengan BCrypt.
    4. Batasan transaksi database multi-tabel.
* **Javadoc:**
  - Buat Javadoc secara ringkas dan informatif pada setiap public method di controller, handler, dan repository.
* **Sinkronisasi README.md (Wajib):**
  - **Wajib Update README.md:** Setiap kali terjadi:
    - Penambahan dependency baru atau penggantian dependency di `pom.xml`.
    - Pembaruan konfigurasi database, JDBC driver, atau property aplikasi di `application.yml` / `.env`.
    - Modifikasi atau penambahan endpoint API dan alur otentikasi.
* **Jika perlu update AGENT.md, ARCHITECTURE.md dan PROJECT_INDEX.md**

---

## 6. Referensi Pola Kode

### Entity Record
```java
package tech.harlabs.repo.jdbi.example;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import tech.harlabs.repo.util.RepoUtil;

public record ExampleEnt(
    @ColumnName(ID) UUID id,
    @ColumnName(NAME) String name,
    @ColumnName(CREATED_AT) LocalDateTime createdAt
) {
    public static final String TABLE_NAME = "example";
    public static final String ID = "ex_id";
    public static final String NAME = "ex_name";
    public static final String CREATED_AT = "ex_created_at";

    // Gunakan konkat string langsung (hindari String.join untuk kebutuhan anotasi)
    public static final String FIELDS = ID + ", " + NAME + ", " + CREATED_AT;
    public static final String BINDERS = ":id, :name, :createdAt";

    public static ExampleEnt create(String name) {
        return new ExampleEnt(RepoUtil.generateId(), name, LocalDateTime.now());
    }
}
```

### DAO SqlObject
```java
package tech.harlabs.repo.jdbi.example;

import static tech.harlabs.repo.jdbi.example.ExampleEnt.*;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterConstructorMapper(ExampleEnt.class)
public interface ExampleDao {

    @SqlUpdate("INSERT INTO " + TABLE_NAME + " (" + FIELDS + ") VALUES (" + BINDERS + ")")
    void insert(@BindMethods ExampleEnt ent);

    @SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Optional<ExampleEnt> findById(@Bind("id") UUID id);
}
```

### Repository Bean
```java
package tech.harlabs.repo.jdbi.example;

import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExampleRepos {

    private final Jdbi jdbi;

    public ExampleRepos(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public ExampleEnt createEnt(String name) {
        var ent = ExampleEnt.create(name);
        // Selalu gunakan withExtension atau useExtension (DILARANG onDemand)
        jdbi.useExtension(ExampleDao.class, dao -> dao.insert(ent));
        return ent;
    }

    public Optional<ExampleEnt> findById(UUID id) {
        return jdbi.withExtension(ExampleDao.class, dao -> dao.findById(id));
    }
}
```

---

## 10. Standar Komponen UI: Floating UI Terpadu (@floating-ui/dom) untuk Dropdown, Menu, Popover & Tooltip
* **Filosofi & Konsistensi UI:**
  - Seluruh elemen mengambang (*floating elements*) seperti dropdown pemilihan, menu aksi tabel, popover navigasi, dan tooltip distandarisasi menggunakan pustaka resmi **Floating UI DOM (`@floating-ui/dom`)** berbasis JavaScript murni (tanpa React/framework JS berat).
  - Mengintegrasikan fungsi inti `computePosition` dan `autoUpdate` dengan middleware `offset`, `flip`, `shift`, dan `size`.
* **Dropdown & Combobox Terpadu (`components/dropdown.html` & `Alpine.data('dropdownSelect')`):**
  - Mengeliminasi tag raw `<select>` HTML standar agar seluruh form memiliki tampilan modern, rounded border (`rounded-xl`), efek ring focus (`focus:ring-2 focus:ring-blue-500`), dan transisi chevron 180°.
  - **Positioning Dinamis:** Floating UI memposisikan menu dropdown relatif terhadap trigger input atau button secara dinamis. Menggunakan middleware `size` untuk menyamakan lebar menu dengan elemen trigger (`rects.reference.width`) serta membatasi tinggi maksimum menu sesuai ruang viewport yang tersedia (`availableHeight`).
  - **Auto Flip & Shift:** Jika posisi dropdown berada di dekat batas bawah layar, menu secara otomatis membalik ke atas (`placement: 'top-start'`) via `flip()` dan bergeser menjauhi tepi layar via `shift({ padding: 8 })`.
  - **Mode Filterable / Searchable (`searchable=true`):** Dilengkapi input pencarian real-time (nama & ID), badge kecocokan (`Cocok Nama`, `Cocok ID`, `Cocok Nama & ID`), clear button, dan preview item terpilih.
  - **Mode Sederhana / Simple (`searchable=false`):** Dropdown pilihan statis dengan trigger button dan visual checkmark seragam.
  - **Data Injection via DOM (`sourceId`):** Opsi didefinisikan secara bersih lewat hidden DOM element `<span data-id="..." data-name="..." data-desc="..."></span>` untuk menghindari syntax conflict di Qute.
* **Menu Aksi & Popover Mengambang (`Alpine.data('floatingMenu')` & `Alpine.data('floatingPopover')`):**
  - Digunakan pada menu aksi tabel (seperti `/admin/users` dan `/admin/auth-methods`) serta dropdown profil di navbar layout member.
  - **Strategi Bebas Clipping (`strategy: 'fixed'`):** Memposisikan menu secara tetap dalam koordinat viewport sehingga menu tidak pernah terpotong (*clipped*) oleh kontainer tabel atau pembungkus yang memiliki `overflow-x: auto` atau `overflow: hidden`.
  - Mendukung penutupan otomatis saat klik luar (`@click.outside="close()"`) dan lifecycle `autoUpdate` yang di-cleanup secara otomatis saat ditutup.
* **Pencegahan Flickering (*Zero-Flicker Floating Elements*):**
  - **Kelas Penempatan & Batasan Dimensi Eksplisit di HTML:** Elemen floating wajib memiliki kelas `fixed` atau `absolute` langsung pada markup HTML agar browser tidak menempatkannya dalam normal document flow saat dibuka pertama kali. Untuk dropdown (`components/dropdown.html`), kelas `absolute left-0 top-full mt-1.5 w-full max-h-60` wajib dipasang langsung di markup agar elemen memiliki lebar dan batas tinggi yang terkunci (maks 240px) sejak frame ke-0, mencegah ekspansi liar yang memicu munculnya scrollbar mendadak (*scrollbar thrashing*) pada modal atau kontainer `overflow-y: auto`.
  - **Inisialisasi Bersih & Visibilitas Atomik:** Komponen menjaga `visibility: hidden` selama inisialisasi dan fase kalkulasi posisi `computePosition`. Jangan menyetel `top: 0` atau `left: 0` pada JavaScript `init()`. Nilai `visibility: visible` hanya disetel bersamaan dengan koordinat `left` dan `top` setelah kalkulasi selesai.
  - **Optimasi `autoUpdate` & Pencegahan Loop ResizeObserver:** Hindari pemanggilan manual `update()` tepat sebelum `autoUpdate(reference, floating, update)` karena `autoUpdate` sudah menjalankan `update()` secara otomatis pada awal pemanggilan. Saat menggunakan middleware `size` yang memodifikasi dimensi elemen floating, sertakan opsi `{ elementResize: false }` pada `autoUpdate` guna mencegah pemicuan ulang ResizeObserver yang tidak perlu. Hal ini menjamin transisi 100% mulus dan bebas flickering pada klik pertama maupun seterusnya di seluruh dropdown (termasuk di dalam modal S2S).
* **Universal Tooltip Manager (`[data-tooltip]` & `Alpine.directive('tooltip')`):**
  - Sistem tooltip global yang terdaftar di `app.js` menggunakan singleton tooltip DOM yang diposisikan secara presisi via Floating UI (`offset(6)`, `flip()`, `shift({ padding: 6 })`).
  - Cukup menambahkan atribut `data-tooltip="Pesan"` (opsional: `data-placement="top|bottom|left|right"`) atau direktif Alpine `x-tooltip="'Pesan'"` pada elemen HTML mana pun. Event delegation otomatis menangani hover dan focus.