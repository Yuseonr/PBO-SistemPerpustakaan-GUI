/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.*;
import com.library.domain.enums.*;
import com.library.repository.*;
import com.library.service.BookService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *
 * @author rafianandra
 */
public class MainBook {

    static final String RESET  = "\u001B[0m";
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE   = "\u001B[34m";
    static final String CYAN   = "\u001B[36m";

    static final int TEST_LIBRARIAN_ID = 8001;
    static final int TEST_MEMBER_ID    = 8002;
    static final int TEST_CATEGORY_ID  = 8001;

    static IBookTitleRepository titleRepo;
    static IBookCopyRepository  copyRepo;
    static BookService          bookService;

    static Librarian librarian;
    static Member    member;
    static Category  category;

    static int passed = 0;
    static int failed = 0;

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) {
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║       BOOK SERVICE — FULL TEST SUITE                 ║");
        System.out.println("║       Strategi: Insert Fresh + Cleanup               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);

        try {
            setupDependencies();
            insertTestData();
            buildDomainObjects();

            runTest1_AddBookTitle_HappyPath();
            runTest2_AddBookTitle_DuplicateIsbn();
            runTest3_AddBookTitle_EmptyFields();
            runTest4_AddBookTitle_NullCategory();
            runTest5_AddBookTitle_UnauthorizedActor();
            runTest6_AddBookCopies_HappyPath();
            runTest7_AddBookCopies_InvalidCount();
            runTest8_AddBookCopies_TitleNotFound();
            runTest9_UpdateBookTitle_HappyPath();
            runTest10_UpdateBookTitle_IsbnConflict();
            runTest11_UpdateBookTitle_SameIsbn();
            runTest12_UpdateBookCopy_HappyPath();
            runTest13_UpdateCopyStatus_HappyPath();
            runTest14_DeleteBookCopy_HappyPath();
            runTest15_DeleteBookTitle_HappyPath();
            runTest16_SearchCatalog_WithKeyword();
            runTest17_SearchCatalog_EmptyKeyword();
            runTest18_GetAvailableStock();
            runTest19_GetTotalCopies();

        } catch (Exception e) {
            System.out.println(RED + "\n❌ FATAL ERROR DI TEST RUNNER: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            cleanupTestData();
            printSummary();
        }
    }

    // ============================================================
    // SETUP
    // ============================================================
    static void setupDependencies() {
        titleRepo   = new BookTitleRepositoryMySQLImpl();
        copyRepo    = new BookCopyRepositoryMySQLImpl();
        bookService = new BookService(titleRepo, copyRepo);
        System.out.println(CYAN + "[SETUP] Dependencies siap." + RESET);
    }

    static void insertTestData() throws SQLException {
        System.out.println(CYAN + "[SETUP] Inserting test data ke database..." + RESET);
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "INSERT INTO users (id, name, email, password_hash, role, active, created_by) VALUES "
                + "(" + TEST_LIBRARIAN_ID + ", 'Test Librarian Book', 'librarian_book@lib.com', 'hash', 'LIBRARIAN', 1, 'TEST_SETUP'), "
                + "(" + TEST_MEMBER_ID    + ", 'Test Member Book',    'member_book@lib.com',    'hash', 'MEMBER',    1, 'TEST_SETUP')"
            );

            stmt.executeUpdate(
                "INSERT INTO categories (id, name, created_by) VALUES "
                + "(" + TEST_CATEGORY_ID + ", 'Test Category Book', 'TEST_SETUP')"
            );
        }
        System.out.println(CYAN + "[SETUP] Test data berhasil di-insert." + RESET);
    }

    static void buildDomainObjects() {
        librarian = new Librarian("Test Librarian Book", "librarian_book@lib.com", "hash", "EMP-BOOK-01", "SHIFT-1");
        librarian.setId(TEST_LIBRARIAN_ID);

        member = new Member(TEST_MEMBER_ID, "Test Member Book");

        category = new Category(TEST_CATEGORY_ID, "Test Category Book");

        System.out.println(CYAN + "[SETUP] Domain objects siap." + RESET);
    }

    // ============================================================
    // CLEANUP
    // ============================================================
    static void cleanupTestData() {
        System.out.println(CYAN + "\n[CLEANUP] Menghapus semua test data..." + RESET);
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM book_copies WHERE book_title_id IN (SELECT id FROM book_titles WHERE category_id = " + TEST_CATEGORY_ID + ")");
            stmt.executeUpdate("DELETE FROM book_titles WHERE category_id = " + TEST_CATEGORY_ID);
            stmt.executeUpdate("DELETE FROM categories WHERE id = " + TEST_CATEGORY_ID);
            stmt.executeUpdate("DELETE FROM users WHERE id IN (" + TEST_LIBRARIAN_ID + "," + TEST_MEMBER_ID + ")");

            System.out.println(CYAN + "[CLEANUP] Selesai. Database bersih." + RESET);
        } catch (SQLException e) {
            System.out.println(RED + "[CLEANUP] ERROR: " + e.getMessage() + RESET);
        }
    }

    // ============================================================
    // HELPER
    // ============================================================
    static void assertPass(String label) {
        System.out.println(GREEN + "  ✔ LULUS: " + label + RESET);
        passed++;
    }

    static void assertFail(String label, String reason) {
        System.out.println(RED + "  ✖ GAGAL: " + label + " → " + reason + RESET);
        failed++;
    }

    static void printHeader(String title) {
        System.out.println(YELLOW + "\n" + title + RESET);
    }

    // Buat BookTitle baru langsung ke DB dan return object-nya
    static BookTitle createTitle(String title, String isbn) {
        bookService.addBookTitle(librarian, title, "Penulis Test", "Penerbit Test", isbn, "Deskripsi test", category);
        return titleRepo.findByIsbn(isbn);
    }

    // ============================================================
    // TEST 1 — addBookTitle: happy path
    // ============================================================
    static void runTest1_AddBookTitle_HappyPath() {
        printHeader("[TEST 1] addBookTitle: Happy Path");
        try {
            bookService.addBookTitle(librarian, "Pemrograman Java", "Budi Santoso", "Gramedia", "ISBN-TEST-001", "Belajar Java OOP", category);

            BookTitle result = titleRepo.findByIsbn("ISBN-TEST-001");
            if (result != null && result.getTitle().equals("Pemrograman Java")) {
                assertPass("Buku berhasil disimpan ke database");
            } else {
                assertFail("Buku tersimpan", "findByIsbn mengembalikan null atau judul tidak cocok");
            }

            if (result != null && result.getCategory().getId() == TEST_CATEGORY_ID) {
                assertPass("Kategori tersimpan dengan benar");
            } else {
                assertFail("Kategori tersimpan", "category id tidak cocok");
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 2 — addBookTitle: ISBN duplikat harus ditolak
    // ============================================================
    static void runTest2_AddBookTitle_DuplicateIsbn() {
        printHeader("[TEST 2] addBookTitle: ISBN Duplikat");
        try {
            bookService.addBookTitle(librarian, "Buku Unik", "Author", "Publisher", "ISBN-DUPLIKAT-001", "Desc", category);

            // Tambah buku kedua dengan ISBN yang sama
            try {
                bookService.addBookTitle(librarian, "Buku Lain", "Author2", "Publisher2", "ISBN-DUPLIKAT-001", "Desc2", category);
                assertFail("ISBN duplikat harus ditolak", "sistem membiarkan ISBN ganda");
            } catch (IllegalArgumentException e) {
                assertPass("ISBN duplikat ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga di setup", e.getMessage());
        }
    }

    // ============================================================
    // TEST 3 — addBookTitle: judul atau ISBN kosong
    // ============================================================
    static void runTest3_AddBookTitle_EmptyFields() {
        printHeader("[TEST 3] addBookTitle: Judul atau ISBN Kosong");

        // Judul kosong
        try {
            bookService.addBookTitle(librarian, "", "Author", "Publisher", "ISBN-EMPTY-001", "Desc", category);
            assertFail("Judul kosong harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Judul kosong ditolak: " + e.getMessage());
        }

        // Judul null
        try {
            bookService.addBookTitle(librarian, null, "Author", "Publisher", "ISBN-NULL-001", "Desc", category);
            assertFail("Judul null harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Judul null ditolak: " + e.getMessage());
        }

        // ISBN kosong
        try {
            bookService.addBookTitle(librarian, "Judul Valid", "Author", "Publisher", "", "Desc", category);
            assertFail("ISBN kosong harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("ISBN kosong ditolak: " + e.getMessage());
        }

        // ISBN null
        try {
            bookService.addBookTitle(librarian, "Judul Valid", "Author", "Publisher", null, "Desc", category);
            assertFail("ISBN null harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("ISBN null ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 4 — addBookTitle: kategori null
    // ============================================================
    static void runTest4_AddBookTitle_NullCategory() {
        printHeader("[TEST 4] addBookTitle: Kategori Null");
        try {
            bookService.addBookTitle(librarian, "Judul Valid", "Author", "Publisher", "ISBN-NOCAT-001", "Desc", null);
            assertFail("Kategori null harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Kategori null ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 5 — addBookTitle: actor bukan librarian/admin
    // ============================================================
    static void runTest5_AddBookTitle_UnauthorizedActor() {
        printHeader("[TEST 5] addBookTitle: Actor Tidak Punya Izin (Member)");
        try {
            bookService.addBookTitle(member, "Buku Ilegal", "Author", "Publisher", "ISBN-ILEGAL-001", "Desc", category);
            assertFail("Member harus ditolak akses CRUD_BOOK", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Member ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 6 — addBookCopies: happy path, tambah beberapa copy
    // ============================================================
    static void runTest6_AddBookCopies_HappyPath() {
        printHeader("[TEST 6] addBookCopies: Happy Path (Tambah 3 Copy)");
        try {
            BookTitle title = createTitle("Buku Copy Test", "ISBN-COPY-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            bookService.addBookCopies(librarian, title.getId(), 3, "Rak A1");

            int total = bookService.getTotalCopies(title.getId());
            if (total == 3) {
                assertPass("3 copy berhasil ditambahkan");
            } else {
                assertFail("Total copy harus 3", "actual: " + total);
            }

            int available = bookService.getAvailableStock(title.getId());
            if (available == 3) {
                assertPass("Semua 3 copy berstatus AVAILABLE");
            } else {
                assertFail("Available stock harus 3", "actual: " + available);
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 7 — addBookCopies: jumlah copy tidak valid
    // ============================================================
    static void runTest7_AddBookCopies_InvalidCount() {
        printHeader("[TEST 7] addBookCopies: Jumlah Copy Tidak Valid");
        try {
            BookTitle title = createTitle("Buku Count Test", "ISBN-COUNT-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            // Nol
            try {
                bookService.addBookCopies(librarian, title.getId(), 0, "Rak B1");
                assertFail("0 copy harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("0 copy ditolak: " + e.getMessage());
            }

            // Negatif
            try {
                bookService.addBookCopies(librarian, title.getId(), -5, "Rak B1");
                assertFail("-5 copy harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("-5 copy ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga di setup", e.getMessage());
        }
    }

    // ============================================================
    // TEST 8 — addBookCopies: title ID tidak ada di database
    // ============================================================
    static void runTest8_AddBookCopies_TitleNotFound() {
        printHeader("[TEST 8] addBookCopies: BookTitle Tidak Ditemukan");
        try {
            bookService.addBookCopies(librarian, 999999, 1, "Rak X");
            assertFail("Title tidak ditemukan harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Title tidak ditemukan ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // TEST 9 — updateBookTitle: happy path
    // ============================================================
    static void runTest9_UpdateBookTitle_HappyPath() {
        printHeader("[TEST 9] updateBookTitle: Happy Path");
        try {
            BookTitle title = createTitle("Judul Lama", "ISBN-UPDATE-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            bookService.updateBookTitle(librarian, title.getId(), "Judul Baru", "Author Baru", "Publisher Baru", "ISBN-UPDATE-001", "Desc Baru", category);

            BookTitle updated = titleRepo.findById(title.getId());
            if ("Judul Baru".equals(updated.getTitle())) {
                assertPass("Judul berhasil diupdate");
            } else {
                assertFail("Judul terupdate", "actual: " + updated.getTitle());
            }

            if ("Author Baru".equals(updated.getAuthor())) {
                assertPass("Author berhasil diupdate");
            } else {
                assertFail("Author terupdate", "actual: " + updated.getAuthor());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 10 — updateBookTitle: ganti ISBN ke ISBN milik buku lain
    // ============================================================
    static void runTest10_UpdateBookTitle_IsbnConflict() {
        printHeader("[TEST 10] updateBookTitle: ISBN Konflik Dengan Buku Lain");
        try {
            BookTitle titleA = createTitle("Buku A Conflict", "ISBN-CONFLICT-A");
            BookTitle titleB = createTitle("Buku B Conflict", "ISBN-CONFLICT-B");

            if (titleA == null || titleB == null) {
                assertFail("Setup createTitle gagal", "salah satu null");
                return;
            }

            // Coba update titleB dengan ISBN milik titleA
            try {
                bookService.updateBookTitle(librarian, titleB.getId(), "Buku B Updated", "Author", "Publisher", "ISBN-CONFLICT-A", "Desc", category);
                assertFail("ISBN konflik harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("ISBN konflik ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga di setup", e.getMessage());
        }
    }

    // ============================================================
    // TEST 11 — updateBookTitle: update dengan ISBN sendiri (harus boleh)
    // ============================================================
    static void runTest11_UpdateBookTitle_SameIsbn() {
        printHeader("[TEST 11] updateBookTitle: Update Dengan ISBN Sendiri (Harus Diizinkan)");
        try {
            BookTitle title = createTitle("Buku Same ISBN", "ISBN-SAME-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            // Update judul tapi ISBN tetap sama — harus boleh
            bookService.updateBookTitle(librarian, title.getId(), "Buku Same ISBN Updated", "Author", "Publisher", "ISBN-SAME-001", "Desc", category);

            BookTitle updated = titleRepo.findById(title.getId());
            if ("Buku Same ISBN Updated".equals(updated.getTitle())) {
                assertPass("Update dengan ISBN sendiri berhasil");
            } else {
                assertFail("Update berhasil", "actual title: " + updated.getTitle());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 12 — updateBookCopy: ganti lokasi
    // ============================================================
    static void runTest12_UpdateBookCopy_HappyPath() {
        printHeader("[TEST 12] updateBookCopy: Ganti Lokasi");
        try {
            BookTitle title = createTitle("Buku Copy Update", "ISBN-COPYUPD-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            bookService.addBookCopies(librarian, title.getId(), 1, "Rak Lama");

            List<BookCopy> copies = copyRepo.findByBookTitleId(title.getId());
            if (copies.isEmpty()) {
                assertFail("Setup copy gagal", "findByBookTitleId kosong");
                return;
            }

            BookCopy copy = copies.get(0);
            bookService.updateBookCopy(librarian, copy.getId(), null, "Rak Baru");

            BookCopy updated = copyRepo.findById(copy.getId());
            if ("Rak Baru".equals(updated.getLocation())) {
                assertPass("Lokasi copy berhasil diupdate");
            } else {
                assertFail("Lokasi copy terupdate", "actual: " + updated.getLocation());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 13 — updateCopyStatus: ganti status copy
    // ============================================================
    static void runTest13_UpdateCopyStatus_HappyPath() {
        printHeader("[TEST 13] updateCopyStatus: Ganti Status Copy");
        try {
            BookTitle title = createTitle("Buku Status Test", "ISBN-STATUS-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            bookService.addBookCopies(librarian, title.getId(), 1, "Rak Status");

            List<BookCopy> copies = copyRepo.findByBookTitleId(title.getId());
            if (copies.isEmpty()) {
                assertFail("Setup copy gagal", "findByBookTitleId kosong");
                return;
            }

            BookCopy copy = copies.get(0);

            // AVAILABLE → UNAVAILABLE (rusak/hilang)
            bookService.updateCopyStatus(librarian, copy.getId(), BookCopyStatus.UNAVAILABLE);
            BookCopy updated = copyRepo.findById(copy.getId());
            if (updated.getStatus() == BookCopyStatus.UNAVAILABLE) {
                assertPass("Status berhasil diubah ke UNAVAILABLE");
            } else {
                assertFail("Status UNAVAILABLE", "actual: " + updated.getStatus());
            }

            // UNAVAILABLE → AVAILABLE (sudah diperbaiki)
            bookService.updateCopyStatus(librarian, copy.getId(), BookCopyStatus.AVAILABLE);
            BookCopy restored = copyRepo.findById(copy.getId());
            if (restored.getStatus() == BookCopyStatus.AVAILABLE) {
                assertPass("Status berhasil dikembalikan ke AVAILABLE");
            } else {
                assertFail("Status AVAILABLE", "actual: " + restored.getStatus());
            }

            // Status null harus ditolak
            try {
                bookService.updateCopyStatus(librarian, copy.getId(), null);
                assertFail("Status null harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("Status null ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 14 — deleteBookCopy: hapus copy
    // ============================================================
    static void runTest14_DeleteBookCopy_HappyPath() {
        printHeader("[TEST 14] deleteBookCopy: Hapus Satu Copy");
        try {
            BookTitle title = createTitle("Buku Delete Copy", "ISBN-DELCOPY-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            bookService.addBookCopies(librarian, title.getId(), 2, "Rak Del");

            List<BookCopy> before = copyRepo.findByBookTitleId(title.getId());
            if (before.size() != 2) {
                assertFail("Setup 2 copy", "actual: " + before.size());
                return;
            }

            bookService.deleteBookCopy(librarian, before.get(0).getId());

            List<BookCopy> after = copyRepo.findByBookTitleId(title.getId());
            if (after.size() == 1) {
                assertPass("1 copy berhasil dihapus, sisa 1");
            } else {
                assertFail("Sisa 1 copy", "actual: " + after.size());
            }

            // Copy yang sudah dihapus tidak bisa ditemukan
            BookCopy deleted = copyRepo.findById(before.get(0).getId());
            if (deleted == null) {
                assertPass("Copy yang dihapus tidak ditemukan di DB");
            } else {
                assertFail("Copy seharusnya null", "masih ada di DB");
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 15 — deleteBookTitle: hapus judul buku
    // ============================================================
    static void runTest15_DeleteBookTitle_HappyPath() {
        printHeader("[TEST 15] deleteBookTitle: Hapus Judul Buku");
        try {
            BookTitle title = createTitle("Buku Akan Dihapus", "ISBN-DELTITLE-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            bookService.deleteBookTitle(librarian, title.getId());

            BookTitle deleted = titleRepo.findById(title.getId());
            if (deleted == null) {
                assertPass("Judul buku berhasil dihapus dari DB");
            } else {
                assertFail("Judul buku seharusnya null", "masih ada di DB");
            }

            // Hapus title yang tidak ada
            try {
                bookService.deleteBookTitle(librarian, 999999);
                assertFail("Title tidak ditemukan harus ditolak", "sistem membiarkan");
            } catch (IllegalArgumentException e) {
                assertPass("Title tidak ditemukan ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 16 — searchCatalog: pencarian dengan keyword
    // ============================================================
    static void runTest16_SearchCatalog_WithKeyword() {
        printHeader("[TEST 16] searchCatalog: Pencarian Dengan Keyword");
        try {
            createTitle("Algoritma dan Pemrograman", "ISBN-SEARCH-001");
            createTitle("Struktur Data Java",        "ISBN-SEARCH-002");
            createTitle("Basis Data MySQL",          "ISBN-SEARCH-003");

            // Cari berdasarkan judul
            List<BookTitle> byJudul = bookService.searchCatalog("Algoritma");
            boolean foundAlgo = byJudul.stream().anyMatch(b -> b.getIsbn().equals("ISBN-SEARCH-001"));
            if (foundAlgo) {
                assertPass("Pencarian keyword judul 'Algoritma' ditemukan");
            } else {
                assertFail("Pencarian 'Algoritma'", "tidak ditemukan di hasil");
            }

            // Cari berdasarkan author — semua buku test pakai "Penulis Test"
            List<BookTitle> byAuthor = bookService.searchCatalog("Penulis Test");
            boolean foundAuthor = byAuthor.stream().anyMatch(b -> b.getIsbn().equals("ISBN-SEARCH-002"));
            if (foundAuthor) {
                assertPass("Pencarian keyword author 'Penulis Test' ditemukan");
            } else {
                assertFail("Pencarian author 'Penulis Test'", "tidak ditemukan di hasil");
            }

            // Cari berdasarkan ISBN
            List<BookTitle> byIsbn = bookService.searchCatalog("ISBN-SEARCH-003");
            boolean foundIsbn = byIsbn.stream().anyMatch(b -> b.getIsbn().equals("ISBN-SEARCH-003"));
            if (foundIsbn) {
                assertPass("Pencarian keyword ISBN ditemukan");
            } else {
                assertFail("Pencarian ISBN 'ISBN-SEARCH-003'", "tidak ditemukan di hasil");
            }

            // Keyword tidak cocok — hasil harus kosong atau tidak mengandung buku test ini
            List<BookTitle> notFound = bookService.searchCatalog("KEYWORD_TIDAK_ADA_XYZ123");
            boolean containsTestBook = notFound.stream().anyMatch(b -> b.getIsbn().startsWith("ISBN-SEARCH-"));
            if (!containsTestBook) {
                assertPass("Keyword tidak cocok tidak mengembalikan buku test");
            } else {
                assertFail("Keyword tidak cocok seharusnya kosong", "masih ada buku test di hasil");
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 17 — searchCatalog: keyword kosong atau null → return semua
    // ============================================================
    static void runTest17_SearchCatalog_EmptyKeyword() {
        printHeader("[TEST 17] searchCatalog: Keyword Kosong → Return Semua");
        try {
            List<BookTitle> allFromEmpty = bookService.searchCatalog("");
            List<BookTitle> allFromNull  = bookService.searchCatalog(null);

            if (!allFromEmpty.isEmpty()) {
                assertPass("Keyword kosong mengembalikan semua buku (" + allFromEmpty.size() + " buku)");
            } else {
                assertFail("Keyword kosong harus return non-empty", "hasil kosong");
            }

            if (!allFromNull.isEmpty()) {
                assertPass("Keyword null mengembalikan semua buku (" + allFromNull.size() + " buku)");
            } else {
                assertFail("Keyword null harus return non-empty", "hasil kosong");
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 18 — getAvailableStock: hitung stok tersedia
    // ============================================================
    static void runTest18_GetAvailableStock() {
        printHeader("[TEST 18] getAvailableStock: Hitung Stok Tersedia");
        try {
            BookTitle title = createTitle("Buku Stok Test", "ISBN-STOCK-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            bookService.addBookCopies(librarian, title.getId(), 4, "Rak Stok");

            int available = bookService.getAvailableStock(title.getId());
            if (available == 4) {
                assertPass("Stok tersedia = 4 (semua AVAILABLE)");
            } else {
                assertFail("Stok tersedia harus 4", "actual: " + available);
            }

            // Ubah 1 copy jadi UNAVAILABLE, stok harus berkurang
            List<BookCopy> copies = copyRepo.findByBookTitleId(title.getId());
            bookService.updateCopyStatus(librarian, copies.get(0).getId(), BookCopyStatus.UNAVAILABLE);

            int afterChange = bookService.getAvailableStock(title.getId());
            if (afterChange == 3) {
                assertPass("Stok tersedia = 3 setelah 1 copy UNAVAILABLE");
            } else {
                assertFail("Stok tersedia harus 3", "actual: " + afterChange);
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 19 — getTotalCopies: hitung total copy termasuk yang tidak tersedia
    // ============================================================
    static void runTest19_GetTotalCopies() {
        printHeader("[TEST 19] getTotalCopies: Hitung Total Copy");
        try {
            BookTitle title = createTitle("Buku Total Copy", "ISBN-TOTAL-001");
            if (title == null) {
                assertFail("Setup createTitle gagal", "findByIsbn null");
                return;
            }

            bookService.addBookCopies(librarian, title.getId(), 5, "Rak Total");

            int total = bookService.getTotalCopies(title.getId());
            if (total == 5) {
                assertPass("Total copy = 5");
            } else {
                assertFail("Total copy harus 5", "actual: " + total);
            }

            // Ubah beberapa jadi UNAVAILABLE — total tidak boleh berubah
            List<BookCopy> copies = copyRepo.findByBookTitleId(title.getId());
            bookService.updateCopyStatus(librarian, copies.get(0).getId(), BookCopyStatus.UNAVAILABLE);
            bookService.updateCopyStatus(librarian, copies.get(1).getId(), BookCopyStatus.UNAVAILABLE);

            int totalAfter = bookService.getTotalCopies(title.getId());
            if (totalAfter == 5) {
                assertPass("Total copy tetap 5 meskipun 2 UNAVAILABLE (total != available)");
            } else {
                assertFail("Total copy tetap 5", "actual: " + totalAfter);
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // PRINT SUMMARY
    // ============================================================
    static void printSummary() {
        int total = passed + failed;
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.printf ("║  HASIL: %d/%d test lulus                              ║%n", passed, total);
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);
        if (failed == 0) {
            System.out.println(GREEN + "🎉 SEMUA TEST LULUS" + RESET);
        } else {
            System.out.println(RED + "⚠️  " + failed + " TEST GAGAL — cek output di atas" + RESET);
        }
    }
}