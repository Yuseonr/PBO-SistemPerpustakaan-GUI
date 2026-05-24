/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

import com.library.domain.enums.BorrowType;
import com.library.domain.enums.LoanStatus;
import com.library.domain.interfaces.IAuditable;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author rafianandra
 */
public class LoanTransaction implements IAuditable {
    
    // Atribut untuk LoanTransaction
    private Integer id;
    private Member member;
    private BookCopy bookCopy;
    private Librarian approvedBy;

    private BorrowType borrowType;
    private LoanStatus status;

    // Atribut untuk tanggal-tanggal penting dalam proses peminjaman
    private LocalDateTime requestDate;
    private LocalDate scheduledPickupDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LocalDateTime cancelledAt;
    
    // Atribut untuk perhitungan denda
    private Double fineAmount;
    private Double finePerDaySnapshot;
    private LocalDateTime fineCalculatedAt;
    
    // Atribut untuk audit informasi
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    // Konstruktor tanpa parameter
    public LoanTransaction() {
        this.fineAmount = 0.0; // Default denda awal 0
    }

    // Konstruktor dengan parameter
    public LoanTransaction(Member member, BookCopy bookCopy, BorrowType borrowType, LoanStatus initialStatus) {
        this.id = null; // ID akan di-set oleh repository saat disimpan
        this.member = member;
        this.bookCopy = bookCopy;
        this.borrowType = borrowType;
        this.status = initialStatus; // Status awal saat transaksi dibuat
        this.requestDate = LocalDateTime.now(); // Tanggal request otomatis di-set saat dibuat
        this.fineAmount = 0.0; // Default denda awal 0
    }

    // Method internal untuk cek keterlambatan
    public boolean isOverdue() {
        return status == LoanStatus.OVERDUE ||
               (status == LoanStatus.ACTIVE && dueDate != null && LocalDate.now().isAfter(dueDate));
    }

    // Implementasi Interface IAuditable
    @Override
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String getCreatedBy() { return createdBy; }

    // Setter untuk info audit
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Getter default
    public Integer getId() { return id; }
    public Member getMember() { return member; }
    public BookCopy getBookCopy() { return bookCopy; }
    public Librarian getApprovedBy() { return approvedBy; }
    public BorrowType getBorrowType() { return borrowType; }
    public LoanStatus getStatus() { return status; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public LocalDate getScheduledPickupDate() { return scheduledPickupDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public Double getFineAmount() { return fineAmount; }
    public Double getFinePerDaySnapshot() { return finePerDaySnapshot; }
    public LocalDateTime getFineCalculatedAt() { return fineCalculatedAt; }

    // Setter default
    public void setId(Integer id) { this.id = id; }
    public void setMember(Member member) { this.member = member; }
    public void setBookCopy(BookCopy bookCopy) { this.bookCopy = bookCopy; }
    public void setApprovedBy(Librarian approvedBy) { this.approvedBy = approvedBy; }
    public void setBorrowType(BorrowType borrowType) { this.borrowType = borrowType; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public void setScheduledPickupDate(LocalDate scheduledPickupDate) { this.scheduledPickupDate = scheduledPickupDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }
    public void setFinePerDaySnapshot(Double finePerDaySnapshot) { this.finePerDaySnapshot = finePerDaySnapshot; }
    public void setFineCalculatedAt(LocalDateTime fineCalculatedAt) { this.fineCalculatedAt = fineCalculatedAt; }

}
