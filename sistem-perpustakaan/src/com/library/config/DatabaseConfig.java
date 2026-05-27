/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * 
 * @author rafianandra
 */
public class DatabaseConfig {
    // Konfigurasi koneksi database
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/myperpus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // agar tidak di inisialisasi
    private DatabaseConfig() {
    }

    // Mendapatkan koneksi ke database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
