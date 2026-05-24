/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

import java.util.List;
import java.util.Arrays;

import com.library.domain.enums.MemberStatus;
import com.library.domain.enums.UserRole;

/**
 *
 * @author rafianandra
 */
public class Member extends User {

    // Atribut khusus untuk Member
    private String membershipNumber;
    private String address;
    private String phoneNumber;
    private MemberStatus status;

    // Konstruktor tanpa parameter
    protected Member() { super(); }

    // Konstruktor dengan parameter
    public Member(String name, String email, String passwordHash, String membershipNumber, String address, String phoneNumber) {
        super(name, email, passwordHash, UserRole.MEMBER); // Role otomatis MEMBER
        this.membershipNumber = membershipNumber;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.status = MemberStatus.ACTIVE; // Default status saat pendaftaran
    }

    // Implementasi metode abstrak dari User
    @Override
    public String getDashboardTitle() {
        return "Member Dashboard"; }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
            "SEARCH_BOOK", 
            "BORROW_BOOK", 
            "VIEW_OWN_LOANS", 
            "CANCEL_OWN_LOAN"
        );
    }

    // Methode untuk check member valid
    public boolean isMembershipValid() {
        return this.status == MemberStatus.ACTIVE && this.isActive();
    }

    // Getter default untuk atribut khusus Member
    public String getMembershipNumber() { return membershipNumber; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public MemberStatus getStatus() { return status; }

    // Setter default
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setStatus(MemberStatus status) { this.status = status; }

}
