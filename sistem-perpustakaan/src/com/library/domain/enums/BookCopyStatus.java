/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.library.domain.enums;

/**
 *
 * @author rafianandra
 */
public enum BookCopyStatus {
    AVAILABLE,  // Tersedia untuk dipinjam
    RESERVED,   // Dipsean tapi belum diambil
    LOANED,     // Sedang dipinjam
    UNAVAILABLE // tidak tersedia kaya rusak / hilang
}
