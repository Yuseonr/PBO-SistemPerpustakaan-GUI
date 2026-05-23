/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.domain.interfaces;

/**
 * Interface untuk menandai bahwa suatu entitas bisa dicari dengan keyword tertentu.
 * Contoh: Buku bisa dicari dengan judul, penulis, atau ISBN. Member
 * Contoh : Member bisa dicari dengan nama atau ID member.
 * 
 * @author rafianandra
 */
public interface ISearchable {
    
    // Mengambil keyword pencarian untuk semua field relvan agar bisa cek ada ngga nya keyword di object
    public boolean matchesKeyword(String keyword);
}
