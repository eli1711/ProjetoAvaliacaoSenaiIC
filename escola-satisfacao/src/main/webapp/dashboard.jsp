<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.escola.model.Admin" %>
<%
    Admin admin = (Admin) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect("../adminLogin.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dashboard Administrativo</title>
    <style>
        /* Estilos anteriores mantidos */
        .admin-info {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .admin-details {
            font-size: 14px;
            color: #555;
        }
        .logout-btn {
            background-color: #dc3545;
            color: white;
            padding: 8px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .logout-btn:hover {
            background-color: #c82333;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="admin-info">
            <div>
                <h2>Bem-vindo, <%= admin.getNome() %></h2>
                <div class="admin-details">
                    <%= admin.getCargo() %> | <%= admin.getEmail() %> | <%= admin.getTelefone() %>
                </div>
            </div>
            <form action="../logoutAdmin" method="post">
                <button type="submit" class="logout-btn">Sair</button>
            </form>
        </div>
        
        <!-- Restante do conteúdo do dashboard -->
    </div>
</body>
</html>