/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.repository;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.BookCopy;
import com.library.domain.entities.BookTitle;
import com.library.domain.entities.Category;
import com.library.domain.enums.BookCopyStatus;

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
public class BookCopyRepositoryMySQLImpl implements IBookCopyRepository {

    @Override
    public void save(BookCopy entity) {
        String sql = "INSERT INTO book_copies (book_title_id, location, status) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, entity.getBookTitle().getId()); 
            stmt.setString(2, entity.getLocation());
            stmt.setString(3, entity.getStatus().name());
            
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
    public void update(BookCopy entity) {
        String sql = "UPDATE book_copies SET book_title_id = ?, location = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, entity.getBookTitle().getId());
            stmt.setString(2, entity.getLocation());
            stmt.setString(3, entity.getStatus().name());
            stmt.setInt(4, entity.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM book_copies WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BookCopy findById(Integer id) {
        String sql = """
                     SELECT 
                         bc.id AS copy_id, bc.location, bc.status,
                         bt.id AS title_id, bt.title, bt.author, bt.publisher, bt.isbn, bt.description AS title_desc,
                         c.id AS cat_id, c.name AS cat_name, c.description AS cat_desc, c.active AS cat_active
                     FROM book_copies bc
                     INNER JOIN book_titles bt ON bc.book_title_id = bt.id
                     INNER JOIN categories c ON bt.category_id = c.id
                     WHERE bc.id = ?
                     """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBookCopy(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<BookCopy> findAll() {
        List<BookCopy> copies = new ArrayList<>();
        String sql = """
                     SELECT 
                         bc.id AS copy_id, bc.location, bc.status,
                         bt.id AS title_id, bt.title, bt.author, bt.publisher, bt.isbn, bt.description AS title_desc,
                         c.id AS cat_id, c.name AS cat_name, c.description AS cat_desc, c.active AS cat_active
                     FROM book_copies bc
                     INNER JOIN book_titles bt ON bc.book_title_id = bt.id
                     INNER JOIN categories c ON bt.category_id = c.id
                     """;
                     
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                copies.add(mapResultSetToBookCopy(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return copies;
    }

    @Override
    public List<BookCopy> findByBookTitleId(Integer bookTitleId) {
        List<BookCopy> copies = new ArrayList<>();
        String sql = """
                     SELECT 
                         bc.id AS copy_id, bc.location, bc.status,
                         bt.id AS title_id, bt.title, bt.author, bt.publisher, bt.isbn, bt.description AS title_desc,
                         c.id AS cat_id, c.name AS cat_name, c.description AS cat_desc, c.active AS cat_active
                     FROM book_copies bc
                     INNER JOIN book_titles bt ON bc.book_title_id = bt.id
                     INNER JOIN categories c ON bt.category_id = c.id
                     WHERE bc.book_title_id = ?
                     """;
                     
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookTitleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    copies.add(mapResultSetToBookCopy(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return copies;
    }

    @Override
    public int countAvailableByBookTitleId(Integer bookTitleId) {
        String sql = "SELECT COUNT(*) FROM book_copies WHERE book_title_id = ? AND status = 'AVAILABLE'";
        int count = 0;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookTitleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    // @Override
    // public List<BookCopy> findByBookTitleIdAndStatus(Integer bookTitleId, BookCopyStatus status) {
    //     List<BookCopy> copies = new ArrayList<>();
    //     String sql = """
    //                  SELECT 
    //                      bc.id AS copy_id, bc.location, bc.status,
    //                      bt.id AS title_id, bt.title, bt.author, bt.publisher, bt.isbn, bt.description AS title_desc,
    //                      c.id AS cat_id, c.name AS cat_name, c.description AS cat_desc, c.active AS cat_active
    //                  FROM book_copies bc
    //                  INNER JOIN book_titles bt ON bc.book_title_id = bt.id
    //                  INNER JOIN categories c ON bt.category_id = c.id
    //                  WHERE bc.book_title_id = ? AND bc.status = ?
    //                  """;
                     
    //     try (Connection conn = DatabaseConfig.getConnection();
    //          PreparedStatement stmt = conn.prepareStatement(sql)) {
            
    //         stmt.setInt(1, bookTitleId);
    //         stmt.setString(2, status.name());
    //         try (ResultSet rs = stmt.executeQuery()) {
    //             while (rs.next()) {
    //                 copies.add(mapResultSetToBookCopy(rs));
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //     return copies;
    // }

   
    private BookCopy mapResultSetToBookCopy(ResultSet rs) throws SQLException {

        Category category = new Category(
            rs.getString("cat_name"),
            rs.getString("cat_desc")
        );
        category.setId(rs.getInt("cat_id"));
        category.setActive(rs.getBoolean("cat_active"));

        BookTitle bookTitle = new BookTitle(
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("publisher"),
            rs.getString("isbn"),
            rs.getString("title_desc"),
            category 
        );
        bookTitle.setId(rs.getInt("title_id"));

        BookCopy bookCopy = new BookCopy(
            bookTitle, 
            rs.getString("location")
        );
        bookCopy.setId(rs.getInt("copy_id"));
        
        String statusString = rs.getString("status");
        if (statusString != null) {
            bookCopy.setStatus(BookCopyStatus.valueOf(statusString));
        }

        return bookCopy;
    }
}