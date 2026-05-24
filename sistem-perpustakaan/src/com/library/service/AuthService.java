/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.service;

import com.library.domain.entities.Member;
import com.library.domain.entities.User;
import com.library.repository.IUserRepository;
import com.library.util.PasswordHasher;

/**
 *
 * @author rafianandra
 */
public class AuthService {

    // Atribut untuk menyimpan referensi ke repository pengguna
    private final IUserRepository userRepository;

    // Konstruktor serta untuk memasukan dependency repository
    public AuthService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Methode untuk registrasi member baru
    public void registerMember(String name, String email, String plainPassword, String membershipNumber, String address, String phoneNumber) {

        // Vlidasi
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama tidak boleh kosong.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email tidak boleh kosong.");
        }
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password tidak boleh kosong.");
        }
        
        // Apakah ada user dengan email yang sama?
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            throw new IllegalArgumentException("Registrasi gagal: Email sudah terdaftar di dalam sistem.");
        }

        // Hash password sebelum disimpan
        String hashedPassword = PasswordHasher.hashPassword(plainPassword);

        // Objek baru + Simpan ke repository (database)
        Member newMember = new Member(name, email, hashedPassword, membershipNumber, address, phoneNumber);
        newMember.setCreatedBy("System");
        userRepository.save(newMember);
    }

    // Methode untuk login
    public User login(String email, String plainPassword) {
        
        // Apakah ada user dengan email tersebut?
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Login gagal: Email tidak ditemukan.");
        }

        // Apakah akun aktif?
        if (!user.isActive()) {
            throw new IllegalStateException("Login ditolak: Akun Anda telah dinonaktifkan.");
        }

        // Apakah password benar?
        if (!PasswordHasher.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Login gagal: Password salah.");
        }

        return user;
    }
}
