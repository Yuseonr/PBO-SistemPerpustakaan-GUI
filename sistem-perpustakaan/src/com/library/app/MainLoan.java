/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.*;
import com.library.domain.enums.*;
import com.library.repository.*;
import com.library.service.LoanService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 *
 * @author rafianandra
 */
public class MainLoan {
    // ============================================================
    // ANSI COLOR
    // ============================================================
    static final String RESET  = "\u001B[0m";
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE   = "\u001B[34m";
    static final String CYAN   = "\u001B[36m";

    // ============================================================
    // ID YANG DIPAKAI UNTUK DATA TEST — mudah di-cleanup
    // ============================================================
    static final int TEST_LIBRARIAN_ID = 9001;
    static final int TEST_MEMBER_A_ID  = 9002;
    static final int TEST_MEMBER_B_ID  = 9003;
    static final int TEST_CATEGORY_ID  = 9001;
    static final int TEST_TITLE_1_ID   = 9001;
    static final int TEST_TITLE_2_ID   = 9002;
    static final int TEST_TITLE_3_ID   = 9003;
    static final int TEST_COPY_1_ID    = 9001;
    static final int TEST_COPY_2_ID    = 9002;
    static final int TEST_COPY_3_ID    = 9003;

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    static ILoanTransactionRepository loanRepo;
    static IBookCopyRepository copyRepo;
    static LoanService loanService;

    static Member memberA;
    static Member memberB;
    static Librarian librarian;
    static BookCopy copy1;
    static BookCopy copy2;
    static BookCopy copy3;

    // ============================================================
    // COUNTER
    // ============================================================
    static int passed = 0;
    static int failed = 0;

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) {
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║       LOAN SERVICE — FULL TEST SUITE                 ║");
        System.out.println("║       Strategi: Insert Fresh + Cleanup               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);

        try {
            setupDependencies();
            insertTestData();
            buildDomainObjects();

            runTest1_HappyPathOnline();
            runTest2_HappyPathOffline();
            runTest3_BookNotAvailable();
            runTest4_InvalidPickupDate();
            runTest5_MaxBorrowLimit();
            runTest6_UnauthorizedCancel();
            runTest7_InvalidStateTransition();
            runTest8_SchedulerExpiredPickup();
            runTest9_SchedulerOverdue();

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
        loanRepo    = new LoanTransactionRepositoryMySQLImpl();
        copyRepo    = new BookCopyRepositoryMySQLImpl();

        LibraryConfig config = new LibraryConfig();
        config.setMaxBorrowLimit(2);
        config.setMaxBorrowDays(7);
        config.setFinePerDay(2000.0);
        config.setMaxReservationDaysAhead(3);
        config.setPickupWindowDays(1);

        loanService = new LoanService(loanRepo, copyRepo, config);
        System.out.println(CYAN + "[SETUP] Dependencies siap." + RESET);
    }

    // ============================================================
    // INSERT TEST DATA KE DATABASE
    // ============================================================
    static void insertTestData() throws SQLException {
        System.out.println(CYAN + "[SETUP] Inserting test data ke database..." + RESET);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Users — librarian dan dua member
            stmt.executeUpdate(
                "INSERT INTO users (id, name, email, password_hash, role, active, created_by) VALUES "
                + "(" + TEST_LIBRARIAN_ID + ", 'Test Librarian', 'librarian_test@lib.com', 'hash', 'LIBRARIAN', 1, 'TEST_SETUP'), "
                + "(" + TEST_MEMBER_A_ID  + ", 'Test Member A',  'membera_test@lib.com',   'hash', 'MEMBER',    1, 'TEST_SETUP'), "
                + "(" + TEST_MEMBER_B_ID  + ", 'Test Member B',  'memberb_test@lib.com',   'hash', 'MEMBER',    1, 'TEST_SETUP')"
            );

            // Category
            stmt.executeUpdate(
                "INSERT INTO categories (id, name, created_by) VALUES "
                + "(" + TEST_CATEGORY_ID + ", 'Test Category', 'TEST_SETUP')"
            );

            // Book titles
            stmt.executeUpdate(
                "INSERT INTO book_titles (id, title, author, category_id) VALUES "
                + "(" + TEST_TITLE_1_ID + ", 'Buku Test 1', 'Penulis A', " + TEST_CATEGORY_ID + "), "
                + "(" + TEST_TITLE_2_ID + ", 'Buku Test 2', 'Penulis B', " + TEST_CATEGORY_ID + "), "
                + "(" + TEST_TITLE_3_ID + ", 'Buku Test 3', 'Penulis C', " + TEST_CATEGORY_ID + ")"
            );

            // Book copies
            stmt.executeUpdate(
                "INSERT INTO book_copies (id, book_title_id, location, status) VALUES "
                + "(" + TEST_COPY_1_ID + ", " + TEST_TITLE_1_ID + ", 'Rak TEST-A1', 'AVAILABLE'), "
                + "(" + TEST_COPY_2_ID + ", " + TEST_TITLE_2_ID + ", 'Rak TEST-A2', 'AVAILABLE'), "
                + "(" + TEST_COPY_3_ID + ", " + TEST_TITLE_3_ID + ", 'Rak TEST-B1', 'AVAILABLE')"
            );
        }

        System.out.println(CYAN + "[SETUP] Test data berhasil di-insert." + RESET);
    }

    // ============================================================
    // BUILD DOMAIN OBJECTS DARI DATA YANG SUDAH DI-INSERT
    // ============================================================
    static void buildDomainObjects() {
        librarian = new Librarian(
            "Test Librarian",
            "librarian_test@lib.com",
            "hash",
            "EMP-TEST-01",
            "SHIFT-TEST"
        );
        librarian.setId(TEST_LIBRARIAN_ID);

        memberA = new Member(TEST_MEMBER_A_ID, "Test Member A");
        memberB = new Member(TEST_MEMBER_B_ID, "Test Member B");

        Category cat = new Category(TEST_CATEGORY_ID, "Test Category");

        BookTitle title1 = new BookTitle(TEST_TITLE_1_ID, "Buku Test 1", "Penulis A", cat);
        BookTitle title2 = new BookTitle(TEST_TITLE_2_ID, "Buku Test 2", "Penulis B", cat);
        BookTitle title3 = new BookTitle(TEST_TITLE_3_ID, "Buku Test 3", "Penulis C", cat);

        copy1 = new BookCopy(TEST_COPY_1_ID, "Rak TEST-A1", title1);
        copy1.setStatus(BookCopyStatus.AVAILABLE);

        copy2 = new BookCopy(TEST_COPY_2_ID, "Rak TEST-A2", title2);
        copy2.setStatus(BookCopyStatus.AVAILABLE);

        copy3 = new BookCopy(TEST_COPY_3_ID, "Rak TEST-B1", title3);
        copy3.setStatus(BookCopyStatus.AVAILABLE);

        System.out.println(CYAN + "[SETUP] Domain objects siap." + RESET);
    }

    // ============================================================
    // CLEANUP
    // ============================================================
    static void cleanupTestData() {
        System.out.println(CYAN + "\n[CLEANUP] Menghapus semua test data..." + RESET);
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Hapus loans dulu — FK ke users dan book_copies
            stmt.executeUpdate(
                "DELETE FROM loans WHERE member_id IN ("
                + TEST_MEMBER_A_ID + "," + TEST_MEMBER_B_ID + ")"
            );

            // Hapus book copies
            stmt.executeUpdate(
                "DELETE FROM book_copies WHERE id IN ("
                + TEST_COPY_1_ID + "," + TEST_COPY_2_ID + "," + TEST_COPY_3_ID + ")"
            );

            // Hapus book titles
            stmt.executeUpdate(
                "DELETE FROM book_titles WHERE id IN ("
                + TEST_TITLE_1_ID + "," + TEST_TITLE_2_ID + "," + TEST_TITLE_3_ID + ")"
            );

            // Hapus category
            stmt.executeUpdate(
                "DELETE FROM categories WHERE id = " + TEST_CATEGORY_ID
            );

            // Hapus users
            stmt.executeUpdate(
                "DELETE FROM users WHERE id IN ("
                + TEST_LIBRARIAN_ID + "," + TEST_MEMBER_A_ID + "," + TEST_MEMBER_B_ID + ")"
            );

            System.out.println(CYAN + "[CLEANUP] Selesai. Database bersih." + RESET);

        } catch (SQLException e) {
            System.out.println(RED + "[CLEANUP] ERROR saat cleanup: " + e.getMessage() + RESET);
        }
    }

    // ============================================================
    // HELPER — refresh copy dari DB agar status selalu akurat
    // ============================================================
    static BookCopy freshCopy(int copyId) {
        return copyRepo.findById(copyId);
    }

    // ============================================================
    // HELPER — assert
    // ============================================================
    static void assertPass(String label) {
        System.out.println(GREEN + "  ✔ LULUS: " + label + RESET);
        passed++;
    }

    static void assertFail(String label, String reason) {
        System.out.println(RED + "  ✖ GAGAL: " + label + " → " + reason + RESET);
        failed++;
    }

    // ============================================================
    // TEST 1 — Happy Path Online: REQUESTED → WAITING_PICKUP → ACTIVE → RETURNED
    // ============================================================
    static void runTest1_HappyPathOnline() {
        printHeader("[TEST 1] Happy Path: Pinjam Online Lengkap");
        try {
            BookCopy copy = freshCopy(TEST_COPY_1_ID);

            // Step 1 — request online
            LocalDate besok = LocalDate.now().plusDays(1);
            LoanTransaction txn = loanService.requestOnlineLoan(memberA, copy, besok);

            if (copy.getStatus() == BookCopyStatus.RESERVED) {
                assertPass("Status buku RESERVED setelah request");
            } else {
                assertFail("Status buku RESERVED", "actual: " + copy.getStatus());
            }

            // Step 2 — simulasi hari H: update scheduledPickupDate ke hari ini lalu update DB
            txn.setScheduledPickupDate(LocalDate.now());
            loanRepo.update(txn); // ← wajib agar scheduler bisa query

            loanService.processScheduledPickups();

            // Reload dari DB untuk cek status terbaru
            LoanTransaction reloaded = loanRepo.findById(txn.getId());
            if (reloaded.getStatus() == LoanStatus.WAITING_PICKUP) {
                assertPass("Status WAITING_PICKUP setelah scheduler");
            } else {
                assertFail("Status WAITING_PICKUP", "actual: " + reloaded.getStatus());
            }

            // Step 3 — confirm pickup
            loanService.confirmPickup(txn.getId(), librarian);
            LoanTransaction afterPickup = loanRepo.findById(txn.getId());
            BookCopy afterPickupCopy   = freshCopy(TEST_COPY_1_ID);

            if (afterPickup.getStatus() == LoanStatus.ACTIVE) {
                assertPass("Status ACTIVE setelah confirmPickup");
            } else {
                assertFail("Status ACTIVE", "actual: " + afterPickup.getStatus());
            }

            if (afterPickupCopy.getStatus() == BookCopyStatus.LOANED) {
                assertPass("Status buku LOANED setelah confirmPickup");
            } else {
                assertFail("Status buku LOANED", "actual: " + afterPickupCopy.getStatus());
            }

            // Step 4 — return
            loanService.processReturn(txn.getId(), librarian);
            LoanTransaction afterReturn = loanRepo.findById(txn.getId());
            BookCopy afterReturnCopy    = freshCopy(TEST_COPY_1_ID);

            if (afterReturn.getStatus() == LoanStatus.RETURNED) {
                assertPass("Status RETURNED setelah processReturn");
            } else {
                assertFail("Status RETURNED", "actual: " + afterReturn.getStatus());
            }

            if (afterReturnCopy.getStatus() == BookCopyStatus.AVAILABLE) {
                assertPass("Status buku AVAILABLE setelah return");
            } else {
                assertFail("Status buku AVAILABLE", "actual: " + afterReturnCopy.getStatus());
            }

            if (afterReturn.getFineAmount() != null && afterReturn.getFineAmount() == 0.0) {
                assertPass("Denda 0 karena dikembalikan tepat waktu");
            } else {
                assertFail("Denda 0", "actual: " + afterReturn.getFineAmount());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 2 — Happy Path Offline: langsung ACTIVE → RETURNED + denda
    // ============================================================
    static void runTest2_HappyPathOffline() {
        printHeader("[TEST 2] Happy Path: Pinjam Offline + Return Terlambat (Ada Denda)");
        try {
            BookCopy copy = freshCopy(TEST_COPY_2_ID);

            // Step 1 — buat pinjaman offline
            LoanTransaction txn = loanService.createOfflineLoan(memberB, copy, librarian);
            LoanTransaction saved = loanRepo.findById(txn.getId());

            if (saved.getStatus() == LoanStatus.ACTIVE) {
                assertPass("Status ACTIVE langsung setelah createOfflineLoan");
            } else {
                assertFail("Status ACTIVE", "actual: " + saved.getStatus());
            }

            if (freshCopy(TEST_COPY_2_ID).getStatus() == BookCopyStatus.LOANED) {
                assertPass("Status buku LOANED setelah createOfflineLoan");
            } else {
                assertFail("Status buku LOANED", "actual: " + freshCopy(TEST_COPY_2_ID).getStatus());
            }

            // Step 2 — simulasi terlambat: mundurkan dueDate ke 3 hari lalu
            txn.setDueDate(LocalDate.now().minusDays(3));
            loanRepo.update(txn);

            // Step 3 — return
            loanService.processReturn(txn.getId(), librarian);
            LoanTransaction afterReturn = loanRepo.findById(txn.getId());

            if (afterReturn.getStatus() == LoanStatus.RETURNED) {
                assertPass("Status RETURNED setelah return terlambat");
            } else {
                assertFail("Status RETURNED", "actual: " + afterReturn.getStatus());
            }

            // Denda = 3 hari * 2000 = 6000
            if (afterReturn.getFineAmount() != null && afterReturn.getFineAmount() == 6000.0) {
                assertPass("Denda Rp6.000 benar (3 hari * Rp2.000)");
            } else {
                assertFail("Denda Rp6.000", "actual: " + afterReturn.getFineAmount());
            }

            if (afterReturn.getFinePerDaySnapshot() != null && afterReturn.getFinePerDaySnapshot() == 2000.0) {
                assertPass("Snapshot finePerDay tersimpan dengan benar");
            } else {
                assertFail("Snapshot finePerDay", "actual: " + afterReturn.getFinePerDaySnapshot());
            }

            // Step 4 — proses pembayaran denda
            loanService.processFinePayment(txn.getId(), librarian);
            LoanTransaction afterPayment = loanRepo.findById(txn.getId());

            if (afterPayment.getFinePaidAt() != null) {
                assertPass("finePaidAt tersimpan setelah pembayaran");
            } else {
                assertFail("finePaidAt tersimpan", "actual: null");
            }

            if (afterPayment.getFineProcessedBy() != null
                    && afterPayment.getFineProcessedBy().getId().equals(librarian.getId())) {
                assertPass("fineProcessedBy tersimpan dengan benar");
            } else {
                assertFail("fineProcessedBy", "actual: "
                        + (afterPayment.getFineProcessedBy() == null
                                ? "null"
                                : afterPayment.getFineProcessedBy().getId()));
            }

            if (freshCopy(TEST_COPY_2_ID).getStatus() == BookCopyStatus.AVAILABLE) {
                assertPass("Status buku AVAILABLE setelah return");
            } else {
                assertFail("Status buku AVAILABLE", "actual: " + freshCopy(TEST_COPY_2_ID).getStatus());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 3 — Buku tidak tersedia
    // ============================================================
    static void runTest3_BookNotAvailable() {
        printHeader("[TEST 3] Edge Case: Meminjam Buku Tidak Tersedia");
        try {
            // Set buku jadi LOANED langsung di DB
            BookCopy copy = freshCopy(TEST_COPY_3_ID);
            copy.setStatus(BookCopyStatus.LOANED);
            copyRepo.update(copy);

            BookCopy lockedCopy = freshCopy(TEST_COPY_3_ID);
            loanService.requestOnlineLoan(memberA, lockedCopy, LocalDate.now().plusDays(1));

            assertFail("Harus ditolak karena buku LOANED", "sistem membiarkan pinjam");
        } catch (IllegalStateException e) {
            assertPass("Request ditolak: " + e.getMessage());
        } finally {
            // Kembalikan ke AVAILABLE untuk test berikutnya
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE book_copies SET status = 'AVAILABLE' WHERE id = ?")) {
                stmt.setInt(1, TEST_COPY_3_ID);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                System.out.println(RED + "  [CLEANUP TEST 3] Gagal reset copy3: " + ex.getMessage() + RESET);
            }
        }
    }

    // ============================================================
    // TEST 4 — Tanggal pickup tidak valid
    // ============================================================
    static void runTest4_InvalidPickupDate() {
        printHeader("[TEST 4] Edge Case: Tanggal Pickup Tidak Valid");
        BookCopy copy = freshCopy(TEST_COPY_3_ID);

        // Hari ini — harus ditolak
        try {
            loanService.requestOnlineLoan(memberA, copy, LocalDate.now());
            assertFail("Pickup hari ini harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Pickup hari ini ditolak: " + e.getMessage());
        }

        // Kemarin — harus ditolak
        try {
            loanService.requestOnlineLoan(memberA, copy, LocalDate.now().minusDays(1));
            assertFail("Pickup kemarin harus ditolak", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Pickup kemarin ditolak: " + e.getMessage());
        }

        // H+5 melebihi maxReservationDaysAhead(3) — harus ditolak
        try {
            loanService.requestOnlineLoan(memberA, copy, LocalDate.now().plusDays(5));
            assertFail("Pickup H+5 harus ditolak (max H+3)", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Pickup H+5 ditolak: " + e.getMessage());
        }

        // H+2 — harus diterima
        try {
            LoanTransaction txn = loanService.requestOnlineLoan(memberA, copy, LocalDate.now().plusDays(2));
            assertPass("Pickup H+2 diterima (valid)");
            // Cleanup — cancel transaksi ini dan kembalikan copy
            loanService.cancelLoan(txn.getId(), memberA);
        } catch (Exception e) {
            assertFail("Pickup H+2 seharusnya valid", e.getMessage());
        }
    }

    // ============================================================
    // TEST 5 — Batas maksimal pinjaman
    // ============================================================
    static void runTest5_MaxBorrowLimit() {
        printHeader("[TEST 5] Edge Case: Batas Maksimal Pinjaman (Maks 2)");
        try {
            BookCopy c1 = freshCopy(TEST_COPY_1_ID);
            BookCopy c2 = freshCopy(TEST_COPY_2_ID);
            BookCopy c3 = freshCopy(TEST_COPY_3_ID);

            // Pinjam buku pertama
            LoanTransaction t1 = loanService.requestOnlineLoan(memberA, c1, LocalDate.now().plusDays(1));
            assertPass("Pinjaman ke-1 berhasil");

            // Pinjam buku kedua
            LoanTransaction t2 = loanService.requestOnlineLoan(memberA, c2, LocalDate.now().plusDays(1));
            assertPass("Pinjaman ke-2 berhasil");

            // Pinjam buku ketiga — harus ditolak
            try {
                loanService.requestOnlineLoan(memberA, c3, LocalDate.now().plusDays(1));
                assertFail("Pinjaman ke-3 harus ditolak (kuota habis)", "sistem membiarkan");
            } catch (IllegalStateException e) {
                assertPass("Pinjaman ke-3 ditolak: " + e.getMessage());
            }

            // Cleanup
            loanService.cancelLoan(t1.getId(), memberA);
            loanService.cancelLoan(t2.getId(), memberA);

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 6 — Pembatalan oleh member yang salah
    // ============================================================
    static void runTest6_UnauthorizedCancel() {
        printHeader("[TEST 6] Edge Case: Pembatalan Oleh Member Lain");
        try {
            BookCopy copy = freshCopy(TEST_COPY_3_ID);
            LoanTransaction txn = loanService.requestOnlineLoan(memberA, copy, LocalDate.now().plusDays(1));
            assertPass("MemberA berhasil request pinjaman");

            // MemberB coba cancel transaksi milik MemberA
            try {
                loanService.cancelLoan(txn.getId(), memberB);
                assertFail("MemberB harus ditolak cancel milik MemberA", "sistem membiarkan");
            } catch (IllegalStateException e) {
                assertPass("Cancel oleh MemberB ditolak: " + e.getMessage());
            }

            // MemberA cancel miliknya sendiri — harus berhasil
            loanService.cancelLoan(txn.getId(), memberA);
            LoanTransaction afterCancel = loanRepo.findById(txn.getId());

            if (afterCancel.getStatus() == LoanStatus.CANCELLED) {
                assertPass("MemberA berhasil cancel transaksinya sendiri");
            } else {
                assertFail("Status CANCELLED", "actual: " + afterCancel.getStatus());
            }

            if (freshCopy(TEST_COPY_3_ID).getStatus() == BookCopyStatus.AVAILABLE) {
                assertPass("Status buku kembali AVAILABLE setelah cancel");
            } else {
                assertFail("Status buku AVAILABLE", "actual: " + freshCopy(TEST_COPY_3_ID).getStatus());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 7 — State machine: return pada transaksi yang sudah RETURNED
    // ============================================================
    static void runTest7_InvalidStateTransition() {
        printHeader("[TEST 7] Edge Case: State Machine — Return Transaksi Yang Sudah RETURNED");
        try {
            BookCopy copy = freshCopy(TEST_COPY_1_ID);
            LoanTransaction txn = loanService.createOfflineLoan(memberA, copy, librarian);

            // Return pertama — valid
            loanService.processReturn(txn.getId(), librarian);
            assertPass("Return pertama berhasil");

            // Return kedua pada transaksi yang sama — harus ditolak
            try {
                loanService.processReturn(txn.getId(), librarian);
                assertFail("Return kedua harus ditolak", "sistem membiarkan");
            } catch (IllegalStateException e) {
                assertPass("Return kedua ditolak: " + e.getMessage());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 8 — Scheduler: WAITING_PICKUP → EXPIRED
    // ============================================================
    static void runTest8_SchedulerExpiredPickup() {
        printHeader("[TEST 8] Scheduler: WAITING_PICKUP → EXPIRED");
        try {
            BookCopy copy = freshCopy(TEST_COPY_2_ID);

            // Buat transaksi online, lalu paksa ke WAITING_PICKUP dengan tanggal kemarin
            LoanTransaction txn = loanService.requestOnlineLoan(memberB, copy, LocalDate.now().plusDays(1));

            // Simulasi: jadikan WAITING_PICKUP dengan scheduledPickupDate kemarin
            txn.setStatus(LoanStatus.WAITING_PICKUP);
            txn.setScheduledPickupDate(LocalDate.now().minusDays(2)); // sudah lewat pickupWindowDays(1)
            loanRepo.update(txn);

            loanService.processExpiredPickups();

            LoanTransaction afterExpiry = loanRepo.findById(txn.getId());

            if (afterExpiry.getStatus() == LoanStatus.EXPIRED) {
                assertPass("Status EXPIRED setelah processExpiredPickups");
            } else {
                assertFail("Status EXPIRED", "actual: " + afterExpiry.getStatus());
            }

            if (freshCopy(TEST_COPY_2_ID).getStatus() == BookCopyStatus.AVAILABLE) {
                assertPass("Status buku kembali AVAILABLE setelah expired");
            } else {
                assertFail("Status buku AVAILABLE", "actual: " + freshCopy(TEST_COPY_2_ID).getStatus());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // TEST 9 — Scheduler: ACTIVE → OVERDUE
    // ============================================================
    static void runTest9_SchedulerOverdue() {
        printHeader("[TEST 9] Scheduler: ACTIVE → OVERDUE");
        try {
            BookCopy copy = freshCopy(TEST_COPY_3_ID);

            // Buat offline loan lalu mundurkan dueDate ke masa lalu
            LoanTransaction txn = loanService.createOfflineLoan(memberA, copy, librarian);
            txn.setDueDate(LocalDate.now().minusDays(3));
            loanRepo.update(txn);

            loanService.processOverdueLoans();

            LoanTransaction afterOverdue = loanRepo.findById(txn.getId());

            if (afterOverdue.getStatus() == LoanStatus.OVERDUE) {
                assertPass("Status OVERDUE setelah processOverdueLoans");
            } else {
                assertFail("Status OVERDUE", "actual: " + afterOverdue.getStatus());
            }

            // Return dari OVERDUE — harus tetap bisa
            loanService.processReturn(txn.getId(), librarian);
            LoanTransaction afterReturn = loanRepo.findById(txn.getId());

            if (afterReturn.getStatus() == LoanStatus.RETURNED) {
                assertPass("OVERDUE → RETURNED berhasil");
            } else {
                assertFail("OVERDUE → RETURNED", "actual: " + afterReturn.getStatus());
            }

            // Denda = 3 hari * 2000 = 6000
            if (afterReturn.getFineAmount() != null && afterReturn.getFineAmount() == 6000.0) {
                assertPass("Denda dari OVERDUE dihitung benar (Rp6.000)");
            } else {
                assertFail("Denda Rp6.000", "actual: " + afterReturn.getFineAmount());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ============================================================
    // PRINT HELPER
    // ============================================================
    static void printHeader(String title) {
        System.out.println(YELLOW + "\n" + title + RESET);
    }

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