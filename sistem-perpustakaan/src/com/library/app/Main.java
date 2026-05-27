package com.library.app;

import com.library.domain.entities.Admin;
import com.library.domain.entities.BookCopy;
import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.entities.LibraryConfig;
import com.library.domain.entities.Member;
import com.library.domain.enums.BookCopyStatus;
import com.library.domain.enums.MemberStatus;
import com.library.domain.enums.UserRole;
import com.library.repository.BookCopyRepositoryMySQLImpl;
import com.library.repository.BookTitleRepositoryMySQLImpl;
import com.library.repository.CategoryRepositoryMySQLImpl;
import com.library.repository.IBookCopyRepository;
import com.library.repository.IBookTitleRepository;
import com.library.repository.ICategoryRepository;
import com.library.repository.ILibraryConfigRepository;
import com.library.repository.ILoanTransactionRepository;
import com.library.repository.IUserRepository;
import com.library.repository.LibraryConfigRepositoryMySQLImpl;
import com.library.repository.LoanTransactionRepositoryMySQLImpl;
import com.library.repository.UserRepositoryMySQLImpl;
import com.library.service.AuthService;
import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.service.ConfigService;
import com.library.service.LoanService;
import com.library.service.ReportService;
import com.library.service.UserService;
import com.library.tui.TuiController;
import com.library.util.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        
        // 1. Initialize Repositories
        IUserRepository userRepository = new UserRepositoryMySQLImpl();
        IBookTitleRepository bookTitleRepository = new BookTitleRepositoryMySQLImpl();
        IBookCopyRepository bookCopyRepository = new BookCopyRepositoryMySQLImpl();
        ILoanTransactionRepository loanTransactionRepository = new LoanTransactionRepositoryMySQLImpl();
        ICategoryRepository categoryRepository = new CategoryRepositoryMySQLImpl();
        ILibraryConfigRepository configRepository = new LibraryConfigRepositoryMySQLImpl();

        // 2. Fetch Config
        LibraryConfig config = configRepository.getConfig();

        // 3. Seed Dummy Data directly in Main
        seedIfEmpty(userRepository, categoryRepository, bookTitleRepository, bookCopyRepository, configRepository);
        config = configRepository.getConfig(); // reload in case it was null before seeding

        // 4. Initialize Services
        AuthService authService = new AuthService(userRepository);
        BookService bookService = new BookService(bookTitleRepository, bookCopyRepository);
        CategoryService categoryService = new CategoryService(categoryRepository);
        UserService userService = new UserService(userRepository);
        LoanService loanService = new LoanService(loanTransactionRepository, bookCopyRepository, config);
        ConfigService configService = new ConfigService(configRepository);
        ReportService reportService = new ReportService(userRepository, bookTitleRepository, bookCopyRepository, categoryRepository, loanTransactionRepository);

        // 5. Initialize TUI Controller and start loop
        TuiController tuiController = new TuiController(authService, bookService, loanService, userService, categoryService, reportService, configService);
        
        tuiController.start();
    }

    private static void seedIfEmpty(IUserRepository userRepository, ICategoryRepository categoryRepository, IBookTitleRepository bookTitleRepository, IBookCopyRepository bookCopyRepository, ILibraryConfigRepository configRepository) {
        System.out.println("[Seeder] Checking missing base data (Config & Default Users)...");

        // 1. Seed Config
        if (configRepository.getConfig().getLibraryName() == null) {
            LibraryConfig config = new LibraryConfig();
            config.setLibraryName("Central Java Library");
            config.setFinePerDay(5000.0);
            config.setMaxBorrowDays(14);
            config.setMaxBorrowLimit(3);
            config.setMaxReservationDaysAhead(7);
            config.setPickupWindowDays(2);
            configRepository.update(config);
        }

        // 2. Seed Admin User
        if (userRepository.findByEmail("admin@library.com") == null) {
            Admin admin = new Admin("Super Admin", "admin@library.com", PasswordHasher.hashPassword("123456"));
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            admin.setCreatedBy("SYSTEM");
            userRepository.save(admin);
        }

        // 3. Seed Demo Member
        if (userRepository.findByEmail("member@library.com") == null) {
            Member member = new Member("Demo Member", "member@library.com", PasswordHasher.hashPassword("123456"), "MEM-001", "123 Library St", "555-1234");
            member.setRole(UserRole.MEMBER);
            member.setActive(true);
            member.setStatus(MemberStatus.ACTIVE);
            member.setCreatedBy("SYSTEM");
            userRepository.save(member);
        }

        // Only seed books if there are no books
        if (!bookTitleRepository.findAll().isEmpty()) {
            return;
        }

        System.out.println("[Seeder] Database books are empty. Seeding 100+ Books...");

        // 4. Seed Categories
        String[] catNames = {"Fiction", "Science & Technology", "History", "Fantasy & Sci-Fi", "Biography"};
        List<Category> categories = new ArrayList<>();
        for (String cName : catNames) {
            Category c = new Category(cName, "Books related to " + cName);
            c.setCreatedBy("admin@library.com");
            categoryRepository.save(c);
            categories.add(c);
        }

        // 5. Seed 100 Books
        String[] adjectives = {"The Great", "Introduction to", "Advanced", "History of", "The Secret", "Mastering", "Elements of", "Journey to", "Visions of", "The Lost"};
        String[] nouns = {"Java", "Quantum Mechanics", "World War II", "Dragons", "Space", "Algorithms", "Chemistry", "Empires", "Design Patterns", "Data Structures"};
        String[] authors = {"John Doe", "Jane Smith", "Alan Turing", "Isaac Asimov", "George R.R. Martin", "J.K. Rowling", "Robert C. Martin", "Joshua Bloch", "Stephen Hawking", "Carl Sagan"};

        int bookCount = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                String title = adjectives[i] + " " + nouns[j];
                String author = authors[(i + j) % authors.length];
                Category category = categories.get((i + j) % categories.size());
                String isbn = "978-" + (1000000000L + bookCount);
                String desc = "This is an automatically generated description for " + title + ". It provides deep insights into the topic by " + author + ".";
                
                BookTitle book = new BookTitle(title, author, "Seeder Press", isbn, desc, category);
                bookTitleRepository.save(book);
                
                // Add 3 physical copies for each book
                for (int k = 1; k <= 3; k++) {
                    BookCopy copy = new BookCopy(book, "Condition: Good, Copy #" + k);
                    copy.setStatus(BookCopyStatus.AVAILABLE);
                    bookCopyRepository.save(copy);
                }
                bookCount++;
            }
        }
        System.out.println("[Seeder] Successfully seeded " + bookCount + " titles with 3 copies each.");
    }
}
