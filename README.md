# 📚 Sistem Perpustakaan MyPerpus : Library Management System

Aplikasi manajemen perpustakaan berbasis desktop yang dikembangkan menggunakan bahasa **Java** dengan pendekatan **Object-Oriented Programming (OOP)**. Sistem ini dirancang untuk memfasilitasi operasional perpustakaan secara menyeluruh  mulai dari manajemen pengguna, sirkulasi peminjaman, pengelolaan katalog buku, hingga kalkulasi denda.

---

## 🌟 Fitur Utama

Sistem menggunakan **control berdasarkan role** dengan tiga jenis pengguna:

### 👤 Admin
- **Manajemen Akun:** Membuat, memperbarui, dan menghapus akun Librarian *(hanya Admin yang dapat mendaftarkan akun Librarian)*; mereset password akun Member.
- **Konfigurasi Sistem:** Mengatur aturan inti perpustakaan , biaya denda keterlambatan, batas maksimal masa pinjam, toleransi hari pengambilan (*pickup window*), kuota peminjaman per anggota, dan batas maksimal hari reservasi.
- **Dashboard Statistik:** Memantau total pengguna aktif, inventaris buku, akumulasi denda terkumpul, dan judul buku yang paling sering dipinjam.

### 📖 Librarian (Pustakawan)
- **Manajemen Katalog (CRUD):** Mengelola kategori buku, judul buku referensi (*BookTitle*), dan eksemplar fisik (*BookCopy*) beserta lokasi penyimpanannya.
- **Sirkulasi Peminjaman:** Memproses peminjaman *offline* langsung di tempat, mengonfirmasi pengambilan buku dari peminjaman *online*, serta mengeksekusi proses pengembalian buku beserta pencatatan pembayaran denda.
- **Monitoring Transaksi:** Melacak status seluruh transaksi secara menyeluruh , aktif, *overdue*, menunggu pengambilan (*waiting pickup*), atau kedaluwarsa (*expired*).

### 🙋 Member (Anggota)
- **Eksplorasi Katalog:** Mencari buku berdasarkan kata kunci *(judul, penulis, ISBN, penerbit, deskripsi)* maupun kategori spesifik.
- **Peminjaman Mandiri:** Melakukan reservasi dan peminjaman buku secara *online* dengan memilih tanggal pengambilan.
- **Transparansi Akun:** Memeriksa daftar buku yang sedang dipinjam, riwayat peminjaman lengkap, *due date*, status transaksi, dan tagihan denda.

---

## 🏗️ Arsitektur & Teknologi

| Aspek | Detail |
|---|---|
| Bahasa Pemrograman | Java |
| Paradigma | Object-Oriented Programming (OOP) |
| Database | MySQL |
| Konektivitas DB | JDBC (Java Database Connectivity) |
| Pola Arsitektur | Layered Architecture + Repository Pattern |
| Prinsip Desain | SOLID Principles |

---

## 🧱 Konsep OOP yang Diterapkan

### Encapsulation
Seluruh atribut pada kelas entitas (`User`, `Member`, `BookTitle`, `BookCopy`, `LoanTransaction`, dll.) dideklarasikan sebagai `private` dan hanya dapat diakses melalui metode *getter* dan *setter*.

### Inheritance
Kelas abstrak `User` sebagai *superclass* yang mewariskan atribut umum (`id`, `name`, `email`, `passwordHash`, `role`, `active`) ke tiga *subclass*: `Member`, `Librarian`, dan `Admin`.

```
User (abstract)
├── Member
├── Librarian
└── Admin
```

### Polymorphism
- **Overloading:** Variasi konstruktor pada kelas entitas (contoh: `Member()` dengan parameter berbeda).
- **Inclusion:** Metode `login()` di `AuthService` mengembalikan tipe `User` yang secara aktual bisa berupa `Admin`, `Member`, atau `Librarian`.
- **Parametric:** Interface generik `IRepository<T, ID>` yang dikhususkan menjadi `IUserRepository extends IRepository<User, Integer>`.

### Abstraction
- Kelas `User` sebagai *abstract class* mendeklarasikan metode abstrak `getDashboardTitle()` dan `getPermissions()` yang wajib diimplementasikan oleh setiap *subclass*.
- Interface `IAuditable` dan `ISearchable` mendefinisikan kontrak perilaku lintas kelas.

---

## 🔷 Prinsip SOLID

| Prinsip | Penerapan |
|---|---|
| **SRP** | Setiap lapisan punya tanggung jawab tunggal , `Entity` untuk data, `Service` untuk logika bisnis, `Repository` untuk akses database, `GUI` untuk tampilan. |
| **OCP** | Kelas `User` terbuka untuk ekstensi (tambah jenis pengguna baru) tanpa mengubah kode yang sudah ada. |
| **LSP** | `Admin`, `Librarian`, dan `Member` dapat menggantikan `User` di mana pun tanpa mengubah perilaku program. |
| **ISP** | Interface dipecah sesuai kebutuhan: `ISearchable` untuk pencarian, `IAuditable` untuk audit data. |
| **DIP** | `UserService` bergantung pada abstraksi `IUserRepository`, bukan pada implementasi database secara langsung. |

---

## 📂 Struktur Proyek

```
src/
└── com/library/
    ├── domain/
    │   ├── entities/       # Objek bisnis utama
    │   │   ├── User.java           (abstract)
    │   │   ├── Member.java
    │   │   ├── Librarian.java
    │   │   ├── Admin.java
    │   │   ├── BookTitle.java
    │   │   ├── BookCopy.java
    │   │   ├── Category.java
    │   │   ├── LoanTransaction.java
    │   │   ├── LibraryConfig.java
    │   │   └── DashboardStats.java
    │   ├── enums/          # Konstanta state sistem
    │   │   ├── UserRole.java
    │   │   ├── MemberStatus.java
    │   │   ├── LoanStatus.java
    │   │   ├── BookCopyStatus.java
    │   │   └── BorrowType.java
    │   └── interfaces/     # Kontrak perilaku
    │       ├── IAuditable.java
    │       ├── ISearchable.java
    │       └── IRepository.java
    ├── service/            # Business logic layer
    │   ├── AuthService.java
    │   ├── BookService.java
    │   ├── CategoryService.java
    │   ├── ConfigService.java
    │   ├── LoanService.java
    │   ├── ReportService.java
    │   └── UserService.java
    ├── repository/         # Data access layer (MySQL)
    │   ├── IUserRepository.java
    │   ├── IBookTitleRepository.java
    │   ├── IBookCopyRepository.java
    │   ├── ICategoryRepository.java
    │   ├── ILoanTransactionRepository.java
    │   ├── ILibraryConfigRepository.java
    │   ├── UserRepositoryMySQLImpl.java
    │   ├── BookTitleRepositoryMySQLImpl.java
    │   ├── BookCopyRepositoryMySQLImpl.java
    │   ├── CategoryRepositoryMySQLImpl.java
    │   ├── LoanTransactionRepositoryMySQLImpl.java
    │   └── LibraryConfigRepositoryMySQLImpl.java
    └── util/               # Kelas utilitas
        ├── PasswordHasher.java
        └── FineCalculator.java
```

---

## 🗃️ Model Domain Utama

| Kelas | Deskripsi |
|---|---|
| `User` | Kelas abstrak induk semua pengguna sistem. |
| `Member` | Anggota perpustakaan dengan data keanggotaan dan status aktif/suspend. |
| `Librarian` | Petugas perpustakaan dengan data kepegawaian dan info shift. |
| `Admin` | Administrator dengan hak akses tertinggi. |
| `BookTitle` | Data katalog buku (judul, penulis, ISBN, penerbit, deskripsi, kategori). |
| `BookCopy` | Eksemplar fisik buku dengan status ketersediaan dan lokasi rak. |
| `Category` | Pengelompokan buku dengan dukungan *soft delete*. |
| `LoanTransaction` | Rekaman lengkap siklus hidup peminjaman dari *request* hingga pengembalian dan pelunasan denda. |
| `LibraryConfig` | Konfigurasi aturan operasional perpustakaan yang dapat diubah oleh Admin. |
| `DashboardStats` | DTO agregasi statistik untuk kebutuhan tampilan *dashboard*. |

---

## 👥 Tim Pengembang

Proyek ini disusun sebagai tugas akhir mata kuliah **Pemrograman Berorientasi Objek (PBO)**, Departemen Informatika, Fakultas Sains dan Matematika, **Universitas Diponegoro** (2026).

| Nama | NIM |
|---|---|
| Shafa Aqilla Zahira | 24060124140146 |
| Shofwan Fikrul Huda | 24060124130106 |
| Syifa Aeni Mudrikah | 24060124120043 |
| Rafi Anandra Dharmawan | 24060124130071 |