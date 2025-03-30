package com.escola.dao;

import com.escola.model.Avaliacao;
import com.escola.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoDAO {
    public boolean salvar(Avaliacao avaliacao) {
        String sql = "INSERT INTO avaliacoes (aluno_id, satisfacao, satisfacao_infra, comentario) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, avaliacao.getAlunoId());
            stmt.setInt(2, avaliacao.getSatisfacao());
            stmt.setInt(3, avaliacao.getSatisfacaoInfra());
            stmt.setString(4, avaliacao.getComentario());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Avaliacao> listarTodas() {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        String sql = "SELECT * FROM avaliacoes ORDER BY data_avaliacao DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Avaliacao av = new Avaliacao();
                av.setId(rs.getInt("id"));
                av.setAlunoId(rs.getInt("aluno_id"));
                av.setSatisfacao(rs.getInt("satisfacao"));
                av.setSatisfacaoInfra(rs.getInt("satisfacao_infra"));
                av.setComentario(rs.getString("comentario"));
                av.setDataAvaliacao(rs.getTimestamp("data_avaliacao"));
                avaliacoes.add(av);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return avaliacoes;
    }

    public int getTotalAvaliacoes() {
        String sql = "SELECT COUNT(*) as total FROM avaliacoes";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getMediaSatisfacao() {
        String sql = "SELECT AVG((satisfacao + satisfacao_infra)/2.0) as media FROM avaliacoes";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("media");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}