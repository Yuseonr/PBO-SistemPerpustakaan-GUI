/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

import java.util.List;
import java.util.Arrays;

import com.library.domain.enums.UserRole;

/**
 *
 * @author rafianandra
 */
public class Admin extends User {

    // Konstruktor tanpa parameter
    protected Admin() { super(); }

    // Konstruktor dengan parameter
    public Admin(String name, String email, String passwordHash) {
        super(name, email, passwordHash, UserRole.ADMIN); // Role otomatis ADMIN
    }

    // Implementasi metode abstrak dari User
    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard"; }
    
    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
            "CRUD_MEMBER",
            "CRUD_LIBRARIAN",
            "RESET_PASSWORD",
            "MANAGE_CONFIG",
            "VIEW_STATISTICS",
            "VIEW_ALL_LOANS",
            "MANAGE_USER"
        ); 
    }
}
