package com.escola.dao;

import com.escola.model.Admin;
import com.escola.util.DatabaseUtil;
import java.sql.*;

public class AdminDAO {
    
    public boolean cadastrar(Admin admin) {
        String sql = "INSERT INTO administradores (nome, email, senha_hash, cargo) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, admin.getNome());
            stmt.setString(2, admin.getEmail());
            stmt.setString(3, admin.getSenhaHash());
            stmt.setString(4, admin.getCargo());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar administrador: " + e.getMessage());
            return false;
        }
    }

    public Admin login(String email, String senha) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }
}