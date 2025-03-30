package com.escola.controller;

import com.escola.dao.AdminDAO;
import com.escola.dao.AlunoDAO;
import com.escola.model.Admin;
import com.escola.model.Aluno;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String email = req.getParameter("email");
        String senha = req.getParameter("senha");
        String tipoUsuario = req.getParameter("tipoUsuario"); // "aluno" ou "admin"

        if (tipoUsuario == null) {
            req.setAttribute("error", "Selecione o tipo de usuário");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
            return;
        }

        try {
            if ("aluno".equals(tipoUsuario)) {
                processarLoginAluno(email, senha, req, resp);
            } else if ("admin".equals(tipoUsuario)) {
                processarLoginAdmin(email, senha, req, resp);
            } else {
                req.setAttribute("error", "Tipo de usuário inválido");
                req.getRequestDispatcher("login.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            req.setAttribute("error", "Erro durante o login: " + e.getMessage());
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        }
    }

    private void processarLoginAluno(String email, String senha, 
                                   HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        AlunoDAO alunoDAO = new AlunoDAO();
        Aluno aluno = alunoDAO.login(email, senha);

        if (aluno != null) {
            HttpSession session = req.getSession();
            session.setAttribute("usuario", aluno);
            session.setAttribute("tipoUsuario", "aluno");
            resp.sendRedirect(req.getContextPath() + "/aluno/avaliacao.jsp");
        } else {
            req.setAttribute("error", "Credenciais de aluno inválidas");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        }
    }

    private void processarLoginAdmin(String email, String senha, 
                                   HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        AdminDAO adminDAO = new AdminDAO();
        Admin admin = adminDAO.login(email, senha);

        if (admin != null) {
            HttpSession session = req.getSession();
            session.setAttribute("usuario", admin);
            session.setAttribute("tipoUsuario", "admin");
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard.jsp");
        } else {
            req.setAttribute("error", "Credenciais de administrador inválidas");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        }
    }
}