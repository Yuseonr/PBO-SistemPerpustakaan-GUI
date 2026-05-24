/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.User;
import com.library.repository.IUserRepository;
import com.library.repository.UserRepositoryMySQLImpl;
import com.library.service.AuthService;
import com.library.util.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Full Test Suite untuk AuthService (Registrasi & Login)
 * @author rafianandra
 */
public class MainAuth {
    
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
    // DEPENDENCIES & COUNTERS
    // ============================================================
    static IUserRepository userRepository;
    static AuthService authService;
    
    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println(BLUE);
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║       AUTH SERVICE — FULL TEST SUITE                 ║");
        System.out.println("║       Strategi: Insert Fresh + Cleanup               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println(RESET);

        try {
            setupDependencies();
            cleanupTestData(); // Bersihkan sisa data sebelumnya jika ada
            insertDummyData();

            runTest1_HappyPathRegister();
            runTest2_HappyPathLoginAndPolymorphism();
            runTest3_EdgeCaseWrongPassword();
            runTest4_EdgeCaseEmailNotFound();
            runTest5_EdgeCaseSuspendedAccount();
            runTest6_EdgeCaseDuplicateEmail();
            runTest7_EdgeCaseInvalidInputs();

        } catch (Exception e) {
            System.out.println(RED + "\n❌ FATAL ERROR DI TEST RUNNER: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            cleanupTestData(); // Hapus data setelah test selesai
            printSummary();
        }
    }

    // ============================================================
    // SETUP & TEARDOWN
    // ============================================================
    static void setupDependencies() {
        userRepository = new UserRepositoryMySQLImpl();
        authService = new AuthService(userRepository);
        System.out.println(CYAN + "[SETUP] Dependencies siap." + RESET);
    }

    static void insertDummyData() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String passHash = PasswordHasher.hashPassword("rahasia123");
            
            // Insert 1 Akun Admin (Valid) dan 1 Akun Member (Suspended)
            stmt.executeUpdate(
                "INSERT INTO users (id, name, email, password_hash, role, active, created_by) VALUES "
                + "(9901, 'Admin Test', 'admin@test.com', '" + passHash + "', 'ADMIN', 1, 'SETUP'), "
                + "(9902, 'Banned Member', 'banned@test.com', '" + passHash + "', 'MEMBER', 0, 'SETUP')"
            );
        }
        System.out.println(CYAN + "[SETUP] Data dummy (Admin & Suspended Member) disisipkan." + RESET);
    }

    static void cleanupTestData() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            // Hapus semua email yang berakhiran @test.com
            stmt.executeUpdate("DELETE FROM users WHERE email LIKE '%@test.com'");
        } catch (SQLException e) {
            System.out.println(RED + "[CLEANUP] Gagal menghapus test data: " + e.getMessage() + RESET);
        }
    }

    // ============================================================
    // HELPER ASSERTIONS
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

    // ============================================================
    // SKENARIO PENGUJIAN
    // ============================================================

    static void runTest1_HappyPathRegister() {
        printHeader("[TEST 1] Happy Path: Registrasi Member Baru");
        try {
            authService.registerMember(
                "User Baru", "baru@test.com", "password123", "MEM-001", "Jl. Tes", "0811"
            );
            assertPass("Member berhasil diregistrasi tanpa error");
        } catch (Exception e) {
            assertFail("Registrasi Gagal", e.getMessage());
        }
    }

    static void runTest2_HappyPathLoginAndPolymorphism() {
        printHeader("[TEST 2] Happy Path: Login & Polimorfisme");
        try {
            // Login menggunakan data yang dibuat di Test 1
            User member = authService.login("baru@test.com", "password123");
            assertPass("Login berhasil dengan email & password yang benar");
            
            // Tes Polimorfisme
            if (member.getDashboardTitle().contains("Member")) {
                assertPass("Polimorfisme bekerja: getDashboardTitle() = " + member.getDashboardTitle());
            } else {
                assertFail("Polimorfisme Gagal", "Title: " + member.getDashboardTitle());
            }
            
            // Tes Login Admin (Data Dummy)
            User admin = authService.login("admin@test.com", "rahasia123");
            if (admin.getDashboardTitle().contains("Admin")) {
                assertPass("Login Admin sukses: getDashboardTitle() = " + admin.getDashboardTitle());
            } else {
                assertFail("Polimorfisme Admin Gagal", "Title: " + admin.getDashboardTitle());
            }

        } catch (Exception e) {
            assertFail("Login/Polimorfisme gagal", e.getMessage());
        }
    }

    static void runTest3_EdgeCaseWrongPassword() {
        printHeader("[TEST 3] Edge Case: Login Gagal (Password Salah)");
        try {
            authService.login("admin@test.com", "password_salah_nih");
            assertFail("Sistem membiarkan login", "Password salah tapi lolos");
        } catch (IllegalArgumentException e) {
            assertPass("Login ditolak (Expected): " + e.getMessage());
        }
    }

    static void runTest4_EdgeCaseEmailNotFound() {
        printHeader("[TEST 4] Edge Case: Login Gagal (Email Tidak Terdaftar)");
        try {
            authService.login("hantu@test.com", "rahasia123");
            assertFail("Sistem membiarkan login", "Email tidak ada tapi lolos");
        } catch (IllegalArgumentException e) {
            assertPass("Login ditolak (Expected): " + e.getMessage());
        }
    }

    static void runTest5_EdgeCaseSuspendedAccount() {
        printHeader("[TEST 5] Edge Case: Login Ditolak (Akun Suspended / Inactive)");
        try {
            authService.login("banned@test.com", "rahasia123"); // Email & pass benar, tapi status mati
            assertFail("Sistem membiarkan login", "Akun mati tapi bisa login");
        } catch (IllegalStateException e) {
            assertPass("Login ditolak (Expected): " + e.getMessage());
        }
    }

    static void runTest6_EdgeCaseDuplicateEmail() {
        printHeader("[TEST 6] Edge Case: Registrasi Gagal (Email Duplikat)");
        try {
            // Gunakan email yang sudah didaftarkan di Test 1
            authService.registerMember("Kloningan", "baru@test.com", "pass123", "002", "-", "-");
            assertFail("Sistem membiarkan registrasi email ganda", "Lolosan email duplicate");
        } catch (IllegalArgumentException e) {
            assertPass("Registrasi ditolak (Expected): " + e.getMessage());
        }
    }

    static void runTest7_EdgeCaseInvalidInputs() {
        printHeader("[TEST 7] Edge Case: Registrasi Gagal (Input Kosong/Pendek)");
        
        try {
            authService.registerMember("", "email@test.com", "123456", "01", "-", "-");
            assertFail("Sistem menerima nama kosong", "");
        } catch (IllegalArgumentException e) {
            assertPass("Nama kosong ditolak: " + e.getMessage());
        }

        try {
            authService.registerMember("Nama", "email@test.com", "", "01", "-", "-");
            assertFail("Sistem menerima sandi kosong", "");
        } catch (IllegalArgumentException e) {
            assertPass("Sandi kosong ditolak: " + e.getMessage());
        }
    }

    // ============================================================
    // SUMMARY
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