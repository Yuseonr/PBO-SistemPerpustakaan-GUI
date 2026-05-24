/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

import com.library.domain.interfaces.ISearchable;

/**
 *
 * @author rafianandra
 */
public class BookTitle implements ISearchable{
    
    // Atribut untuk BookTitle
    private Integer id;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String description;

    private Category category;

    // Konstruktor tanpa parameter
    protected BookTitle() {}

    // Konstruktor dengan id, title, author, category
    public BookTitle(Integer id, String title, String author, Category category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
    }

    // Konstruktor dengan parameter
    public BookTitle(String title, String author, String publisher, String isbn, String description, Category category) {
        this.id = null; // ID akan di-set oleh repository saat disimpan
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.description = description;
        this.category = category;
    }
    
    // Implemetasi metode dari ISearchable
    // Metode ini akan mengecek apakah keyword yang diberikan ada di salah satu field (title, author, publisher, isbn, description)
    @Override
    public boolean matchesKeyword(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return title.toLowerCase().contains(lowerKeyword) ||
               author.toLowerCase().contains(lowerKeyword) ||
               publisher.toLowerCase().contains(lowerKeyword) ||
               isbn.toLowerCase().contains(lowerKeyword) ||
               description.toLowerCase().contains(lowerKeyword);
    }

    // Getter default
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public String getIsbn() { return isbn; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }

    // Setter default
    public void setId(Integer id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }

}
