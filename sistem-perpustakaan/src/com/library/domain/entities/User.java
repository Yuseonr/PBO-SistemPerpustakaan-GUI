/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.library.domain.enums.UserRole;
import com.library.domain.interfaces.IAuditable;

/**
 *
 * @author rafianandra
 */
public abstract class User implements IAuditable {
    // Atribut dasar untuk User
    private Integer id; // dipilih Integer agar bisa null sebelum disimpan ke repository
    private String name;
    private String email;
    private String passwordHash; 
    private UserRole role;
    private boolean Active;

    // Atribut untuk audit informasi
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    // Konstruktor tanpa parameter
    protected User() {}

    // Konstruktor dengan parameter
    protected User(String name, String email, String passwordHash, UserRole role) {
        this.id = null; // ID akan di-set oleh repository saat disimpan 
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.Active = true; // Default user baru langsung aktif
    }

    // Abstract Methode 
    // Mengembalikan Judul Dasboard sesuai dengan role masing masing
    public abstract String getDashboardTitle();

    // Mengembalikan list of permission yang dimiliki, yang nantinya digunakan buat dicek di Service 
    // note : ntar bisa di buat ke enum kalo terlalu ribet buat string, tapi untuk sekarang biar fleksibel aja pake string
    public abstract List<String> getPermissions();

    // Implementasi IAuditable
    @Override
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String getCreatedBy() { return createdBy; }

    // Setter untuk info ayudit
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Getter default
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return Active; }

    // Setter default
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(UserRole role) { this.role = role; }
    public void setActive(boolean Active) { this.Active = Active; }

}
