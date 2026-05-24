/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.service;

import com.library.domain.entities.DashboardStats;
import com.library.domain.entities.LoanTransaction;
import com.library.domain.entities.User;
import com.library.domain.enums.LoanStatus;
import com.library.domain.enums.UserRole;
import com.library.repository.IBookCopyRepository;
import com.library.repository.IBookTitleRepository;
import com.library.repository.ICategoryRepository;
import com.library.repository.ILoanTransactionRepository;
import com.library.repository.IUserRepository;

import java.util.List;

/**
 *
 * 
 * @author rafianandra
 */
public class ReportService {

    // Atribut untuk menyimpan referensi ke berbagai repository yang dibutuhkan
    private final IUserRepository userRepository;
    private final IBookTitleRepository bookTitleRepository;
    private final IBookCopyRepository bookCopyRepository;
    private final ICategoryRepository categoryRepository;
    private final ILoanTransactionRepository loanTransactionRepository;

    // Konstruktor serta untuk memasukan dependency repository
    public ReportService(IUserRepository userRepository, 
                         IBookTitleRepository bookTitleRepository, 
                         IBookCopyRepository bookCopyRepository, 
                         ICategoryRepository categoryRepository, 
                         ILoanTransactionRepository loanTransactionRepository) {
        this.userRepository = userRepository;
        this.bookTitleRepository = bookTitleRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.categoryRepository = categoryRepository;
        this.loanTransactionRepository = loanTransactionRepository;
    }

    // Method untuk mengumpulkan semua metrik utama dan menghasilkan statistik dashboard
    public DashboardStats generateDashboardStats(User actor) {
        checkPermission(actor);

        DashboardStats stats = new DashboardStats();

        // Menghitung statistik pengguna berdasarkan peran
        int activeMembers = 0;
        int totalAdmins = 0;
        int totalLibrarians = 0;

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user.getRole() == UserRole.MEMBER && user.isActive()) {
                activeMembers++;
            } else if (user.getRole() == UserRole.ADMIN) {
                totalAdmins++;
            } else if (user.getRole() == UserRole.LIBRARIAN) {
                totalLibrarians++;
            }
        }
        stats.setTotalActiveMembers(activeMembers);
        stats.setTotalAdmins(totalAdmins);
        stats.setTotalLibrarians(totalLibrarians);
        stats.setTotalBookTitles(bookTitleRepository.findAll().size());
        stats.setTotalBookCopies(bookCopyRepository.findAll().size());
        stats.setTotalCategories(categoryRepository.findAll().size());

        double totalFines = 0.0;
        List<LoanTransaction> allLoans = loanTransactionRepository.findAll();
        for (LoanTransaction txn : allLoans) {
            if (txn.getStatus() == LoanStatus.RETURNED) {
                if (txn.getFineAmount() != null && txn.getFineAmount() > 0) {
                    totalFines += txn.getFineAmount();
                }
            }
        }
        stats.setTotalFineCollected(totalFines);

        return stats;
    }

    // Method untuk memeriksa izin akses berdasarkan peran pengguna untuk melihat statistik
    private void checkPermission(User actor) {
        if (actor.getPermissions().contains("VIEW_STATS") == false) {
            throw new SecurityException("Akses ditolak: Anda tidak memiliki izin untuk melihat statistik.");
        }
    }
}