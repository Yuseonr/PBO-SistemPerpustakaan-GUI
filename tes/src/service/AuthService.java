/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import domain.User;
import repository.UserRepository;
/**
 *
 * @author delli
 */
public class AuthService {
    private UserRepository userRepository =
            new UserRepository();

    public User login(String email, String password) {

        User user =
                userRepository.findByEmail(email);

        if (user != null) {

            if (user.passwordHash.equals(password)) {

                return user;
            }
        }

        return null;
    }
    
}
