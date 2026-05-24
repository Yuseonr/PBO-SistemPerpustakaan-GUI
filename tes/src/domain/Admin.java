/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package domain;

import enumtype.Role;
/**
 *
 * @author delli
 */
public class Admin extends User {
    public Admin() {
        this.role = Role.ADMIN;
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard";
    }
    
}
