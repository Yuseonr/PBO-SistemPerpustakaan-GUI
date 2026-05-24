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
public class User {
    protected int id;
    public String name;
    public String email;
    public String passwordHash;
    protected Role role;

    public abstract String getDashboardTitle();

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
    
}
