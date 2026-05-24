/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.library.repository;

import com.library.config.DatabaseConfig;
import com.library.domain.entities.Admin;
import com.library.domain.entities.Librarian;
import com.library.domain.entities.Member;
import com.library.domain.entities.User;
import com.library.domain.enums.MemberStatus;
import com.library.domain.enums.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rafianandra
 */

public class UserRepositoryMySQLImpl implements IUserRepository {

    // Implementasi metode save untuk menyimpan User ke database MySQL pada tabel "users"
    @Override
    public void save(User entity) {
        String sql = "INSERT INTO users (name, email, password_hash, role, active, "
                + "membership_number, address, phone_number, member_status, "
                + "employee_number, shift_info, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getEmail());
            stmt.setString(3, entity.getPasswordHash());
            stmt.setString(4, entity.getRole().name());
            stmt.setBoolean(5, entity.isActive());
            stmt.setString(12, entity.getCreatedBy());

            if (entity instanceof Member) {
                Member member = (Member) entity;
                stmt.setString(6, member.getMembershipNumber());
                stmt.setString(7, member.getAddress());
                stmt.setString(8, member.getPhoneNumber());
                stmt.setString(9, member.getStatus().name());

                stmt.setNull(10, java.sql.Types.VARCHAR);
                stmt.setNull(11, java.sql.Types.VARCHAR);

            } else if (entity instanceof Librarian) {
                Librarian librarian = (Librarian) entity;
                stmt.setNull(6, java.sql.Types.VARCHAR);
                stmt.setNull(7, java.sql.Types.VARCHAR);
                stmt.setNull(8, java.sql.Types.VARCHAR);
                stmt.setNull(9, java.sql.Types.VARCHAR);

                stmt.setString(10, librarian.getEmployeeNumber());
                stmt.setString(11, librarian.getShiftInfo());

            } else { // Admin
                stmt.setNull(6, java.sql.Types.VARCHAR);
                stmt.setNull(7, java.sql.Types.VARCHAR);
                stmt.setNull(8, java.sql.Types.VARCHAR);
                stmt.setNull(9, java.sql.Types.VARCHAR);
                stmt.setNull(10, java.sql.Types.VARCHAR);
                stmt.setNull(11, java.sql.Types.VARCHAR);
            }

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

    // Implemetasi metode findbyemail untuk mencari User berdasarkan email
    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Kembalikan null jika email tidak ditemukan
    }

    @Override
    public void update(User entity) {
        String sql = "UPDATE users SET name = ?, email = ?, password_hash = ?, active = ?, "
                + "membership_number = ?, address = ?, phone_number = ?, member_status = ?, "
                + "employee_number = ?, shift_info = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getEmail());
            stmt.setString(3, entity.getPasswordHash());
            stmt.setBoolean(4, entity.isActive());

            if (entity instanceof Member) {
                Member m = (Member) entity;
                stmt.setString(5, m.getMembershipNumber());
                stmt.setString(6, m.getAddress());
                stmt.setString(7, m.getPhoneNumber());
                stmt.setString(8, m.getStatus().name());
                stmt.setNull(9, java.sql.Types.VARCHAR);
                stmt.setNull(10, java.sql.Types.VARCHAR);
            } else if (entity instanceof Librarian) {
                Librarian l = (Librarian) entity;
                stmt.setNull(5, java.sql.Types.VARCHAR);
                stmt.setNull(6, java.sql.Types.VARCHAR);
                stmt.setNull(7, java.sql.Types.VARCHAR);
                stmt.setNull(8, java.sql.Types.VARCHAR);
                stmt.setString(9, l.getEmployeeNumber());
                stmt.setString(10, l.getShiftInfo());
            } else {
                stmt.setNull(5, java.sql.Types.VARCHAR);
                stmt.setNull(6, java.sql.Types.VARCHAR);
                stmt.setNull(7, java.sql.Types.VARCHAR);
                stmt.setNull(8, java.sql.Types.VARCHAR);
                stmt.setNull(9, java.sql.Types.VARCHAR);
                stmt.setNull(10, java.sql.Types.VARCHAR);
            }

            stmt.setInt(11, entity.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer id) {
        // Soft delete — nonaktifkan akun, tidak hapus data
        String sql = "UPDATE users SET active = false WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE active = true";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String roleStr = rs.getString("role");
        UserRole role = UserRole.valueOf(roleStr);

        User user = null;

        switch (role) {
            case MEMBER:
                user = new Member(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("membership_number"),
                        rs.getString("address"),
                        rs.getString("phone_number"));

                String dbMemberStatus = rs.getString("member_status");
                if (dbMemberStatus != null) {
                    ((Member) user).setStatus(MemberStatus.valueOf(dbMemberStatus));
                }
                break;

            case LIBRARIAN:
                user = new Librarian(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("employee_number"),
                        rs.getString("shift_info"));
                break;

            case ADMIN:
                user = new Admin(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"));
                break;
        }

        if (user != null) {
            user.setId(rs.getInt("id"));
            user.setActive(rs.getBoolean("active"));

            if (rs.getTimestamp("created_at") != null) {
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            if (rs.getString("created_by") != null) {
                user.setCreatedBy(rs.getString("created_by"));
            }
        }

        return user;
    }

}