/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

/**
 * Biar ngga manggil service berkali kali
 * @author rafianandra
 */
public class DashboardStats {
    
    // Atribut untuk menyimpan statistik dashboard
    private int totalActiveMembers;
    private int totalAdmins;
    private int totalLibrarians;
    private int totalBookTitles;
    private int totalBookCopies;
    private int totalCategories;
    private double totalFineCollected;

    // Getter dan Setter
    public int getTotalActiveMembers() { return totalActiveMembers; }
    public void setTotalActiveMembers(int totalActiveMembers) { this.totalActiveMembers = totalActiveMembers; }

    public int getTotalBookCopies() { return totalBookCopies; }
    public void setTotalBookCopies(int totalBookCopies) { this.totalBookCopies = totalBookCopies; }

    public int getTotalAdmins() { return totalAdmins; }
    public void setTotalAdmins(int totalAdmins) { this.totalAdmins = totalAdmins; }

    public int getTotalLibrarians() { return totalLibrarians; }
    public void setTotalLibrarians(int totalLibrarians) { this.totalLibrarians = totalLibrarians; }

    public int getTotalBookTitles() { return totalBookTitles; }
    public void setTotalBookTitles(int totalBookTitles) { this.totalBookTitles = totalBookTitles; }

    public int getTotalCategories() { return totalCategories; }
    public void setTotalCategories(int totalCategories) { this.totalCategories = totalCategories; }
    
    public double getTotalFineCollected() { return totalFineCollected; }
    public void setTotalFineCollected(double totalFineCollected) { this.totalFineCollected = totalFineCollected; }
}
