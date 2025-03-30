<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sistema de Avaliação - Login</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .login-container {
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            text-align: center;
            width: 350px;
        }
        h1 {
            color: #333;
            margin-bottom: 30px;
        }
        .login-form {
            margin-bottom: 20px;
        }
        input {
            width: 100%;
            padding: 10px;
            margin: 10px 0;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        button {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-weight: bold;
            transition: background-color 0.3s;
        }
        .btn-aluno {
            background-color: #4CAF50;
            color: white;
        }
        .btn-aluno:hover {
            background-color: #45a049;
        }
        .btn-admin {
            background-color: #2196F3;
            color: white;
        }
        .btn-admin:hover {
            background-color: #0b7dda;
        }
        .error {
            color: #f44336;
            margin-top: 10px;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h1>Sistema de Avaliação</h1>
        
        <form id="loginForm" class="login-form" action="login" method="post">
            <input type="email" name="email" placeholder="E-mail" required>
            <input type="password" name="senha" placeholder="Senha" required>
            
            <button type="submit" class="btn-aluno">Sou Aluno</button>
        </form>
        
        <form action="adminLogin.jsp" method="get">
            <button type="submit" class="btn-admin">Acesso Administrativo</button>
        </form>
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>
    </div>
</body>
</html>