<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login – MediControl</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="pagina-login">

<main class="card-login">
    <h1>MediControl</h1>
    <p class="subtitulo">Controle de Medicamentos</p>

    <%
        String erro = (String) request.getAttribute("erro");
        if (erro != null) {
    %>
        <div class="alerta-erro"><%= erro %></div>
    <% } %>

    <form id="formLogin" method="post"
          action="${pageContext.request.contextPath}/login"
          novalidate>

        <div class="campo">
            <label for="email">E-mail</label>
            <input type="email" id="email" name="email"
                   value="${emailPreenchido}"
                   placeholder="seu@email.com"
                   autocomplete="email"
                   required>
        </div>

        <div class="campo">
            <label for="senha">Senha</label>
            <input type="password" id="senha" name="senha"
                   placeholder="••••••"
                   autocomplete="current-password"
                   required>
        </div>

        <label class="lembrar">
            <input type="checkbox" name="lembrar"
                   ${ not empty emailPreenchido ? 'checked' : '' }>
            Lembrar meu e-mail
        </label>

        <button type="submit" class="btn-primario">Entrar</button>
    </form>

    <p style="text-align:center; margin-top:1rem; font-size:.875rem; color:#718096">
        Não tem conta? <a href="${pageContext.request.contextPath}/cadastro" style="color:#2b6cb0">Criar conta</a>
    </p>
</main>

<script>
    document.getElementById('formLogin').addEventListener('submit', function (e) {
        var email = document.getElementById('email').value.trim();
        var senha = document.getElementById('senha').value;
        var erros = [];

        var reEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!reEmail.test(email)) {
            erros.push('Informe um e-mail válido.');
        }
        if (senha.length < 6) {
            erros.push('A senha deve ter pelo menos 6 caracteres.');
        }

        if (erros.length > 0) {
            e.preventDefault();
            alert(erros.join('\n'));
        }
    });
</script>

</body>
</html>
