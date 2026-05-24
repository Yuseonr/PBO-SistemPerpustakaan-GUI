/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.repository;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.LibraryConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementasi Repositori untuk Konfigurasi Perpustakaan.
 * Memastikan database hanya membaca dan memperbarui baris ID = 1.
 * @author rafianandra
 */
public class LibraryConfigRepositoryMySQLImpl implements ILibraryConfigRepository {

    @Override
    public LibraryConfig getConfig() {
        // Selalu mengambil baris pertama (ID = 1)
        String sql = "SELECT * FROM library_config WHERE id = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return mapResultSetToConfig(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new LibraryConfig(); 
    }

    @Override
    public void update(LibraryConfig entity) {
        String sql = "UPDATE library_config SET fine_per_day = ?, max_borrow_days = ?, "
                + "max_borrow_limit = ?, max_reservation_days_ahead = ?, pickup_window_days = ?, "
                + "library_name = ?, library_description = ?"
                + " WHERE id = 1"; 

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, entity.getFinePerDay());
            stmt.setInt(2, entity.getMaxBorrowDays());
            stmt.setInt(3, entity.getMaxBorrowLimit());
            stmt.setInt(4, entity.getMaxReservationDaysAhead());
            stmt.setInt(5, entity.getPickupWindowDays());
            stmt.setString(6, entity.getLibraryName());
            stmt.setString(7, entity.getLibraryDescription());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public LibraryConfig findById(Integer id) {
        return getConfig();
    }

    @Override
    public List<LibraryConfig> findAll() {
        // Kembalikan dalam bentuk list yang hanya berisi 1 elemen
        List<LibraryConfig> list = new ArrayList<>();
        list.add(getConfig());
        return list;
    }

    @Override
    public void save(LibraryConfig entity) {
        throw new UnsupportedOperationException("Tabel konfigurasi bersifat tunggal. Gunakan update() alih-alih save().");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Konfigurasi perpustakaan tidak boleh dihapus dari sistem.");
    }

    private LibraryConfig mapResultSetToConfig(ResultSet rs) throws SQLException {
        LibraryConfig config = new LibraryConfig();
        
        config.setFinePerDay(rs.getDouble("fine_per_day"));
        config.setMaxBorrowDays(rs.getInt("max_borrow_days"));
        config.setMaxBorrowLimit(rs.getInt("max_borrow_limit"));
        config.setMaxReservationDaysAhead(rs.getInt("max_reservation_days_ahead"));
        config.setPickupWindowDays(rs.getInt("pickup_window_days"));
        
        config.setLibraryName(rs.getString("library_name"));
        config.setLibraryDescription(rs.getString("library_description"));
        
        return config;
    }
}