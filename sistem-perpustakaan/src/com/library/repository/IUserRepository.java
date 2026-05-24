/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.library.repository;

import com.library.domain.entities.User;

/**
 *
 * @author rafianandra
 */
public interface IUserRepository extends IRepository<User, Integer> {

    // Methode untuk mencari user berdasarkan email
    User findByEmail(String email);
}
