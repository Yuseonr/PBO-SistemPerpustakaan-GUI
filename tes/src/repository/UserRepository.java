/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;

import config.DatabaseConnection;
import domain.Admin;
import domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author delli
 */
public class UserRepository {
     public User findByEmail(String email) {

        try {

            Connection conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM users WHERE email = ?";

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String role = rs.getString("role");

                if (role.equals("ADMIN")) {

                    Admin admin = new Admin();

                    admin.name = rs.getString("name");
                    admin.email = rs.getString("email");
                    admin.passwordHash =
                            rs.getString("password_hash");

                    return admin;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
    
}
