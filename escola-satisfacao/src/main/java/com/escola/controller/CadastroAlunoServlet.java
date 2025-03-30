package com.escola.controller;

import com.escola.dao.AlunoDAO;
import com.escola.model.Aluno;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;

@WebServlet("/cadastroAluno")
public class CadastroAlunoServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Aluno aluno = new Aluno();
        aluno.setNome(request.getParameter("nome"));
        aluno.setEmail(request.getParameter("email"));
        aluno.setSenhaHash(request.getParameter("senha"));
        aluno.setMatricula(request.getParameter("matricula"));
        
        AlunoDAO alunoDAO = new AlunoDAO();
        boolean sucesso = alunoDAO.cadastrar(aluno);
        
        if (sucesso) {
            response.sendRedirect("sucessoCadastro.jsp");
        } else {
            request.setAttribute("erro", "Falha ao cadastrar aluno");
            request.getRequestDispatcher("cadastroAluno.jsp").forward(request, response);
        }
    }
}