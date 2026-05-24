/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.domain.interfaces;

import java.time.LocalDateTime;

/**
 * Interface untuk audit informasi seperti createdAt, updatedAt, dan createdBy.
 * Semua yang menerapkan interface ini harus menyediakan implementasi untuk audit data.
 * 
 * @author rafianandra
 */
public interface IAuditable {

    // Mengambil creation timestamp
    public LocalDateTime getCreatedAt();

    // Mengambil last modified timestamp
    public LocalDateTime getUpdatedAt();

    // Mengambil nama user yang membuat entri
    public String getCreatedBy();
}
