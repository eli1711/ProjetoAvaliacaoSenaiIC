package com.escola.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hashSenha(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt());
    }
    
    public static boolean verificarSenha(String senha, String hash) {
        try {
            return BCrypt.checkpw(senha, hash);
        } catch (Exception e) {
            return false;
        }
    }
}