/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.repository;

import com.library.domain.entities.BookCopy;
import java.util.List;

/**
 *
 * @author rafianandra
 */
public interface IBookCopyRepository extends IRepository<BookCopy, Integer> {
    
    // Cari semua copy buku berdasarkan bookTitleId
    List<BookCopy> findByBookTitleId(Integer bookTitleId);

    // menghitung jumlah copy yang tersedia (available) berdasarkan bookTitleId ntar untuk uinya
    int countAvailableByBookTitleId(Integer bookTitleId);

}
