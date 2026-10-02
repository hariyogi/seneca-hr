---
version: "patternfly-enterprise-light"
name: "Seneca HR PatternFly Enterprise Light Interface"
description: "Sistem desain antarmuka enterprise Seneca HR berbasis PatternFly (https://www.patternfly.org/) dengan tema default Light Mode yang nyaman di mata (anti-glare), aksen ungu modern dan terpercaya (Trusted Purple), font Inter resmi dari Google Fonts, posisi main view terpusat di tengah (mx-auto), navigasi samping collapsable, header terpadu dengan aksi user di pojok kanan atas, serta komponen dropdown modern interaktif."
colors:
  primary: "#6d28d9"      # Purple 700 (Modern & Trusted Brand Primary)
  primary-hover: "#5b21b6"# Purple 800
  primary-light: "#f5f3ff"# Purple 50 (Tint / Active State)
  background: "#f1f5f9"   # Slate 100 (Soothing Canvas Background - Anti-Glare)
  surface: "#ffffff"      # Pure White (Card & Modal Surfaces)
  surface-subtle: "#e2e8f0"# Slate 200 (Dividers, Active Pill Track)
  border: "#e2e8f0"       # Slate 200 (Clean Hairline Borders)
  border-dark: "#cbd5e1"  # Slate 300 (Input / Control Borders)
  text-primary: "#0f172a" # Slate 900 (High-Contrast Heading & Body)
  text-secondary: "#64748b" # Slate 500 (Subtitles, Metadata)
  accent-success: "#059669" # Emerald 600 (Active, Online)
  accent-warning: "#d97706" # Amber 600 (Suspended, Notice)
  accent-danger: "#e11d48"  # Rose 600 (Closed, Logout, Inactive)
typography:
  fontFamily-sans: "'Inter', system-ui, -apple-system, BlinkMacSystemFont, sans-serif"
  fontFamily-mono: "'Inter', system-ui, -apple-system, BlinkMacSystemFont, sans-serif"
---

# Seneca HR Design System: PatternFly Enterprise Light Mode

## 1. Filosofi & Karakteristik Desain
Desain Seneca HR mengadopsi standar sistem desain enterprise **[PatternFly](https://www.patternfly.org/)**:
* **Universal Inter Font (100% Inter):** Seluruh tulisan dan tipografi antarmuka tanpa terkecuali menggunakan font resmi **Inter** (dimuat via Google Fonts CDN: `Inter:wght@300;400;500;600;700;800;900`). Seluruh teks judul, paragraf, label form, tombol, navigasi, tabel data, badge status, hingga input form disatukan menggunakan font **Inter** (`body, input, button, select, textarea, optgroup { font-family: 'Inter', ... }`). Font icon (**Line Awesome**) diproteksi secara ketat (`.la, .las, .lar, .lab { font-family: 'Line Awesome Free' !important; }`) agar seluruh glyph ikon selalu tampil sempurna tanpa bentrok Unicode box/tofu.
* **Soft Light Mode (Anti-Glare / Nyaman di Mata):** Menghindari latar putih murni 100% yang menyilaukan pada layar lebar. Latar belakang kanvas aplikasi menggunakan **Slate 100 (`#f1f5f9` / `bg-slate-100`)**, senada dengan standar kanvas PatternFly (`--pf-v5-global--BackgroundColor--200`). Kartu dan panel konten menggunakan warna putih berbingkai halus (`bg-white border border-slate-200 shadow-xs`), memberikan kedalaman visual dan kontras yang seimbang dan tidak melelahkan mata.
* **Main View Terpusat di Tengah (*Centered Content Layout*):** Konten utama aplikasi dibungkus dengan `w-full max-w-7xl mx-auto` sehingga selalu berada di tengah area tampilan (tidak menempel ke sisi kiri pada monitor resolusi tinggi).
* **Modern & Trusted Purple:** Aksen warna utama menggunakan gradasi ungu modern (`#6d28d9` / `purple-700`) yang memancarkan stabilitas, profesionalisme, dan otoritas enterprise.

---

## 2. Palet Token Warna (Tailwind CSS Extended)

| Token | Nilai Hex | Penggunaan Utama |
|---|---|---|
| `brand-50` | `#f5f3ff` | Latar item aktif navigasi, pill status, dan baris terpilih |
| `brand-100` | `#ede9fe` | Tag badge tenant dan highlight lembut |
| `brand-600` | `#7c3aed` | Status hover dan fokus interaktif |
| `brand-700` | `#6d28d9` | **Warna Brand Utama**: Tombol aksi utama, header aktif, icon |
| `brand-800` | `#5b21b6` | State tombol primary aktif / ditekan |
| `canvas` | `#f1f5f9` | **Latar Kanvas Aplikasi** (`bg-slate-100`) lembut dan nyaman di mata |
| `surface` | `#ffffff` | Kartu tabel, modal dialog, panel dropdown, dan header |
| `border` | `#e2e8f0` | Garis pembatas kontainer dan divider baris |
| `border-ctrl`| `#cbd5e1` | Garis input field, trigger dropdown, dan kontrol form |

---

## 3. Komponen Utama & Pola Responsif

### 3.1 Main View Terpusat (`w-full max-w-7xl mx-auto`)
* Kontainer konten di dalam area tampilan utama (`flex-1 min-w-0 overflow-y-auto`) dibungkus menggunakan kelas `w-full max-w-7xl mx-auto p-4 sm:p-6 lg:p-8 space-y-6`.
* Menjamin tata letak simetris di tengah pada monitor desktop ultrawide, 1440p, maupun 1080p, sekaligus tetap adaptif dengan padding responsif pada tablet dan ponsel.

### 3.2 Side Navigation Terpadu, Collapsible & Anti-Flickering (`components/sidenav.html`)
* **Bebas Flickering & Layout Shifts:**
  - Sidebar diinisialisasi dengan kelas lebar default statis (`lg:w-64 -translate-x-full lg:translate-x-0`) sehingga ter-render tepat pada lebar target sebelum Alpine.js di-mount tanpa layout jumping.
  - Transisi dibatasi secara spesifik pada `transition-[width,transform] duration-200 ease-in-out` (menghindari `transition-all` yang menggerakkan padding konten).
  - Tautan menu yang sedang aktif dicegah dari reload ulang (`href="javascript:void(0)" aria-current="page"`).
  - Mengaktifkan cross-document View Transitions natif (`@view-transition { navigation: auto; }`) untuk transisi antar halaman yang mulus.
  - Status ciutkan/perluas (`sidebarCollapsed`) disimpan di `localStorage` (`seneca_sidebar_collapsed`) agar posisi sidebar tetap konsisten saat perpindahan halaman.
* **Dual-Mode Desktop:**
  - **Expanded Mode (Lebar 256px / `w-64`):** Menampilkan ikon, label menu, nama sistem, dan badge portal (`Super Admin` / `Owner Portal`).
  - **Collapsed Mode (Lebar 80px / `w-20`):** Menyusut dan **hanya menampilkan ikon** navigasi secara terpusat (`justify-center`).
  - **Toggle Kontrol:** Disediakan tombol ciutkan/perluas di bagian bawah sidebar serta tombol hamburger di header atas.
* **Mobile Drawer Off-Canvas:**
  - Pada layar ponsel (`lg:hidden`), sidebar bertransformasi menjadi off-canvas drawer yang meluncur masuk (`translate-x-0`) dilengkapi *backdrop blur* gelap (`bg-slate-900/50 backdrop-blur-xs`) dengan transisi halus.

### 3.3 Top Header Masthead (`components/header.html`)
* **Posisi Sticky Atas:** Memastikan kontrol selalu dapat diakses saat konten digulir.
* **Kiri:** Tombol hamburger (mobile) / toggle ciutkan sidebar (desktop), judul modul halaman, dan subjudul kontekstual.
* **Kanan Atas (Top-Right):** 
  - **User Profile Pill:** Menampilkan avatar inisial berwarna ungu, nama lengkap user, alamat email, dan pill hak akses (`SUPER` / `OWNER` / peran aktif).
  - **Tombol Logout:** Tombol *Keluar Sesi* (`/logout`) diletakkan di **pojok kanan atas**, konsisten dengan standar usability enterprise modern.

### 3.4 Komponen Dropdown Modern (`components/dropdown.html` & `modernDropdown`)
* **Zero Raw `<select>`:** Seluruh elemen `<select>` HTML standar digantikan oleh komponen dropdown berbasis Alpine.js.
* **Dua Mode Operasi:**
  1. **Mode Filterable / Searchable (`searchable: true`):** Dilengkapi input pencarian cepat real-time untuk memilih dari banyak opsi (misal: pemilihan zona waktu, pemilihan tenant aktif).
  2. **Mode Sederhana (`searchable: false`):** Dropdown pilihan bersih untuk pergantian status tenant atau peran pegawai.
* **Integrasi Form Native (`onchange: 'submit'`):** Mendukung auto-submit formulir secara otomatis saat opsi dipilih melalui input tersembunyi (`<input type="hidden" :name="name" :value="value">`).
* **Fitur Aksesibilitas:** Auto-close saat klik di luar (`@click.outside="open = false"`), indikator checkmark pada opsi aktif, dan animasi rotasi chevron 180°.