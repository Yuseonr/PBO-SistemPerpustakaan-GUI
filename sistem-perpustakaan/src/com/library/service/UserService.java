/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.service;

import com.library.domain.entities.Librarian;
import com.library.domain.entities.Member;
import com.library.domain.entities.User;
import com.library.domain.enums.MemberStatus;
import com.library.domain.enums.UserRole;
import com.library.repository.IUserRepository;
import com.library.util.PasswordHasher;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author rafianandra
 */
public class UserService {
    
    // Atribut untuk menyimpan referensi ke repository pengguna
    private final IUserRepository UserRepository;

    //  Konstruktor serta untuk memasukan dependency repository
    public UserService(IUserRepository UserRepository) {
        this.UserRepository = UserRepository;
    }

    // Method untuk mendapatkan pengguna berdasarkan ID
    public User getUserById(Integer id) {
        User user = UserRepository.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Pengguna dengan ID " + id + " tidak ditemukan.");
        }
        return user;
    }

    // Method untuk mendapatkan  semua user
    public List<User> getAllUsers() {
        return UserRepository.findAll();
    }

    // Method untuk mendapatkan semua user berdasarkan peran (role)
    public List<User> getUsersByRole(UserRole role) {
        List<User> allUsers = UserRepository.findAll();
        List<User> filteredUsers = new ArrayList<>();
        for (User u : allUsers) {
            if (u.getRole() == role) {
                filteredUsers.add(u);
            }
        }
        return filteredUsers;
    }

    // Method untuk mendaftarkan librarian baru (hanya bisa dilakukan oleh admin)
    public Librarian registerLibrarian(User actor, Librarian librarian, String rawPassword) {
        checkPermission(actor, "CRUD_LIBRARIAN");

        if (librarian.getEmail() == null || librarian.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email tidak boleh kosong.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter.");
        }

        User existingUser = UserRepository.findByEmail(librarian.getEmail());
        if (existingUser != null) {
            throw new IllegalStateException("Email " + librarian.getEmail() + " sudah terdaftar di sistem.");
        }

        librarian.setPasswordHash(PasswordHasher.hashPassword(rawPassword));
        librarian.setRole(UserRole.LIBRARIAN);
        librarian.setActive(true);
        librarian.setCreatedBy(actor.getEmail());

        UserRepository.save(librarian);
        return librarian;
    }

    // Method untuk menangguhkan (suspend) atau mengaktifkan kembali (activate) akun pengguna
    public void suspendUser(User actor, Integer targetId) {
        checkPermission(actor, "MANAGE_USER");

        User user = getUserById(targetId);
        if (!user.isActive()) {
            throw new IllegalStateException("Akun ini sudah dalam status non-aktif.");
        }

        user.setActive(false);
        if (user instanceof Member) {
            ((Member) user).setStatus(MemberStatus.SUSPENDED);
        }

        UserRepository.update(user);
    }

    // Method untuk mengaktifkan kembali akun pengguna yang sebelumnya ditangguhkan
    public void activateUser(User actor, Integer targetId) {
        checkPermission(actor, "MANAGE_USER");

        User user = getUserById(targetId);
        if (user.isActive()) {
            throw new IllegalStateException("Akun ini sudah dalam status aktif.");
        }

        user.setActive(true);
        if (user instanceof Member) {
            ((Member) user).setStatus(MemberStatus.ACTIVE);
        }

        UserRepository.update(user);
    }

    // Method untuk mereset password pengguna ke password default (hanya bisa dilakukan oleh admin)
    public void resetPasswordToDefault(User actor, Integer targetId, String defaultPassword) {
        checkPermission(actor, "RESET_PASSWORD");

        if (defaultPassword == null || defaultPassword.length() < 6) {
            throw new IllegalArgumentException("Password default harus minimal 6 karakter.");
        }

        User user = getUserById(targetId);
        user.setPasswordHash(PasswordHasher.hashPassword(defaultPassword));
        UserRepository.update(user);
    }

    // Method untuk memperbarui informasi pengguna (misalnya nama, email, nomor telepon) - hanya bisa dilakukan oleh admin atau oleh pengguna itu sendiri
    public void updateUserInfo(User actor, User targetUser) {
        checkPermission(actor, "MANAGE_USER");

        User existingUser = UserRepository.findById(targetUser.getId());
        if (existingUser == null) {
            throw new IllegalArgumentException("Pengguna tidak ditemukan.");
        }

        if (!existingUser.getEmail().equalsIgnoreCase(targetUser.getEmail())) {
            User emailCheck = UserRepository.findByEmail(targetUser.getEmail());
            if (emailCheck != null) {
                throw new IllegalStateException("Email " + targetUser.getEmail() + " sudah digunakan oleh akun lain.");
            }
        }

        UserRepository.update(targetUser);
    }

    // Method untuk menghapus pengguna (soft delete dengan menonaktifkan akun) - hanya bisa dilakukan oleh admin
    public void deleteUser(User actor, Integer targetId) {
        checkPermission(actor, "MANAGE_USER");
        getUserById(targetId);
        UserRepository.delete(targetId);
    }

    // Method untuk memeriksa izin akses berdasarkan peran pengguna untuk menggunakan fitur manajemen pengguna
    private void checkPermission(User actor, String permission) {
        if (!actor.getPermissions().contains(permission)) {
            throw new SecurityException("Akses ditolak: Anda tidak memiliki izin '" + permission + "'.");
        }
    }
}