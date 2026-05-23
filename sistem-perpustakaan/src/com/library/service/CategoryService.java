/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.service;

import com.library.domain.entities.Category;
import com.library.domain.entities.User;
import com.library.repository.ICategoryRepository;
import java.util.List;

/**
 *
 * @author rafianandra
 */
public class CategoryService {

    // Atribut untuk menyimpan referensi ke repository kategori
    private final ICategoryRepository categoryRepository;

    // Konstruktor serta untuk memasukan dependency repository
    public CategoryService(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Method untuk membuat kategori baru
    public void createCategory(User actor, String name, String description) {

        // check apakah actor memiliki izin untuk membuat kategori 
        checkPermission(actor);

        // nama kategori tidak boleh kosong
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama kategori tidak boleh kosong.");
        }

        // nama kategori tidak boleh duplikat (baik yang aktif maupun yang non-aktif)
        Category existing = categoryRepository.findByName(name.trim());
        if (existing != null) {
            if (!existing.isActive()) {
                throw new IllegalArgumentException("Kategori '" + name + "' sudah ada tetapi dalam status non-aktif. Silakan gunakan fitur reaktivasi.");
            }
            throw new IllegalArgumentException("Kategori '" + name + "' sudah terdaftar di sistem.");
        }

        Category newCategory = new Category(name.trim(), description);
        newCategory.setCreatedBy(actor.getName());
        categoryRepository.save(newCategory);
    }

    // Method untuk mengubah nama dan/atau deskripsi kategori yang sudah ada
    public void updateCategory(User actor, Integer id, String newName, String newDescription) {

        // check apakah actor memiliki izin untuk mengubah kategori 
        checkPermission(actor);

        // Cek apakah kategori dengan id tersebut ada
        Category existingCategory = categoryRepository.findById(id);
        if (existingCategory == null) {
            throw new IllegalArgumentException("Kategori tidak ditemukan.");
        }

        // nama kategori baru tidak boleh kosong
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama kategori baru tidak boleh kosong.");
        }

        // Jika nama baru berbeda dengan nama lama, cek duplikat nama kategori lain baik yang aktif maupun yang non-aktif
        String cleanedName = newName.trim();
        if (!existingCategory.getName().equalsIgnoreCase(cleanedName)) {
            Category checkDuplicate = categoryRepository.findByName(cleanedName);
            if (checkDuplicate != null && !checkDuplicate.getId().equals(id)) {
                throw new IllegalArgumentException("Nama kategori '" + cleanedName + "' sudah dipakai oleh kategori lain.");
            }
        }

        existingCategory.setName(cleanedName);
        existingCategory.setDescription(newDescription);
        categoryRepository.update(existingCategory);
    }

    // Overload method updateCategory untuk kasus hanya ingin mengubah nama saja tanpa deskripsi
    public void updateCategory(User actor, Integer id, String newName) {

        // check apakah kategori ada ditemukan
        Category existingCategory = categoryRepository.findById(id);
        if (existingCategory == null) {
            throw new IllegalArgumentException("Kategori tidak ditemukan.");
        }

        // Panggil method updateCategory yang sudah ada dengan deskripsi lama
        updateCategory(actor, id, newName, existingCategory.getDescription());
    }

    // Method untuk menghapus kategori (soft delete dengan mengubah status active menjadi false)
    public void deleteCategory(User actor, Integer id) {

        // check apakah actor memiliki izin untuk menghapus kategori
        checkPermission(actor);
        
        // Cek apakah kategori dengan id tersebut ada
        Category existing = categoryRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Kategori tidak ditemukan.");
        }
        
        categoryRepository.delete(id);
    }

    public void reactivateCategory(User actor, Integer id) {

        // check apakah actor memiliki izin untuk mengubah kategori
        checkPermission(actor);

        // Cek apakah kategori dengan id tersebut ada
        Category existing = categoryRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Kategori tidak ditemukan.");
        }
        // Cek apakah kategori sudah aktif
        if (existing.isActive()) {
            throw new IllegalArgumentException("Kategori sudah dalam status aktif.");
        }
        // Reaktivasi dengan mengubah status active menjadi true
        existing.setActive(true);
        categoryRepository.update(existing);
    }

    // Method untuk mendapatkan semua kategori yang aktif (untuk ditampilkan di dropdown saat input buku)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAll();
    }

    // Method untuk memeriksa izin akses berdasarkan peran pengguna untuk menggunakan fitur manajemen kategori
    private void checkPermission(User actor) {
        if (actor.getPermissions().contains("CRUD_CATEGORY") == false) {
            throw new SecurityException("Akses ditolak: Anda tidak memiliki izin untuk mengelola kategori.");
        }
        
    }

}
