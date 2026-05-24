package com.library.tui;

import com.library.domain.entities.User;
import com.library.domain.enums.UserRole;
import com.library.service.AuthService;
import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.service.ConfigService;
import com.library.service.LoanService;
import com.library.service.ReportService;
import com.library.service.UserService;
import com.library.tui.views.AdminView;
import com.library.tui.views.AuthView;
import com.library.tui.views.LibrarianView;
import com.library.tui.views.MemberView;

public class TuiController {

    private final AuthService authService;
    private final BookService bookService;
    private final LoanService loanService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ReportService reportService;
    private final ConfigService configService;

    private User currentUser;

    public TuiController(AuthService authService, BookService bookService, LoanService loanService, UserService userService, CategoryService categoryService, ReportService reportService, ConfigService configService) {
        this.authService = authService;
        this.bookService = bookService;
        this.loanService = loanService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.reportService = reportService;
        this.configService = configService;
    }

    public void start() {
        while (true) {
            if (currentUser == null) {
                AuthView authView = new AuthView(authService);
                currentUser = authView.showMenu();
                
                // Trigger background schedulers upon any successful login
                if (currentUser != null) {
                    try {
                        loanService.processScheduledPickups();
                        loanService.processExpiredPickups();
                        loanService.processOverdueLoans();
                    } catch (Exception e) {
                        System.out.println("Warning: Failed to run background schedulers: " + e.getMessage());
                    }
                }
            } else {
                // Route based on Role Polymorphism (or enum)
                if (currentUser.getRole() == UserRole.MEMBER) {
                    MemberView memberView = new MemberView(currentUser, bookService, loanService);
                    memberView.showMenu();
                } else if (currentUser.getRole() == UserRole.LIBRARIAN) {
                    LibrarianView librarianView = new LibrarianView(currentUser, bookService, loanService, categoryService);
                    librarianView.showMenu();
                } else if (currentUser.getRole() == UserRole.ADMIN) {
                    AdminView adminView = new AdminView(currentUser, userService, reportService, configService);
                    adminView.showMenu();
                } else {
                    System.out.println("Unknown Role. Logging out...");
                }
                // When they exit the view, they are logged out
                currentUser = null;
            }
        }
    }
}
