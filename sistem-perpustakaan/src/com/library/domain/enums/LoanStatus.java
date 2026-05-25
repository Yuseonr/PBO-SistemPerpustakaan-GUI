/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.library.domain.enums;

/**
 *
 * @author rafianandra
 */
public enum LoanStatus {
    REQUESTED,     // Pinjaman baru dibuat, menunggu jadwal pickup
    WAITING_PICKUP,// Sudah masuk jadwal pickup, menunggu member datang
    ACTIVE,        // Sudah dipinjam, sedang dipakai
    OVERDUE,       // Sudah melewati due date tapi belum dikembalikan
    RETURNED,      // Sudah dikembalikan
    CANCELLED,     // Pinjaman dibatalkan sebelum pickup
    EXPIRED        // Pinjaman tidak diambil melewati pickup window
}
