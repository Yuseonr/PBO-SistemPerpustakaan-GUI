/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.repository;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.BookTitle;
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
public class BookTitleRepositoryMySQLImpl implements IBookTitleRepository {

    @Override
    public void save(BookTitle entity) {
        String sql = "INSERT INTO book_titles (title, author, publisher, isbn, description, category_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, entity.getTitle());
            stmt.setString(2, entity.getAuthor());
            stmt.setString(3, entity.getPublisher());
            stmt.setString(4, entity.getIsbn());
            stmt.setString(5, entity.getDescription());
            stmt.setInt(6, entity.getCategory().getId()); 
            
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
    public void update(BookTitle entity) {
        String sql = "UPDATE book_titles SET title = ?, author = ?, publisher = ?, isbn = ?, " +
                     "description = ?, category_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, entity.getTitle());
            stmt.setString(2, entity.getAuthor());
            stmt.setString(3, entity.getPublisher());
            stmt.setString(4, entity.getIsbn());
            stmt.setString(5, entity.getDescription());
            stmt.setInt(6, entity.getCategory().getId());
            stmt.setInt(7, entity.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM book_titles WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BookTitle findById(Integer id) {
        String sql = "SELECT bt.*, c.name AS category_name, c.description AS category_desc, c.active AS category_active, " +
                     "c.created_at AS category_created_at, c.updated_at AS category_updated_at, c.created_by AS category_created_by " +
                     "FROM book_titles bt " +
                     "INNER JOIN categories c ON bt.category_id = c.id " +
                     "WHERE bt.id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBookTitle(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BookTitle findByIsbn(String isbn) {
        String sql = "SELECT bt.*, c.name AS category_name, c.description AS category_desc, c.active AS category_active, " +
                     "c.created_at AS category_created_at, c.updated_at AS category_updated_at, c.created_by AS category_created_by " +
                     "FROM book_titles bt " +
                     "INNER JOIN categories c ON bt.category_id = c.id " +
                     "WHERE bt.isbn = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBookTitle(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<BookTitle> searchByKeyword(String keyword) {
        List<BookTitle> books = new ArrayList<>();
        String sql = "SELECT bt.*, c.name AS category_name, c.description AS category_desc, c.active AS category_active, " +
                     "c.created_at AS category_created_at, c.updated_at AS category_updated_at, c.created_by AS category_created_by " +
                     "FROM book_titles bt " +
                     "INNER JOIN categories c ON bt.category_id = c.id " +
                     "WHERE bt.title LIKE ? OR bt.author LIKE ? OR bt.isbn LIKE ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String formattedKeyword = "%" + keyword + "%";
            stmt.setString(1, formattedKeyword);
            stmt.setString(2, formattedKeyword);
            stmt.setString(3, formattedKeyword);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBookTitle(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public List<BookTitle> findAll() {
        List<BookTitle> books = new ArrayList<>();
        String sql = "SELECT bt.*, c.name AS category_name, c.description AS category_desc, c.active AS category_active, " +
                     "c.created_at AS category_created_at, c.updated_at AS category_updated_at, c.created_by AS category_created_by " +
                     "FROM book_titles bt " +
                     "INNER JOIN categories c ON bt.category_id = c.id " +
                     "ORDER BY bt.title ASC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                books.add(mapResultSetToBookTitle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    
    private BookTitle mapResultSetToBookTitle(ResultSet rs) throws SQLException {
        Category bookcategory = new Category(
            rs.getString("category_name"),
            rs.getString("category_desc")
        );
        bookcategory.setId(rs.getInt("category_id"));
        bookcategory.setActive(rs.getBoolean("category_active"));
        
        if (rs.getTimestamp("category_created_at") != null) {
            bookcategory.setCreatedAt(rs.getTimestamp("category_created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("category_updated_at") != null) {
            bookcategory.setUpdatedAt(rs.getTimestamp("category_updated_at").toLocalDateTime());
        }
        bookcategory.setCreatedBy(rs.getString("category_created_by"));

        BookTitle bookTitle = new BookTitle(
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("publisher"),
            rs.getString("isbn"),
            rs.getString("description"),
            bookcategory
        );
        
        bookTitle.setId(rs.getInt("id"));
        
        return bookTitle;
    }
}