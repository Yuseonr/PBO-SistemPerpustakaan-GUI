/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.app;

import com.library.domain.entities.User;
import com.library.repository.IUserRepository;
import com.library.repository.UserRepositoryMySQLImpl;
import com.library.service.AuthService;

/**
 *
 * @author rafianandra
 */
public class MainAuth {
    
    public static void main(String[] args) {
        
        System.out.println("=== Inisialisasi Sistem Perpustakaan ===");
        
        // 1. Dependency Injection: Kita buat repositori konkretnya
        IUserRepository userRepository = new UserRepositoryMySQLImpl();
        
        // 2. Kita suntikkan repositori ke dalam AuthService
        AuthService authService = new AuthService(userRepository);
        
        System.out.println("Sistem berhasil diinisialisasi.\n");

        // --- SKENARIO 1: REGISTRASI MEMBER BARU ---
        System.out.println("--- Skenario 1: Registrasi Member ---");
        try {
            authService.registerMember(
                "Rafi Anandra", 
                "rafi@example.com", 
                "rahasia123", 
                "MEM-2026-001", 
                "Jl. Diponegoro No. 1", 
                "08123456789"
            );
            System.out.println("[SUKSES] Member berhasil diregistrasi!");
        } catch (IllegalArgumentException e) {
            System.out.println("[GAGAL] " + e.getMessage());
        }

        // --- SKENARIO 2: LOGIN SUKSES & PEMBUKTIAN POLIMORFISME ---
        System.out.println("\n--- Skenario 2: Login Valid ---");
        try {
            // Coba login dengan email dan password yang baru saja didaftarkan
            User loggedInUser = authService.login("rafi@example.com", "rahasia123");
            
            System.out.println("[SUKSES] Selamat datang, " + loggedInUser.getName() + "!");
            
            // PEMBUKTIAN PBO09: Polimorfisme
            // JVM akan memanggil getDashboardTitle() milik class Member, bukan User!
            System.out.println("Membuka form: " + loggedInUser.getDashboardTitle());
            System.out.println("Hak Akses yang dimiliki: " + loggedInUser.getPermissions());
            
        } catch (Exception e) {
            System.out.println("[GAGAL] " + e.getMessage());
        }

        // --- SKENARIO 3: LOGIN GAGAL (PASSWORD SALAH) ---
        System.out.println("\n--- Skenario 3: Login Gagal (Password Salah) ---");
        try {
            authService.login("rafi@example.com", "password_ngawur");
        } catch (IllegalArgumentException e) {
            System.out.println("[EXPECTED ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[UNEXPECTED ERROR] " + e.getMessage());
        }
        
        // --- SKENARIO 4: REGISTRASI EMAIL GANDA ---
        System.out.println("\n--- Skenario 4: Mendaftar dengan Email yang Sama ---");
        try {
            authService.registerMember(
                "Penipu", 
                "rafi@example.com", // Email ini sudah terdaftar di Skenario 1
                "12345", 
                "MEM-999", 
                "Alamat Palsu", 
                "000"
            );
        } catch (IllegalArgumentException e) {
            System.out.println("[EXPECTED ERROR] " + e.getMessage());
        }
    }
}

