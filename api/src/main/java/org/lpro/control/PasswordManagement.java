package org.lpro.control;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordManagement {

    public static String digestPassword(String plainTextPassword) {
        try {
            String hashed = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
            System.out.println(">>>> hashed : " + hashed);
            return hashed;
        } catch (Exception e) {
            throw new RuntimeException("Pb hashing mot de passe", e);
        }
    }
}
