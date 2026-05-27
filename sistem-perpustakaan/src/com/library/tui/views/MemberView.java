package com.library.tui.views;

import com.library.domain.entities.BookCopy;
import com.library.domain.entities.BookTitle;
import com.library.domain.entities.LoanTransaction;
import com.library.domain.entities.Member;
import com.library.domain.entities.User;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.tui.util.TuiUtils;

import java.time.LocalDate;
import java.util.List;

public class MemberView {

    private final Member member;
    private final BookService bookService;
    private final LoanService loanService;

    public MemberView(User user, BookService bookService, LoanService loanService) {
        this.member = (Member) user;
        this.bookService = bookService;
        this.loanService = loanService;
    }

    public void showMenu() {
        while (true) {
            TuiUtils.printHeader(member.getDashboardTitle());
            System.out.println(TuiUtils.GREEN + "  Selamat datang kembali, " + member.getName() + "!" + TuiUtils.RESET);
            System.out.println();
            
            String[] options = {
                "1. Katalog Interaktif (Cari & Pinjam)",
                "2. Pinjaman Aktif Saya (Interaktif)",
                "3. Riwayat Pinjaman Saya (Interaktif)",
                "0. Keluar (Logout)"
            };
            TuiUtils.printBoxMenu(options);
            System.out.println();

            int choice = TuiUtils.readInt("Masukkan pilihan: ");

            switch (choice) {
                case 1:
                    interactiveCatalog();
                    break;
                case 2:
                    interactiveLoans();
                    break;
                case 3:
                    interactiveLoanHistory();
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

    private void interactiveCatalog() {
        String keyword = "";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("KATALOG INTERAKTIF");
            
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
            System.out.println(TuiUtils.CYAN + "Perintah: [N]ext Page | [P]rev Page | [S]earch | [V]iew Detail | [B]orrow | [Q]uit" + TuiUtils.RESET);
            String cmd = TuiUtils.readString("Masukkan perintah: ").trim().toUpperCase();
            
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
                case "V":
                    int viewId = TuiUtils.readInt("Masukkan ID Buku untuk lihat detail: ");
                    viewBookDetail(books, viewId);
                    break;
                case "B":
                    int borrowId = TuiUtils.readInt("Masukkan ID Buku untuk dipinjam: ");
                    processBorrowOnline(books, borrowId);
                    break;
                case "Q":
                    return;
                default:
                    TuiUtils.printError("Perintah tidak valid.");
                    TuiUtils.waitForEnter();
            }
        }
    }

    private void viewBookDetail(List<BookTitle> currentList, int viewId) {
        BookTitle selected = currentList.stream().filter(b -> b.getId() == viewId).findFirst().orElse(null);
        if (selected != null) {
            TuiUtils.clearScreen();
            String[] details = {
                "Penulis: " + selected.getAuthor(),
                "ISBN: " + selected.getIsbn(),
                "Penerbit: " + selected.getPublisher(),
                "Kategori: " + selected.getCategory().getName(),
                "Stok Tersedia: " + bookService.getAvailableStock(selected.getId()),
                "",
                "Deskripsi:",
                selected.getDescription()
            };
            TuiUtils.printBoxDetail("BUKU: " + selected.getTitle(), details);
        } else {
            TuiUtils.printError("ID Buku tidak ditemukan dalam daftar pencarian.");
        }
        TuiUtils.waitForEnter();
    }

    private void processBorrowOnline(List<BookTitle> currentList, int titleId) {
        BookTitle selected = currentList.stream().filter(b -> b.getId() == titleId).findFirst().orElse(null);
        if (selected == null) {
            TuiUtils.printError("ID Buku tidak ditemukan dalam daftar pencarian.");
            TuiUtils.waitForEnter();
            return;
        }

        try {
            int stock = bookService.getAvailableStock(titleId);
            if (stock <= 0) {
                TuiUtils.printError("Maaf, tidak ada eksemplar tersedia untuk buku ini.");
            } else {
                BookCopy copy = bookService.findAvailableCopyByTitleId(titleId);
                if (copy == null) {
                    TuiUtils.printError("Error: Eksemplar fisik tidak ditemukan meskipun stok > 0.");
                    TuiUtils.waitForEnter();
                    return;
                }

                LocalDate pickupDate = TuiUtils.readDate("Masukkan tanggal pengambilan");
                loanService.requestOnlineLoan(member, copy, pickupDate);
                TuiUtils.printSuccess("Buku berhasil direservasi! Silakan ambil pada " + pickupDate);
            }
        } catch (Exception e) {
            TuiUtils.printError(e.getMessage());
        }
        TuiUtils.waitForEnter();
    }

    private void interactiveLoans() {
        String keyword = "";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("PINJAMAN AKTIF SAYA");
            
            try {
                loanService.processScheduledPickups();
                loanService.processExpiredPickups();
                loanService.processOverdueLoans();
            } catch (Exception e) {}
            
            List<LoanTransaction> allLoans = loanService.getMemberLoans(member);
            final String currentKeyword = keyword;
            List<LoanTransaction> filtered = allLoans.stream()
                .filter(l -> l.getStatus() != com.library.domain.enums.LoanStatus.RETURNED)
                .filter(l -> currentKeyword.isEmpty() 
                    || l.getBookCopy().getBookTitle().getTitle().toLowerCase().contains(currentKeyword.toLowerCase())
                    || l.getStatus().name().toLowerCase().contains(currentKeyword.toLowerCase())
                    || String.valueOf(l.getId()).equals(currentKeyword))
                .collect(java.util.stream.Collectors.toList());
                
            int totalItems = filtered.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;
            if (currentPage < 0) currentPage = 0;

            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            
            if (keyword.isEmpty()) {
                System.out.println(TuiUtils.YELLOW + "Menampilkan pinjaman aktif (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            } else {
                System.out.println(TuiUtils.YELLOW + "Hasil pencarian untuk '" + keyword + "' (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            }
            
            if (filtered.isEmpty()) {
                TuiUtils.printInfo("Tidak ada pinjaman aktif.");
            } else {
                String[] headers = {"ID Txn", "Judul Buku", "Status", "Tgl Ambil", "Tenggat", "Denda"};
                String[][] data = new String[end - start][6];
                
                for (int i = start; i < end; i++) {
                    LoanTransaction txn = filtered.get(i);
                    String title = txn.getBookCopy().getBookTitle().getTitle();
                    title = title.length() > 15 ? title.substring(0, 12) + "..." : title;
                    Double currentFine = loanService.calculateCurrentFine(txn);
                    String fine = currentFine > 0 ? "Rp" + currentFine : "0.0";
                    
                    data[i - start][0] = String.valueOf(txn.getId());
                    data[i - start][1] = title;
                    data[i - start][2] = txn.getStatus().name();
                    data[i - start][3] = txn.getScheduledPickupDate() != null ? txn.getScheduledPickupDate().toString() : "-";
                    data[i - start][4] = txn.getDueDate() != null ? txn.getDueDate().toString() : "-";
                    data[i - start][5] = fine;
                }
                TuiUtils.printTable(headers, data);
            }
            
            System.out.println();
            System.out.println(TuiUtils.CYAN + "Perintah: [N]ext | [P]rev | [S]earch | [C]ancel Pending | [Q]uit" + TuiUtils.RESET);
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
                        keyword = TuiUtils.readString("Masukkan kata kunci (Judul, Status, ID): ");
                        currentPage = 0;
                        break;
                    case "C":
                        int txnId = TuiUtils.readInt("Masukkan ID Txn untuk dibatalkan: ");
                        loanService.cancelLoan(txnId, member);
                        TuiUtils.printSuccess("Pinjaman berhasil dibatalkan.");
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

    private void interactiveLoanHistory() {
        String keyword = "";
        int currentPage = 0;
        int pageSize = 10;

        while (true) {
            TuiUtils.clearScreen();
            TuiUtils.printHeader("RIWAYAT PINJAMAN SAYA");
            
            try {
                loanService.processScheduledPickups();
                loanService.processExpiredPickups();
                loanService.processOverdueLoans();
            } catch (Exception e) {}
            
            List<LoanTransaction> allLoans = loanService.getAllMemberLoans(member);
            final String currentKeyword = keyword;
            List<LoanTransaction> filtered = allLoans.stream()
                .filter(l -> l.getStatus() == com.library.domain.enums.LoanStatus.RETURNED ||
                             l.getStatus() == com.library.domain.enums.LoanStatus.EXPIRED ||
                             l.getStatus() == com.library.domain.enums.LoanStatus.CANCELLED)
                .filter(l -> currentKeyword.isEmpty() 
                    || l.getBookCopy().getBookTitle().getTitle().toLowerCase().contains(currentKeyword.toLowerCase())
                    || String.valueOf(l.getId()).equals(currentKeyword))
                .collect(java.util.stream.Collectors.toList());
                
            int totalItems = filtered.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            if (totalPages == 0) totalPages = 1;
            if (currentPage >= totalPages) currentPage = totalPages - 1;
            if (currentPage < 0) currentPage = 0;

            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            
            if (keyword.isEmpty()) {
                System.out.println(TuiUtils.YELLOW + "Menampilkan semua pinjaman (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            } else {
                System.out.println(TuiUtils.YELLOW + "Hasil pencarian untuk '" + keyword + "' (Halaman " + (currentPage + 1) + " dari " + totalPages + ")" + TuiUtils.RESET);
            }
            
            if (filtered.isEmpty()) {
                TuiUtils.printInfo("Riwayat pinjaman tidak ditemukan.");
            } else {
                String[] headers = {"ID Txn", "Judul Buku", "Tgl Pinjam", "Tgl Kembali", "Denda"};
                String[][] data = new String[end - start][5];
                
                for (int i = start; i < end; i++) {
                    LoanTransaction txn = filtered.get(i);
                    String title = txn.getBookCopy().getBookTitle().getTitle();
                    title = title.length() > 15 ? title.substring(0, 12) + "..." : title;
                    String returnDate = txn.getReturnDate() != null ? txn.getReturnDate().toString() : "-";
                    Double currentFine = loanService.calculateCurrentFine(txn);
                    String fine = currentFine > 0 ? "Rp" + currentFine : "0.0";
                    
                    data[i - start][0] = String.valueOf(txn.getId());
                    data[i - start][1] = title;
                    data[i - start][2] = txn.getRequestDate() != null ? txn.getRequestDate().toLocalDate().toString() : "-";
                    data[i - start][3] = returnDate;
                    data[i - start][4] = fine;
                }
                TuiUtils.printTable(headers, data);
            }
            
            System.out.println();
            System.out.println(TuiUtils.CYAN + "Perintah: [N]ext | [P]rev | [S]earch | [Q]uit" + TuiUtils.RESET);
            String cmd = TuiUtils.readString("Masukkan perintah: ").trim().toUpperCase();
            
            switch (cmd) {
                case "N":
                    if (currentPage < totalPages - 1) currentPage++;
                    break;
                case "P":
                    if (currentPage > 0) currentPage--;
                    break;
                case "S":
                    keyword = TuiUtils.readString("Masukkan kata kunci (Judul atau ID): ");
                    currentPage = 0;
                    break;
                case "Q":
                    return;
                default:
                    TuiUtils.printError("Perintah tidak valid.");
                    TuiUtils.waitForEnter();
            }
        }
    }
}
