<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.UsuarioSessao, model.Alerta, model.RolePessoa, java.util.List" %>
<%
    UsuarioSessao usuario = (UsuarioSessao) request.getAttribute("usuario");
    @SuppressWarnings("unchecked")
    List<Alerta> alertas  = (List<Alerta>) request.getAttribute("alertas");
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard – MediControl</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<header class="cabecalho">
    <h1>MediControl</h1>
    <nav class="nav-usuario">
        <span class="badge-papel"><%= usuario.getPapel() %></span>
        <span>Olá, <strong><%= usuario.getNome() %></strong></span>
        <a href="${pageContext.request.contextPath}/api/auth/logout" class="btn-sair">Sair</a>
    </nav>
</header>

<nav class="nav-principal">
    <a href="${pageContext.request.contextPath}/dashboard" class="nav-link ativo">Dashboard</a>
    <a href="${pageContext.request.contextPath}/medicamentos.jsp" class="nav-link">Medicamentos</a>
    <a href="${pageContext.request.contextPath}/catalogo.jsp" class="nav-link">Catálogo</a>
    <% if (usuario.getPapel() == RolePessoa.CUIDADOR) { %>
    <a href="${pageContext.request.contextPath}/vinculos.jsp" class="nav-link">Vínculos</a>
    <% } %>
</nav>

<main class="conteudo">

    <section class="card">
        <h2>Alertas não lidos
            <span class="badge-count"><%= alertas.size() %></span>
        </h2>

        <% if (alertas.isEmpty()) { %>
            <p class="vazio">Nenhum alerta pendente.</p>
        <% } else { %>
            <ul class="lista-alertas">
            <% for (Alerta a : alertas) { %>
                <li class="alerta-item">
                    <span class="tipo-alerta"><%= a.getTipo() %></span>
                    <span class="mensagem-alerta"><%= a.getMensagem() %></span>
                    <small class="data-alerta"><%= a.getData_geracao() %></small>
                </li>
            <% } %>
            </ul>
        <% } %>
    </section>

</main>

<script>
    (function () {
        var total = document.querySelectorAll('.alerta-item').length;
        if (total > 0) {
            document.title = '(' + total + ') Dashboard – MediControl';
        }
    })();
</script>

</body>
</html>
