/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author rafianandra
 */
public class FineCalculator {
    public static Double calculateFine(LocalDateTime dueDate, LocalDateTime returnDate, Double finePerDay) {
        // Validasi input
        if (dueDate == null || returnDate == null || finePerDay == null || finePerDay <= 0) {
            return 0.0;
        }
        // Jika dikembalikan sebelum atau pas pada hari jatuh tempo
        if (returnDate.isBefore(dueDate) || returnDate.toLocalDate().isEqual(dueDate.toLocalDate())) {
            return 0.0; 
        }
        // Hitung selisih HARI 
        long daysLate = ChronoUnit.DAYS.between(dueDate.toLocalDate(), returnDate.toLocalDate());

        if (daysLate <= 0) {
            return 0.0;
        }
        return daysLate * finePerDay;
    }
}