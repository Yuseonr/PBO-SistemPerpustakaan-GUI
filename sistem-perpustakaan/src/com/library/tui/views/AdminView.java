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
            System.out.println(TuiUtils.GREEN + "  Welcome, " + admin.getName() + " (Admin)" + TuiUtils.RESET);
            System.out.println();
            
            String[] options = {
                "1. View Dashboard Stats",
                "2. Interactive User Manager",
                "3. System Configuration",
                "0. Logout"
            };
            TuiUtils.printBoxMenu(options);
            System.out.println();

            int choice = TuiUtils.readInt("Enter your choice: ");

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
                    TuiUtils.printInfo("Logging out...");
                    return;
                default:
                    TuiUtils.printError("Invalid choice.");
                    TuiUtils.waitForEnter();
            }
        }
    }

    private void viewStats() {
        TuiUtils.printHeader("DASHBOARD STATS");
        try {
            DashboardStats stats = reportService.generateDashboardStats(admin);
            String[] details = {
                "Total Active Members : " + stats.getTotalActiveMembers(),
                "Total Librarians     : " + stats.getTotalLibrarians(),
                "Total Admins         : " + stats.getTotalAdmins(),
                "",
                "Total Categories     : " + stats.getTotalCategories(),
                "Total Book Titles    : " + stats.getTotalBookTitles(),
                "Total Book Copies    : " + stats.getTotalBookCopies(),
                "",
                "Total Fines Collected: Rp. " + stats.getTotalFineCollected()
            };
            TuiUtils.printBoxDetail("LIBRARY METRICS", details);
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
            TuiUtils.printHeader("INTERACTIVE USER MANAGER");
            
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
            
            System.out.println(TuiUtils.YELLOW + "Filter: " + currentRoleFilter + " | Search: '" + currentKeyword + "' (Page " + (currentPage + 1) + " of " + totalPages + ")" + TuiUtils.RESET);
            
            if (filtered.isEmpty()) {
                TuiUtils.printInfo("No users found.");
            } else {
                String[] headers = {"ID", "Name", "Email", "Role", "Active"};
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
                    data[i - start][4] = u.isActive() ? "YES" : "NO";
                }
                TuiUtils.printTable(headers, data);
            }
            
            System.out.println();
            System.out.println(TuiUtils.CYAN + "Commands: [N]ext | [P]rev | [S]earch | [F]ilter Role | [R]egister Librarian | [T]oggle Status | [Q]uit" + TuiUtils.RESET);
            String cmd = TuiUtils.readString("Enter command: ").trim().toUpperCase();
            
            try {
                switch (cmd) {
                    case "N":
                        if (currentPage < totalPages - 1) currentPage++;
                        break;
                    case "P":
                        if (currentPage > 0) currentPage--;
                        break;
                    case "S":
                        keyword = TuiUtils.readString("Enter keyword (Name, Email, ID): ");
                        currentPage = 0;
                        break;
                    case "F":
                        System.out.println("Available roles: ALL, MEMBER, LIBRARIAN, ADMIN");
                        String f = TuiUtils.readString("Enter role: ").toUpperCase();
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
                        TuiUtils.printError("Invalid command.");
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
        System.out.println(TuiUtils.CYAN + "--- REGISTER LIBRARIAN ---" + TuiUtils.RESET);
        String name = TuiUtils.readString("Name: ");
        String email = TuiUtils.readString("Email: ");
        String password = TuiUtils.readString("Password: ");
        String empNumber = TuiUtils.readString("Employee Number: ");
        String shiftInfo = TuiUtils.readString("Shift Info: ");
        
        try {
            Librarian newLibrarian = new Librarian(name, email, "", empNumber, shiftInfo);
            userService.registerLibrarian(admin, newLibrarian, password);
            TuiUtils.printSuccess("Librarian registered successfully!");
        } catch (Exception e) {
            TuiUtils.printError(e.getMessage());
        }
        TuiUtils.waitForEnter();
    }

    private void toggleUserStatus() {
        System.out.println();
        int userId = TuiUtils.readInt("Enter User ID to toggle status: ");
        System.out.println();
        String[] options = {"1. Suspend User", "2. Activate User"};
        TuiUtils.printBoxMenu(options);
        int action = TuiUtils.readInt("Choice: ");
        
        try {
            if (action == 1) {
                userService.suspendUser(admin, userId);
                TuiUtils.printSuccess("User suspended.");
            } else if (action == 2) {
                userService.activateUser(admin, userId);
                TuiUtils.printSuccess("User activated.");
            } else {
                TuiUtils.printError("Invalid choice.");
            }
        } catch (Exception e) {
            TuiUtils.printError(e.getMessage());
        }
        TuiUtils.waitForEnter();
    }

    private void systemConfiguration() {
        TuiUtils.printHeader("SYSTEM CONFIGURATION");
        try {
            LibraryConfig config = configService.getLibraryConfig();
            if(config == null) {
                TuiUtils.printError("Config not found in database.");
                TuiUtils.waitForEnter();
                return;
            }
            String[] details = {
                "Library Name: " + config.getLibraryName(),
                "Fine Per Day: " + config.getFinePerDay(),
                "Max Borrow Days: " + config.getMaxBorrowDays(),
                "Max Borrow Limit: " + config.getMaxBorrowLimit(),
                "Max Reservation Days Ahead: " + config.getMaxReservationDaysAhead(),
                "Pickup Window Days: " + config.getPickupWindowDays()
            };
            TuiUtils.printBoxDetail("CURRENT CONFIG", details);
            System.out.println();
            
            int update = TuiUtils.readInt("Update config? (1 for Yes, 0 for No): ");
            if (update == 1) {
                String newName = TuiUtils.readString("New Library Name (or press Enter to keep current): ");
                if(!newName.trim().isEmpty()) {
                    config.setLibraryName(newName);
                }
                
                String newFine = TuiUtils.readString("New Fine Per Day (or press Enter to keep current): ");
                if(!newFine.trim().isEmpty()) {
                    config.setFinePerDay(Double.parseDouble(newFine));
                }

                String newMaxBorrow = TuiUtils.readString("New Max Borrow Days (or press Enter to keep current): ");
                if(!newMaxBorrow.trim().isEmpty()) {
                    config.setMaxBorrowDays(Integer.parseInt(newMaxBorrow));
                }

                String newMaxLimit = TuiUtils.readString("New Max Borrow Limit (or press Enter to keep current): ");
                if(!newMaxLimit.trim().isEmpty()) {
                    config.setMaxBorrowLimit(Integer.parseInt(newMaxLimit));
                }

                String newMaxRes = TuiUtils.readString("New Max Reservation Days Ahead (or press Enter to keep current): ");
                if(!newMaxRes.trim().isEmpty()) {
                    config.setMaxReservationDaysAhead(Integer.parseInt(newMaxRes));
                }

                String newPickup = TuiUtils.readString("New Pickup Window Days (or press Enter to keep current): ");
                if(!newPickup.trim().isEmpty()) {
                    config.setPickupWindowDays(Integer.parseInt(newPickup));
                }
                
                configService.updateLibraryConfig(admin, config);
                TuiUtils.printSuccess("Configuration updated successfully.");
            }
        } catch (Exception e) {
            TuiUtils.printError(e.getMessage());
        }
        TuiUtils.waitForEnter();
    }
}
