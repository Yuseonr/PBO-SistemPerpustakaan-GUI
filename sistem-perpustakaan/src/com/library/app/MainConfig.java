/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.*;
import com.library.repository.*;
import com.library.service.ConfigService;
import com.library.util.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author rafianandra
 */
public class MainConfig {

    static final String RESET  = "\u001B[0m";
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE   = "\u001B[34m";
    static final String CYAN   = "\u001B[36m";

    static final int TEST_ADMIN_ID    = 7001;
    static final int TEST_INTRUDER_ID = 7002;

    static ILibraryConfigRepository configRepo;
    static ConfigService configService;

    static Admin admin;
    static Member intruder;

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║       CONFIG SERVICE — FULL TEST SUITE               ║");
        System.out.println("║       Strategi: Insert Fresh + Cleanup               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);

        try {
            setupDependencies();
            insertTestData();
            buildDomainObjects();

            runTest1_GetConfig_ReturnsNotNull();
            runTest2_GetConfig_ValuesCorrect();
            runTest3_UpdateConfig_Success();
            runTest4_UpdateConfig_NegativeFine();
            runTest5_UpdateConfig_ZeroBorrowDays();
            runTest6_UpdateConfig_ZeroBorrowLimit();
            runTest7_UpdateConfig_ZeroReservationDays();
            runTest8_UpdateConfig_ZeroPickupWindow();
            runTest9_UpdateConfig_EmptyLibraryName();
            runTest10_UpdateConfig_NullLibraryName();
            runTest11_UpdateConfig_NoPermission();
            runTest12_UpdateConfig_ValuesPersisted();

        } catch (Exception e) {
            System.out.println(RED + "\n❌ FATAL ERROR DI TEST RUNNER: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            cleanupTestData();
            printSummary();
        }
    }

    static void setupDependencies() {
        configRepo    = new LibraryConfigRepositoryMySQLImpl();
        configService = new ConfigService(configRepo);
        System.out.println(CYAN + "[SETUP] Dependencies siap." + RESET);
    }

    static void insertTestData() throws SQLException {
        System.out.println(CYAN + "[SETUP] Inserting test data ke database..." + RESET);
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "INSERT INTO users (id, name, email, password_hash, role, active, created_by) VALUES "
                + "(" + TEST_ADMIN_ID    + ", 'Config Admin',    'config_admin_test@lib.com',    '" + PasswordHasher.hashPassword("admin123")    + "', 'ADMIN',  1, 'TEST_SETUP'), "
                + "(" + TEST_INTRUDER_ID + ", 'Config Intruder', 'config_intruder_test@lib.com', '" + PasswordHasher.hashPassword("intruder123") + "', 'MEMBER', 1, 'TEST_SETUP')"
            );
        }
        System.out.println(CYAN + "[SETUP] Test data berhasil di-insert." + RESET);
    }

    static void buildDomainObjects() {
        admin = new Admin("Config Admin", "config_admin_test@lib.com", PasswordHasher.hashPassword("admin123"));
        admin.setId(TEST_ADMIN_ID);

        intruder = new Member(TEST_INTRUDER_ID, "Config Intruder");

        System.out.println(CYAN + "[SETUP] Domain objects siap." + RESET);
    }

    static void cleanupTestData() {
        System.out.println(CYAN + "\n[CLEANUP] Menghapus semua test data..." + RESET);
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "DELETE FROM users WHERE id IN (" + TEST_ADMIN_ID + "," + TEST_INTRUDER_ID + ")"
            );

            // Kembalikan config ke nilai default setelah test
            stmt.executeUpdate(
                "UPDATE library_config SET fine_per_day = 2000, max_borrow_days = 7, "
                + "max_borrow_limit = 3, max_reservation_days_ahead = 3, "
                + "pickup_window_days = 1, library_name = 'Perpustakaan' WHERE id = 1"
            );

            System.out.println(CYAN + "[CLEANUP] Selesai. Database bersih." + RESET);

        } catch (SQLException e) {
            System.out.println(RED + "[CLEANUP] ERROR saat cleanup: " + e.getMessage() + RESET);
        }
    }

    static LibraryConfig validConfig() {
        LibraryConfig c = new LibraryConfig();
        c.setFinePerDay(3000.0);
        c.setMaxBorrowDays(14);
        c.setMaxBorrowLimit(5);
        c.setMaxReservationDaysAhead(7);
        c.setPickupWindowDays(2);
        c.setLibraryName("Perpustakaan Test");
        c.setLibraryDescription("Deskripsi test");
        return c;
    }

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

    static void runTest1_GetConfig_ReturnsNotNull() {
        printHeader("[TEST 1] getLibraryConfig — Return Tidak Null");
        try {
            LibraryConfig config = configService.getLibraryConfig();
            if (config != null) {
                assertPass("getLibraryConfig return object tidak null");
            } else {
                assertFail("Config harus tidak null", "return null");
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    static void runTest2_GetConfig_ValuesCorrect() {
        printHeader("[TEST 2] getLibraryConfig — Nilai Sesuai Database");
        try {
            LibraryConfig config = configService.getLibraryConfig();

            if (config.getFinePerDay() >= 0) {
                assertPass("finePerDay >= 0: " + config.getFinePerDay());
            } else {
                assertFail("finePerDay harus >= 0", "actual: " + config.getFinePerDay());
            }

            if (config.getMaxBorrowDays() > 0) {
                assertPass("maxBorrowDays > 0: " + config.getMaxBorrowDays());
            } else {
                assertFail("maxBorrowDays harus > 0", "actual: " + config.getMaxBorrowDays());
            }

            if (config.getLibraryName() != null && !config.getLibraryName().isBlank()) {
                assertPass("libraryName tidak kosong: " + config.getLibraryName());
            } else {
                assertFail("libraryName harus tidak kosong", "actual: " + config.getLibraryName());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    static void runTest3_UpdateConfig_Success() {
        printHeader("[TEST 3] updateLibraryConfig — Sukses");
        try {
            configService.updateLibraryConfig(admin, validConfig());
            assertPass("updateLibraryConfig berhasil tanpa exception");
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    static void runTest4_UpdateConfig_NegativeFine() {
        printHeader("[TEST 4] updateLibraryConfig — Denda Negatif");
        try {
            LibraryConfig c = validConfig();
            c.setFinePerDay(-500.0);
            configService.updateLibraryConfig(admin, c);
            assertFail("Harus ditolak karena denda negatif", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Denda negatif ditolak: " + e.getMessage());
        }
    }

    static void runTest5_UpdateConfig_ZeroBorrowDays() {
        printHeader("[TEST 5] updateLibraryConfig — MaxBorrowDays = 0");
        try {
            LibraryConfig c = validConfig();
            c.setMaxBorrowDays(0);
            configService.updateLibraryConfig(admin, c);
            assertFail("Harus ditolak karena maxBorrowDays = 0", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("maxBorrowDays = 0 ditolak: " + e.getMessage());
        }
    }

    static void runTest6_UpdateConfig_ZeroBorrowLimit() {
        printHeader("[TEST 6] updateLibraryConfig — MaxBorrowLimit = 0");
        try {
            LibraryConfig c = validConfig();
            c.setMaxBorrowLimit(0);
            configService.updateLibraryConfig(admin, c);
            assertFail("Harus ditolak karena maxBorrowLimit = 0", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("maxBorrowLimit = 0 ditolak: " + e.getMessage());
        }
    }

    static void runTest7_UpdateConfig_ZeroReservationDays() {
        printHeader("[TEST 7] updateLibraryConfig — MaxReservationDaysAhead = 0");
        try {
            LibraryConfig c = validConfig();
            c.setMaxReservationDaysAhead(0);
            configService.updateLibraryConfig(admin, c);
            assertFail("Harus ditolak karena maxReservationDaysAhead = 0", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("maxReservationDaysAhead = 0 ditolak: " + e.getMessage());
        }
    }

    static void runTest8_UpdateConfig_ZeroPickupWindow() {
        printHeader("[TEST 8] updateLibraryConfig — PickupWindowDays = 0");
        try {
            LibraryConfig c = validConfig();
            c.setPickupWindowDays(0);
            configService.updateLibraryConfig(admin, c);
            assertFail("Harus ditolak karena pickupWindowDays = 0", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("pickupWindowDays = 0 ditolak: " + e.getMessage());
        }
    }

    static void runTest9_UpdateConfig_EmptyLibraryName() {
        printHeader("[TEST 9] updateLibraryConfig — LibraryName Kosong");
        try {
            LibraryConfig c = validConfig();
            c.setLibraryName("   ");
            configService.updateLibraryConfig(admin, c);
            assertFail("Harus ditolak karena nama kosong", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("LibraryName kosong ditolak: " + e.getMessage());
        }
    }

    static void runTest10_UpdateConfig_NullLibraryName() {
        printHeader("[TEST 10] updateLibraryConfig — LibraryName Null");
        try {
            LibraryConfig c = validConfig();
            c.setLibraryName(null);
            configService.updateLibraryConfig(admin, c);
            assertFail("Harus ditolak karena nama null", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("LibraryName null ditolak: " + e.getMessage());
        }
    }

    static void runTest11_UpdateConfig_NoPermission() {
        printHeader("[TEST 11] updateLibraryConfig — Akses Ditolak");
        try {
            configService.updateLibraryConfig(intruder, validConfig());
            assertFail("Harus ditolak karena intruder tidak punya MANAGE_CONFIG", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Akses ditolak dengan benar: " + e.getMessage());
        }
    }

    static void runTest12_UpdateConfig_ValuesPersisted() {
        printHeader("[TEST 12] updateLibraryConfig — Nilai Tersimpan ke Database");
        try {
            LibraryConfig toSave = validConfig();
            toSave.setFinePerDay(9999.0);
            toSave.setMaxBorrowDays(21);
            toSave.setLibraryName("Perpustakaan Diperbarui");

            configService.updateLibraryConfig(admin, toSave);

            LibraryConfig reloaded = configService.getLibraryConfig();

            if (reloaded.getFinePerDay() == 9999.0) {
                assertPass("finePerDay tersimpan benar: " + reloaded.getFinePerDay());
            } else {
                assertFail("finePerDay harus 9999.0", "actual: " + reloaded.getFinePerDay());
            }

            if (reloaded.getMaxBorrowDays() == 21) {
                assertPass("maxBorrowDays tersimpan benar: " + reloaded.getMaxBorrowDays());
            } else {
                assertFail("maxBorrowDays harus 21", "actual: " + reloaded.getMaxBorrowDays());
            }

            if ("Perpustakaan Diperbarui".equals(reloaded.getLibraryName())) {
                assertPass("libraryName tersimpan benar: " + reloaded.getLibraryName());
            } else {
                assertFail("libraryName harus 'Perpustakaan Diperbarui'", "actual: " + reloaded.getLibraryName());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    static void printSummary() {
        int total = passed + failed;
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.printf ("║  HASIL: %d/%d test lulus                             ║%n", passed, total);
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);
        if (failed == 0) {
            System.out.println(GREEN + "🎉 SEMUA TEST LULUS" + RESET);
        } else {
            System.out.println(RED + "⚠️  " + failed + " TEST GAGAL — cek output di atas" + RESET);
        }
    }
}