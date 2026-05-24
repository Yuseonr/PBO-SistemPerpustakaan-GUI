/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.domain.entities.Category;
import com.library.domain.entities.Librarian;
import com.library.domain.entities.Member;
import com.library.domain.entities.User;
import com.library.repository.CategoryRepositoryMySQLImpl;
import com.library.repository.ICategoryRepository;
import com.library.service.CategoryService;

import java.util.List;

/**
 *
 * @author rafianandra
 */
public class MainCategory {
    public static void main(String[] args) {
        System.out.println("=== Inisialisasi Sistem Kategori ===");

        // 1. Dependency Injection
        ICategoryRepository categoryRepository = new CategoryRepositoryMySQLImpl();
        CategoryService categoryService = new CategoryService(categoryRepository);

        // 2. Persiapan: Membuat Actor di Memory (Simulasi Session Login)
        // Kita tidak perlu menyimpan ke DB untuk test ini karena checkPermission 
        // hanya mengecek isi List getPermissions() dari objek di memori.
        User pustakawan = new Librarian("Budi Santoso", "budi@lib.com", "hash", "EMP-001", "Pagi");
        User memberBiasa = new Member("Rafi Anandra", "rafi@email.com", "hash", "MEM-123", "Semarang", "0811");
        
        System.out.println("Dependensi & Actor berhasil disiapkan.\n");

        // --- SKENARIO 1: MEMBER MENCOBA MEMBUAT KATEGORI (RBAC TEST) ---
        System.out.println("--- Skenario 1: Member Membuat Kategori ---");
        try {
            categoryService.createCategory(memberBiasa, "Fiksi", "Buku cerita");
            System.out.println("[GAGAL] Sistem bocor! Member seharusnya tidak bisa CRUD Kategori.");
        } catch (IllegalStateException e) {
            System.out.println("[EXPECTED ERROR] " + e.getMessage());
        }

        // --- SKENARIO 2: PUSTAKAWAN MEMBUAT KATEGORI ---
        System.out.println("\n--- Skenario 2: Pustakawan Membuat Kategori 'Sains' & 'Teknologi' ---");
        try {
            categoryService.createCategory(pustakawan, "Sains", "Buku IPA");
            categoryService.createCategory(pustakawan, "Teknologi", "Buku Komputer");
            System.out.println("[SUKSES] Kategori Sains dan Teknologi berhasil dibuat.");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }

        // --- SKENARIO 3: VALIDASI NAMA DUPLIKAT ---
        System.out.println("\n--- Skenario 3: Pustakawan Membuat 'Sains' Lagi ---");
        try {
            categoryService.createCategory(pustakawan, "Sains", "Sains duplikat");
        } catch (IllegalArgumentException e) {
            System.out.println("[EXPECTED ERROR] " + e.getMessage());
        }

        // --- SKENARIO 4: UPDATE KATEGORI (MENGGUNAKAN OVERLOAD) ---
        System.out.println("\n--- Skenario 4: Update 'Teknologi' menjadi 'Ilmu Komputer' ---");
        try {
            // Cari dulu ID Teknologi
            Integer idTekno = null;
            for (Category c : categoryService.getAllActiveCategories()) {
                if (c.getName().equalsIgnoreCase("Teknologi")) {
                    idTekno = c.getId();
                    break;
                }
            }
            
            if (idTekno != null) {
                // Memanggil method overload (hanya update nama)
                categoryService.updateCategory(pustakawan, idTekno, "Ilmu Komputer");
                System.out.println("[SUKSES] Kategori berhasil di-update.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }

        // --- SKENARIO 5: SOFT DELETE ---
        System.out.println("\n--- Skenario 5: Menghapus (Soft Delete) 'Sains' ---");
        Integer idSains = null;
        try {
            for (Category c : categoryService.getAllActiveCategories()) {
                if (c.getName().equalsIgnoreCase("Sains")) {
                    idSains = c.getId();
                    break;
                }
            }
            
            if (idSains != null) {
                categoryService.deleteCategory(pustakawan, idSains);
                System.out.println("[SUKSES] Kategori Sains di-soft delete.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }

        // --- SKENARIO 6: MEMBUAT KATEGORI YANG SEDANG TIDUR ---
        System.out.println("\n--- Skenario 6: Pustakawan Membuat Ulang 'Sains' ---");
        try {
            categoryService.createCategory(pustakawan, "Sains", "Coba bikin lagi");
        } catch (IllegalArgumentException e) {
            System.out.println("[EXPECTED ERROR] " + e.getMessage());
        }

        // --- SKENARIO 7: REAKTIVASI KATEGORI ---
        System.out.println("\n--- Skenario 7: Pustakawan Reaktivasi 'Sains' ---");
        try {
            if (idSains != null) {
                categoryService.reactivateCategory(pustakawan, idSains);
                System.out.println("[SUKSES] Kategori Sains berhasil dihidupkan kembali.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }

        // --- SKENARIO 8: READ SEMUA DATA ---
        System.out.println("\n--- Skenario 8: Menampilkan Daftar Kategori (Dropdown UI) ---");
        List<Category> aktif = categoryService.getAllActiveCategories();
        for (Category c : aktif) {
            System.out.println("- " + c.getName() + " (Deskripsi: " + c.getDescription() + ")");
        }
    }
}