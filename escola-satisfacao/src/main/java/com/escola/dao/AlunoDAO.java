package com.escola.dao;

import com.escola.model.Aluno;
import com.escola.util.DatabaseUtil;
import java.sql.*;

public class AlunoDAO {
    
    public boolean cadastrar(Aluno aluno) {
        String sql = "INSERT INTO alunos (nome, email, senha_hash, matricula) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getEmail());
            stmt.setString(3, aluno.getSenhaHash());
            stmt.setString(4, aluno.getMatricula());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar aluno: " + e.getMessage());
            return false;
        }
    }

    public Aluno login(String email, String senha) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }
}