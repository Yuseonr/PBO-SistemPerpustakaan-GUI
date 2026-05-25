package com.library.tui.views;

import com.library.domain.entities.DashboardStats;
import com.library.domain.entities.Librarian;
import com.library.domain.entities.LibraryConfig;
import com.library.domain.entities.User;
import com.library.service.ConfigService;
import com.library.service.ReportService;
import com.library.service.UserService;
import com.library.tui.util.TuiUtils;

import java.util.List;
import java.util.stream.Collectors;

public class AdminView {

    private final User admin;
    private final UserService userService;
    private final ReportService reportService;
    private final ConfigService configService;

    public AdminView(User admin, UserService userService, ReportService reportService, ConfigService configService) {
        this.admin = admin;
        this.userService = userService;
        this.reportService = reportService;
        this.configService = configService;
    }

    public void showMenu() {
        while (true) {
            TuiUtils.printHeader(admin.getDashboardTitle());
            System.out.println(TuiUtils.GREEN + "  Selamat datang, " + admin.getName() + " (Admin)" + TuiUtils.RESET);
            System.out.println();
            
            String[] options = {
                "1. Lihat Statistik Dashboard",
                "2. Manajer User Interaktif",
                "3. Konfigurasi Sistem",
                "0. Keluar (Logout)"
            };
            TuiUtils.printBoxMenu(options);
            System.out.println();

            int choice = TuiUtils.readInt("Masukkan pilihan: ");

            switch (choice) {
                case 1:
                    viewStats();
                    break;
                case 2:
                    interactiveUsers();
                    break;
                case 3:
                    systemConfiguration();
                    break;
                case 0:
                    TuiUtils.printInfo("Keluar...");
                    return;
                default:
                    TuiUtils.printError("Pilihan tidak valid.");
                    TuiUtils.waitForEnter();
            }
        }
    }

    private void viewStats() {
        TuiUtils.printHeader("STATISTIK DASHBOARD");
        try {
            DashboardStats stats = reportService.generateDashboardStats(admin);
            String[] details = {
                "Total Member Aktif : " + stats.getTotalActiveMembers(),
                "Total Pustakawan   : " + stats.getTotalLibrarians(),
                "Total Admin        : " + stats.getTotalAdmins(),
                "",
                "Total Kategori     : " + stats.getTotalCategories(),
                "Total Judul Buku   : " + stats.getTotalBookTitles(),
                "Total Eksemplar    : " + stats.getTotalBookCopies(),
                "",
                "Total Denda Terkumpul: Rp. " + stats.getTotalFineCollected()
            };
            TuiUtils.printBoxDetail("METRIK PERPUSTAKAAN", details);
        } catch (Exception e) {
            TuiUtils.printError(e.getMessage());
        }
        TuiUtils.waitForEnter();
    }

    private void interactiveUsers() {
        String keyword = "";
        String roleFilter = "ALL";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("MANAJER USER INTERAKTIF");
            
            List<User> allUsers = userService.getAllUsers();
            final String currentKeyword = keyword;
            final String currentRoleFilter = roleFilter;
            
            List<User> filtered = allUsers.stream()
                .filter(u -> currentRoleFilter.equals("ALL") || u.getRole().name().equals(currentRoleFilter))
                .filter(u -> currentKeyword.isEmpty() 
                    || u.getName().toLowerCase().contains(currentKeyword.toLowerCase())
                    || u.getEmail().toLowerCase().contains(currentKeyword.toLowerCase())
                    || String.valueOf(u.getId()).equals(currentKeyword))
                .collect(Collectors.toList());
                
            int totalItems = filtered.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;
            if (currentPage < 0) currentPage = 0;

            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            
            System.out.println(TuiUtils.YELLOW + "Filter: " + currentRoleFilter + " | Pencarian: '" + currentKeyword + "' (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            
            if (filtered.isEmpty()) {
                TuiUtils.printInfo("Tidak ada user ditemukan.");
            } else {
                String[] headers = {"ID", "Nama", "Email", "Role", "Aktif"};
                String[][] data = new String[end - start][5];
                
                for (int i = start; i < end; i++) {
                    User u = filtered.get(i);
                    String name = u.getName();
                    name = name.length() > 15 ? name.substring(0, 12) + "..." : name;
                    String email = u.getEmail();
                    email = email.length() > 20 ? email.substring(0, 17) + "..." : email;
                    
                    data[i - start][0] = String.valueOf(u.getId());
                    data[i - start][1] = name;
                    data[i - start][2] = email;
                    data[i - start][3] = u.getRole().name();
                    data[i - start][4] = u.isActive() ? "YA" : "TIDAK";
                }
                TuiUtils.printTable(headers, data);
            }
            
            System.out.println();
            System.out.println(TuiUtils.CYAN + "Perintah: [N]ext | [P]rev | [S]earch | [F]ilter Role | [R]egister Librarian | [T]oggle Status | [Q]uit" + TuiUtils.RESET);
            String cmd = TuiUtils.readString("Masukkan perintah: ").trim().toUpperCase();
            
            try {
                switch (cmd) {
                    case "N":
                        if (currentPage < totalPages - 1) currentPage++;
                        break;
                    case "P":
                        if (currentPage > 0) currentPage--;
                        break;
                    case "S":
                        keyword = TuiUtils.readString("Masukkan kata kunci (Nama, Email, ID): ");
                        currentPage = 0;
                        break;
                    case "F":
                        System.out.println("Role tersedia: ALL, MEMBER, LIBRARIAN, ADMIN");
                        String f = TuiUtils.readString("Masukkan role: ").toUpperCase();
                        roleFilter = f;
                        currentPage = 0;
                        break;
                    case "R":
                        registerLibrarian();
                        break;
                    case "T":
                        toggleUserStatus();
                        break;
                    case "Q":
                        return;
                    default:
                        TuiUtils.printError("Perintah tidak valid.");
                        TuiUtils.waitForEnter();
                }
            } catch (Exception e) {
                TuiUtils.printError(e.getMessage());
                TuiUtils.waitForEnter();
            }
        }
    }

    private void registerLibrarian() {
        System.out.println();
        System.out.println(TuiUtils.CYAN + "--- DAFTAR PUSTAKAWAN ---" + TuiUtils.RESET);
        String name = TuiUtils.readString("Nama: ");
        String email = TuiUtils.readString("Email: ");
        String password = TuiUtils.readString("Password: ");
        String empNumber = TuiUtils.readString("Nomor Pegawai: ");
        String shiftInfo = TuiUtils.readString("Info Shift: ");
        
        try {
            Librarian newLibrarian = new Librarian(name, email, "", empNumber, shiftInfo);
            userService.registerLibrarian(admin, newLibrarian, password);
            TuiUtils.printSuccess("Pustakawan berhasil didaftarkan!");
        } catch (Exception e) {
            TuiUtils.printError(e.getMessage());
        }
        TuiUtils.waitForEnter();
    }

    private void toggleUserStatus() {
        System.out.println();
        int userId = TuiUtils.readInt("Masukkan ID User untuk ubah status: ");
        System.out.println();
        String[] options = {"1. Suspend User", "2. Aktifkan User"};
        TuiUtils.printBoxMenu(options);
        int action = TuiUtils.readInt("Pilihan: ");
        
        try {
            if (action == 1) {
                userService.suspendUser(admin, userId);
                TuiUtils.printSuccess("User di-suspend.");
            } else if (action == 2) {
                userService.activateUser(admin, userId);
                TuiUtils.printSuccess("User diaktifkan.");
            } else {
                TuiUtils.printError("Pilihan tidak valid.");
            }
        } catch (Exception e) {
            TuiUtils.printError(e.getMessage());
        }
        TuiUtils.waitForEnter();
    }

    private void systemConfiguration() {
        TuiUtils.printHeader("KONFIGURASI SISTEM");
        try {
            LibraryConfig config = configService.getLibraryConfig();
            if(config == null) {
                TuiUtils.printError("Config tidak ditemukan di database.");
                TuiUtils.waitForEnter();
                return;
            }
            String[] details = {
                "Nama Perpustakaan: " + config.getLibraryName(),
                "Denda Per Hari: " + config.getFinePerDay(),
                "Maks Hari Pinjam: " + config.getMaxBorrowDays(),
                "Batas Maks Pinjam: " + config.getMaxBorrowLimit(),
                "Maks Hari Reservasi ke Depan: " + config.getMaxReservationDaysAhead(),
                "Hari Batas Pengambilan: " + config.getPickupWindowDays()
            };
            TuiUtils.printBoxDetail("CONFIG SAAT INI", details);
            System.out.println();
            
            int update = TuiUtils.readInt("Update config? (1 untuk Ya, 0 untuk Tidak): ");
            if (update == 1) {
                String newName = TuiUtils.readString("Nama Perpustakaan Baru (tekan Enter untuk lewati): ");
                if(!newName.trim().isEmpty()) {
                    config.setLibraryName(newName);
                }
                
                String newFine = TuiUtils.readString("Denda Per Hari Baru (tekan Enter untuk lewati): ");
                if(!newFine.trim().isEmpty()) {
                    config.setFinePerDay(Double.parseDouble(newFine));
                }

                String newMaxBorrow = TuiUtils.readString("Maks Hari Pinjam Baru (tekan Enter untuk lewati): ");
                if(!newMaxBorrow.trim().isEmpty()) {
                    config.setMaxBorrowDays(Integer.parseInt(newMaxBorrow));
                }

                String newMaxLimit = TuiUtils.readString("Batas Maks Pinjam Baru (tekan Enter untuk lewati): ");
                if(!newMaxLimit.trim().isEmpty()) {
                    config.setMaxBorrowLimit(Integer.parseInt(newMaxLimit));
                }

                String newMaxRes = TuiUtils.readString("Maks Hari Reservasi Baru (tekan Enter untuk lewati): ");
                if(!newMaxRes.trim().isEmpty()) {
                    config.setMaxReservationDaysAhead(Integer.parseInt(newMaxRes));
                }

                String newPickup = TuiUtils.readString("Hari Batas Pengambilan Baru (tekan Enter untuk lewati): ");
                if(!newPickup.trim().isEmpty()) {
                    config.setPickupWindowDays(Integer.parseInt(newPickup));
                }
                
                configService.updateLibraryConfig(admin, config);
                TuiUtils.printSuccess("Konfigurasi berhasil diperbarui.");
            }
        } catch (Exception e) {
            TuiUtils.printError(e.getMessage());
        }
        TuiUtils.waitForEnter();
    }
}
