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
- Domain class hanya boleh berisi data (field), getter/setter, dan method yang hanya beroperasi pada state internal sendiri. Semua aksi bisnis diletakkan di service layer.

Keputusan paling penting adalah memisahkan `BookTitle` dan `BookCopy`. Ini membuat sistem lebih realistis karena satu judul bisa punya banyak eksemplar fisik, dan status pinjam seharusnya menempel ke eksemplar fisik, bukan ke judul buku.

## 3. Arsitektur Lapisan Aplikasi

Struktur yang disarankan:

- `ui`: form login, register, dashboard, dialog pinjam, dialog pengembalian.
- `service`: logika bisnis dan validasi aturan.
- `domain`: entity, enum, value object, interface kontrak.
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

Class inti yang menjadi induk semua akun adalah `User`. Class ini dibuat `abstract` agar tidak bisa diinstansiasi langsung — hanya subclass konkret (`Member`, `Librarian`, `Admin`) yang boleh diinstansiasi.

Field umum:

- `id`
- `name`
- `email`
- `passwordHash`
- `role`
- `active`
- `createdAt`
- `updatedAt`

Method umum:

- getter dan setter seperlunya
- `isActive()`
- `getRole()`
- `getDashboardTitle(): String` — method abstract yang di-override oleh setiap subclass untuk mengembalikan label dashboard masing-masing role (contoh penerapan polimorfisme, lihat Bagian 9/PBO09)
- `getPermissions(): List<String>` — method abstract yang di-override oleh setiap subclass untuk mengembalikan daftar hak akses sesuai role

Catatan penting: `verifyPassword()` tidak diletakkan di `User`. Verifikasi password adalah tanggung jawab `AuthService` yang menggunakan `PasswordHasher` utility. Domain class `User` hanya menyimpan `passwordHash` sebagai field privat.

Alasan `User` dibuat abstract:

- semua role punya data identitas yang sama;
- login dan authorization butuh struktur yang seragam;
- inheritance mengurangi duplikasi kode;
- tidak ada objek "user generik" yang valid dalam sistem ini — setiap user pasti salah satu dari tiga role.

### 4.2 Member extends User

`Member` adalah pengguna yang bisa registrasi sendiri.

Field tambahan:

- `membershipNumber`
- `address`
- `phone`
- `status` : `ACTIVE`, `SUSPENDED`

Method internal (hanya beroperasi pada state sendiri):

- `isActive()`
- `isMembershipValid()`
- `getDashboardTitle(): String` — override, mengembalikan misalnya `"Member Dashboard"`
- `getPermissions(): List<String>` — override, mengembalikan daftar permission member

Catatan: method seperti `requestBorrow()` atau `cancelPendingLoan()` **tidak** ada di class ini. Semua aksi bisnis tersebut adalah tanggung jawab `LoanService`.

### 4.3 Librarian extends User

`Librarian` adalah akun yang dibuat admin, bukan registrasi mandiri.

Field tambahan:

- `employeeNumber`
- `shiftInfo` 

Method internal (hanya beroperasi pada state sendiri):

- `getDashboardTitle(): String` — override, mengembalikan misalnya `"Librarian Dashboard"`
- `getPermissions(): List<String>` — override, mengembalikan daftar permission pustakawan

Catatan: method seperti `confirmPickup()` atau `confirmReturn()` **tidak** ada di class ini. Semua aksi bisnis tersebut adalah tanggung jawab `LoanService`.

Behavior utama:

- CRUD buku
- konfirmasi pickup pinjaman
- konfirmasi pengembalian
- konfirmasi pengembalian yang overdue dan perlu membayar denda
- melihat transaksi aktif dan overdue

### 4.4 Admin extends User

`Admin` adalah role tertinggi untuk pengelolaan sistem.

Method internal (hanya beroperasi pada state sendiri):

- `getDashboardTitle(): String` — override, mengembalikan misalnya `"Admin Dashboard"`
- `getPermissions(): List<String>` — override, mengembalikan daftar permission admin

Catatan: Admin tidak memiliki field tambahan karena seluruh data identitas sudah tercukupi dari `User`. Semua aksi bisnis admin (CRUD akun, reset password, konfigurasi) ditangani oleh `UserService` dan `ConfigService`.

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

`BookTitle` mewakili data katalog judul buku. Class ini mengimplementasikan interface `Searchable` (lihat Bagian 8a).

Field:

- `id`
- `title`
- `author`
- `publisher`
- `isbn`
- `description`
- `coverImagePath`
- `category` — object reference ke `Category`, bukan integer ID

Method:

- `getDisplayLabel()`
- `matchesKeyword(String keyword): boolean` — implementasi dari interface `Searchable`

Catatan relasi: `BookTitle` menyimpan referensi object `Category` secara langsung (`private Category category`), bukan hanya integer foreign key. Di layer repository, mapping dari ID ke object dilakukan saat data diambil dari database.

Catatan tambahan: method `getTotalCopies()` dan `getAvailableCopies()` **tidak** ada di class ini karena `BookTitle` tidak menyimpan koleksi `BookCopy`. Penghitungan jumlah copy adalah tanggung jawab `BookService` atau `BookCopyRepository`.

### 5.2 BookCopy

`BookCopy` mewakili satu eksemplar fisik.

Field:

- `id`
- `bookTitle` — object reference ke `BookTitle`
- `location`
- `status`

Status:

- `AVAILABLE`   : bisa dipinjam
- `RESERVED`    : sudah dipesan tapi belum diambil
- `LOANED`      : sedang dipinjam
- `UNAVAILABLE` : rusak atau hilang

Method internal (hanya beroperasi pada state sendiri):

- `canBeBorrowed(): boolean` — mengecek apakah `status == AVAILABLE`

Kenapa status menempel ke `BookCopy`:

- satu judul bisa punya banyak fisik;
- satu fisik hanya bisa berada di satu status pada satu waktu;
- aturan lock, pickup, dan return jadi lebih aman.

Catatan status `RESERVED`: dipasang saat transaksi masuk status `REQUESTED` (hard reservation).

## 6. Model Transaksi Peminjaman

### 6.1 LoanTransaction

Satu transaksi pinjam harus merepresentasikan satu buku fisik dan satu member. Class ini mengimplementasikan interface `Auditable` (lihat Bagian 8a).

Field penting:

- `id`
- `member` — object reference ke `Member`
- `bookCopy` — object reference ke `BookCopy`
- `borrowType` : `ONLINE` atau `OFFLINE`
- `requestDate` : tanggal permintaan pinjam
- `scheduledPickupDate` : tanggal yang dipilih member saat memesan; sekaligus tanggal aktual pickup karena member harus datang tepat di hari itu
- `dueDate` : tanggal jatuh tempo pengembalian; dihitung oleh `LoanService` dari `scheduledPickupDate + LibraryConfig.maxBorrowDays` sejak status `REQUESTED` dibuat
- `returnDate` : tanggal pengembalian buku
- `status` : lihat alur di bawah
- `fineAmount` : snapshot final denda; hanya diset oleh `FineCalculator` saat transaksi selesai, tidak pernah dihitung ulang
- `finePerDaySnapshot` : nilai tarif denda pada saat perhitungan
- `fineCalculatedAt` : waktu denda dihitung
- `approvedBy` — object reference ke `Librarian` yang memverifikasi pickup atau return
- `cancelledAt` : tanggal pembatalan jika dibatalkan
- `createdAt` : implementasi dari `Auditable`
- `updatedAt` : implementasi dari `Auditable`
- `createdBy` : implementasi dari `Auditable`

Alur status:

```
REQUESTED -> WAITING_PICKUP -> ACTIVE -> RETURNED
REQUESTED -> CANCELLED
             WAITING_PICKUP -> EXPIRED
             WAITING_PICKUP -> CANCELLED
                               ACTIVE -> OVERDUE -> RETURNED
```

Status yang disarankan:

- `REQUESTED`
- `WAITING_PICKUP`
- `ACTIVE`
- `RETURNED`
- `CANCELLED`
- `EXPIRED`
- `OVERDUE`

Definisi `REQUESTED`: member sudah memilih tanggal pickup tertentu dan `BookCopy` langsung dikunci menjadi `RESERVED` sejak status ini. Perpindahan `REQUESTED` → `WAITING_PICKUP` dilakukan oleh scheduler ketika `scheduledPickupDate` = hari ini.

Catatan status `OVERDUE`: status ini disimpan secara eksplisit di database. Perpindahan dari `ACTIVE` ke `OVERDUE` dilakukan oleh background scheduler (lihat Bagian 8.4) yang berjalan periodik untuk mengecek semua transaksi `ACTIVE` yang sudah melewati `dueDate`. Tanpa scheduler yang berjalan, status tidak akan terupdate otomatis — ini adalah konsekuensi yang harus diperhatikan saat implementasi.

Jenis pinjam:

- `ONLINE`
- `OFFLINE`

Catatan penting: method `calculateFine()` **tidak** ada di class ini. Kalkulasi denda adalah tanggung jawab `FineCalculator`. `LoanTransaction` hanya menyimpan hasil snapshot denda di field `fineAmount` setelah dihitung.

### 6.2 Kenapa transaksi dibuat atomic per buku

Desain atomic per buku lebih cocok karena:

- validasi stok lebih sederhana;
- konflik peminjaman lebih mudah dicegah;
- status fisik buku dan status transaksi selalu sinkron;
- history menjadi lebih jelas dan akurat;
- UI lebih mudah menampilkan detail satu buku yang dipinjam.

Jika satu member meminjam 3 buku, maka sistem membuat 3 transaksi terpisah. Ini lebih baik daripada satu transaksi berisi banyak item, karena penanganan overdue, pengembalian sebagian, dan pembatalan sebagian menjadi lebih sulit jika semua digabung.

## 7. Aturan Denda dan Kebijakan Perpustakaan

### 7.1 LibraryConfig

Config global disimpan di class `LibraryConfig`.

Field:

- `finePerDay`
- `maxBorrowDays`
- `onlinePickupLimitDays`
- `maxBorrowLimit`
- `maxReservationDaysAhead`
- `pickupWindowDays`
- `libraryName`
- `libraryDescription`
- `libraryLogoPath`

Catatan: `maxReservationDaysAhead` dan `pickupWindowDays` disimpan di `LibraryConfig` agar semua kebijakan perpustakaan bisa diubah admin tanpa edit source code.

### 7.2 FineCalculator

`FineCalculator` adalah class service/helper yang menghitung denda berdasarkan:

- tanggal due date;
- tanggal pengembalian;
- tarif denda;
- kebijakan grace period jika ada.

Contoh alur:

- jika return date lebih lambat dari due date, selisih hari dikali `finePerDay`;
- jika belum dikembalikan dan sudah lewat due date, status berubah menjadi `OVERDUE` (diupdate oleh scheduler);
- estimasi denda dapat ditampilkan sebelum return dikonfirmasi.

#### Snapshotting fines (important)

Jangan hanya menghitung denda on-the-fly saat menampilkan history; simpan snapshot hasil perhitungan denda pada saat transaksi selesai (return) agar histori keuangan tidak berubah bila aturan denda di masa depan diubah.

Prinsip implementasi:

- **Sumber kebenaran**: `LoanTransaction.fineAmount` adalah snapshot final denda untuk transaksi itu.
- **Versi aturan**: simpan nilai tarif denda pada saat perhitungan di field `LoanTransaction.finePerDaySnapshot` dan `LoanTransaction.fineCalculatedAt`.
- **Pembayaran**: gunakan tabel `FinePayment` untuk mencatat pembayaran terkait `loan_transaction.id` sehingga pembayaran dapat direkonsiliasi dengan snapshot denda.
- **Tidak mengubah snapshot**: setelah `fineAmount` diset pada saat return/dikonfirmasi, jangan overwrite kecuali ada koreksi manual yang tercatat sebagai audit.

Aturan jumlah pinjaman aktif juga dibaca dari `LibraryConfig.maxBorrowLimit`, bukan dari field khusus di `Member`.

## 8. Interface Kontrak Domain

Interface berikut diimplementasikan oleh class domain yang relevan untuk memenuhi kontrak perilaku tertentu.

### 8a.1 Auditable

Interface untuk class yang perlu mencatat jejak pembuatan dan perubahan data.

```java
public interface Auditable {
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    String getCreatedBy();
}
```

Diimplementasikan oleh: `User` (dan seluruh subclass), `LoanTransaction`.

### 8a.2 Searchable

Interface untuk class yang bisa dicari berdasarkan keyword teks bebas.

```java
public interface Searchable {
    boolean matchesKeyword(String keyword);
}
```

Diimplementasikan oleh: `BookTitle` (mencari berdasarkan judul, author, ISBN), `Member` (mencari berdasarkan nama, email, nomor anggota).

## 9. Service Layer yang Wajib Ada

### 9.1 AuthService

Tugas:

- login dengan memvalidasi email dan password menggunakan `PasswordHasher`;
- registrasi member;
- menentukan role tujuan dashboard;
- membaca `getDashboardTitle()` dari object `User` hasil login untuk menentukan tampilan awal.

Catatan: `verifyPassword()` diimplementasikan di sini menggunakan `PasswordHasher`, bukan di class `User`.

### 9.2 UserService

Tugas:

- CRUD member;
- CRUD librarian;
- reset password;
- aktivasi dan nonaktivasi akun.

### 9.3 BookService

Tugas:

- CRUD `BookTitle`;
- CRUD `BookCopy`;
- validasi stok;
- mengubah status copy;
- memproses pencarian katalog;
- menghitung `getTotalCopies()` dan `getAvailableCopies()` per judul buku berdasarkan query ke `BookCopyRepository`.

### 9.3a CategoryService

Tugas:

- CRUD `Category`;
- validasi nama kategori unik;
- menyediakan daftar kategori aktif untuk dropdown UI;
- menonaktifkan kategori tanpa menghapus data historis (soft delete);
- memastikan `BookTitle.category` mengacu ke kategori yang valid.

### 9.4 LoanService

Tugas:

- membuat `LoanTransaction` berstatus `REQUESTED` untuk pinjam online;
- membuat pinjaman offline;
- memverifikasi pickup dan mengubah transaksi menjadi `ACTIVE` tanpa menghitung ulang `dueDate`;
- mengubah pinjaman menjadi `ACTIVE`;
- memproses pengembalian;
- memanggil `FineCalculator` untuk menghitung dan menyimpan snapshot denda;
- membatalkan pinjaman yang belum diambil;
- meng-expire pinjaman yang tidak diambil;
- menyediakan method yang dipanggil oleh scheduler untuk:
    - mengupdate status `REQUESTED` → `WAITING_PICKUP` saat `scheduledPickupDate` = hari ini;
    - mengupdate status `WAITING_PICKUP` → `EXPIRED` setelah melewati `pickupWindowDays`;
    - mengupdate status `ACTIVE` → `OVERDUE` bagi transaksi yang sudah melewati `dueDate`.

Validasi tambahan untuk pinjam online:

- tanggal yang dipilih minimal besok (tidak boleh hari ini atau masa lalu);
- tanggal yang dipilih maksimal hari ini + `LibraryConfig.maxReservationDaysAhead`;
- harus ada `BookCopy` berstatus `AVAILABLE` saat pesanan dibuat.

Catatan scheduler: perpindahan status `ACTIVE` ke `OVERDUE` tidak terjadi otomatis tanpa scheduler yang berjalan. Implementasi scheduler dapat menggunakan `java.util.Timer`, `ScheduledExecutorService`, atau mekanisme cron eksternal yang memanggil method di `LoanService` secara periodik (misalnya setiap jam atau setiap hari saat aplikasi aktif).

### 9.5 ReportService

Tugas:

- total aset buku;
- total anggota terdaftar;
- total pemasukan denda bulan ini;
- buku paling sering dipinjam;
- daftar pinjaman aktif dan history.

### 9.6 ConfigService

Tugas:

- membaca dan menyimpan konfigurasi perpustakaan;
- mengubah denda, kuota, batas pinjam, profil perpustakaan.

## 10. Repository Layer dan Persistensi MySQL

Repository bertugas menghubungkan object dengan database. Semua repository mengimplementasikan interface generik `Repository<T, ID>`.

### 10.1 Interface Generik Repository

```java
public interface Repository<T, ID> {
    void save(T entity);
    void update(T entity);
    void delete(ID id);
    T findById(ID id);
    List<T> findAll();
}
```

Keuntungan generik:

- mengurangi duplikasi kode;
- type-safe;
- mudah diperluas ke entity baru.

### 10.2 Daftar Repository

Repository yang disarankan (semua mengimplementasikan `Repository<T, ID>`):

- `UserRepository`
- `CategoryRepository`
- `BookTitleRepository`
- `BookCopyRepository` — menyediakan method tambahan `countByCopyStatus(BookTitle, BookCopyStatus)` untuk keperluan `BookService`
- `LoanTransactionRepository`
- `FinePaymentRepository`
- `LibraryConfigRepository`

## 11. Model FinePayment

`FinePayment` mencatat pembayaran denda yang terkait dengan satu `LoanTransaction`.

Field:

- `id`
- `loanTransaction` — object reference ke `LoanTransaction`
- `amount`
- `paidAt`
- `processedBy` — object reference ke `Librarian` yang memproses pembayaran; penting untuk audit trail

## 12. Relasi Antar Class

Relasi yang disarankan:

- `User` mewariskan `Member`, `Librarian`, dan `Admin`.
- `User` mengimplementasikan `Auditable`.
- `BookTitle` mengimplementasikan `Searchable`.
- `Member` mengimplementasikan `Searchable`.
- `LoanTransaction` mengimplementasikan `Auditable`.
- `BookTitle` memiliki referensi object `Category`.
- `BookTitle` memiliki banyak `BookCopy` (dikelola via `BookCopyRepository`, bukan sebagai field koleksi di `BookTitle`).
- `LoanTransaction` menyimpan referensi object `Member`, `BookCopy`, dan `Librarian` (arah relasi: `LoanTransaction → Member`, `LoanTransaction → BookCopy`, `LoanTransaction → Librarian`).
- `FinePayment` menyimpan referensi object `LoanTransaction` dan `Librarian`.
- `LoanService` bergantung pada repository dan `FineCalculator`.
- `ReportService` bergantung pada repository untuk agregasi data.
- `LibraryConfig` dibaca oleh banyak service tetapi disimpan sebagai satu data global.

Jenis relasi yang paling sesuai:

- inheritance untuk role user;
- composition untuk judul dan copy;
- association searah dari transaksi ke entitas terkait (Member, BookCopy, Librarian);
- dependency untuk service ke repository;
- aggregation untuk katalog dan koleksi buku.

## 13. Alur Interaksi Sistem

### 13.1 Entry Flow

1. User membuka aplikasi.
2. UI menampilkan login dan registrasi member.
3. Jika login sukses, `AuthService` mengembalikan object `User`.
4. `AuthService` memanggil `getDashboardTitle()` pada object `User` untuk menentukan dashboard yang sesuai (polimorfisme runtime).
5. Sistem mengarahkan user ke dashboard sesuai role.

### 13.2 Registrasi Member

1. User mengisi nama, email, password, dan data tambahan.
2. `AuthService` memvalidasi format email dan keunikan email.
3. Password di-hash oleh `PasswordHasher` sebelum disimpan.
4. `UserRepository` menyimpan data member baru.
5. Sistem mengirim user ke halaman login atau langsung login jika kebijakan mengizinkan.

### 13.3 Login

1. User memasukkan email atau username dan password.
2. `AuthService` mengambil data user dari repository.
3. `AuthService` menggunakan `PasswordHasher` untuk memverifikasi password.
4. Jika valid, `AuthService` memanggil `getDashboardTitle()` untuk menentukan dashboard.
5. Dashboard dibuka sesuai role: member, librarian, atau admin.

### 13.4 Cari Buku

1. Member membuka katalog.
2. UI mengirim filter ke `BookService`.
3. `BookService` memanggil `BookTitleRepository` dan `BookCopyRepository`.
4. Untuk pencarian keyword, `BookService` dapat memanfaatkan `matchesKeyword()` dari interface `Searchable` pada `BookTitle`.
5. Hasil dikembalikan sebagai kartu buku.
6. Saat kartu dibuka, detail buku dan status stok ditampilkan. Jumlah copy tersedia dihitung oleh `BookService` via `BookCopyRepository`.

### 13.5 Pinjam Buku Online

1. Member klik pinjam dari detail buku.
2. UI membuka form dengan tanggal pinjam.
3. `LoanService` mengecek stok, quota (dari `LibraryConfig.maxBorrowLimit`), dan aturan tanggal.
4. Jika valid, sistem membuat `LoanTransaction` berstatus `REQUESTED`.
5. `BookCopy` langsung berubah menjadi `RESERVED`.
6. Scheduler mengubah `REQUESTED` → `WAITING_PICKUP` saat `scheduledPickupDate` = hari ini.
7. Member punya 1 hari sejak masuk `WAITING_PICKUP` untuk datang.
8. Librarian mengubah transaksi menjadi `ACTIVE` tanpa menghitung ulang `dueDate`.

### 13.6 Pinjam Buku Offline

1. Member datang langsung ke perpustakaan.
2. Librarian memilih buku dan member.
3. `LoanService` membuat `LoanTransaction` baru dengan status `ACTIVE`.
4. `LoanService` menghitung `dueDate = scheduledPickupDate + LibraryConfig.maxBorrowDays` (untuk offline, `scheduledPickupDate` = hari ini).
5. `BookCopy` langsung tidak tersedia.

### 13.7 Pembatalan Pinjaman Online

1. Member melihat transaksi status `WAITING_PICKUP`.
2. Member menekan batal.
3. `LoanService` memastikan status masih boleh dibatalkan.
4. Jika valid, transaksi diubah menjadi `CANCELLED`.
5. `BookCopy` kembali `AVAILABLE`.

### 13.8 Pickup oleh Pustakawan

1. Member datang ke perpustakaan.
2. Librarian membuka daftar pinjaman `WAITING_PICKUP`.
3. Setelah verifikasi, `LoanService.confirmPickup()` dipanggil oleh pustakawan melalui UI.
4. Status transaksi diubah menjadi `ACTIVE`.
5. `dueDate` sudah dihitung sejak `REQUESTED` dibuat dengan formula `scheduledPickupDate + LibraryConfig.maxBorrowDays`.
6. Field `approvedBy` diisi dengan referensi `Librarian` yang memverifikasi.

### 13.9 Pengembalian Buku

1. Member menyerahkan buku ke pustakawan.
2. Librarian membuka transaksi aktif.
3. `LoanService` memanggil `FineCalculator` untuk menghitung estimasi denda.
4. Jika ada denda, sistem menampilkan estimasi.
5. Setelah konfirmasi, `fineAmount` dan `finePerDaySnapshot` disimpan sebagai snapshot final di `LoanTransaction`.
6. Transaksi menjadi `RETURNED`.
7. `BookCopy` kembali `AVAILABLE`.
8. Jika ada denda, pustakawan memproses `FinePayment` yang mencatat `processedBy` dengan referensi `Librarian` bersangkutan.

### 13.10 Expired dan Overdue

1. Jika pinjaman online tidak diambil melewati batas waktu, status menjadi `EXPIRED`.
2. Scheduler yang berjalan periodik memanggil method di `LoanService` untuk:
    - mengubah `REQUESTED` → `WAITING_PICKUP` saat `scheduledPickupDate` = hari ini;
    - mengubah `WAITING_PICKUP` → `EXPIRED` setelah melewati `pickupWindowDays`;
    - mengecek semua transaksi `ACTIVE` yang sudah melewati `dueDate`.
3. Transaksi yang melewati `dueDate` diupdate menjadi `OVERDUE`.
4. Denda dihitung estimasinya dari konfigurasi dan snapshot tersimpan saat pengembalian akhirnya terjadi.

## 14. Mapping Materi PBO ke Project

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
- relasi antar class dibuat melalui object reference (association, dependency, dan composition), bukan sekadar integer foreign key.

### Materi 4 - Inheritance

Penerapan:

- `Member`, `Librarian`, dan `Admin` mewarisi `User`;
- reusability tinggi;
- code duplikasi berkurang.

### Materi 5 - Abstract Class dan Interface

Penerapan:

- `User` dibuat `abstract` sehingga tidak bisa diinstansiasi langsung;
- interface `Auditable` diimplementasikan oleh `User` dan `LoanTransaction` untuk kontrak audit trail;
- interface `Searchable` diimplementasikan oleh `BookTitle` dan `Member` untuk kontrak pencarian keyword;
- interface generik `Repository<T, ID>` diimplementasikan oleh semua repository.

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

- `User` mendeklarasikan method abstract `getDashboardTitle()` dan `getPermissions()`;
- `Member`, `Librarian`, dan `Admin` masing-masing meng-override kedua method tersebut;
- saat login, `AuthService` memegang referensi bertipe `User` tetapi memanggil `getDashboardTitle()` — JVM memilih implementasi yang sesuai subclass secara runtime (dynamic dispatch);
- ini adalah contoh polimorfisme sejati: satu referensi `User`, perilaku berbeda sesuai subclass konkret.

### PBO10 - Generik

Penerapan:

- interface `Repository<T, ID>` diimplementasikan oleh semua repository;
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

## 15. Perubahan Yang Disarankan Dari Proyek Sebelumnya

Perubahan yang paling penting:

- gunakan model atomic per book copy, bukan satu transaksi gabungan banyak buku;
- pisahkan katalog judul dan stok fisik;
- pindahkan semua aturan bisnis ke service layer; domain class hanya boleh menyimpan state dan method internal;
- gunakan enum untuk status dan role;
- simpan password dalam bentuk hash; verifikasi password dilakukan di `AuthService` menggunakan `PasswordHasher`, bukan di class `User`;
- gunakan object reference antar class domain, bukan integer foreign key;
- tambahkan histori status jika ingin audit yang lebih kuat;
- buat konfigurasi perpustakaan sebagai data yang bisa diubah admin tanpa edit source code;
- gunakan repository generic `Repository<T, ID>` untuk mengurangi duplikasi akses database;
- implementasikan interface `Auditable` dan `Searchable` untuk memenuhi materi PBO05;
- tambahkan `processedBy` (referensi `Librarian`) di `FinePayment` untuk audit trail pembayaran denda.

## SAMPAI SINI DESAIN UTAMA, BERIKUTNYA ADALAH ASUMSI DAN SARAN

## 16. Kelas Tambahan Yang Sangat Disarankan

Supaya sistem lebih matang, class berikut sebaiknya ada:

- `SessionUser` untuk menyimpan user login aktif;
- `Permission` atau `RolePermission` untuk hak akses;
- `AuditLog` untuk mencatat aksi penting;
- `NotificationService` jika nanti ingin notifikasi keterlambatan atau pickup reminder;
- `DateHelper` untuk perhitungan tanggal;
- `PasswordHasher` untuk hashing dan verifikasi password (digunakan oleh `AuthService`);
- `SearchCriteria` untuk filter katalog;
- `DashboardStats` untuk ringkasan statistik;
- Scheduler (misalnya berbasis `ScheduledExecutorService`) untuk mengupdate status `ACTIVE` → `OVERDUE` secara periodik.

## 17. Workflow Implementasi

### Tahap 1 - Finalisasi Requirement

- kunci daftar role;
- kunci alur login dan registrasi;
- tentukan status transaksi;
- tentukan aturan denda dan batas pinjam;
- putuskan apakah one loan per copy atau masih ingin batch transaksi.

### Tahap 2 - Desain Domain Class

- buat `User` (abstract), `Member`, `Librarian`, `Admin`;
- buat interface `Auditable` dan `Searchable`;
- buat `BookTitle` dan `BookCopy`;
- buat `LoanTransaction`, `LibraryConfig`, dan `FinePayment`;
- buat enum `Role`, `BookCopyStatus`, `LoanStatus`, `BorrowType`.

### Tahap 3 - Desain Relasi dan Enum

- tentukan relasi inheritance, association, dan composition;
- pastikan semua relasi antar class domain menggunakan object reference, bukan integer ID;
- pastikan setiap status punya definisi yang jelas.

### Tahap 4 - Desain Database MySQL

- buat tabel sesuai entity;
- pisahkan tabel judul buku dan copy buku;
- buat foreign key yang konsisten;
- indekskan field yang sering dicari seperti email, isbn, barcode, dan status.

### Tahap 5 - Repository Layer

- implementasikan interface generik `Repository<T, ID>`;
- implementasikan repository untuk semua entity utama;
- pastikan query pencarian, filtering, dan lookup cepat.

### Tahap 6 - Service Layer

- implementasikan `PasswordHasher` dan `AuthService` (login, registrasi, verifikasi password);
- implementasikan katalog dan pencarian menggunakan interface `Searchable`;
- implementasikan pinjam, pickup (dengan kalkulasi `dueDate`), return, cancel, expired;
- implementasikan `FineCalculator` dan snapshotting denda;
- implementasikan scheduler untuk update status `OVERDUE`.

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

- uji login dan polimorfisme routing dashboard;
- uji registrasi;
- uji pencarian buku (termasuk `matchesKeyword()`);
- uji pinjam online;
- uji pickup dan kalkulasi `dueDate`;
- uji return dan snapshotting denda;
- uji cancel dan expired;
- uji scheduler update OVERDUE.

### Tahap 10 - Penyempurnaan

- rapikan UX;
- tambahkan statistik;
- tambahkan audit log;
- optimalkan query database;
- rapikan dokumentasi class dan flow.

## 18. Contoh Skenario Interaksi

### Skenario A - Member registrasi lalu pinjam online

1. User baru mendaftar sebagai member.
2. User login; `AuthService` memanggil `getDashboardTitle()` → menampilkan Member Dashboard.
3. User mencari buku menggunakan keyword; `BookTitle.matchesKeyword()` digunakan dalam proses filter.
4. User membuka detail buku.
5. User memilih pinjam online.
6. `LoanService` membuat `LoanTransaction` berstatus `REQUESTED`.
7. User datang ke perpustakaan pada hari H.
8. Librarian memanggil `LoanService.confirmPickup()`; status menjadi `ACTIVE` dan `dueDate` dihitung.
9. Setelah selesai masa pinjam, user mengembalikan buku.
10. Librarian memanggil `LoanService.processReturn()`; `FineCalculator` menghitung denda, snapshot disimpan, status menjadi `RETURNED`.
11. Jika ada denda, pustakawan memproses `FinePayment` dengan field `processedBy` terisi.

### Skenario B - Pinjam offline

1. Member datang langsung ke perpustakaan.
2. Librarian memilih stok fisik dan member.
3. `LoanService` membuat `LoanTransaction` dengan status `ACTIVE` dan menghitung `dueDate`.
4. Saat kembali, transaksi ditutup menjadi `RETURNED`.

### Skenario C - Pinjaman online tidak diambil

1. Member memesan buku online.
2. Member tidak datang pada waktu yang ditentukan.
3. Scheduler periodik memanggil `LoanService` untuk mengecek `WAITING_PICKUP` yang sudah melewati batas.
4. Sistem mengubah status menjadi `EXPIRED`.
5. `BookCopy` kembali `AVAILABLE`.

## 19. Kenapa Desain Ini Cocok

Desain ini cocok karena:

- memenuhi konsep OOP dari dasar sampai lanjut, termasuk abstract class, interface, polimorfisme sejati, dan generik;
- sangat dekat dengan kebutuhan sistem perpustakaan nyata;
- memudahkan validasi dan pengembangan bertahap;
- mudah dihubungkan ke Java Ant dan MySQL;
- memudahkan pembuatan UI berbasis role;
- aman untuk pengelolaan stok dan transaksi;
- lebih mudah dirawat daripada desain transaksi campur banyak buku.

## 20. Output Yang Diharapkan Dari Dokumen Ini

Setelah PLAN ini dijalankan, hasil yang diharapkan adalah:

- class diagram yang jelas;
- alur user yang konsisten;
- struktur package yang rapi;
- database yang sesuai dengan object model;
- UI yang terpisah dari bisnis logic;
- sistem yang bisa dikembangkan tanpa bongkar total.

## 21. Ringkasan Keputusan Arsitektur

- Gunakan `User` sebagai abstract superclass.
- Gunakan `Member`, `Librarian`, dan `Admin` sebagai subclass konkret.
- Override `getDashboardTitle()` dan `getPermissions()` di setiap subclass untuk polimorfisme.
- Verifikasi password dilakukan di `AuthService` menggunakan `PasswordHasher`, bukan di `User`.
- Pisahkan `BookTitle` dan `BookCopy`.
- Gunakan object reference antar class domain, bukan integer foreign key.
- Gunakan `LoanTransaction` atomic per buku.
- `calculateFine()` ada di `FineCalculator`, bukan di `LoanTransaction`.
- `getTotalCopies()` dan `getAvailableCopies()` ada di `BookService`/`BookCopyRepository`, bukan di `BookTitle`.
- Simpan aturan perpustakaan di `LibraryConfig`.
- Letakkan logika bisnis di service layer; domain class hanya menyimpan state.
- Letakkan akses data di repository layer dengan interface generik `Repository<T, ID>`.
- Implementasikan interface `Auditable` dan `Searchable` untuk memenuhi materi PBO05.
- Status `OVERDUE` disimpan eksplisit, diupdate oleh scheduler periodik.
- Tidak ada class `BorrowRequest`. Setiap peminjaman langsung melahirkan satu `LoanTransaction` yang berdiri sendiri — 1 `BookCopy` = 1 `LoanTransaction`, tanpa header pengelompokan.
- `FinePayment` menyimpan referensi `Librarian` di field `processedBy`.
- Gunakan enum, exception, koleksi, generik, dan modularisasi secara aktif.

Dokumen ini menjadi baseline desain. Jika nanti dibutuhkan, bagian berikutnya bisa dipecah menjadi class diagram detail, tabel database, atau urutan implementasi per file Java.