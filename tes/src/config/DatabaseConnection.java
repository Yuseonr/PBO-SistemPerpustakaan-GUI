/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import java.sql.Connection;
import java.sql.DriverManager;


/**
 *
 * @author delli
 */
public class DatabaseConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3306/library_system";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {

        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }
    
    public static void main(String[] args) {

    Connection conn = getConnection();

    if(conn != null){
        System.out.println("Koneksi berhasil");
    } else {
        System.out.println("Koneksi gagal");
    }
}
}


