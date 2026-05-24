/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.*;
import com.library.domain.enums.*;
import com.library.repository.*;
import com.library.service.UserService;
import com.library.util.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author rafianandra
 */
public class MainUser {

    static final String RESET  = "\u001B[0m";
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE   = "\u001B[34m";
    static final String CYAN   = "\u001B[36m";

    static final int TEST_ADMIN_ID      = 8001;
    static final int TEST_LIBRARIAN_ID  = 8002;
    static final int TEST_MEMBER_ID     = 8003;
    static final int TEST_INTRUDER_ID   = 8004; // Member yang mencoba aksi admin

    static IUserRepository userRepo;
    static UserService userService;

    static Admin admin;
    static Member intruder;

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║       USER SERVICE — FULL TEST SUITE                 ║");
        System.out.println("║       Strategi: Insert Fresh + Cleanup               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);

        try {
            setupDependencies();
            insertTestData();
            buildDomainObjects();

            runTest1_GetUserById_Found();
            runTest2_GetUserById_NotFound();
            runTest3_GetAllUsers();
            runTest4_GetUsersByRole();
            runTest5_RegisterLibrarian_Success();
            runTest6_RegisterLibrarian_DuplicateEmail();
            runTest7_RegisterLibrarian_EmptyEmail();
            runTest8_RegisterLibrarian_ShortPassword();
            runTest9_RegisterLibrarian_NoPermission();
            runTest10_SuspendUser_Success();
            runTest11_SuspendUser_AlreadySuspended();
            runTest12_SuspendUser_NoPermission();
            runTest13_ActivateUser_Success();
            runTest14_ActivateUser_AlreadyActive();
            runTest15_ActivateUser_NoPermission();
            runTest16_ResetPassword_Success();
            runTest17_ResetPassword_TooShort();
            runTest18_ResetPassword_NoPermission();
            runTest19_UpdateUserInfo_Success();
            runTest20_UpdateUserInfo_EmailConflict();
            runTest21_UpdateUserInfo_UserNotFound();
            runTest22_UpdateUserInfo_NoPermission();
            runTest23_DeleteUser_Success();
            runTest24_DeleteUser_NotFound();
            runTest25_DeleteUser_NoPermission();

        } catch (Exception e) {
            System.out.println(RED + "\n❌ FATAL ERROR DI TEST RUNNER: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            cleanupTestData();
            printSummary();
        }
    }

    static void setupDependencies() {
        userRepo    = new UserRepositoryMySQLImpl();
        userService = new UserService(userRepo);
        System.out.println(CYAN + "[SETUP] Dependencies siap." + RESET);
    }

    static void insertTestData() throws SQLException {
        System.out.println(CYAN + "[SETUP] Inserting test data ke database..." + RESET);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "INSERT INTO users (id, name, email, password_hash, role, active, created_by) VALUES "
                + "(" + TEST_ADMIN_ID     + ", 'Test Admin',    'admin_test@lib.com',    '" + PasswordHasher.hashPassword("admin123")    + "', 'ADMIN',     1, 'TEST_SETUP'), "
                + "(" + TEST_LIBRARIAN_ID + ", 'Test Librarian','librarian_test@lib.com','" + PasswordHasher.hashPassword("libr123")     + "', 'LIBRARIAN', 1, 'TEST_SETUP'), "
                + "(" + TEST_MEMBER_ID    + ", 'Test Member',   'member_test@lib.com',   '" + PasswordHasher.hashPassword("member123")   + "', 'MEMBER',    1, 'TEST_SETUP'), "
                + "(" + TEST_INTRUDER_ID  + ", 'Test Intruder', 'intruder_test@lib.com', '" + PasswordHasher.hashPassword("intruder123") + "', 'MEMBER',    1, 'TEST_SETUP')"
            );
        }

        System.out.println(CYAN + "[SETUP] Test data berhasil di-insert." + RESET);
    }

    static void buildDomainObjects() {
        admin = new Admin("Test Admin", "admin_test@lib.com", PasswordHasher.hashPassword("admin123"));
        admin.setId(TEST_ADMIN_ID);

        intruder = new Member(TEST_INTRUDER_ID, "Test Intruder");

        System.out.println(CYAN + "[SETUP] Domain objects siap." + RESET);
    }

    static void cleanupTestData() {
        System.out.println(CYAN + "\n[CLEANUP] Menghapus semua test data..." + RESET);
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "DELETE FROM users WHERE id IN ("
                + TEST_ADMIN_ID + "," + TEST_LIBRARIAN_ID + ","
                + TEST_MEMBER_ID + "," + TEST_INTRUDER_ID
                + ") OR email LIKE '%_newlib_test@lib.com'"
            );

            System.out.println(CYAN + "[CLEANUP] Selesai. Database bersih." + RESET);

        } catch (SQLException e) {
            System.out.println(RED + "[CLEANUP] ERROR saat cleanup: " + e.getMessage() + RESET);
        }
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

    // ==========================================
    // TEST 1 — getUserById: user ditemukan
    // ==========================================
    static void runTest1_GetUserById_Found() {
        printHeader("[TEST 1] getUserById — User Ditemukan");
        try {
            User user = userService.getUserById(TEST_ADMIN_ID);
            if (user != null && user.getId().equals(TEST_ADMIN_ID)) {
                assertPass("User ditemukan dengan ID " + TEST_ADMIN_ID);
            } else {
                assertFail("User harus ditemukan", "result null atau ID salah");
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 2 — getUserById: user tidak ada
    // ==========================================
    static void runTest2_GetUserById_NotFound() {
        printHeader("[TEST 2] getUserById — User Tidak Ditemukan");
        try {
            userService.getUserById(99999);
            assertFail("Harus던지 exception karena ID tidak ada", "tidak ada exception");
        } catch (IllegalArgumentException e) {
            assertPass("Exception benar saat ID tidak ditemukan: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 3 — getAllUsers
    // ==========================================
    static void runTest3_GetAllUsers() {
        printHeader("[TEST 3] getAllUsers — Return List Tidak Null");
        try {
            var users = userService.getAllUsers();
            if (users != null) {
                assertPass("getAllUsers return list tidak null, size: " + users.size());
            } else {
                assertFail("getAllUsers harus return list", "return null");
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 4 — getUsersByRole
    // ==========================================
    static void runTest4_GetUsersByRole() {
        printHeader("[TEST 4] getUsersByRole — Filter Berdasarkan Role");
        try {
            var admins = userService.getUsersByRole(UserRole.ADMIN);
            boolean found = admins.stream().anyMatch(u -> u.getId().equals(TEST_ADMIN_ID));
            if (found) {
                assertPass("Admin test ditemukan di list role ADMIN");
            } else {
                assertFail("Admin test harus ada di list", "tidak ditemukan");
            }

            boolean noMember = admins.stream().noneMatch(u -> u.getRole() == UserRole.MEMBER);
            if (noMember) {
                assertPass("Tidak ada MEMBER di list role ADMIN");
            } else {
                assertFail("List ADMIN tidak boleh mengandung MEMBER", "ada MEMBER di list");
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 5 — registerLibrarian: sukses
    // ==========================================
    static void runTest5_RegisterLibrarian_Success() {
        printHeader("[TEST 5] registerLibrarian — Sukses");
        try {
            Librarian newLib = new Librarian(
                "New Librarian Test",
                "newlib_newlib_test@lib.com",
                null,
                "EMP-TEST-NEW",
                "PAGI"
            );
            newLib.setCreatedBy("TEST_SETUP");

            Librarian saved = userService.registerLibrarian(admin, newLib, "libpass123");

            if (saved.getId() != null) {
                assertPass("Librarian baru tersimpan dengan ID: " + saved.getId());
            } else {
                assertFail("ID harus ter-generate setelah save", "ID masih null");
            }

            if (saved.getPasswordHash() != null && !saved.getPasswordHash().equals("libpass123")) {
                assertPass("Password berhasil di-hash sebelum disimpan");
            } else {
                assertFail("Password harus di-hash", "password plain atau null");
            }

            if (UserRole.LIBRARIAN == saved.getRole()) {
                assertPass("Role ter-set LIBRARIAN dengan benar");
            } else {
                assertFail("Role harus LIBRARIAN", "actual: " + saved.getRole());
            }

        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 6 — registerLibrarian: email duplikat
    // ==========================================
    static void runTest6_RegisterLibrarian_DuplicateEmail() {
        printHeader("[TEST 6] registerLibrarian — Email Sudah Terdaftar");
        try {
            Librarian dup = new Librarian(
                "Duplikat",
                "librarian_test@lib.com", // email yang sudah ada
                null, "EMP-DUP", "SORE"
            );
            userService.registerLibrarian(admin, dup, "password123");
            assertFail("Harus ditolak karena email duplikat", "sistem membiarkan");
        } catch (IllegalStateException e) {
            assertPass("Duplikat email ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 7 — registerLibrarian: email kosong
    // ==========================================
    static void runTest7_RegisterLibrarian_EmptyEmail() {
        printHeader("[TEST 7] registerLibrarian — Email Kosong");
        try {
            Librarian lib = new Librarian("No Email", "", null, "EMP-NOEMAIL", "PAGI");
            userService.registerLibrarian(admin, lib, "password123");
            assertFail("Harus ditolak karena email kosong", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Email kosong ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 8 — registerLibrarian: password terlalu pendek
    // ==========================================
    static void runTest8_RegisterLibrarian_ShortPassword() {
        printHeader("[TEST 8] registerLibrarian — Password Terlalu Pendek");
        try {
            Librarian lib = new Librarian("Short Pass", "shortpass_test@lib.com", null, "EMP-SHORT", "MALAM");
            userService.registerLibrarian(admin, lib, "abc");
            assertFail("Harus ditolak karena password < 6 karakter", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Password pendek ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 9 — registerLibrarian: tidak punya permission
    // ==========================================
    static void runTest9_RegisterLibrarian_NoPermission() {
        printHeader("[TEST 9] registerLibrarian — Akses Ditolak (Bukan Admin)");
        try {
            Librarian lib = new Librarian("Unauthorized", "unauth_test@lib.com", null, "EMP-UNAUTH", "PAGI");
            userService.registerLibrarian(intruder, lib, "password123");
            assertFail("Harus ditolak karena intruder tidak punya CRUD_LIBRARIAN", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Akses ditolak dengan benar: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 10 — suspendUser: sukses
    // ==========================================
    static void runTest10_SuspendUser_Success() {
        printHeader("[TEST 10] suspendUser — Sukses");
        try {
            userService.suspendUser(admin, TEST_MEMBER_ID);
            User suspended = userService.getUserById(TEST_MEMBER_ID);

            if (!suspended.isActive()) {
                assertPass("Akun berhasil di-suspend (active = false)");
            } else {
                assertFail("Akun harus non-aktif setelah suspend", "masih aktif");
            }

            if (suspended instanceof Member) {
                Member m = (Member) suspended;
                if (m.getStatus() == MemberStatus.SUSPENDED) {
                    assertPass("MemberStatus berubah ke SUSPENDED");
                } else {
                    assertFail("MemberStatus harus SUSPENDED", "actual: " + m.getStatus());
                }
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 11 — suspendUser: sudah non-aktif
    // ==========================================
    static void runTest11_SuspendUser_AlreadySuspended() {
        printHeader("[TEST 11] suspendUser — Akun Sudah Non-Aktif");
        try {
            // TEST_MEMBER_ID sudah di-suspend di test 10
            userService.suspendUser(admin, TEST_MEMBER_ID);
            assertFail("Harus ditolak karena sudah non-aktif", "sistem membiarkan");
        } catch (IllegalStateException e) {
            assertPass("Double suspend ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 12 — suspendUser: tidak punya permission
    // ==========================================
    static void runTest12_SuspendUser_NoPermission() {
        printHeader("[TEST 12] suspendUser — Akses Ditolak");
        try {
            userService.suspendUser(intruder, TEST_LIBRARIAN_ID);
            assertFail("Harus ditolak karena intruder tidak punya MANAGE_USER", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Akses ditolak dengan benar: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 13 — activateUser: sukses
    // ==========================================
    static void runTest13_ActivateUser_Success() {
        printHeader("[TEST 13] activateUser — Sukses");
        try {
            // TEST_MEMBER_ID masih suspended dari test 10
            userService.activateUser(admin, TEST_MEMBER_ID);
            User activated = userService.getUserById(TEST_MEMBER_ID);

            if (activated.isActive()) {
                assertPass("Akun berhasil diaktifkan kembali");
            } else {
                assertFail("Akun harus aktif setelah activate", "masih non-aktif");
            }

            if (activated instanceof Member) {
                Member m = (Member) activated;
                if (m.getStatus() == MemberStatus.ACTIVE) {
                    assertPass("MemberStatus kembali ke ACTIVE");
                } else {
                    assertFail("MemberStatus harus ACTIVE", "actual: " + m.getStatus());
                }
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 14 — activateUser: sudah aktif
    // ==========================================
    static void runTest14_ActivateUser_AlreadyActive() {
        printHeader("[TEST 14] activateUser — Akun Sudah Aktif");
        try {
            userService.activateUser(admin, TEST_MEMBER_ID);
            assertFail("Harus ditolak karena sudah aktif", "sistem membiarkan");
        } catch (IllegalStateException e) {
            assertPass("Double activate ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 15 — activateUser: tidak punya permission
    // ==========================================
    static void runTest15_ActivateUser_NoPermission() {
        printHeader("[TEST 15] activateUser — Akses Ditolak");
        try {
            userService.activateUser(intruder, TEST_LIBRARIAN_ID);
            assertFail("Harus ditolak karena intruder tidak punya MANAGE_USER", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Akses ditolak dengan benar: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 16 — resetPassword: sukses
    // ==========================================
    static void runTest16_ResetPassword_Success() {
        printHeader("[TEST 16] resetPasswordToDefault — Sukses");
        try {
            userService.resetPasswordToDefault(admin, TEST_LIBRARIAN_ID, "newpass123");
            User updated = userService.getUserById(TEST_LIBRARIAN_ID);

            if (updated.getPasswordHash() != null
                    && !updated.getPasswordHash().equals("newpass123")
                    && PasswordHasher.verifyPassword("newpass123", updated.getPasswordHash())) {
                assertPass("Password berhasil direset dan ter-hash dengan benar");
            } else {
                assertFail("Password harus ter-hash dan bisa diverifikasi", "verifikasi gagal");
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 17 — resetPassword: password terlalu pendek
    // ==========================================
    static void runTest17_ResetPassword_TooShort() {
        printHeader("[TEST 17] resetPasswordToDefault — Password Terlalu Pendek");
        try {
            userService.resetPasswordToDefault(admin, TEST_LIBRARIAN_ID, "abc");
            assertFail("Harus ditolak karena password < 6 karakter", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("Password pendek ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 18 — resetPassword: tidak punya permission
    // ==========================================
    static void runTest18_ResetPassword_NoPermission() {
        printHeader("[TEST 18] resetPasswordToDefault — Akses Ditolak");
        try {
            userService.resetPasswordToDefault(intruder, TEST_LIBRARIAN_ID, "newpass123");
            assertFail("Harus ditolak karena intruder tidak punya RESET_PASSWORD", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Akses ditolak dengan benar: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 19 — updateUserInfo: sukses
    // ==========================================
    static void runTest19_UpdateUserInfo_Success() {
        printHeader("[TEST 19] updateUserInfo — Sukses");
        try {
            User existing = userService.getUserById(TEST_MEMBER_ID);
            existing.setName("Updated Member Name");

            userService.updateUserInfo(admin, existing);

            User afterUpdate = userService.getUserById(TEST_MEMBER_ID);
            if ("Updated Member Name".equals(afterUpdate.getName())) {
                assertPass("Nama berhasil diperbarui");
            } else {
                assertFail("Nama harus berubah", "actual: " + afterUpdate.getName());
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 20 — updateUserInfo: email bentrok dengan akun lain
    // ==========================================
    static void runTest20_UpdateUserInfo_EmailConflict() {
        printHeader("[TEST 20] updateUserInfo — Email Konflik Dengan Akun Lain");
        try {
            User member = userService.getUserById(TEST_MEMBER_ID);
            member.setEmail("librarian_test@lib.com"); // email milik librarian test

            userService.updateUserInfo(admin, member);
            assertFail("Harus ditolak karena email sudah dipakai akun lain", "sistem membiarkan");
        } catch (IllegalStateException e) {
            assertPass("Email konflik ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 21 — updateUserInfo: user tidak ditemukan
    // ==========================================
    static void runTest21_UpdateUserInfo_UserNotFound() {
        printHeader("[TEST 21] updateUserInfo — User Tidak Ditemukan");
        try {
            Member ghost = new Member(99999, "Ghost User");
            ghost.setEmail("ghost@lib.com");

            userService.updateUserInfo(admin, ghost);
            assertFail("Harus ditolak karena user tidak ada", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("User tidak ditemukan ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 22 — updateUserInfo: tidak punya permission
    // ==========================================
    static void runTest22_UpdateUserInfo_NoPermission() {
        printHeader("[TEST 22] updateUserInfo — Akses Ditolak");
        try {
            User member = userService.getUserById(TEST_MEMBER_ID);
            member.setName("Hacked Name");

            userService.updateUserInfo(intruder, member);
            assertFail("Harus ditolak karena intruder tidak punya MANAGE_USER", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Akses ditolak dengan benar: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 23 — deleteUser: sukses (soft delete)
    // ==========================================
    static void runTest23_DeleteUser_Success() {
        printHeader("[TEST 23] deleteUser — Sukses (Soft Delete)");
        try {
            userService.deleteUser(admin, TEST_LIBRARIAN_ID);

            // findById masih return user tapi active = false
            User deleted = userRepo.findById(TEST_LIBRARIAN_ID);
            if (deleted != null && !deleted.isActive()) {
                assertPass("Soft delete berhasil — user masih ada tapi active = false");
            } else if (deleted == null) {
                assertFail("Soft delete seharusnya tidak hapus fisik", "user hilang dari DB");
            } else {
                assertFail("User harus non-aktif setelah delete", "masih aktif");
            }
        } catch (Exception e) {
            assertFail("Exception tidak terduga", e.getMessage());
        }
    }

    // ==========================================
    // TEST 24 — deleteUser: user tidak ada
    // ==========================================
    static void runTest24_DeleteUser_NotFound() {
        printHeader("[TEST 24] deleteUser — User Tidak Ditemukan");
        try {
            userService.deleteUser(admin, 99999);
            assertFail("Harus ditolak karena user tidak ada", "sistem membiarkan");
        } catch (IllegalArgumentException e) {
            assertPass("User tidak ditemukan ditolak: " + e.getMessage());
        }
    }

    // ==========================================
    // TEST 25 — deleteUser: tidak punya permission
    // ==========================================
    static void runTest25_DeleteUser_NoPermission() {
        printHeader("[TEST 25] deleteUser — Akses Ditolak");
        try {
            userService.deleteUser(intruder, TEST_MEMBER_ID);
            assertFail("Harus ditolak karena intruder tidak punya MANAGE_USER", "sistem membiarkan");
        } catch (SecurityException e) {
            assertPass("Akses ditolak dengan benar: " + e.getMessage());
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