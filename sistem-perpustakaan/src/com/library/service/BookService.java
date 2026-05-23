/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.service;

import com.library.domain.entities.BookCopy;
import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.entities.User;
import com.library.repository.IBookCopyRepository;
import com.library.repository.IBookTitleRepository;

import java.util.List;

/**
 *
 * @author rafianandra
 */
public class BookService {

    // Atribut untuk menyimpan referensi ke repository buku
    private final IBookTitleRepository bookTitleRepository;
    private final IBookCopyRepository bookCopyRepository;

    // Konstruktor serta untuk memasukan dependency repository
    public BookService(IBookTitleRepository bookTitleRepository, IBookCopyRepository bookCopyRepository) {
        this.bookTitleRepository = bookTitleRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    // Method untuk menambahkan judul buku baru ke dalam katalog
    public void addBookTitle(User actor, String title, String author, String publisher, String isbn, String description, Category category) {
        checkPermission(actor);

        if (title == null || title.trim().isEmpty() || isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("Judul dan ISBN tidak boleh kosong.");
        }

        if (category == null) {
            throw new IllegalArgumentException("Buku harus memiliki kategori yang valid.");
        }

        BookTitle existingBook = bookTitleRepository.findByIsbn(isbn.trim());
        if (existingBook != null) {
            throw new IllegalArgumentException("Buku dengan ISBN '" + isbn + "' sudah terdaftar di katalog.");
        }

        BookTitle newBook = new BookTitle(title.trim(), author, publisher, isbn.trim(), description, category);
        bookTitleRepository.save(newBook);
    }

    // Method untuk menambahkan eksemplar buku (fisik) ke dalam sistem berdasarkan judul buku yang sudah ada
    public void addBookCopies(User actor, Integer bookTitleId, int numberOfCopies, String location) {
        checkPermission(actor);

        if (numberOfCopies <= 0) {
            throw new IllegalArgumentException("Jumlah eksemplar yang ditambahkan minimal 1.");
        }

        BookTitle existingTitle = bookTitleRepository.findById(bookTitleId);
        if (existingTitle == null) {
            throw new IllegalArgumentException("Judul buku tidak ditemukan di sistem.");
        }

        // Loop untuk membuat beberapa fisik buku sekaligus
        for (int i = 0; i < numberOfCopies; i++) {
            BookCopy copy = new BookCopy(existingTitle, location);
            bookCopyRepository.save(copy);
        }
    }

    // Method untuk mencari buku berdasarkan keyword (bisa judul, penulis, atau kategori)    
    public List<BookTitle> searchCatalog(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookTitleRepository.findAll();
        }
        return bookTitleRepository.searchByKeyword(keyword.trim());
    }

    // Method untuk mendapatkan jumlah stok yang tersedia dari suatu judul buku berdasarkan id judul bukunya
    public int getAvailableStock(Integer bookTitleId) {
        return bookCopyRepository.countAvailableByBookTitleId(bookTitleId);
    }


    // Method untuk memeriksa izin akses berdasarkan peran pengguna untuk menggunakan fitur manajemen buku
    private void checkPermission(User actor) {
        if (actor.getPermissions().contains("CRUD_BOOK") == false) {
            throw new SecurityException("Akses ditolak: Anda tidak memiliki izin untuk mengelola buku.");
        }
        
    }
}