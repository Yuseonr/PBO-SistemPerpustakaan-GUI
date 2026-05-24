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
public class Librarian extends User {
    
    // Atribut khusus untuk Librarian
    private String employeeNumber;
    private String shiftInfo; // Bisa ganti ke enum (pagi, sore, malam)

     // Konstruktor tanpa parameter
    protected Librarian() { super(); }

    // Konstruktor dengan parameter
    public Librarian(String name, String email, String passwordHash, String employeeNumber, String shiftInfo) {
        super(name, email, passwordHash, UserRole.LIBRARIAN); // Role otomatis LIBRARIAN
        this.employeeNumber = employeeNumber;
        this.shiftInfo = shiftInfo;
    }

    // Implementasi metode abstrak dari User
    @Override
    public String getDashboardTitle() {
        return "Librarian Dashboard"; }
    
    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
            "SEARCH_BOOK",
            "CRUD_BOOK",
            "CRUD_CATEGORY",
            "CONFIRM_PICKUP",
            "CONFIRM_RETURN",
            "VIEW_ALL_LOANS"
        );
    }

    // Getter default untuk atribut khusus Librarian
    public String getEmployeeNumber() { return employeeNumber; }
    public String getShiftInfo() { return shiftInfo; }

    // Setter default
    public void setShiftInfo(String shiftInfo) { this.shiftInfo = shiftInfo; } // Bisa ganti ke enum (pagi, sore, malam)


}
