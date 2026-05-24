/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.repository;

/**
 *
 * @author rafianandra
 */
public interface IRepository<T, ID> {

    // Save ke repository / create
    void save(T entity);

    // Update data yang sudah ada di repository
    void update(T entity);

    // Delete data dari repository berdasarkan ID
    void delete(ID id);

    // Cari data di repository berdasarkan ID single 
    T findById(ID id);

    // return semua data di repository
    java.util.List<T> findAll();
}

