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
            TuiUtils.printHeader("LIBRARY SYSTEM - AUTHENTICATION");
            System.out.println("1. Login");
            System.out.println("2. Register as Member");
            System.out.println("3. [DEV] Fast-Forward Time (+1 Day)");
            System.out.println("4. [DEV] Reset Database");
            System.out.println("0. Exit System");
            System.out.println("--------------------------------------------------");

            int choice = TuiUtils.readInt("Enter your choice: ");

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
                    TuiUtils.printInfo("Exiting System. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    TuiUtils.printError("Invalid choice. Please select 1, 2, 3, 4, or 0.");
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
            TuiUtils.printSuccess("Simulated +1 Day by shifting " + affected + " database records backward by 24 hours.");
            TuiUtils.printInfo("Overdue logic will now treat today as tomorrow!");
        } catch (Exception e) {
            TuiUtils.printError("Failed to simulate time: " + e.getMessage());
        }
        TuiUtils.waitForEnter();
    }

    private void clearDatabase() {
        TuiUtils.printHeader("RESET DATABASE");
        String confirm = TuiUtils.readString("Are you sure you want to completely wipe the database? (Y/N): ");
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
                
                TuiUtils.printSuccess("Database successfully wiped!");
                TuiUtils.printInfo("Please restart the application to trigger the DummyDataSeeder and regenerate base data.");
                System.exit(0);
            } catch (Exception e) {
                TuiUtils.printError("Failed to clear database: " + e.getMessage());
                TuiUtils.waitForEnter();
            }
        } else {
            TuiUtils.printInfo("Reset cancelled.");
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
                TuiUtils.printSuccess("[DEV] Fast-track Admin Login successful.");
                TuiUtils.waitForEnter();
                return admin;
            } catch (Exception e) {
                TuiUtils.printError("Fast-track failed (Admin user might not be seeded yet). Falling back to normal login.");
            }
        }

        try {
            User user = authService.login(email, password);
            TuiUtils.printSuccess("Login successful! Welcome, " + user.getName());
            TuiUtils.waitForEnter();
            return user;
        } catch (IllegalArgumentException | IllegalStateException e) {
            TuiUtils.printError(e.getMessage());
            TuiUtils.waitForEnter();
            return null;
        }
    }

    private void processRegistration() {
        TuiUtils.printHeader("REGISTER MEMBER");
        String name = TuiUtils.readString("Full Name: ");
        String email = TuiUtils.readString("Email: ");
        String password = TuiUtils.readString("Password: ");
        String membershipNumber = TuiUtils.readString("Membership Number (e.g. MEM-001): ");
        String address = TuiUtils.readString("Address: ");
        String phone = TuiUtils.readString("Phone Number: ");

        try {
            authService.registerMember(name, email, password, membershipNumber, address, phone);
            TuiUtils.printSuccess("Registration successful! You can now login.");
            TuiUtils.waitForEnter();
        } catch (IllegalArgumentException e) {
            TuiUtils.printError(e.getMessage());
            TuiUtils.waitForEnter();
        }
    }
}
