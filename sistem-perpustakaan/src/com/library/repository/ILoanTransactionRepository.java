/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.repository;

import java.time.LocalDate;
import java.util.List;

import com.library.domain.entities.LoanTransaction;
import com.library.domain.enums.LoanStatus;
/**
 *
 * @author rafianandra
 */
public interface ILoanTransactionRepository extends IRepository<LoanTransaction, Integer> {
    
    // Menampilkan seluruh riwayat pinjaman milik satu member tertentu
    List<LoanTransaction> findByMemberId(Integer memberId);
    
    // Untuk memvalidasi aturan "maxBorrowLimit" dari LibraryConfig
    List<LoanTransaction> findActiveLoansByMemberId(Integer memberId);
    
    // Untuk fitur Pustakawan: Melihat semua transaksi yang "WAITING_PICKUP", "OVERDUE", dll.
    List<LoanTransaction> findByStatus(LoanStatus status);

    // Scheduler: REQUESTED -> WAITING_PICKUP saat scheduledPickupDate = hari ini
    List<LoanTransaction> findByStatusAndScheduledPickupDate(LoanStatus status, LocalDate date);

    // Scheduler: ACTIVE -> OVERDUE saat dueDate sudah terlewat
    List<LoanTransaction> findOverdueActive(LocalDate today);

    // Scheduler: WAITING_PICKUP -> EXPIRED saat pickup window terlewat
    List<LoanTransaction> findExpiredWaitingPickup(LocalDate cutoffDate);
    
}
