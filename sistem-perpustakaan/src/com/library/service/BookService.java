/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.service;

import com.library.domain.entities.BookCopy;
import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.entities.User;
import com.library.domain.enums.BookCopyStatus;
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

    // Method untuk mengubah data judul buku
    public void updateBookTitle(User actor, Integer id, String title, String author, String publisher, String isbn, String description, Category category) {
        checkPermission(actor);

        BookTitle existingTitle = bookTitleRepository.findById(id);
        if (existingTitle == null) {
            throw new IllegalArgumentException("Judul buku tidak ditemukan di sistem.");
        }

        if (title == null || title.trim().isEmpty() || isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("Judul dan ISBN tidak boleh kosong.");
        }

        if (category == null) {
            throw new IllegalArgumentException("Buku harus memiliki kategori yang valid.");
        }

        String cleanedIsbn = isbn.trim();
        if (!cleanedIsbn.equalsIgnoreCase(existingTitle.getIsbn())) {
            BookTitle isbnCheck = bookTitleRepository.findByIsbn(cleanedIsbn);
            if (isbnCheck != null && !isbnCheck.getId().equals(id)) {
                throw new IllegalArgumentException("Buku dengan ISBN '" + cleanedIsbn + "' sudah terdaftar di katalog.");
            }
        }

        existingTitle.setTitle(title.trim());
        existingTitle.setAuthor(author);
        existingTitle.setPublisher(publisher);
        existingTitle.setIsbn(cleanedIsbn);
        existingTitle.setDescription(description);
        existingTitle.setCategory(category);

        bookTitleRepository.update(existingTitle);
    }

    // Method untuk menghapus judul buku dari katalog
    public void deleteBookTitle(User actor, Integer id) {
        checkPermission(actor);

        BookTitle existingTitle = bookTitleRepository.findById(id);
        if (existingTitle == null) {
            throw new IllegalArgumentException("Judul buku tidak ditemukan di sistem.");
        }

        bookTitleRepository.delete(id);
    }

    // Method untuk mengubah data copy buku
    public void updateBookCopy(User actor, Integer copyId, Integer bookTitleId, String location) {
        checkPermission(actor);

        BookCopy existingCopy = bookCopyRepository.findById(copyId);
        if (existingCopy == null) {
            throw new IllegalArgumentException("Copy buku tidak ditemukan di sistem.");
        }

        if (bookTitleId != null) {
            BookTitle existingTitle = bookTitleRepository.findById(bookTitleId);
            if (existingTitle == null) {
                throw new IllegalArgumentException("Judul buku tidak ditemukan di sistem.");
            }
            existingCopy.setBookTitle(existingTitle);
        }

        if (location != null && !location.trim().isEmpty()) {
            existingCopy.setLocation(location.trim());
        }

        bookCopyRepository.update(existingCopy);
    }

    // Method untuk mengubah status copy buku
    public void updateCopyStatus(User actor, Integer copyId, BookCopyStatus status) {
        checkPermission(actor);

        if (status == null) {
            throw new IllegalArgumentException("Status copy tidak boleh kosong.");
        }

        BookCopy existingCopy = bookCopyRepository.findById(copyId);
        if (existingCopy == null) {
            throw new IllegalArgumentException("Copy buku tidak ditemukan di sistem.");
        }

        existingCopy.setStatus(status);
        bookCopyRepository.update(existingCopy);
    }

    // Method untuk menghapus copy buku
    public void deleteBookCopy(User actor, Integer copyId) {
        checkPermission(actor);

        BookCopy existingCopy = bookCopyRepository.findById(copyId);
        if (existingCopy == null) {
            throw new IllegalArgumentException("Copy buku tidak ditemukan di sistem.");
        }

        bookCopyRepository.delete(copyId);
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

    // Method untuk mencari satu copy fisik buku yang AVAILABLE untuk dipinjam
    public BookCopy findAvailableCopyByTitleId(Integer bookTitleId) {
        List<BookCopy> copies = bookCopyRepository.findByBookTitleId(bookTitleId);
        if (copies != null) {
            for (BookCopy copy : copies) {
                if (copy.getStatus() == BookCopyStatus.AVAILABLE) {
                    return copy;
                }
            }
        }
        return null;
    }

    // Method untuk mendapatkan jumlah total copy berdasarkan id judul buku
    public int getTotalCopies(Integer bookTitleId) {
        List<BookCopy> copies = bookCopyRepository.findByBookTitleId(bookTitleId);
        return copies == null ? 0 : copies.size();
    }


    // Method untuk memeriksa izin akses berdasarkan peran pengguna untuk menggunakan fitur manajemen buku
    private void checkPermission(User actor) {
        if (actor.getPermissions().contains("CRUD_BOOK") == false) {
            throw new SecurityException("Akses ditolak: Anda tidak memiliki izin untuk mengelola buku.");
        }
        
    }
}