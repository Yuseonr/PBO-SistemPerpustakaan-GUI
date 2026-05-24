/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.service;

import com.library.domain.entities.LibraryConfig;
import com.library.domain.entities.User;
import com.library.repository.ILibraryConfigRepository;

/**
 * 
 * 
 * @author rafianandra
 */
public class ConfigService {

    // Atribut untuk menyimpan referensi ke repository konfigurasi perpustakaan
    private final ILibraryConfigRepository configRepo;

    // Konstruktor untuk memasukkan dependency repository konfigurasi
    public ConfigService(ILibraryConfigRepository configRepo) {
        this.configRepo = configRepo;
    }

    // Method untuk mendapatkan konfigurasi perpustakaan saat ini
    public LibraryConfig getLibraryConfig() {
        return configRepo.getConfig();
    }

    // Method untuk memperbarui konfigurasi perpustakaan (hanya bisa dilakukan oleh admin)
    public void updateLibraryConfig(User actor, LibraryConfig newConfig) {
        checkPermission(actor, "MANAGE_CONFIG");

        // biar ga nguawur 
        if (newConfig.getFinePerDay() < 0) {
            throw new IllegalArgumentException("Denda per hari tidak boleh bernilai negatif.");
        }
        if (newConfig.getMaxBorrowDays() <= 0) {
            throw new IllegalArgumentException("Batas maksimal hari peminjaman harus lebih dari 0 hari.");
        }
        if (newConfig.getMaxBorrowLimit() <= 0) {
            throw new IllegalArgumentException("Batas maksimal buku yang dipinjam harus lebih dari 0 buku.");
        }
        if (newConfig.getMaxReservationDaysAhead() <= 0) {
            throw new IllegalArgumentException("Batas hari reservasi ke depan harus lebih dari 0 hari.");
        }
        if (newConfig.getPickupWindowDays() <= 0) {
            throw new IllegalArgumentException("Waktu toleransi pengambilan harus lebih dari 0 hari.");
        }
        if (newConfig.getLibraryName() == null || newConfig.getLibraryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama perpustakaan tidak boleh kosong.");
        }

        // Jika semua validasi lolos, simpan ke database
        configRepo.update(newConfig);
    }
    
    private void checkPermission(User actor, String permission) {
        if (!actor.getPermissions().contains(permission)) {
            throw new SecurityException("Akses ditolak: Anda tidak memiliki izin '" + permission + "'.");
        }
    }
}