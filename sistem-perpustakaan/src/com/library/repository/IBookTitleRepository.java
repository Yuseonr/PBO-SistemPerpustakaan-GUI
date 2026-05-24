/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.repository;

import com.library.domain.entities.BookTitle;
import java.util.List;

/**
 *
 * @author rafianandra
 */
public interface IBookTitleRepository extends IRepository<BookTitle, Integer> {

    // Mencari buku dengan isbn
    BookTitle findByIsbn(String isbn);

    // Search by keyword untuk dapet list biar bisa di LIKE %somthinggitu% di SQL
    List<BookTitle> searchByKeyword(String keyword);
}
