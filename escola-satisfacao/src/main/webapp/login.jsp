<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Avaliação - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/login.css">
</head>
<body>
    <main class="login-container">
        <header class="login-header">
            <h1 class="login-title">Sistema de Avaliação</h1>
            <img src="${pageContext.request.contextPath}/resources/images/logo.png" alt="Logo da Escola" class="login-logo">
        </header>

        <form action="${pageContext.request.contextPath}/login" method="post" class="login-form" id="loginForm">
            <div class="form-group">
                <label for="tipoUsuario" class="form-label">Tipo de Usuário</label>
                <select name="tipoUsuario" id="tipoUsuario" class="form-select" required>
                    <option value="" disabled selected>Selecione seu perfil</option>
                    <option value="aluno">Aluno</option>
                    <option value="admin">Administrador</option>
                </select>
            </div>
            
            <div class="form-group">
                <label for="email" class="form-label">E-mail</label>
                <input type="email" id="email" name="email" class="form-input" required 
                       placeholder="Digite seu e-mail institucional">
            </div>
            
            <div class="form-group">
                <label for="senha" class="form-label">Senha</label>
                <div class="password-container">
                    <input type="password" id="senha" name="senha" class="form-input" required
                           placeholder="Digite sua senha" minlength="6">
                    <button type="button" class="toggle-password" aria-label="Mostrar senha">
                        <i class="eye-icon"></i>
                    </button>
                </div>
            </div>
            
            <button type="submit" class="btn-submit">Acessar Sistema</button>
            
            <div class="login-links">
                <a href="${pageContext.request.contextPath}/recuperar-senha" class="forgot-password">Esqueci minha senha</a>
                <a href="${pageContext.request.contextPath}/cadastro" class="register-link">Não tem conta? Cadastre-se</a>
            </div>
        </form>

        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert">
                <i class="alert-icon"></i>
                ${error}
            </div>
        </c:if>
    </main>

    <script src="${pageContext.request.contextPath}/resources/js/login.js"></script>
</body>
</html>