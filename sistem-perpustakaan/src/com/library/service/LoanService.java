/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.service;

import com.library.domain.entities.BookCopy;
import com.library.domain.entities.Librarian;
import com.library.domain.entities.LibraryConfig;
import com.library.domain.entities.LoanTransaction;
import com.library.domain.entities.Member;
import com.library.domain.enums.BookCopyStatus;
import com.library.domain.enums.BorrowType;
import com.library.domain.enums.LoanStatus;
import com.library.repository.IBookCopyRepository;
import com.library.repository.ILoanTransactionRepository;
import com.library.util.FineCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author rafianandra
 */
public class LoanService {
    
    // Atribut untuk repository dan config yang dibutuhkan
    private final ILoanTransactionRepository loanRepo;
    private final IBookCopyRepository copyRepo;
    private final LibraryConfig config;

    // Konstruktor untuk inisialisasi repository dan config
    public LoanService(ILoanTransactionRepository loanRepo, IBookCopyRepository copyRepo, LibraryConfig config) {
        this.loanRepo = loanRepo;
        this.copyRepo = copyRepo;
        this.config = config;
    }
    
    // Member request dari online
    public LoanTransaction requestOnlineLoan(Member member, BookCopy copy, LocalDate pickupDate) {

        if (copy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new IllegalStateException("Buku fisik ini sedang tidak tersedia.");
        }

        List<LoanTransaction> activeLoans = loanRepo.findActiveLoansByMemberId(member.getId());
        if (activeLoans.size() >= config.getMaxBorrowLimit()) {
            throw new IllegalStateException("Batas maksimal pinjaman aktif tercapai ("
                    + config.getMaxBorrowLimit() + " buku).");
        }

        if (!pickupDate.isAfter(LocalDate.now()) ||
             pickupDate.isAfter(LocalDate.now().plusDays(config.getMaxReservationDaysAhead()))) {
            throw new IllegalArgumentException("Tanggal pengambilan harus minimal besok dan maksimal "
                    + config.getMaxReservationDaysAhead() + " hari ke depan.");
        }

        LoanTransaction txn = new LoanTransaction(member, copy, BorrowType.ONLINE, LoanStatus.REQUESTED);
        txn.setScheduledPickupDate(pickupDate);
        txn.setDueDate(pickupDate.plusDays(config.getMaxBorrowDays())); // Dihitung sekali di sini
        txn.setCreatedBy(member.getEmail());
        loanRepo.save(txn);

        copy.setStatus(BookCopyStatus.RESERVED);
        copyRepo.update(copy);

        return txn;
    }

    // Pustakawan buat pinjaman langsung di tempat
    public LoanTransaction createOfflineLoan(Member member, BookCopy copy, Librarian librarian) {

        if (copy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new IllegalStateException("Buku fisik ini sedang tidak tersedia.");
        }

        List<LoanTransaction> activeLoans = loanRepo.findActiveLoansByMemberId(member.getId());
        if (activeLoans.size() >= config.getMaxBorrowLimit()) {
            throw new IllegalStateException("Batas maksimal pinjaman aktif tercapai ("
                    + config.getMaxBorrowLimit() + " buku).");
        }

        LoanTransaction txn = new LoanTransaction(member, copy, BorrowType.OFFLINE, LoanStatus.ACTIVE);
        txn.setApprovedBy(librarian);
        txn.setScheduledPickupDate(LocalDate.now());
        txn.setDueDate(LocalDate.now().plusDays(config.getMaxBorrowDays()));
        txn.setCreatedBy(librarian.getEmail());
        loanRepo.save(txn);

        copy.setStatus(BookCopyStatus.LOANED);
        copyRepo.update(copy);

        return txn;
    }

    // Pustakawan konfirmasi bahwa member sudah mengambil buku yang dipesan
    public void confirmPickup(Integer transactionId, Librarian librarian) {
        LoanTransaction txn = loanRepo.findById(transactionId);

        if (txn == null || txn.getStatus() != LoanStatus.WAITING_PICKUP) {
            throw new IllegalStateException("Transaksi tidak valid atau tidak dalam status WAITING_PICKUP.");
        }

        txn.setStatus(LoanStatus.ACTIVE);
        txn.setApprovedBy(librarian);
        loanRepo.update(txn);

        BookCopy copy = txn.getBookCopy();
        copy.setStatus(BookCopyStatus.LOANED);
        copyRepo.update(copy);
    }

    // Proses pengembalian buku oleh member tapi dilakukan oleh pustakawan 
    public void processReturn(Integer transactionId, Librarian librarian) {
        LoanTransaction txn = loanRepo.findById(transactionId);

        if (txn == null || (txn.getStatus() != LoanStatus.ACTIVE && txn.getStatus() != LoanStatus.OVERDUE)) {
            throw new IllegalStateException("Transaksi tidak sedang aktif.");
        }

        txn.setReturnDate(LocalDate.now());
        Double fine = FineCalculator.calculateFine(
                txn.getDueDate(), txn.getReturnDate(), config.getFinePerDay());

        txn.setFineAmount(fine);
        txn.setFinePerDaySnapshot(config.getFinePerDay());
        txn.setFineCalculatedAt(LocalDateTime.now());
        txn.setStatus(LoanStatus.RETURNED);
        txn.setApprovedBy(librarian);
        loanRepo.update(txn);

        BookCopy copy = txn.getBookCopy();
        copy.setStatus(BookCopyStatus.AVAILABLE);
        copyRepo.update(copy);
    }

    // Pustakawan konfirmasi pembayaran denda
    public void processFinePayment(Integer transactionId, Librarian librarian) {
        if (librarian == null) {
            throw new IllegalArgumentException("Pustakawan tidak boleh kosong.");
        }

        LoanTransaction txn = loanRepo.findById(transactionId);

        if (txn == null) {
            throw new IllegalArgumentException("Transaksi tidak ditemukan.");
        }

        if (txn.getStatus() != LoanStatus.RETURNED) {
            throw new IllegalStateException("Pembayaran denda hanya bisa dilakukan setelah RETURNED.");
        }

        if (txn.getFineAmount() == null || txn.getFineAmount() <= 0) {
            throw new IllegalStateException("Transaksi ini tidak memiliki denda.");
        }

        if (txn.getFinePaidAt() != null) {
            throw new IllegalStateException("Denda sudah dibayar sebelumnya.");
        }

        txn.setFinePaidAt(LocalDateTime.now());
        txn.setFineProcessedBy(librarian);
        loanRepo.update(txn);
    }

    // Member membatalkan peminjaman yang sudah diajukan tapi belum diambil
    public void cancelLoan(Integer transactionId, Member member) {
        LoanTransaction txn = loanRepo.findById(transactionId);

        if (txn == null) {
            throw new IllegalArgumentException("Transaksi tidak ditemukan.");
        }

        if (!txn.getMember().getId().equals(member.getId())) {
            throw new IllegalStateException("Anda tidak berhak membatalkan transaksi ini.");
        }

        if (txn.getStatus() != LoanStatus.REQUESTED && txn.getStatus() != LoanStatus.WAITING_PICKUP) {
            throw new IllegalStateException("Transaksi tidak dapat dibatalkan pada status "
                    + txn.getStatus() + ".");
        }

        txn.setStatus(LoanStatus.CANCELLED);
        txn.setCancelledAt(LocalDateTime.now());
        loanRepo.update(txn);

        BookCopy copy = txn.getBookCopy();
        copy.setStatus(BookCopyStatus.AVAILABLE);
        copyRepo.update(copy);
    }

    // scheduler untuk memproses transaksi yang sudah waktunya dijemput hari ini
    public void processScheduledPickups() {
        List<LoanTransaction> list = loanRepo.findByStatusAndScheduledPickupDate(
                LoanStatus.REQUESTED, LocalDate.now());
        for (LoanTransaction txn : list) {
            txn.setStatus(LoanStatus.WAITING_PICKUP);
            loanRepo.update(txn);
        }
    }

    // scheduler untuk memproses transaksi yang sudah lewat waktu jemput tapi belum diambil
    public void processExpiredPickups() {
        LocalDate cutoff = LocalDate.now().minusDays(config.getPickupWindowDays());
        List<LoanTransaction> list = loanRepo.findExpiredWaitingPickup(cutoff);
        for (LoanTransaction txn : list) {
            txn.setStatus(LoanStatus.EXPIRED);
            txn.setCancelledAt(LocalDateTime.now());
            loanRepo.update(txn);

            BookCopy copy = txn.getBookCopy();
            copy.setStatus(BookCopyStatus.AVAILABLE);
            copyRepo.update(copy);
        }
    }

    // scheduler untuk memproses transaksi yang sudah lewat waktu pengembalian
    public void processOverdueLoans() {
        List<LoanTransaction> list = loanRepo.findOverdueActive(LocalDate.now());
        for (LoanTransaction txn : list) {
            txn.setStatus(LoanStatus.OVERDUE);
            loanRepo.update(txn);
        }
    }

    public List<LoanTransaction> getMemberLoans(Member member) {
        return loanRepo.findActiveLoansByMemberId(member.getId()); 
    }

    // Method untuk mendapatkan seluruh riwayat pinjaman member termasuk yang sudah selesai/batal
    public List<LoanTransaction> getAllMemberLoans(Member member) {
        return loanRepo.findByMemberId(member.getId());
    }

    public List<LoanTransaction> getAllLoans() {
        return loanRepo.findAll();
    }

    // Menghitung denda secara dinamis untuk tampilan TUI (sebelum buku benar-benar dikembalikan)
    public Double calculateCurrentFine(LoanTransaction txn) {
        if (txn.getStatus() == LoanStatus.OVERDUE && txn.getDueDate() != null) {
            return FineCalculator.calculateFine(txn.getDueDate(), LocalDate.now(), config.getFinePerDay());
        } else if (txn.getFineAmount() != null) {
            return txn.getFineAmount();
        }
        return 0.0;
    }
}