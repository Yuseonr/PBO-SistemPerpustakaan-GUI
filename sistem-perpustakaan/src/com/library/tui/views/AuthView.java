package com.library.tui.views;

import com.library.domain.entities.User;
import com.library.service.AuthService;
import com.library.tui.util.TuiUtils;

public class AuthView {

    private final AuthService authService;

    public AuthView(AuthService authService) {
        this.authService = authService;
    }

    public User showMenu() {
        while (true) {
            TuiUtils.printHeader("SISTEM PERPUSTAKAAN - AUTENTIKASI");
            System.out.println("1. Masuk (Login)");
            System.out.println("2. Daftar sebagai Member");
            System.out.println("3. [DEV] Fast-Forward Time (+1 Day)");
            System.out.println("4. [DEV] Reset Database");
            System.out.println("0. Keluar dari Sistem");
            System.out.println("--------------------------------------------------");

            int choice = TuiUtils.readInt("Masukkan pilihan: ");

            switch (choice) {
                case 1:
                    User user = processLogin();
                    if (user != null) {
                        return user; // Successfully logged in
                    }
                    break;
                case 2:
                    processRegistration();
                    break;
                case 3:
                    simulateTimeTravel();
                    break;
                case 4:
                    clearDatabase();
                    break;
                case 0:
                    TuiUtils.printInfo("Keluar dari Sistem. Sampai jumpa!");
                    System.exit(0);
                    break;
                default:
                    TuiUtils.printError("Pilihan tidak valid. Silakan pilih 1, 2, 3, 4, atau 0.");
                    TuiUtils.waitForEnter();
            }
        }
    }

    private void simulateTimeTravel() {
        TuiUtils.printHeader("TIME TRAVEL SIMULATION");
        try (java.sql.Connection conn = com.library.config.DatabaseConfig.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            String sql = "UPDATE loans SET " +
                         "scheduled_pickup_date = DATE_SUB(scheduled_pickup_date, INTERVAL 1 DAY), " +
                         "request_date = DATE_SUB(request_date, INTERVAL 1 DAY), " +
                         "due_date = DATE_SUB(due_date, INTERVAL 1 DAY), " +
                         "return_date = DATE_SUB(return_date, INTERVAL 1 DAY)";
            int affected = stmt.executeUpdate(sql);
            TuiUtils.printSuccess("Simulasi +1 Hari berhasil dengan menggeser " + affected + " data mundur 24 jam.");
            TuiUtils.printInfo("Logika overdue sekarang menganggap hari ini adalah besok!");
        } catch (Exception e) {
            TuiUtils.printError("Gagal simulasi waktu: " + e.getMessage());
        }
        TuiUtils.waitForEnter();
    }

    private void clearDatabase() {
        TuiUtils.printHeader("RESET DATABASE");
        String confirm = TuiUtils.readString("Apakah Anda yakin ingin menghapus seluruh database? (Y/N): ");
        if (confirm.equalsIgnoreCase("Y")) {
            try (java.sql.Connection conn = com.library.config.DatabaseConfig.getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("TRUNCATE TABLE loans");
                stmt.execute("TRUNCATE TABLE book_copies");
                stmt.execute("TRUNCATE TABLE book_titles");
                stmt.execute("TRUNCATE TABLE categories");
                stmt.execute("TRUNCATE TABLE users");
                stmt.execute("TRUNCATE TABLE library_config");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                
                TuiUtils.printSuccess("Database berhasil dihapus!");
                TuiUtils.printInfo("Silakan jalankan ulang aplikasi untuk men-trigger DummyDataSeeder.");
                System.exit(0);
            } catch (Exception e) {
                TuiUtils.printError("Gagal menghapus database: " + e.getMessage());
                TuiUtils.waitForEnter();
            }
        } else {
            TuiUtils.printInfo("Reset dibatalkan.");
            TuiUtils.waitForEnter();
        }
    }

    private User processLogin() {
        TuiUtils.printHeader("LOGIN");
        String email = TuiUtils.readString("Email: ");
        String password = TuiUtils.readString("Password: ");

        if (email.equals("admin") && password.equals("123")) {
            try {
                User admin = authService.login("admin@library.com", "123456");
                TuiUtils.printSuccess("[DEV] Fast-track Admin Login berhasil.");
                TuiUtils.waitForEnter();
                return admin;
            } catch (Exception e) {
                TuiUtils.printError("Fast-track gagal. Kembali ke login normal.");
            }
        }

        try {
            User user = authService.login(email, password);
            TuiUtils.printSuccess("Login berhasil! Selamat datang, " + user.getName());
            TuiUtils.waitForEnter();
            return user;
        } catch (IllegalArgumentException | IllegalStateException e) {
            TuiUtils.printError(e.getMessage());
            TuiUtils.waitForEnter();
            return null;
        }
    }

    private void processRegistration() {
        TuiUtils.printHeader("DAFTAR MEMBER");
        String name = TuiUtils.readString("Nama Lengkap: ");
        String email = TuiUtils.readString("Email: ");
        String password = TuiUtils.readString("Password: ");
        String membershipNumber = TuiUtils.readString("Nomor Keanggotaan (contoh: MEM-001): ");
        String address = TuiUtils.readString("Alamat: ");
        String phone = TuiUtils.readString("Nomor Telepon: ");

        try {
            authService.registerMember(name, email, password, membershipNumber, address, phone);
            TuiUtils.printSuccess("Registrasi berhasil! Anda sekarang dapat login.");
            TuiUtils.waitForEnter();
        } catch (IllegalArgumentException e) {
            TuiUtils.printError(e.getMessage());
            TuiUtils.waitForEnter();
        }
    }
}
