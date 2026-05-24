/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

import java.time.LocalDateTime;

import com.library.domain.interfaces.IAuditable;

/**
 *
 * @author rafianandra
 */
public class Category implements IAuditable {

    // Atribute Category
    private Integer id;
    private String name;
    private String description;
    private boolean Active;

    // Atribute untuk audit informasi
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    // Konstruktor tanpa parameter
    protected Category() {}

    // Konstruktor dengan id dan nama
    public Category(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    // Konstruktor yang menerima name, description 
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.Active = true; // Default aktif saat dibuat
    }

    // Implementasi IAuditable
    @Override
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String getCreatedBy() { return createdBy; }

    // Setter untuk info audit
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Getter default
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return Active; }

    // Setter default
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(boolean Active) { this.Active = Active; }


}
