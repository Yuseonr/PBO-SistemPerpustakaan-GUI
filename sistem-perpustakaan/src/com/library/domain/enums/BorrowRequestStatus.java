/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.library.domain.enums;

/**
 *
 * @author rafianandra
 */
public enum BorrowRequestStatus {
    OPEN,                // Permintaan baru, belum ada buku yang diambil 
    PARTIALLY_ACTIVE,    // Ada buku yang sudah diambil tapi belum semua
    ACTIVE,              // Semua buku sudah diambil
    CLOSED,              // Semua buku sudah dikembalikan   
    CANCELLED,           // Permintaan dibatalkan dari level permintaan / semua permintaan buku dibatalkan
    EXPIRED              // Semua permintaan buku telah expired
}
