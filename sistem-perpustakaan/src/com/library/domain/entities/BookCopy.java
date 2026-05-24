/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

import com.library.domain.enums.BookCopyStatus;

/**
 *
 * @author rafianandra
 */
public class BookCopy {

    // Atribut untuk BookCopy
    private BookTitle bookTitle; // buku fisik ini tersambung ke booktitle mana
    private Integer id;
    private String location;
    private BookCopyStatus status;

    // Konstruktor tanpa parameter
    protected BookCopy() {}

    // Konstruktor dengan id, location, bookTitle
    public BookCopy(Integer id, String Location, BookTitle bookTitle) {
        this.id = id;
        this.location = Location;
        this.bookTitle = bookTitle;
    }

    // Konstruktor dengan parameter
    public BookCopy(BookTitle bookTitle, String location) {
        this.bookTitle = bookTitle;
        this.location = location;
        this.status = BookCopyStatus.AVAILABLE; // Default status saat dibuat
    }

    // Methode untuk cek apakah buku ini bisa dipinjam
    public boolean canBeBorrowed() {
        return this.status == BookCopyStatus.AVAILABLE;
    }

    // Getter default
    public Integer getId() { return id; }
    public BookTitle getBookTitle() { return bookTitle; }
    public String getLocation() { return location; }
    public BookCopyStatus getStatus() { return status; }

    // Setter default
    public void setId(Integer id) { this.id = id; }
    public void setBookTitle(BookTitle bookTitle) { this.bookTitle = bookTitle; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(BookCopyStatus status) { this.status = status; } 

}
