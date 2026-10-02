# Indeks Proyek: Seneca HR Engine (PROJECT_INDEX.md)

Dokumen ini memetakan seluruh komponen kode, package, entitas, DAO, repository, handler bisnis, controller web, dan migrasi Flyway pada codebase Seneca HR.

---

## 1. Pemetaan Package & Komponen

```
src/main/java/tech/harlabs/
├── config/
│   └── JdbiProvider.java                     # CDI Provider untuk @Singleton Jdbi instance
├── dto/
│   └── MemberDetailDto.java                  # DTO informasi gabungan profil pegawai & peran admin
├── handler/
│   ├── AuthHandler.java                      # Logika otentikasi login, sesi, & ganti tenant
│   ├── SuperHandler.java                     # Logika operasional global Super Admin (sysadmin)
│   └── OwnerHandler.java                     # Logika operasional Tenant Owner
├── repo/
│   ├── util/
│   │   └── RepoUtil.java                     # Generator UUID v7 time-ordered sequential
│   └── jdbi/
│       ├── user/
│       │   ├── UsersEnt.java                 # Entity record tabel users
│       │   ├── UsersDao.java                 # JDBI SqlObject interface users
│       │   └── UsersRepos.java               # Repository bean users (@ApplicationScoped)
│       ├── tenant/
│       │   ├── TenantsEnt.java               # Entity record tabel tenants
│       │   ├── TenantsDao.java               # JDBI SqlObject interface tenants
│       │   └── TenantsRepos.java             # Repository bean tenants (@ApplicationScoped)
│       ├── tenantmember/
│       │   ├── TenantMembersEnt.java         # Entity record tabel tenant_members
│       │   ├── TenantMembersDao.java         # JDBI SqlObject interface tenant_members
│       │   └── TenantMembersRepos.java       # Repository bean tenant_members (@ApplicationScoped)
│       ├── adminrole/
│       │   ├── AdminRolesEnt.java            # Entity record tabel admin_roles
│       │   ├── AdminRolesDao.java            # JDBI SqlObject interface admin_roles
│       │   ├── AdminRolesRepos.java          # Repository bean admin_roles (@ApplicationScoped)
│       │   ├── AdminRolesPermissionsEnt.java # Entity record tabel admin_roles_permissions
│       │   ├── AdminRolesPermissionsDao.java # JDBI SqlObject interface admin_roles_permissions
│       │   └── AdminRolesPermissionsRepos.java # Repository bean admin_roles_permissions
│       ├── memberadminrole/
│       │   ├── MemberAdminRolesEnt.java      # Entity record tabel member_admin_roles
│       │   ├── MemberAdminRolesDao.java      # JDBI SqlObject interface member_admin_roles
│       │   └── MemberAdminRolesRepos.java    # Repository bean member_admin_roles
│       └── face/
│           ├── FaceEmbeddingsEnt.java        # Entity record tabel face_embeddings
│           ├── FaceEmbeddingsDao.java        # JDBI SqlObject interface face_embeddings
│           └── FaceEmbeddingsRepos.java      # Repository bean face_embeddings
└── web/
    ├── controller/
    │   ├── AuthController.java               # Web Controller login, logout, & switch tenant
    │   ├── SuperController.java              # Web Controller portal super admin (/super/**)
    │   └── OwnerController.java              # Web Controller portal tenant owner (/owner/**)
    └── security/
        ├── PasswordUtil.java                 # Helper hashing BCrypt via Quarkus BcryptUtil
        ├── SenecaUserSession.java            # Model sesi terotentikasi pengguna
        ├── SessionTokenService.java          # Service signing & parsing token sesi JJWT
        ├── WebSessionFilter.java             # @ServerRequestFilter pra-pencocokan & pelindung rute
        └── WebSessionHelper.java             # RequestScoped accessor sesi pengguna
```

---

## 2. Katalog Basis Data & Migrasi Flyway (Satu File per Tabel)

Direktori: `src/main/resources/db/migration/`

| File Migrasi | Target Tabel | Deskripsi |
|---|---|---|
| `V1__create_users.sql` | `users` | Akun pengguna global (email, phone, password_hash, full_name, is_active). |
| `V2__create_tenants.sql` | `tenants` | Organisasi / perusahaan multi-tenant (code, name, legal_name, npwp, timezone, status). |
| `V3__create_tenant_members.sql` | `tenant_members` | Keterhubungan user dengan tenant beserta peran (`owner`, `admin`, `employee`). |
| `V4__create_admin_roles.sql` | `admin_roles` | Definisi peran admin kustom yang dibuat oleh Owner per tenant. |
| `V5__create_admin_roles_permissions.sql` | `admin_roles_permissions` | Kode izin (*permission codes*) yang disematkan ke dalam peran admin. |
| `V6__create_member_admin_roles.sql` | `member_admin_roles` | Penugasan peran admin kepada pegawai tenant tertentu. |
| `V7__create_face_embeddings.sql` | `face_embeddings` | Vektor representasi wajah (512 dimensi) menggunakan tipe data native `vector(512)` (pgvector). |
| `V8__seed_sysadmin_and_permissions.sql` | Data Seeder | Inisialisasi akun tunggal `sysadmin`, tenant demo awal, peran standar `HR Administrator`, dan izin default. |

---

## 3. Katalog Antarmuka Web (Qute Type-Safe Templates)

Direktori: `src/main/resources/templates/`

| File Template | Base Path & Method | Rute Terkait | Deskripsi Halaman |
|---|---|---|---|
| `base.html` | Layout Shell | - | Layout utama bertema PatternFly Enterprise Light Mode (anti-glare Slate 100), 100% tipografi dan tulisan disatukan menggunakan font resmi Inter dari Google Fonts, proteksi khusus icon font Line Awesome, View Transitions cross-document bebas flickering, layout konten terpusat (`mx-auto`), aksen ungu terpercaya, Tailwind CSS, Alpine.js, Line Awesome, & HTMX. |
| `components/sidenav.html` | Reusable Component | - | Sidebar terpadu anti-flickering dengan desktop icon-only collapsed mode (`w-20` vs `w-64`), persistensi status di localStorage, non-reload active menu, dan mobile off-canvas drawer dengan backdrop blur. |
| `components/header.html` | Reusable Component | - | Header terpadu dengan hamburger drawer mobile, toggle ciutkan desktop, serta avatar/profil user dan tombol logout di pojok kanan atas. |
| `components/dropdown.html` | Reusable Component | - | Dropdown modern interaktif berbasis Alpine.js (`modernDropdown`) mendukung mode sederhana, filter/pencarian real-time, dan auto-submit. |
| `auth/login.html` | `auth/login` | `/login` | Formulir login akun terpadu bertema PatternFly Light Mode dengan shortcut kredensial uji coba. |
| `super/dashboard.html` | `super/dashboard` | `/super/dashboard` | Dasbor metrik sistem global dan daftar tenant terkini dengan grid kartu PatternFly. |
| `super/tenants.html` | `super/tenants` | `/super/tenants` | Manajemen direktori tenant, filter status, modal tambah tenant, dan modern dropdown ubah status. |
| `super/users.html` | `super/users` | `/super/users` | Direktori pengguna global, pencarian real-time, dan toggle aktivasi akun. |
| `owner/dashboard.html` | `owner/dashboard` | `/owner/dashboard` | Dasbor profil tenant aktif, ringkasan pegawai, tenant switcher modern dropdown, dan modal konfigurasi. |
| `owner/members.html` | `owner/members` | `/owner/members` | Direktori pegawai tenant, form tambah pegawai, dan modern dropdown penggantian peran. |
| `owner/roles.html` | `owner/roles` | `/owner/roles` | Pengaturan Admin Roles kustom dan matriks perizinan (*permissions*). |
| `owner/face-embeddings.html` | `owner/face-embeddings` | `/owner/face-embeddings` | Monitoring data biometrik wajah pegawai dan simulasi pendaftaran vektor pgvector (`vector(512)`). |

---

## 4. Konfigurasi Layanan

File: `src/main/resources/application.properties`
- **Port HTTP:** `8698` (`quarkus.http.port=8698`)
- **Basis Data:** `seneca_hr_new` di `jdbc:postgresql://localhost:5432/seneca_hr_new`
- **Flyway:** Aktif saat start (`quarkus.flyway.migrate-at-start=true`)
- **Otentikasi:** Sesi cookie HTTP-Only `seneca_session` dengan masa aktif 1440 menit (24 jam)
