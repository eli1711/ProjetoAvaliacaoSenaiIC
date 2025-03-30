<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Painel Administrativo</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
    <style>
        /* Estilos específicos do dashboard */
        .dashboard-container {
            margin: 20px;
        }
        
        .content-area {
            margin-top: 20px;
            padding: 20px;
            background: #fff;
            border-radius: 5px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
    </style>
</head>
<body>
    <!-- Menu Superior -->
    <nav class="admin-navbar">
        <div class="navbar-brand">Painel Admin</div>
        <div class="navbar-menu">
            <a href="#" onclick="loadContent('cadastro-admin.jsp')" class="navbar-item">Cadastro de ADM</a>
            <a href="#" onclick="loadContent('cadastro-aluno.jsp')" class="navbar-item">Cadastro de Aluno</a>
            <a href="#" onclick="loadContent('pesquisas.jsp')" class="navbar-item">Pesquisas CPA</a>
            <a href="${pageContext.request.contextPath}/logoutAdmin" class="navbar-item logout">Sair</a>
        </div>
    </nav>

    <!-- Área de Conteúdo Dinâmico -->
    <div class="dashboard-container">
        <div class="content-area" id="dynamicContent">
            <h2>Bem-vindo, ${admin.nome}!</h2>
            <p>Selecione uma opção no menu superior para começar.</p>
        </div>
    </div>

    <script>
        function loadContent(page) {
            fetch(page)
                .then(response => response.text())
                .then(html => {
                    document.getElementById('dynamicContent').innerHTML = html;
                })
                .catch(err => {
                    console.error('Erro ao carregar conteúdo:', err);
                });
        }
    </script>
</body>
</html>