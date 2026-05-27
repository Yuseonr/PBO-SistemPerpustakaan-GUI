
-- SCRIPT DDL SISTEM PERPUSTAKAAN
-- Database Name: myperpus

CREATE DATABASE IF NOT EXISTS myperpus;
USE myperpus;

DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS book_copies;
DROP TABLE IF EXISTS book_titles;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS library_config;

-- TABEL 1: USERS
CREATE TABLE users (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    name               VARCHAR(100)                        NOT NULL,
    email              VARCHAR(100)                        NOT NULL UNIQUE,
    password_hash      VARCHAR(255)                        NOT NULL,
    role               ENUM('MEMBER','LIBRARIAN','ADMIN')  NOT NULL,
    active             TINYINT(1)                          NOT NULL DEFAULT 1,
    membership_number  VARCHAR(50)  DEFAULT NULL,
    address            TEXT         DEFAULT NULL,
    phone_number       VARCHAR(20)  DEFAULT NULL,
    member_status      VARCHAR(20)  DEFAULT NULL,
    employee_number    VARCHAR(50)  DEFAULT NULL,
    shift_info         VARCHAR(50)  DEFAULT NULL,
    created_by         VARCHAR(50)  NOT NULL,
    created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- TABEL 2: CATEGORIES
CREATE TABLE categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT         DEFAULT NULL,
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    created_by  VARCHAR(50)  DEFAULT 'System',
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- TABEL 3: BOOK_TITLES
CREATE TABLE book_titles (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    author      VARCHAR(150) NOT NULL,
    publisher   VARCHAR(150) DEFAULT NULL,
    isbn        VARCHAR(20)  DEFAULT NULL UNIQUE,
    description TEXT         DEFAULT NULL,
    category_id INT          NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);


-- TABEL 4: BOOK_COPIES
CREATE TABLE book_copies (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    book_title_id INT          NOT NULL,
    location      VARCHAR(100) NOT NULL,
    status        ENUM('AVAILABLE','RESERVED','LOANED','UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_title_id) REFERENCES book_titles(id)
);


-- TABEL 5: LOANS
CREATE TABLE loans (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    member_id             INT                                                                         NOT NULL,
    book_copy_id          INT                                                                         NOT NULL,
    approved_by           INT                                                                         DEFAULT NULL,
    borrow_type           ENUM('ONLINE','OFFLINE')                                                    NOT NULL,
    status                ENUM('REQUESTED','WAITING_PICKUP','ACTIVE','RETURNED','CANCELLED','EXPIRED','OVERDUE') NOT NULL,
    request_date          TIMESTAMP   DEFAULT NULL,
    scheduled_pickup_date DATE        DEFAULT NULL,
    due_date              DATE        DEFAULT NULL,
    return_date           DATE        DEFAULT NULL,
    cancelled_at          TIMESTAMP   NULL DEFAULT NULL,
    fine_amount           DOUBLE      DEFAULT 0,
    fine_per_day_snapshot DOUBLE      DEFAULT NULL,
    fine_calculated_at    TIMESTAMP   NULL DEFAULT NULL,
    fine_paid_at          TIMESTAMP   NULL DEFAULT NULL,
    fine_processed_by     INT         DEFAULT NULL,
    created_at            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by            VARCHAR(100) DEFAULT NULL,
    FOREIGN KEY (member_id)         REFERENCES users(id),
    FOREIGN KEY (book_copy_id)      REFERENCES book_copies(id),
    FOREIGN KEY (approved_by)       REFERENCES users(id),
    FOREIGN KEY (fine_processed_by) REFERENCES users(id)
);


-- TABEL 6: LIBRARY_CONFIG
CREATE TABLE library_config (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    fine_per_day                DOUBLE        NOT NULL DEFAULT 2000,
    max_borrow_days             INT           NOT NULL DEFAULT 7,
    max_borrow_limit            INT           NOT NULL DEFAULT 3,
    max_reservation_days_ahead  INT           NOT NULL DEFAULT 3,
    pickup_window_days          INT           NOT NULL DEFAULT 1,
    library_name                VARCHAR(100)  DEFAULT NULL,
    library_description         TEXT          DEFAULT NULL
);


-- DML: SEED DATA

-- INSERT INTO library_config (fine_per_day, max_borrow_days, max_borrow_limit, max_reservation_days_ahead, pickup_window_days, library_name, library_description)
-- VALUES (2000, 7, 3, 3, 1, 'Perpustakaan Utama', 'Sistem Perpustakaan Pusat');

-- INSERT INTO users (name, email, password_hash, role, active, created_by)
-- VALUES ('Atmin', 'min@lib.com', SHA2('min123', 256), 'ADMIN', 1, 'System');

-- INSERT INTO users (name, email, password_hash, role, active, employee_number, shift_info, created_by)
-- VALUES ('Pus', 'libra@lib.com', SHA2('lib123', 256), 'LIBRARIAN', 1, 'LIB-001', 'Pagi', 'System');

-- INSERT INTO users (name, email, password_hash, role, active, membership_number, address, phone_number, member_status, created_by)
-- VALUES ('Mber', 'mber@lib.com', SHA2('mber123', 256), 'MEMBER', 1, 'MEM-001', 'Jl. Perpustakaan No. 1', '081234567890', 'ACTIVE', 'System');

-- INSERT INTO categories (name, description) VALUES
-- ('Fiksi',               'Novel, cerpen, dan karya fiksi lainnya'),
-- ('Non-Fiksi',           'Buku berbasis fakta, biografi, dan esai'),
-- ('Sains & Teknologi',   'Fisika, kimia, biologi, komputer, dan teknologi'),
-- ('Sejarah',             'Sejarah dunia, nasional, dan peradaban'),
-- ('Filsafat',            'Pemikiran filsafat, logika, dan etika'),
-- ('Ekonomi & Bisnis',    'Manajemen, keuangan, dan kewirausahaan'),
-- ('Pendidikan',          'Buku pelajaran, pedagogi, dan referensi akademik'),
-- ('Sastra Indonesia',    'Karya sastra klasik dan modern Indonesia'),
-- ('Pemrograman',         'Bahasa pemrograman, algoritma, dan pengembangan perangkat lunak'),
-- ('Psikologi',           'Psikologi umum, perkembangan, dan kesehatan mental');

-- INSERT INTO book_titles (title, author, publisher, isbn, description, category_id) VALUES
-- ('Laskar Pelangi',              'Andrea Hirata',          'Bentang Pustaka',               '978-979-1227-00-2', 'Kisah inspiratif 10 anak Belitung yang berjuang meraih mimpi melalui pendidikan.', 1),
-- ('Bumi Manusia',                'Pramoedya Ananta Toer',  'Hasta Mitra',                   '978-979-2459-50-3', 'Novel sejarah tentang perjuangan Minke melawan kolonialisme Belanda.', 1),
-- ('Ronggeng Dukuh Paruk',        'Ahmad Tohari',           'Gramedia',                      '978-602-03-2515-7', 'Trilogi tentang kehidupan masyarakat desa Jawa dan tradisi ronggeng.', 1),
-- ('Perahu Kertas',               'Dee Lestari',            'Bentang Pustaka',               '978-602-8811-07-0', 'Kisah dua jiwa muda yang menemukan cinta dan passion mereka.', 1),
-- ('Supernova: Ksatria, Puteri, dan Bintang Jatuh', 'Dee Lestari', 'Truedee Books',         '978-979-2459-24-4', 'Novel fiksi sains tentang cinta, spiritualitas, dan sains modern.', 1),
-- ('Sapiens: Riwayat Singkat Umat Manusia', 'Yuval Noah Harari', 'Kepustakaan Populer Gramedia', '978-602-424-045-5', 'Menelusuri sejarah evolusi Homo sapiens dari zaman prasejarah hingga modern.', 2),
-- ('Atomic Habits',               'James Clear',            'Gramedia',                      '978-602-06-1757-0', 'Panduan membangun kebiasaan baik dan menghilangkan kebiasaan buruk.', 2),
-- ('Filosofi Teras',              'Henry Manampiring',      'Kompas',                        '978-602-412-415-1', 'Penerapan filsafat Stoa dalam kehidupan sehari-hari orang Indonesia.', 2),
-- ('A Brief History of Time',     'Stephen Hawking',        'Gramedia',                      '978-602-03-8847-3', 'Penjelasan tentang kosmologi, lubang hitam, dan asal usul alam semesta.', 3),
-- ('Cosmos',                      'Carl Sagan',             'Kepustakaan Populer Gramedia',  '978-979-9100-67-1', 'Eksplorasi alam semesta dan tempat manusia di dalamnya.', 3),
-- ('The Innovators',              'Walter Isaacson',        'Simon & Schuster',              '978-147-1065-98-5', 'Sejarah para inovator yang menciptakan revolusi digital.', 3),
-- ('Sejarah Indonesia Modern',    'M.C. Ricklefs',          'Serambi',                       '978-979-1600-02-8', 'Sejarah komprehensif Indonesia dari abad ke-13 hingga era reformasi.', 4),
-- ('Guns, Germs, and Steel',      'Jared Diamond',          'Gramedia',                      '978-602-03-5678-6', 'Mengapa beberapa peradaban mendominasi yang lain.', 4),
-- ('Nusantara: Sejarah Indonesia', 'Bernard Vlekke',        'Kepustakaan Populer Gramedia',  '978-979-9023-72-0', 'Catatan sejarah Nusantara dari perspektif seorang sejarawan Belanda.', 4),
-- ('Dunia Sophie',                'Jostein Gaarder',        'Mizan',                         '978-979-433-480-0', 'Novel pengantar sejarah filsafat Barat melalui kisah gadis bernama Sophie.', 5),
-- ('Seni Berpikir Jernih',        'Rolf Dobelli',           'Gramedia',                      '978-602-03-9012-4', 'Kumpulan bias kognitif yang sering menjebak cara berpikir manusia.', 5),
-- ('Meditations',                 'Marcus Aurelius',        'Penguin Classics',              '978-014-0449-33-5', 'Refleksi pribadi kaisar Romawi tentang kebijaksanaan Stoa.', 5),
-- ('Rich Dad Poor Dad',           'Robert T. Kiyosaki',     'Gramedia',                      '978-602-03-1234-5', 'Pelajaran keuangan dari dua perspektif ayah yang berbeda.', 6),
-- ('The Lean Startup',            'Eric Ries',              'Elex Media',                    '978-602-02-3456-9', 'Metodologi membangun bisnis startup yang efisien dan adaptif.', 6),
-- ('Thinking, Fast and Slow',     'Daniel Kahneman',        'Penguin Books',                 '978-014-1033-57-0', 'Dua sistem berpikir manusia dan dampaknya pada pengambilan keputusan.', 6),
-- ('Pendidikan Karakter',         'Thomas Lickona',         'Bumi Aksara',                   '978-602-217-290-6', 'Strategi mendidik karakter anak di sekolah dan rumah.', 7),
-- ('Merdeka Belajar',             'Ki Hadjar Dewantara',    'Majelis Luhur Persatuan',       '978-979-8139-01-3', 'Pemikiran pendidikan dari Bapak Pendidikan Nasional Indonesia.', 7),
-- ('Saman',                       'Ayu Utami',              'Kepustakaan Populer Gramedia',  '978-979-9100-12-1', 'Novel pemenang penghargaan tentang empat perempuan dan perjalanan spiritual.', 8),
-- ('Cantik Itu Luka',             'Eka Kurniawan',          'Gramedia',                      '978-602-03-0234-6', 'Novel realisme magis tentang keindahan, kekerasan, dan sejarah Indonesia.', 8),
-- ('Hujan Bulan Juni',            'Sapardi Djoko Damono',   'Gramedia',                      '978-602-03-7891-2', 'Kumpulan puisi liris yang menjadi ikon sastra Indonesia modern.', 8),
-- ('Clean Code',                  'Robert C. Martin',       'Prentice Hall',                 '978-013-2350-88-4', 'Panduan menulis kode yang bersih, mudah dibaca, dan maintainable.', 9),
-- ('Head First Java',             'Kathy Sierra',           'O\'Reilly Media',               '978-059-6009-20-5', 'Pengantar pemrograman Java dengan pendekatan visual dan interaktif.', 9),
-- ('Design Patterns',             'Gang of Four',           'Addison-Wesley',                '978-020-1633-61-0', '23 pola desain klasik dalam pemrograman berorientasi objek.', 9),
-- ('Man\'s Search for Meaning',   'Viktor E. Frankl',       'Beacon Press',                  '978-080-7014-27-9', 'Kisah bertahan hidup di kamp konsentrasi dan pencarian makna hidup.', 10),
-- ('Mindset: The New Psychology of Success', 'Carol S. Dweck', 'Ballantine Books',           '978-034-5472-32-8', 'Perbedaan fixed mindset dan growth mindset serta dampaknya pada kehidupan.', 10);

-- INSERT INTO book_copies (book_title_id, location, status) VALUES
-- (1,'Rak A1-01','AVAILABLE'),(1,'Rak A1-02','AVAILABLE'),(1,'Rak A1-03','AVAILABLE'),
-- (2,'Rak A1-04','AVAILABLE'),(2,'Rak A1-05','AVAILABLE'),
-- (3,'Rak A1-06','AVAILABLE'),(3,'Rak A1-07','AVAILABLE'),
-- (4,'Rak A1-08','AVAILABLE'),(4,'Rak A1-09','AVAILABLE'),
-- (5,'Rak A1-10','AVAILABLE'),(5,'Rak A1-11','AVAILABLE'),(5,'Rak A1-12','AVAILABLE'),
-- (6,'Rak B1-01','AVAILABLE'),(6,'Rak B1-02','AVAILABLE'),
-- (7,'Rak B1-03','AVAILABLE'),(7,'Rak B1-04','AVAILABLE'),(7,'Rak B1-05','AVAILABLE'),
-- (8,'Rak B1-06','AVAILABLE'),(8,'Rak B1-07','AVAILABLE'),
-- (9,'Rak C1-01','AVAILABLE'),(9,'Rak C1-02','AVAILABLE'),
-- (10,'Rak C1-03','AVAILABLE'),(10,'Rak C1-04','AVAILABLE'),
-- (11,'Rak C1-05','AVAILABLE'),(11,'Rak C1-06','AVAILABLE'),
-- (12,'Rak D1-01','AVAILABLE'),(12,'Rak D1-02','AVAILABLE'),
-- (13,'Rak D1-03','AVAILABLE'),(13,'Rak D1-04','AVAILABLE'),
-- (14,'Rak D1-05','AVAILABLE'),(14,'Rak D1-06','AVAILABLE'),
-- (15,'Rak E1-01','AVAILABLE'),(15,'Rak E1-02','AVAILABLE'),
-- (16,'Rak E1-03','AVAILABLE'),(16,'Rak E1-04','AVAILABLE'),
-- (17,'Rak E1-05','AVAILABLE'),(17,'Rak E1-06','AVAILABLE'),
-- (18,'Rak F1-01','AVAILABLE'),(18,'Rak F1-02','AVAILABLE'),(18,'Rak F1-03','AVAILABLE'),
-- (19,'Rak F1-04','AVAILABLE'),(19,'Rak F1-05','AVAILABLE'),
-- (20,'Rak F1-06','AVAILABLE'),(20,'Rak F1-07','AVAILABLE'),
-- (21,'Rak G1-01','AVAILABLE'),(21,'Rak G1-02','AVAILABLE'),
-- (22,'Rak G1-03','AVAILABLE'),(22,'Rak G1-04','AVAILABLE'),
-- (23,'Rak H1-01','AVAILABLE'),(23,'Rak H1-02','AVAILABLE'),
-- (24,'Rak H1-03','AVAILABLE'),(24,'Rak H1-04','AVAILABLE'),
-- (25,'Rak H1-05','AVAILABLE'),(25,'Rak H1-06','AVAILABLE'),
-- (26,'Rak I1-01','AVAILABLE'),(26,'Rak I1-02','AVAILABLE'),(26,'Rak I1-03','AVAILABLE'),
-- (27,'Rak I1-04','AVAILABLE'),(27,'Rak I1-05','AVAILABLE'),
-- (28,'Rak I1-06','AVAILABLE'),(28,'Rak I1-07','AVAILABLE'),
-- (29,'Rak J1-01','AVAILABLE'),(29,'Rak J1-02','AVAILABLE'),
-- (30,'Rak J1-03','AVAILABLE'),(30,'Rak J1-04','AVAILABLE'),(30,'Rak J1-05','AVAILABLE');