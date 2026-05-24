/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.entities.Librarian;
import com.library.domain.entities.Member;
import com.library.domain.entities.User;
import com.library.repository.BookCopyRepositoryMySQLImpl;
import com.library.repository.BookTitleRepositoryMySQLImpl;
import com.library.repository.CategoryRepositoryMySQLImpl;
import com.library.repository.IBookCopyRepository;
import com.library.repository.IBookTitleRepository;
import com.library.repository.ICategoryRepository;
import com.library.service.BookService;
import com.library.service.CategoryService;

import java.util.List;

/**
 *
 * @author rafianandra
 */
public class MainBook {

    public static void main(String[] args) {
        System.out.println("=== COMPREHENSIVE TESTING: BOOK SERVICE & REPOSITORIES ===\n");

        // 1. Inisialisasi Repositori
        ICategoryRepository categoryRepo = new CategoryRepositoryMySQLImpl();
        IBookTitleRepository titleRepo = new BookTitleRepositoryMySQLImpl();
        IBookCopyRepository copyRepo = new BookCopyRepositoryMySQLImpl();

        // 2. Inisialisasi Service
        CategoryService categoryService = new CategoryService(categoryRepo);
        BookService bookService = new BookService(titleRepo, copyRepo);

        // 3. Mock Users
        User pustakawan = new Librarian("Pak Budi", "budi@lib.com", "hash", "EMP-01", "Pagi");
        User member = new Member("Rafi Anandra", "rafi@email.com", "hash", "MEM-01", "Semarang", "081");

        // Variabel untuk menyimpan state antar skenario
        String testIsbn = "978-JAVA-OOP-001";
        Category testCategory = null;
        BookTitle testBookTitle = null;

        // ==========================================
        // PERSIAPAN KATEGORI 
        // ==========================================
        try {
            categoryService.createCategory(pustakawan, "Pemrograman", "Buku tentang koding");
        } catch (Exception e) {
            // Abaikan jika sudah ada
        }
        testCategory = categoryRepo.findByName("Pemrograman");


        // ==========================================
        // SKENARIO 1: UJI KEAMANAN (RBAC)
        // ==========================================
        System.out.println("--- Skenario 1: Member Mencoba Menambah Buku ---");
        try {
            bookService.addBookTitle(member, "Hacking 101", "Anon", "X", "123", "Desc", testCategory);
            System.out.println("[GAGAL] Sistem bocor! Member bisa menambah buku.");
        } catch (SecurityException e) {
            System.out.println("[SUKSES EXPECTED ERROR] " + e.getMessage());
        }


        // ==========================================
        // SKENARIO 2: PUSTAKAWAN MEMBUAT JUDUL BARU
        // ==========================================
        System.out.println("\n--- Skenario 2: Pustakawan Menambah Judul Buku ---");
        try {
            bookService.addBookTitle(
                pustakawan, 
                "Mastering Java OOP", 
                "Rafi Anandra", 
                "Informatika Press", 
                testIsbn, 
                "Panduan lengkap arsitektur N-Tier Java.", 
                testCategory
            );
            System.out.println("[SUKSES] Buku baru berhasil ditambahkan.");
        } catch (IllegalArgumentException e) {
            System.out.println("[INFO] Buku sudah ada. Melanjutkan simulasi...");
        }
        // Kita tarik datanya untuk Skenario 4
        testBookTitle = titleRepo.findByIsbn(testIsbn);


        // ==========================================
        // SKENARIO 3: UJI DUPLIKASI ISBN
        // ==========================================
        System.out.println("\n--- Skenario 3: Pustakawan Memasukkan ISBN Duplikat ---");
        try {
            bookService.addBookTitle(
                pustakawan, 
                "Buku Bajakan", 
                "Plagiator", 
                "X", 
                testIsbn, // Memakai ISBN yang sama dengan Skenario 2
                "Desc", 
                testCategory
            );
            System.out.println("[GAGAL] Sistem mengizinkan ISBN duplikat!");
        } catch (IllegalArgumentException e) {
            System.out.println("[SUKSES EXPECTED ERROR] " + e.getMessage());
        }


        // ==========================================
        // SKENARIO 4: INJEKSI STOK FISIK
        // ==========================================
        System.out.println("\n--- Skenario 4: Pustakawan Menambah 5 Eksemplar Fisik ---");
        if (testBookTitle != null) {
            try {
                bookService.addBookCopies(pustakawan, testBookTitle.getId(), 5, "Rak B-2");
                System.out.println("[SUKSES] 5 eksemplar berhasil diinjeksi ke Rak B-2.");
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        } else {
            System.out.println("[SKIP] Judul buku tidak ditemukan.");
        }


        // ==========================================
        // SKENARIO 5: PENCARIAN KOSONG (ALL)
        // ==========================================
        System.out.println("\n--- Skenario 5: Member Mencari Tanpa Keyword ---");
        List<BookTitle> allBooks = bookService.searchCatalog("");
        System.out.println("[SUKSES] Ditemukan " + allBooks.size() + " judul buku di seluruh sistem.");


        // ==========================================
        // SKENARIO 6: PENCARIAN SPESIFIK & CEK STOK
        // ==========================================
        System.out.println("\n--- Skenario 6: Member Mencari 'Mastering' dan Mengecek Stok ---");
        List<BookTitle> searchResults = bookService.searchCatalog("Mastering");
        
        if (searchResults.isEmpty()) {
            System.out.println("[GAGAL] Buku tidak ditemukan di fitur pencarian.");
        } else {
            for (BookTitle book : searchResults) {
                System.out.println("> ID Judul  : " + book.getId());
                System.out.println("> Judul     : " + book.getTitle());
                System.out.println("> Kategori  : " + book.getCategory().getName());
                
                // Panggil method penghitungan stok yang super efisien dari IBookCopyRepository
                int sisaStok = bookService.getAvailableStock(book.getId());
                System.out.println("> Sisa Stok : " + sisaStok + " Eksemplar berstatus AVAILABLE");
            }
            System.out.println("[SUKSES] Pencarian dan kalkulasi stok fisik berjalan mulus.");
        }
        
        System.out.println("\n=== TESTING VERTICAL SLICE 2 SELESAI ===");
    }
}