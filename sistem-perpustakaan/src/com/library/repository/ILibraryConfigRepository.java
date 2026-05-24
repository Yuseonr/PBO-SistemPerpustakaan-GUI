/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.repository;

import com.library.domain.entities.LibraryConfig;

/**
 *
 * @author rafianandra
 */
public interface ILibraryConfigRepository extends IRepository<LibraryConfig, Integer>{
    
    // Mengambil konfigurasi perpustakaan 
    LibraryConfig getConfig();
}
