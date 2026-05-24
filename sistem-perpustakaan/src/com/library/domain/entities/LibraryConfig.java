/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.domain.entities;

/**
 *
 * @author rafianandra
 */
public class LibraryConfig {
    
    // Atribute rule perpus
    private Integer id;
    private Double finePerDay;               // Misal: 2000.0 (Denda per hari)
    private int maxBorrowDays;               // Misal: 7 (Batas waktu pinjam normal)
    private int pickupWindowDays;            // Misal: 1 (Batas hari pengambilan untuk pinjam online)
    private int maxBorrowLimit;              // Misal: 3 (Maksimal buku yang boleh dipinjam bersamaan)
    private int maxReservationDaysAhead;     // Misal: 3 (Maksimal pesan buku untuk 3 hari ke depan)
    
    // Atribut profil perpus
    private String libraryName;
    private String libraryDescription;

    // Konstruktor tanpa parameter
    public LibraryConfig() {}

    // Getter dan Setter default
    public Integer getId() { return id; }
    public Double getFinePerDay() { return finePerDay; }
    public int getMaxBorrowDays() { return maxBorrowDays; }
    public int getPickupWindowDays() { return pickupWindowDays; }
    public int getMaxBorrowLimit() { return maxBorrowLimit; }
    public int getMaxReservationDaysAhead() { return maxReservationDaysAhead; }
    public String getLibraryName() { return libraryName; }
    public String getLibraryDescription() { return libraryDescription; }

    public void setId(Integer id) { this.id = id; }
    public void setFinePerDay(Double finePerDay) { this.finePerDay = finePerDay; }
    public void setMaxBorrowDays(int maxBorrowDays) { this.maxBorrowDays = maxBorrowDays; }
    public void setPickupWindowDays(int pickupWindowDays) { this.pickupWindowDays = pickupWindowDays; }
    public void setMaxBorrowLimit(int maxBorrowLimit) { this.maxBorrowLimit = maxBorrowLimit; }
    public void setMaxReservationDaysAhead(int maxReservationDaysAhead) { this.maxReservationDaysAhead = maxReservationDaysAhead; }
    public void setLibraryName(String libraryName) { this.libraryName = libraryName; }
    public void setLibraryDescription(String libraryDescription) { this.libraryDescription = libraryDescription; }
}