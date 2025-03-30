package com.escola.controller;

import com.escola.dao.AvaliacaoDAO;
import com.escola.model.Avaliacao;
import com.escola.model.Aluno;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/salvarAvaliacao")
public class SalvarAvaliacaoServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Aluno aluno = (Aluno) session.getAttribute("aluno");
        
        if (aluno == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setAlunoId(aluno.getId());
        avaliacao.setSatisfacao(Integer.parseInt(req.getParameter("q1")));
        avaliacao.setSatisfacaoInfra(Integer.parseInt(req.getParameter("q2")));
        avaliacao.setComentario(req.getParameter("comentario"));

        AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
        if (avaliacaoDAO.salvar(avaliacao)) {
            resp.sendRedirect("obrigado.jsp");
        } else {
            req.setAttribute("error", "Erro ao salvar avaliação. Tente novamente.");
            req.getRequestDispatcher("avaliacao.jsp").forward(req, resp);
        }
    }
}