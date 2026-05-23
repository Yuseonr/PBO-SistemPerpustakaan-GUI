# Architectural Design Choice
This document describes the architectural design choices made for the project. It outlines the key decisions regarding the structure, components, and technologies used in the system.

Structure 
```bash
src
└── com
    └── library
        ├── app         (Main.java, background schedulers)
        ├── config      (Database connections, JDBC utilities)
        ├── domain      (Entities including LibraryConfig, Enums, Interfaces)
        ├── exception   (Custom business exceptions)
        ├── repository  (Data access interfaces and MySQL implementations)
        ├── service     (Business logic, validations)
        ├── ui          (GUI forms, dialogs, terminal tester)
        └── util        (Helpers: PasswordHasher, DateHelper)
```

Even though your plan states that the domain package holds "interface kontrak" (like Auditable and Searchable), those are business/domain contracts. A book being searchable or a transaction being auditable are rules of your business.

The Repository<T, ID> interface, however, is an infrastructure contract. It dictates how data is persisted and retrieved (save, findById, findAll). Therefore, it belongs at the root of the data access layer.


# ENUMS
on writting enums first word must contains what its use about
so change from Role to UserRole


# Penambahan batasan dan logika reservasi 
Because of the reservasion issue we decide to make the reservasion maks of 3 days ahead
say today is 11 then u could borrow it either on 12, 13, or 14
and on those 12, 13, or 14 the book cannot be reserved by other people 
and in the date you chose you have 1 day, before it automaticlly get cancled because no one picked it up
the count down on how long the duedate is based on your chosing


# Slice 1

PBO05 (Abstract & Interface) & PBO09 (Polimorfisme): Sesuai dengan Bagian 4.1 dan 8a.1, User berhasil dibuat abstrak. Member, Librarian, dan Admin mewarisinya. Interface IAuditable dan IUserRepository diterapkan dengan sangat bersih.

Keamanan Terisolasi: Sesuai Bagian 4.1 dan 9.1, entity User hanya menyimpan passwordHash. Validasi dipisah secara mutlak ke AuthService menggunakan PasswordHasher.

Perubahan Arsitektur Mayor (Single Table Inheritance): Di PLAN.md Bagian 10.2, Anda awalnya merencanakan MemberRepository, LibrarianRepository, dan AdminRepository. Keputusan kita: Kita membuang ketiganya dan menggabungkannya ke dalam satu UserRepositoryMySQLImpl menggunakan tabel tunggal (users). Ini adalah keputusan desain yang brilian karena menghilangkan redundansi kode, mencegah multi-query saat login, dan mempermudah relasi Foreign Key di tahap transaksi nanti.

Koneksi Database Thread-Safe: Mengingat PLAN.md Bagian 13.10 mensyaratkan adanya Background Scheduler, kita membuang pola Singleton pada koneksi database dan memastikan setiap request mendapat Connection baru di DatabaseConfig agar terhindar dari tabrakan antar-thread.

Aturan Transaksi Atomik: Sesuai revisi penting di Bagian 21, kita memastikan sistem berjalan tanpa BorrowRequest.

2. Status Saat Ini (Apa yang Sudah Selesai & Ditest)
Phase 1 hingga Phase 3 telah SELESAI.
Anda memiliki fondasi otentikasi yang solid:

Database MySQL (library_db dan tabel users) siap.

AuthService mampu mendaftarkan member baru, mengamankan password (hashing), menolak duplikasi email, dan memastikan state active / member_status terjaga.

Proses Login mampu mendeteksi role secara dinamis dan me-return objek subclass yang tepat (Member, Librarian, Admin), membuktikan bahwa polimorfisme Java Anda berjalan sempurna.