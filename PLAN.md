# PLAN - Sistem Perpustakaan OOP

Dokumen ini menjadi blueprint utama untuk membangun sistem perpustakaan berbasis Java Ant, NetBeans, dan MySQL dengan pendekatan Object Oriented Programming. Fokusnya adalah desain class, relasi antar class, alur interaksi, mapping materi PBO, serta workflow implementasi agar sistem benar-benar bisa dipakai, bukan sekadar demo.

## 1. Tujuan Sistem

Sistem ini harus mendukung tiga peran utama:

- Member: registrasi, login, mencari buku, meminjam, melihat pinjaman aktif, melihat history, dan membatalkan pinjaman online sebelum diambil.
- Pustakawan: CRUD buku, konfirmasi pengambilan, konfirmasi pengembalian, melihat transaksi aktif, melihat history, dan menangani overdue.
- Admin: CRUD akun member dan pustakawan, reset password, mengatur konfigurasi perpustakaan, dan melihat statistik.

Target utama desain class adalah membuat aturan bisnis tetap rapi, mudah dirawat, dan cukup fleksibel untuk UI desktop maupun perubahan kebijakan perpustakaan di masa depan.

## 2. Prinsip Desain Yang Dipakai

Desain sistem ini mengikuti prinsip berikut:

- Satu class memiliki satu tanggung jawab utama.
- UI tidak menyimpan logika bisnis.
- Transaksi pinjam mengacu ke satu buku fisik, bukan ke satu judul secara abstrak.
- Data katalog dipisah dari stok fisik.
- Semua proses penting melewati service layer.
- Status bisnis dinyatakan dengan enum agar konsisten.

Keputusan paling penting adalah memisahkan `BookTitle` dan `BookCopy`. Ini membuat sistem lebih realistis karena satu judul bisa punya banyak eksemplar fisik, dan status pinjam seharusnya menempel ke eksemplar fisik, bukan ke judul buku.

## 3. Arsitektur Lapisan Aplikasi

Struktur yang disarankan:

- `ui`: form login, register, dashboard, dialog pinjam, dialog pengembalian.
- `service`: logika bisnis dan validasi aturan.
- `domain`: entity, enum, value object.
- `repository`: akses database dan query.
- `config`: koneksi database dan konfigurasi aplikasi.
- `exception`: exception bisnis dan validasi.
- `util`: helper umum seperti date helper, password hashing, dan formatter.

Alur kerja ideal:

1. UI menerima input user.
2. UI memanggil service.
3. Service memvalidasi aturan.
4. Service membaca atau menyimpan data lewat repository.
5. Repository berinteraksi dengan MySQL.
6. Hasil dikembalikan ke UI untuk ditampilkan.

Dengan pola ini, perubahan UI tidak merusak aturan bisnis, dan perubahan aturan bisnis tidak memaksa rewrite besar di layer tampilan.

## 4. Model Class Utama

### 4.1 User sebagai superclass

Class inti yang menjadi induk semua akun adalah `User`.

Field umum:

- `id`
- `name`
- `email`
- `passwordHash`
- `role`
- `active`
- `createdAt`

Method umum:

- getter dan setter seperlunya
- `isActive()`
- `getRole()`
- `verifyPassword()` jika diletakkan di domain, atau lebih aman di service/util

Alasan `User` dibuat abstract atau base class:

- semua role punya data identitas yang sama;
- login dan authorization butuh struktur yang seragam;
- inheritance mengurangi duplikasi kode.

### 4.2 Member extends User

`Member` adalah pengguna yang bisa registrasi sendiri.

Field tambahan:

- `membershipNumber`
- `address`
- `phone`
- `status`

Behavior utama:

- mencari buku
- mengajukan pinjaman online
- membatalkan pinjaman yang belum diambil
- melihat pinjaman aktif
- melihat history peminjaman

### 4.3 Librarian extends User

`Librarian` adalah akun yang dibuat admin, bukan registrasi mandiri.

Field tambahan:

- `employeeNumber`
- `shiftInfo` jika diperlukan

Behavior utama:

- CRUD buku
- konfirmasi pickup pinjaman
- konfirmasi pengembalian
- konfirmasi pengembalian yang overdue dan perlu membayar denda
- melihat transaksi aktif dan overdue

### 4.4 Admin extends User

`Admin` adalah role tertinggi untuk pengelolaan sistem.

Behavior utama:

- membuat akun pustakawan
- mengubah akun member dan pustakawan
- reset password
- mengelola konfigurasi perpustakaan
- melihat statistik

## 5. Katalog dan Stok Buku

### 5.0 Category (Tambahan)

`Category` adalah daftar kategori buku yang dikelola oleh pustakawan.

Field:

- `id`
- `name`
- `description`
- `isActive`
- `createdAt`
- `updatedAt`

Tujuan utama:

- pustakawan dapat CRUD kategori secara terpusat;
- input kategori pada buku bukan free text, tetapi pilihan dari daftar kategori;
- meminimalkan typo dan membuat pencarian berdasarkan kategori konsisten.

### 5.1 BookTitle

`BookTitle` mewakili data katalog judul buku.

Field:

- `id`
- `title`
- `author`
- `publisher`
- `isbn`
- `description`
- `coverImagePath`
- `categoryId`

Catatan:

- nilai `categoryId` mengacu ke daftar `Category` (dropdown di UI), bukan input bebas.

Method:

- `getDisplayLabel()`

### 5.2 BookCopy

`BookCopy` mewakili satu eksemplar fisik.

Field:

- `id`
- `bookTitleId`
- `location`
- `status`

Status :

- `AVAILABLE` : bisa dipinjam
- `RESERVED`  : sudah dipesan tapi belum diambil
- `LOANED`  : sedang dipinjam
- `UNAVAILABLE` : rusak atau hilang

Kenapa status menempel ke `BookCopy`:

- satu judul bisa punya banyak fisik;
- satu fisik hanya bisa berada di satu status pada satu waktu;
- aturan lock, pickup, dan return jadi lebih aman.

## 6. Model Transaksi Peminjaman

### 6.1 LoanTransaction

Satu transaksi pinjam harus merepresentasikan satu buku fisik dan satu member.

Field penting:

- `id`
- `memberId`
- `bookCopyId`
- `borrowType` : Online atau Offline
- `requestDate` : tanggal permintaan pinjam
- `pickupDate` : tanggal pengambilan buku oleh member
- `dueDate` : tanggal jatuh tempo pengembalian
- `returnDate` : tanggal pengembalian buku
- `status` : REQUESTED -> WAITING_PICKUP -> ACTIVE -> RETURNED
                          ACTIVE -> OVERDUE -> RETURNED
                          WAITING_PICKUP -> EXPIRED
                          WAITING_PICKUP -> CANCELLED
- `fineAmount` : jumlah denda jika ada
- `approvedByLibrarianId`: pustakawan yang memverifikasi pickup atau return
- `cancelledAt` : tanggal pembatalan jika dibatalkan

Status yang disarankan:

- `REQUESTED`
- `WAITING_PICKUP`
- `ACTIVE`
- `RETURNED`
- `CANCELLED`
- `EXPIRED`
- `OVERDUE`

Jenis pinjam:

- `ONLINE`
- `OFFLINE`

### 6.2 Kenapa transaksi dibuat atomic per buku

Desain atomic per buku lebih cocok karena:

- validasi stok lebih sederhana;
- konflik peminjaman lebih mudah dicegah;
- status fisik buku dan status transaksi selalu sinkron;
- history menjadi lebih jelas dan akurat;
- UI lebih mudah menampilkan detail satu buku yang dipinjam.

Jika satu member meminjam 3 buku, maka sistem membuat 3 transaksi terpisah. Ini lebih baik daripada satu transaksi berisi banyak item, karena penanganan overdue, pengembalian sebagian, dan pembatalan sebagian menjadi lebih sulit jika semua digabung.

### 6.3 Model Header-Detail yang Disarankan

Jika ingin pengelompokan yang lebih rapi di UI, laporan, dan histori, maka implementasi terbaik adalah memakai dua lapis:

- `BorrowRequest` sebagai header.
- `LoanTransaction` sebagai detail atomic per buku.

#### Fungsi `BorrowRequest`

`BorrowRequest` berisi identitas kejadian pinjam secara umum, bukan aturan buku fisik.

Field yang cocok:

- `id`
- `memberId`
- `borrowType`
- `requestDate`
- `createdAt`
- `overallStatus`
- `notes`

`overallStatus` bisa bernilai:

- `OPEN`
- `PARTIALLY_ACTIVE`
- `ACTIVE`
- `CLOSED`
- `CANCELLED`
- `EXPIRED`

#### Fungsi `LoanTransaction`

`LoanTransaction` tetap menjadi entitas utama per buku.

Field tambahan yang relevan:

- `borrowRequestId`
- `bookCopyId`
- `dueDate`
- `pickupDate`
- `returnDate`
- `status`
- `fineAmount`

#### Kenapa header-detail lebih cocok

- satu member bisa meminjam beberapa buku dalam satu kunjungan;
- dashboard member lebih enak menampilkan satu grup peminjaman dengan beberapa item;
- librarian lebih mudah memverifikasi satu kejadian peminjaman, tetapi tetap memproses item satu per satu;
- pembatalan bisa terjadi per item tanpa menghapus seluruh kejadian;
- histori tetap rapi karena ada konteks kelompok dan detail buku.

#### Kenapa header tidak boleh menggantikan atomic per buku

- status tersedia atau tidak tetap milik `BookCopy`;
- overdue dan denda harus dihitung per copy;
- pengembalian bisa parsial;
- pickup online juga sering terjadi per item;
- kalau satu item gagal, item lain tetap bisa diproses.

#### Kesimpulan implementasi

Yang dipakai bukan header saja dan bukan detail saja, tetapi:

- header untuk grouping dan tampilan,
- detail untuk aturan bisnis dan status fisik buku.

Dengan model ini, aplikasi tetap sederhana secara logika, tetapi jauh lebih nyaman untuk UI dan reporting.

## 7. Aturan Denda dan Kebijakan Perpustakaan

### 7.1 LibraryConfig

Config global disimpan di class `LibraryConfig`.

Field:

- `finePerDay`
- `maxBorrowDays`
- `onlinePickupLimitDays`
- `maxBorrowLimit`
- `libraryName`
- `libraryDescription`
- `libraryLogoPath`

### 7.2 FineCalculator

`FineCalculator` adalah class service/helper yang menghitung denda berdasarkan:

- tanggal due date;
- tanggal pengembalian;
- tarif denda;
- kebijakan grace period jika ada.

Contoh alur:

- jika return date lebih lambat dari due date, selisih hari dikali `finePerDay`;
- jika belum dikembalikan dan sudah lewat due date, status berubah menjadi `OVERDUE`;
- estimasi denda dapat ditampilkan sebelum return dikonfirmasi.

#### Snapshotting fines (important)

Jangan hanya menghitung denda on-the-fly saat menampilkan history; simpan snapshot hasil perhitungan denda pada saat transaksi selesai (return) agar histori keuangan tidak berubah bila aturan denda di masa depan diubah.

Prinsip implementasi:

- **Sumber kebenaran**: `LoanTransaction.fineAmount` adalah snapshot final denda untuk transaksi itu.
- **Versi aturan**: simpan versi kebijakan atau snapshot nilai aturan pada saat perhitungan. Misal fields: `LoanTransaction.finePerDaySnapshot`, `LoanTransaction.fineCalculatedAt`, `LoanTransaction.finePolicyVersion` atau `LoanTransaction.finePolicySnapshotJson`.
- **Pembayaran**: gunakan tabel `FinePayment` untuk mencatat pembayaran terkait `loan_transaction.id` sehingga pembayaran dapat direkonsiliasi dengan snapshot denda.
- **Tidak mengubah snapshot**: setelah `fineAmount` diset pada saat return/dikonfirmasi, jangan overwrite kecuali ada koreksi manual yang tercatat sebagai audit (mis. `fineAdjustment` record).

Aturan jumlah pinjaman aktif juga dibaca dari `LibraryConfig.maxBorrowLimit`, bukan dari field khusus di `Member`.


## 8. Service Layer yang Wajib Ada

### 8.1 AuthService

Tugas:

- login;
- registrasi member;
- validasi password;
- menentukan role tujuan dashboard.

### 8.2 UserService

Tugas:

- CRUD member;
- CRUD librarian;
- reset password;
- aktivasi dan nonaktivasi akun.

### 8.3 BookService

Tugas:

- CRUD `BookTitle`;
- CRUD `BookCopy`;
- validasi stok;
- mengubah status copy;
- mengelola tags;
- memproses pencarian katalog.

### 8.3a CategoryService (Tambahan)

Tugas:

- CRUD `Category`;
- validasi nama kategori unik;
- menyediakan daftar kategori aktif untuk dropdown UI;
- menonaktifkan kategori tanpa menghapus data historis (soft delete);
- memastikan `BookTitle.category` mengacu ke kategori yang valid.

### 8.4 LoanService

Tugas:

- membuat permintaan pinjam online;
- membuat pinjaman offline;
- memverifikasi pickup;
- mengubah pinjaman menjadi active;
- memproses pengembalian;
- menghitung overdue dan denda;
- membatalkan pinjaman yang belum diambil;
- meng-expire pinjaman yang tidak diambil.

### 8.5 ReportService

Tugas:

- total aset buku;
- total anggota terdaftar;
- total pemasukan denda bulan ini;
- buku paling sering dipinjam;
- daftar pinjaman aktif dan history.

### 8.6 ConfigService

Tugas:

- membaca dan menyimpan konfigurasi perpustakaan;
- mengubah denda, kuota, batas pinjam, profil perpustakaan.

## 9. Repository Layer dan Persistensi MySQL

Repository bertugas menghubungkan object dengan database.

Repository yang disarankan:

- `UserRepository`
- `MemberRepository`
- `LibrarianRepository`
- `AdminRepository`
- `CategoryRepository`
- `BookTitleRepository`
- `BookCopyRepository`
- `LoanRepository`
- `FinePaymentRepository`
- `LibraryConfigRepository`

Jika ingin lebih modular, gunakan interface generik:

- `Repository<T, ID>`

Method umum:

- `save()`
- `update()`
- `delete()`
- `findById()`
- `findAll()`

Keuntungan generik:

- mengurangi duplikasi kode;
- type-safe;
- mudah diperluas ke entity baru.

## 10. Relasi Antar Class

Relasi yang disarankan:

- `User` mewariskan `Member`, `Librarian`, dan `Admin`.
- `BookTitle` memiliki banyak `BookCopy`.
- `Member` memiliki banyak `LoanTransaction`.
- `BookCopy` terhubung ke satu `BookTitle`.
- `LoanTransaction` terhubung ke satu `Member`, satu `BookCopy`, dan satu `Librarian` yang memverifikasi.
- `LoanService` bergantung pada repository dan `FineCalculator`.
- `ReportService` bergantung pada repository untuk agregasi data.
- `LibraryConfig` dibaca oleh banyak service tetapi disimpan sebagai satu data global.

Jenis relasi yang paling sesuai:

- inheritance untuk role user;
- composition untuk judul dan copy;
- association untuk member dan loan;
- dependency untuk service ke repository;
- aggregation untuk katalog dan koleksi buku.

## 11. Alur Interaksi Sistem

### 11.1 Entry Flow

1. User membuka aplikasi.
2. UI menampilkan login dan registrasi member.
3. Jika login sukses, `AuthService` mengembalikan role.
4. Sistem mengarahkan user ke dashboard sesuai role.

### 11.2 Registrasi Member

1. User mengisi nama, email, password, dan data tambahan.
2. `AuthService` memvalidasi format email dan keunikan email.
3. Password di-hash sebelum disimpan.
4. `UserRepository` menyimpan data member baru.
5. Sistem mengirim user ke halaman login atau langsung login jika kebijakan mengizinkan.

### 11.3 Login

1. User memasukkan email atau username dan password.
2. `AuthService` memeriksa kecocokan credential.
3. Jika valid, sistem membaca role user.
4. Dashboard dibuka sesuai role: member, librarian, atau admin.

### 11.4 Cari Buku

1. Member membuka katalog.
2. UI mengirim filter ke `BookService`.
3. `BookService` memanggil `BookTitleRepository` dan `BookCopyRepository`.
4. Hasil dikembalikan sebagai kartu buku.
5. Saat kartu dibuka, detail buku dan status stok ditampilkan.

### 11.5 Pinjam Buku Online

1. Member klik pinjam dari detail buku.
2. UI membuka form dengan tanggal pinjam dan tanggal kembali.
3. `LoanService` mengecek stok, quota, dan aturan tanggal.
4. Jika valid, sistem membuat `LoanTransaction` berstatus `WAITING_PICKUP`.
5. `BookCopy` berubah menjadi `RESERVED` atau status sejenis.
6. Member datang pada hari H untuk konfirmasi pickup.
7. Librarian mengubah transaksi menjadi `ACTIVE`.

### 11.6 Pinjam Buku Offline

1. Member datang langsung ke perpustakaan.
2. Librarian memilih buku dan member.
3. `LoanService` membuat transaksi baru dengan status `ACTIVE`.
4. `BookCopy` langsung tidak tersedia.

### 11.7 Pembatalan Pinjaman Online

1. Member melihat transaksi status `WAITING_PICKUP`.
2. Member menekan batal.
3. `LoanService` memastikan status masih boleh dibatalkan.
4. Jika valid, transaksi diubah menjadi `CANCELLED`.
5. `BookCopy` kembali `AVAILABLE`.

### 11.8 Pickup oleh Pustakawan

1. Member datang ke perpustakaan.
2. Librarian membuka daftar pinjaman `WAITING_PICKUP`.
3. Setelah verifikasi, status transaksi diubah menjadi `ACTIVE`.
4. Due date dihitung dari aturan sistem.

### 11.9 Pengembalian Buku

1. Member menyerahkan buku ke pustakawan.
2. Librarian membuka transaksi aktif.
3. `LoanService` menghitung apakah ada overdue.
4. Jika ada denda, sistem menampilkan estimasi.
5. Setelah konfirmasi, transaksi menjadi `RETURNED`.
6. `BookCopy` kembali `AVAILABLE`.

### 11.10 Expired dan Overdue

1. Jika pinjaman online tidak diambil melewati batas waktu, status menjadi `EXPIRED`.
2. Jika due date lewat dan belum dikembalikan, status menjadi `OVERDUE`.
3. Denda dihitung otomatis dari konfigurasi.

## 12. Mapping Materi PBO ke Project

### Materi 1 - Object Orientation

Konsep yang dipakai:

- objek merepresentasikan entitas nyata seperti member, buku, dan transaksi;
- class adalah cetak biru objek;
- method merepresentasikan perilaku;
- OOP dipakai agar sistem lebih terstruktur daripada prosedural.

### Materi 2 - Object Class

Penerapan:

- class `User`, `BookTitle`, `BookCopy`, `LoanTransaction`, dan `LibraryConfig`;
- constructor untuk inisialisasi data;
- instansiasi object saat data diambil dari database atau dibuat dari form.

### Materi 3 - Enkapsulasi dan Relasi Antar Objek

Penerapan:

- field dibuat private;
- akses melalui getter/setter atau method khusus;
- relasi antar class dibuat melalui association, dependency, dan composition.

### Materi 4 - Inheritance

Penerapan:

- `Member`, `Librarian`, dan `Admin` mewarisi `User`;
- reusability tinggi;
- code duplikasi berkurang.

### Materi 5 - Abstract Class dan Interface

Penerapan:

- `User` bisa dibuat abstract jika tidak ingin diinstansiasi langsung;
- interface bisa dipakai untuk kontrak seperti `Searchable`, `Printable`, `Reportable`, atau `Auditable`.

### Materi 6 - Exception dan Assertion

Penerapan:

- exception untuk login gagal, buku tidak tersedia, quota habis, transaksi tidak valid, dan akses tidak sah;
- assertion untuk memeriksa invariant internal seperti due date, status transaksi, dan data kosong yang tidak boleh lolos.

### Materi 7 - Review

Penerapan:

- semua konsep sebelumnya dirangkum dalam modul latihan internal, validasi flow, dan studi kasus transaksi pinjam;
- gunakan review untuk memastikan desain class tetap konsisten.

### PBO09 - Polimorfisme dan Binding

Penerapan:

- satu referensi `User` bisa memanggil perilaku berbeda sesuai subclass;
- method override pada role dashboard atau permission checker;
- runtime binding dipakai saat role ditentukan setelah login.

### PBO10 - Generik

Penerapan:

- `Repository<T, ID>`;
- utility untuk daftar hasil pencarian atau pagination;
- service dapat memproses berbagai tipe data secara type-safe.

### PBO11 - Koleksi

Penerapan:

- `List` untuk katalog, pinjaman, history;
- `Set` untuk tag buku;
- `Map` untuk lookup role, konfigurasi, atau cache data;
- `ArrayList` cocok untuk hasil query yang sering diiterasi.

### PBO12 - Persistensi

Penerapan:

- data disimpan di MySQL;
- repository menghandle CRUD;
- model dapat dipetakan ke tabel database;
- data tetap ada setelah aplikasi ditutup.

### PBO13 - Modularisasi

Penerapan:

- pemisahan package;
- pemisahan class menurut tanggung jawab;
- UI, service, domain, dan repository tidak dicampur.

## 13. Perubahan Yang Disarankan Dari Proyek Sebelumnya

Perubahan yang menurut saya paling penting:

- gunakan model atomic per book copy, bukan satu transaksi gabungan banyak buku;
- pisahkan katalog judul dan stok fisik;
- pindahkan semua aturan bisnis ke service layer;
- gunakan enum untuk status dan role;
- simpan password dalam bentuk hash, jangan plain text;
- tambahkan histori status jika ingin audit yang lebih kuat;
- buat konfigurasi perpustakaan sebagai data yang bisa diubah admin tanpa edit source code;
- gunakan repository generic untuk mengurangi duplikasi akses database.

Perubahan ini membuat aplikasi lebih mendekati software perpustakaan nyata dan lebih siap dipakai jangka panjang.


## SAMPAI SINI DESAIN UTAMA, BERIKUTNYA ADALAH ASUMSI DAN SARAN

## 14. Kelas Tambahan Yang Sangat Disarankan

Supaya sistem lebih matang, class berikut sebaiknya ada:

- `SessionUser` untuk menyimpan user login aktif;
- `Permission` atau `RolePermission` untuk hak akses;
- `AuditLog` untuk mencatat aksi penting;
- `NotificationService` jika nanti ingin notifikasi keterlambatan atau pickup reminder;
- `DateHelper` untuk perhitungan tanggal;
- `PasswordHasher` untuk hashing dan verifikasi password;
- `SearchCriteria` untuk filter katalog;
- `DashboardStats` untuk ringkasan statistik.

## 15. Workflow Implementasi

### Tahap 1 - Finalisasi Requirement

- kunci daftar role;
- kunci alur login dan registrasi;
- tentukan status transaksi;
- tentukan aturan denda dan batas pinjam;
- putuskan apakah one loan per copy atau masih ingin batch transaksi.

### Tahap 2 - Desain Domain Class

- buat `User`, `Member`, `Librarian`, `Admin`;
- buat `BookTitle` dan `BookCopy`;
- buat `LoanTransaction`, `LibraryConfig`, dan `FinePayment` jika diperlukan.

### Tahap 3 - Desain Relasi dan Enum

- buat enum `Role`, `BookCopyStatus`, `LoanStatus`, `BorrowType`;
- tentukan relasi inheritance, association, dan composition;
- pastikan setiap status punya definisi yang jelas.

### Tahap 4 - Desain Database MySQL

- buat tabel sesuai entity;
- pisahkan tabel judul buku dan copy buku;
- buat foreign key yang konsisten;
- indekskan field yang sering dicari seperti email, isbn, barcode, dan status.

### Tahap 5 - Repository Layer

- implementasikan repository untuk semua entity utama;
- gunakan generik jika cocok;
- pastikan query pencarian, filtering, dan lookup cepat.

### Tahap 6 - Service Layer

- implementasikan login dan registrasi;
- implementasikan katalog dan pencarian;
- implementasikan pinjam, pickup, return, cancel, expired;
- implementasikan denda dan statistik.

### Tahap 7 - UI Desktop

- buat login form;
- buat register member form;
- buat dashboard member;
- buat dashboard librarian;
- buat dashboard admin;
- buat dialog pinjam dan pengembalian.

### Tahap 8 - Validasi dan Error Handling

- gunakan exception bisnis;
- tampilkan pesan yang jelas di UI;
- pastikan tidak ada aksi ilegal seperti pinjam buku yang tidak tersedia.

### Tahap 9 - Testing Fungsional

- uji login;
- uji registrasi;
- uji pencarian buku;
- uji pinjam online;
- uji pickup;
- uji return dan denda;
- uji cancel dan expired.

### Tahap 10 - Penyempurnaan

- rapikan UX;
- tambahkan statistik;
- tambahkan audit log;
- optimalkan query database;
- rapikan dokumentasi class dan flow.

## 16. Contoh Skenario Interaksi

### Skenario A - Member registrasi lalu pinjam online

1. User baru mendaftar sebagai member.
2. User login.
3. User mencari buku.
4. User membuka detail buku.
5. User memilih pinjam online.
6. Sistem membuat transaksi `WAITING_PICKUP`.
7. User datang ke perpustakaan pada hari H.
8. Librarian mengubah status menjadi `ACTIVE`.
9. Setelah selesai masa pinjam, user mengembalikan buku.
10. Librarian konfirmasi return dan sistem menghitung denda jika ada.

### Skenario B - Pinjam offline

1. Member datang langsung ke perpustakaan.
2. Librarian memilih stok fisik.
3. Transaksi langsung dibuat `ACTIVE`.
4. Saat kembali, transaksi ditutup menjadi `RETURNED`.

### Skenario C - Pinjaman online tidak diambil

1. Member memesan buku online.
2. Member tidak datang pada waktu yang ditentukan.
3. Sistem mengubah status menjadi `EXPIRED`.
4. `BookCopy` kembali `AVAILABLE`.

## 17. Kenapa Desain Ini Cocok

Desain ini cocok karena:

- memenuhi konsep OOP dari dasar sampai lanjut;
- sangat dekat dengan kebutuhan sistem perpustakaan nyata;
- memudahkan validasi dan pengembangan bertahap;
- mudah dihubungkan ke Java Ant dan MySQL;
- memudahkan pembuatan UI berbasis role;
- aman untuk pengelolaan stok dan transaksi;
- lebih mudah dirawat daripada desain transaksi campur banyak buku.

## 18. Output Yang Diharapkan Dari Dokumen Ini

Setelah PLAN ini dijalankan, hasil yang diharapkan adalah:

- class diagram yang jelas;
- alur user yang konsisten;
- struktur package yang rapi;
- database yang sesuai dengan object model;
- UI yang terpisah dari bisnis logic;
- sistem yang bisa dikembangkan tanpa bongkar total.

## 19. Ringkasan Keputusan Arsitektur

- Gunakan `User` sebagai superclass.
- Gunakan `Member`, `Librarian`, dan `Admin` sebagai subclass.
- Pisahkan `BookTitle` dan `BookCopy`.
- Gunakan `LoanTransaction` atomic per buku.
- Simpan aturan perpustakaan di `LibraryConfig`.
- Letakkan logika bisnis di service layer.
- Letakkan akses data di repository layer.
- Gunakan enum, exception, koleksi, generik, dan modularisasi secara aktif.

Dokumen ini menjadi baseline desain. Jika nanti dibutuhkan, bagian berikutnya bisa dipecah menjadi class diagram detail, tabel database, atau urutan implementasi per file Java.