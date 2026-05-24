/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.repository;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author rafianandra
 */
public class CategoryRepositoryMySQLImpl implements ICategoryRepository {

    @Override
    public void save(Category entity) {
        String sql = "INSERT INTO categories (name, description, active, created_by) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getDescription());
            stmt.setBoolean(3, entity.isActive());
            stmt.setString(4, entity.getCreatedBy());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Category entity) {
        String sql = "UPDATE categories SET name = ?, description = ?, active = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getDescription());
            stmt.setBoolean(3, entity.isActive());
            stmt.setInt(4, entity.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer id) {
        // Hanya mengubah status active menjadi false
        String sql = "UPDATE categories SET active = false WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Category findById(Integer id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Implementasi metode findByName untuk mencari Category berdasarkan nama
    @Override
    public Category findByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE active = true ORDER BY name ASC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category(
            rs.getString("name"),
            rs.getString("description")
        );
        
        category.setId(rs.getInt("id"));
        category.setActive(rs.getBoolean("active"));
        
        if (rs.getTimestamp("created_at") != null) {
            category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        if (rs.getString("created_by") != null) {
            category.setCreatedBy(rs.getString("created_by"));
        }
        
        return category;
    }
}
