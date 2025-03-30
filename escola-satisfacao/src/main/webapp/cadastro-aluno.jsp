<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cadastro de Aluno</title>
</head>
<body>
    <h2>Cadastro de Aluno</h2>
    <form action="${pageContext.request.contextPath}/cadastroAluno" method="post">
        Nome: <input type="text" name="nome" required><br>
        Email: <input type="email" name="email" required><br>
        Senha: <input type="password" name="senha" required><br>
        Matrícula: <input type="text" name="matricula" required><br>
        <button type="submit">Cadastrar</button>
    </form>
    <c:if test="${not empty erro}">
        <p style="color:red;">${erro}</p>
    </c:if>
</body>
</html>