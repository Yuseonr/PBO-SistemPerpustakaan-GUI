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
            System.out.println(TuiUtils.GREEN + "  Selamat datang, " + librarian.getName() + " (Pustakawan)" + TuiUtils.RESET);
            System.out.println();
            
            String[] options = {
                "1. Kelola Buku (Katalog Interaktif)",
                "2. Kelola Kategori",
                "3. Kelola Pinjaman (Konfirmasi, Kembali, Denda)",
                "0. Keluar (Logout)"
            };
            TuiUtils.printBoxMenu(options);
            System.out.println();

            int choice = TuiUtils.readInt("Masukkan pilihan: ");

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
                    TuiUtils.printInfo("Keluar...");
                    return; 
                default:
                    TuiUtils.printError("Pilihan tidak valid.");
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
            TuiUtils.printHeader("KELOLA BUKU");
            
            List<BookTitle> books = bookService.searchCatalog(keyword);
            int totalBooks = books.size();
            int totalPages = (int) Math.ceil((double) totalBooks / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;
            if (currentPage < 0) currentPage = 0;

            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, totalBooks);
            
            if (keyword.isEmpty()) {
                System.out.println(TuiUtils.YELLOW + "Menampilkan semua buku (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            } else {
                System.out.println(TuiUtils.YELLOW + "Hasil pencarian untuk '" + keyword + "' (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            }
            
            if (books.isEmpty()) {
                TuiUtils.printInfo("Buku tidak ditemukan.");
            } else {
                String[] headers = {"ID", "Judul", "Penulis", "Kategori", "Tersedia"};
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
            System.out.println(TuiUtils.CYAN + "Perintah: [N]ext | [P]rev | [S]earch | [A]dd | [U]pdate | [D]elete | [C]opies | [Q]uit" + TuiUtils.RESET);
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
                        keyword = TuiUtils.readString("Masukkan kata kunci (kosongkan untuk semua): ");
                        currentPage = 0;
                        break;
                    case "A":
                        String title = TuiUtils.readString("Judul: ");
                        String author = TuiUtils.readString("Penulis: ");
                        String publisher = TuiUtils.readString("Penerbit: ");
                        String isbn = TuiUtils.readString("ISBN: ");
                        String desc = TuiUtils.readString("Deskripsi: ");
                        int catId = TuiUtils.readInt("ID Kategori: ");
                        Category cat = categoryService.getAllActiveCategories().stream().filter(c -> c.getId() == catId).findFirst().orElse(null);
                        if (cat == null) throw new IllegalArgumentException("Invalid Category ID");
                        bookService.addBookTitle(librarian, title, author, publisher, isbn, desc, cat);
                        TuiUtils.printSuccess("Judul Buku Ditambahkan!");
                        TuiUtils.waitForEnter();
                        break;
                    case "U":
                        int uid = TuiUtils.readInt("Masukkan ID Buku untuk Diperbarui: ");
                        String utitle = TuiUtils.readString("Judul Baru: ");
                        String uauthor = TuiUtils.readString("Penulis Baru: ");
                        String upublisher = TuiUtils.readString("Penerbit Baru: ");
                        String uisbn = TuiUtils.readString("ISBN Baru: ");
                        String udesc = TuiUtils.readString("Deskripsi Baru: ");
                        int ucatId = TuiUtils.readInt("ID Kategori Baru: ");
                        Category ucat = categoryService.getAllActiveCategories().stream().filter(c -> c.getId() == ucatId).findFirst().orElse(null);
                        if (ucat == null) throw new IllegalArgumentException("Invalid Category ID");
                        bookService.updateBookTitle(librarian, uid, utitle, uauthor, upublisher, uisbn, udesc, ucat);
                        TuiUtils.printSuccess("Judul Buku Diperbarui!");
                        TuiUtils.waitForEnter();
                        break;
                    case "D":
                        int did = TuiUtils.readInt("Masukkan ID Buku untuk Dihapus: ");
                        bookService.deleteBookTitle(librarian, did);
                        TuiUtils.printSuccess("Judul Buku Dihapus!");
                        TuiUtils.waitForEnter();
                        break;
                    case "C":
                        int copyTitleId = TuiUtils.readInt("Masukkan ID Buku untuk Tambah Eksemplar: ");
                        int numCopies = TuiUtils.readInt("Jumlah eksemplar untuk ditambah: ");
                        String location = TuiUtils.readString("Lokasi (contoh: Rak A1): ");
                        bookService.addBookCopies(librarian, copyTitleId, numCopies, location);
                        TuiUtils.printSuccess(numCopies + " eksemplar ditambahkan ke buku " + copyTitleId);
                        TuiUtils.waitForEnter();
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

    private void interactiveCategories() {
        String keyword = "";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("KELOLA KATEGORI");
            
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
                System.out.println(TuiUtils.YELLOW + "Menampilkan kategori aktif (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            } else {
                System.out.println(TuiUtils.YELLOW + "Hasil pencarian untuk '" + keyword + "' (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            }
            
            if (filtered.isEmpty()) {
                TuiUtils.printInfo("Kategori tidak ditemukan.");
            } else {
                String[] headers = {"ID", "Nama", "Deskripsi"};
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
            System.out.println(TuiUtils.CYAN + "Perintah: [N]ext | [P]rev | [S]earch | [A]dd | [U]pdate | [D]elete | [R]eactivate | [Q]uit" + TuiUtils.RESET);
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
                        keyword = TuiUtils.readString("Masukkan kata kunci (kosongkan untuk semua): ");
                        currentPage = 0;
                        break;
                    case "A":
                        String name = TuiUtils.readString("Nama: ");
                        String desc = TuiUtils.readString("Deskripsi: ");
                        categoryService.createCategory(librarian, name, desc);
                        TuiUtils.printSuccess("Kategori dibuat.");
                        TuiUtils.waitForEnter();
                        break;
                    case "U":
                        int id = TuiUtils.readInt("ID Kategori: ");
                        String newName = TuiUtils.readString("Nama Baru: ");
                        String newDesc = TuiUtils.readString("Deskripsi Baru: ");
                        categoryService.updateCategory(librarian, id, newName, newDesc);
                        TuiUtils.printSuccess("Kategori diperbarui.");
                        TuiUtils.waitForEnter();
                        break;
                    case "D":
                        int delId = TuiUtils.readInt("ID Kategori untuk dihapus: ");
                        categoryService.deleteCategory(librarian, delId);
                        TuiUtils.printSuccess("Kategori dihapus.");
                        TuiUtils.waitForEnter();
                        break;
                    case "R":
                        int reactId = TuiUtils.readInt("ID Kategori untuk diaktifkan ulang (butuh ID pasti): ");
                        categoryService.reactivateCategory(librarian, reactId);
                        TuiUtils.printSuccess("Kategori diaktifkan ulang.");
                        TuiUtils.waitForEnter();
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

    private void interactiveLoans() {
        String keyword = "";
        String statusFilter = "ALL";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("KELOLA PINJAMAN");
            
            try {
                loanService.processScheduledPickups();
                loanService.processExpiredPickups();
                loanService.processOverdueLoans();
            } catch (Exception e) {}
            
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
            
            System.out.println(TuiUtils.YELLOW + "Filter: " + statusFilter + " | Pencarian: '" + keyword + "' (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            
            if (filtered.isEmpty()) {
                TuiUtils.printInfo("Pinjaman tidak ditemukan.");
            } else {
                String[] headers = {"ID Txn", "Member", "Judul Buku", "Status", "Tenggat", "Denda"};
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
            System.out.println(TuiUtils.CYAN + "Perintah: [N]ext | [P]rev | [S]earch | [F]ilter | [O]ffline Borrow | [C]onfirm | [R]eturn | P[A]y Fine | [Q]uit" + TuiUtils.RESET);
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
                        keyword = TuiUtils.readString("Masukkan kata kunci (Nama Member, Judul, ID Txn): ");
                        currentPage = 0;
                        break;
                    case "F":
                        System.out.println("Status tersedia: ALL, WAITING_PICKUP, ACTIVE, OVERDUE, RETURNED");
                        String f = TuiUtils.readString("Masukkan status: ").toUpperCase();
                        statusFilter = f;
                        currentPage = 0;
                        break;
                    case "O":
                        TuiUtils.printInfo("Tidak dapat melakukan Offline Borrow tanpa objek Member langsung.");
                        TuiUtils.printInfo("Gunakan ID mentah untuk simulasi atau buat UI pemilihan member terpisah.");
                        TuiUtils.waitForEnter();
                        break;
                    case "C":
                        int confirmId = TuiUtils.readInt("Masukkan ID Txn untuk konfirmasi ambil: ");
                        loanService.confirmPickup(confirmId, (com.library.domain.entities.Librarian) librarian);
                        TuiUtils.printSuccess("Ambil dikonfirmasi! Pinjaman sekarang ACTIVE.");
                        TuiUtils.waitForEnter();
                        break;
                    case "R":
                        int returnId = TuiUtils.readInt("Masukkan ID Txn untuk kembalikan: ");
                        loanService.processReturn(returnId, (com.library.domain.entities.Librarian) librarian);
                        TuiUtils.printSuccess("Buku berhasil dikembalikan. Denda (jika ada) telah dihitung.");
                        TuiUtils.waitForEnter();
                        break;
                    case "A":
                        int fineId = TuiUtils.readInt("Masukkan ID Txn untuk bayar denda: ");
                        loanService.processFinePayment(fineId, (com.library.domain.entities.Librarian) librarian);
                        TuiUtils.printSuccess("Denda ditandai sebagai LUNAS.");
                        TuiUtils.waitForEnter();
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
}
