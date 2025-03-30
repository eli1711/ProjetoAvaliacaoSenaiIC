package com.escola.controller;

import com.escola.dao.AdminDAO;
import com.escola.model.Admin;
import com.escola.util.PasswordUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;

@WebServlet("/cadastroAdmin")
public class CadastroAdminServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Admin admin = new Admin();
            admin.setNome(request.getParameter("nome"));
            admin.setEmail(request.getParameter("email"));
            
            // Gera o hash da senha
            String senha = request.getParameter("senha");
            if (senha == null || senha.length() < 6) {
                throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
            }
            admin.setSenhaHash(PasswordUtil.hashSenha(senha));
            
            admin.setCargo(request.getParameter("cargo"));
            
            AdminDAO adminDAO = new AdminDAO();
            boolean sucesso = adminDAO.cadastrar(admin);
            
            if (sucesso) {
                response.sendRedirect(request.getContextPath() + "/sucessoCadastro.jsp");
            } else {
                request.setAttribute("erro", "Falha ao cadastrar. E-mail já existe.");
                request.getRequestDispatcher("/cadastro-admin.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("erro", "Erro no cadastro: " + e.getMessage());
            request.getRequestDispatcher("/cadastro-admin.jsp").forward(request, response);
        }
    }
}