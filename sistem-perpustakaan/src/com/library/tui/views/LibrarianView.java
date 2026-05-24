package com.library.tui.views;

import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.entities.LoanTransaction;
import com.library.domain.entities.User;
import com.library.domain.entities.BookCopy;
import com.library.domain.entities.Member;
import com.library.domain.enums.LoanStatus;
import com.library.service.BookService;
import com.library.service.CategoryService;
import com.library.service.LoanService;
import com.library.tui.util.TuiUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class LibrarianView {

    private final User librarian;
    private final BookService bookService;
    private final LoanService loanService;
    private final CategoryService categoryService;

    public LibrarianView(User librarian, BookService bookService, LoanService loanService, CategoryService categoryService) {
        this.librarian = librarian;
        this.bookService = bookService;
        this.loanService = loanService;
        this.categoryService = categoryService;
    }

    public void showMenu() {
        while (true) {
            TuiUtils.printHeader(librarian.getDashboardTitle());
            System.out.println(TuiUtils.GREEN + "  Welcome, " + librarian.getName() + " (Librarian)" + TuiUtils.RESET);
            System.out.println();
            
            String[] options = {
                "1. Manage Books (Interactive Catalog)",
                "2. Manage Categories",
                "3. Manage Loans (Confirm, Return, Fine)",
                "0. Logout"
            };
            TuiUtils.printBoxMenu(options);
            System.out.println();

            int choice = TuiUtils.readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    interactiveBooks();
                    break;
                case 2:
                    interactiveCategories();
                    break;
                case 3:
                    interactiveLoans();
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

    private void interactiveBooks() {
        String keyword = "";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("MANAGE BOOKS");
            
            List<BookTitle> books = bookService.searchCatalog(keyword);
            int totalBooks = books.size();
            int totalPages = (int) Math.ceil((double) totalBooks / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;
            if (currentPage < 0) currentPage = 0;

            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, totalBooks);
            
            if (keyword.isEmpty()) {
                System.out.println(TuiUtils.YELLOW + "Showing all books (Page " + (currentPage + 1) + " of " + totalPages + ")" + TuiUtils.RESET);
            } else {
                System.out.println(TuiUtils.YELLOW + "Search results for '" + keyword + "' (Page " + (currentPage + 1) + " of " + totalPages + ")" + TuiUtils.RESET);
            }
            
            if (books.isEmpty()) {
                TuiUtils.printInfo("No books found.");
            } else {
                String[] headers = {"ID", "Title", "Author", "Category", "Available"};
                String[][] data = new String[end - start][5];
                
                for (int i = start; i < end; i++) {
                    BookTitle book = books.get(i);
                    int available = bookService.getAvailableStock(book.getId());
                    String title = book.getTitle().length() > 30 ? book.getTitle().substring(0, 27) + "..." : book.getTitle();
                    
                    data[i - start][0] = String.valueOf(book.getId());
                    data[i - start][1] = title;
                    data[i - start][2] = book.getAuthor();
                    data[i - start][3] = book.getCategory().getName();
                    data[i - start][4] = String.valueOf(available);
                }
                TuiUtils.printTable(headers, data);
            }
            
            System.out.println();
            System.out.println(TuiUtils.CYAN + "Commands: [N]ext | [P]rev | [S]earch | [A]dd | [U]pdate | [D]elete | [C]opies | [Q]uit" + TuiUtils.RESET);
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
                        keyword = TuiUtils.readString("Enter keyword (leave empty for all): ");
                        currentPage = 0;
                        break;
                    case "A":
                        String title = TuiUtils.readString("Title: ");
                        String author = TuiUtils.readString("Author: ");
                        String publisher = TuiUtils.readString("Publisher: ");
                        String isbn = TuiUtils.readString("ISBN: ");
                        String desc = TuiUtils.readString("Desc: ");
                        int catId = TuiUtils.readInt("Category ID: ");
                        Category cat = categoryService.getAllActiveCategories().stream().filter(c -> c.getId() == catId).findFirst().orElse(null);
                        if (cat == null) throw new IllegalArgumentException("Invalid Category ID");
                        bookService.addBookTitle(librarian, title, author, publisher, isbn, desc, cat);
                        TuiUtils.printSuccess("Book Title Added!");
                        TuiUtils.waitForEnter();
                        break;
                    case "U":
                        int uid = TuiUtils.readInt("Enter Book ID to Update: ");
                        String utitle = TuiUtils.readString("New Title: ");
                        String uauthor = TuiUtils.readString("New Author: ");
                        String upublisher = TuiUtils.readString("New Publisher: ");
                        String uisbn = TuiUtils.readString("New ISBN: ");
                        String udesc = TuiUtils.readString("New Desc: ");
                        int ucatId = TuiUtils.readInt("New Category ID: ");
                        Category ucat = categoryService.getAllActiveCategories().stream().filter(c -> c.getId() == ucatId).findFirst().orElse(null);
                        if (ucat == null) throw new IllegalArgumentException("Invalid Category ID");
                        bookService.updateBookTitle(librarian, uid, utitle, uauthor, upublisher, uisbn, udesc, ucat);
                        TuiUtils.printSuccess("Book Title Updated!");
                        TuiUtils.waitForEnter();
                        break;
                    case "D":
                        int did = TuiUtils.readInt("Enter Book ID to Delete: ");
                        bookService.deleteBookTitle(librarian, did);
                        TuiUtils.printSuccess("Book Title Deleted!");
                        TuiUtils.waitForEnter();
                        break;
                    case "C":
                        int copyTitleId = TuiUtils.readInt("Enter Book ID to Add Copies to: ");
                        int numCopies = TuiUtils.readInt("Number of copies to add: ");
                        String location = TuiUtils.readString("Location (e.g. Shelf A1): ");
                        bookService.addBookCopies(librarian, copyTitleId, numCopies, location);
                        TuiUtils.printSuccess(numCopies + " copies added to book " + copyTitleId);
                        TuiUtils.waitForEnter();
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

    private void interactiveCategories() {
        String keyword = "";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("MANAGE CATEGORIES");
            
            List<Category> allCats = categoryService.getAllActiveCategories();
            final String currentKeyword = keyword;
            List<Category> filtered = currentKeyword.isEmpty() ? allCats : allCats.stream()
                .filter(c -> c.getName().toLowerCase().contains(currentKeyword.toLowerCase()))
                .collect(Collectors.toList());
                
            int totalItems = filtered.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;
            if (currentPage < 0) currentPage = 0;

            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            
            if (keyword.isEmpty()) {
                System.out.println(TuiUtils.YELLOW + "Showing active categories (Page " + (currentPage + 1) + " of " + totalPages + ")" + TuiUtils.RESET);
            } else {
                System.out.println(TuiUtils.YELLOW + "Search results for '" + keyword + "' (Page " + (currentPage + 1) + " of " + totalPages + ")" + TuiUtils.RESET);
            }
            
            if (filtered.isEmpty()) {
                TuiUtils.printInfo("No categories found.");
            } else {
                String[] headers = {"ID", "Name", "Description"};
                String[][] data = new String[end - start][3];
                
                for (int i = start; i < end; i++) {
                    Category cat = filtered.get(i);
                    String desc = cat.getDescription() != null && cat.getDescription().length() > 30 ? cat.getDescription().substring(0, 27) + "..." : cat.getDescription();
                    
                    data[i - start][0] = String.valueOf(cat.getId());
                    data[i - start][1] = cat.getName();
                    data[i - start][2] = desc;
                }
                TuiUtils.printTable(headers, data);
            }
            
            System.out.println();
            System.out.println(TuiUtils.CYAN + "Commands: [N]ext | [P]rev | [S]earch | [A]dd | [U]pdate | [D]elete | [R]eactivate | [Q]uit" + TuiUtils.RESET);
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
                        keyword = TuiUtils.readString("Enter keyword (leave empty for all): ");
                        currentPage = 0;
                        break;
                    case "A":
                        String name = TuiUtils.readString("Name: ");
                        String desc = TuiUtils.readString("Desc: ");
                        categoryService.createCategory(librarian, name, desc);
                        TuiUtils.printSuccess("Category created.");
                        TuiUtils.waitForEnter();
                        break;
                    case "U":
                        int id = TuiUtils.readInt("Category ID: ");
                        String newName = TuiUtils.readString("New Name: ");
                        String newDesc = TuiUtils.readString("New Desc: ");
                        categoryService.updateCategory(librarian, id, newName, newDesc);
                        TuiUtils.printSuccess("Category updated.");
                        TuiUtils.waitForEnter();
                        break;
                    case "D":
                        int delId = TuiUtils.readInt("Category ID to delete: ");
                        categoryService.deleteCategory(librarian, delId);
                        TuiUtils.printSuccess("Category deleted.");
                        TuiUtils.waitForEnter();
                        break;
                    case "R":
                        int reactId = TuiUtils.readInt("Category ID to reactivate (need exact ID): ");
                        categoryService.reactivateCategory(librarian, reactId);
                        TuiUtils.printSuccess("Category reactivated.");
                        TuiUtils.waitForEnter();
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

    private void interactiveLoans() {
        String keyword = "";
        String statusFilter = "ALL";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("MANAGE LOANS");
            
            List<LoanTransaction> allLoans = loanService.getAllLoans();
            final String currentStatusFilter = statusFilter;
            final String currentKeyword = keyword;
            List<LoanTransaction> filtered = allLoans.stream()
                .filter(l -> currentStatusFilter.equals("ALL") || l.getStatus().name().equals(currentStatusFilter))
                .filter(l -> currentKeyword.isEmpty() 
                    || l.getMember().getName().toLowerCase().contains(currentKeyword.toLowerCase())
                    || l.getBookCopy().getBookTitle().getTitle().toLowerCase().contains(currentKeyword.toLowerCase())
                    || String.valueOf(l.getId()).equals(currentKeyword))
                .collect(Collectors.toList());
                
            int totalItems = filtered.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;
            if (currentPage < 0) currentPage = 0;

            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            
            System.out.println(TuiUtils.YELLOW + "Filter: " + statusFilter + " | Search: '" + keyword + "' (Page " + (currentPage + 1) + " of " + totalPages + ")" + TuiUtils.RESET);
            
            if (filtered.isEmpty()) {
                TuiUtils.printInfo("No loans found.");
            } else {
                String[] headers = {"Txn ID", "Member", "Book Title", "Status", "Due Date", "Fine"};
                String[][] data = new String[end - start][6];
                
                for (int i = start; i < end; i++) {
                    LoanTransaction txn = filtered.get(i);
                    String title = txn.getBookCopy().getBookTitle().getTitle();
                    title = title.length() > 15 ? title.substring(0, 12) + "..." : title;
                    String memberName = txn.getMember().getName();
                    memberName = memberName.length() > 10 ? memberName.substring(0, 7) + "..." : memberName;
                    String fine = String.valueOf(loanService.calculateCurrentFine(txn));
                    
                    data[i - start][0] = String.valueOf(txn.getId());
                    data[i - start][1] = memberName;
                    data[i - start][2] = title;
                    data[i - start][3] = txn.getStatus().name();
                    data[i - start][4] = txn.getDueDate() != null ? txn.getDueDate().toString() : "-";
                    data[i - start][5] = fine;
                }
                TuiUtils.printTable(headers, data);
            }
            
            System.out.println();
            System.out.println(TuiUtils.CYAN + "Commands: [N]ext | [P]rev | [S]earch | [F]ilter | [O]ffline Borrow | [C]onfirm | [R]eturn | P[A]y Fine | [Q]uit" + TuiUtils.RESET);
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
                        keyword = TuiUtils.readString("Enter keyword (Member name, Title, Txn ID): ");
                        currentPage = 0;
                        break;
                    case "F":
                        String[] statuses = {"ALL", "WAITING_PICKUP", "ACTIVE", "OVERDUE", "RETURNED"};
                        System.out.println("Available statuses: ALL, WAITING_PICKUP, ACTIVE, OVERDUE, RETURNED");
                        String f = TuiUtils.readString("Enter status: ").toUpperCase();
                        statusFilter = f;
                        currentPage = 0;
                        break;
                    case "O":
                        TuiUtils.printInfo("Cannot safely execute Offline Borrow without fetching Member objects directly.");
                        TuiUtils.printInfo("Use raw IDs to simulate or implement a separate member selection UI.");
                        TuiUtils.waitForEnter();
                        break;
                    case "C":
                        int confirmId = TuiUtils.readInt("Enter Txn ID to confirm pickup: ");
                        loanService.confirmPickup(confirmId, (com.library.domain.entities.Librarian) librarian);
                        TuiUtils.printSuccess("Pickup confirmed! Loan is now ACTIVE.");
                        TuiUtils.waitForEnter();
                        break;
                    case "R":
                        int returnId = TuiUtils.readInt("Enter Txn ID to return: ");
                        loanService.processReturn(returnId, (com.library.domain.entities.Librarian) librarian);
                        TuiUtils.printSuccess("Book returned successfully. Fines (if any) calculated.");
                        TuiUtils.waitForEnter();
                        break;
                    case "A":
                        int fineId = TuiUtils.readInt("Enter Txn ID to pay fine: ");
                        loanService.processFinePayment(fineId, (com.library.domain.entities.Librarian) librarian);
                        TuiUtils.printSuccess("Fine marked as PAID.");
                        TuiUtils.waitForEnter();
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
}
