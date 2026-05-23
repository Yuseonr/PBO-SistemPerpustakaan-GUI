/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.repository;

import com.library.domain.entities.Category;


/**
 *
 * @author rafianandra
 */
public interface ICategoryRepository extends IRepository<Category, Integer> {
    
    // Method untuk mencari category berdasarkan nama
    Category findByName(String name);
    
}