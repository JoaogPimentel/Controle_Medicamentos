<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.UsuarioSessao, model.Alerta, model.RolePessoa, model.Pessoa, java.util.List, java.util.Map, java.util.Collections" %>
<%
    UsuarioSessao usuario = (UsuarioSessao) request.getAttribute("usuario");
    boolean eCuidador = usuario.getPapel() == RolePessoa.CUIDADOR;

    @SuppressWarnings("unchecked")
    List<Alerta> alertas = (List<Alerta>) request.getAttribute("alertas");
    if (alertas == null) alertas = Collections.emptyList();

    @SuppressWarnings("unchecked")
    List<Pessoa> pacientes = (List<Pessoa>) request.getAttribute("pacientes");
    if (pacientes == null) pacientes = Collections.emptyList();

    @SuppressWarnings("unchecked")
    Map<Integer, Integer> alertasPorPaciente = (Map<Integer, Integer>) request.getAttribute("alertasPorPaciente");
    if (alertasPorPaciente == null) alertasPorPaciente = new java.util.HashMap<>();
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
    <% if (!eCuidador) { %>
    <a href="${pageContext.request.contextPath}/medicamentos.jsp" class="nav-link">Medicamentos</a>
    <% } %>
    <a href="${pageContext.request.contextPath}/catalogo.jsp" class="nav-link">Catálogo</a>
    <% if (eCuidador) { %>
    <a href="${pageContext.request.contextPath}/vinculos.jsp" class="nav-link">Vínculos</a>
    <% } %>
</nav>

<main class="conteudo">

<% if (eCuidador) { %>

    <section class="card">
        <div class="card-header">
            <h2>Meus Pacientes <span class="badge-count"><%= pacientes.size() %></span></h2>
            <a href="${pageContext.request.contextPath}/vinculos.jsp" class="btn-secundario">Gerenciar vínculos</a>
        </div>

        <% if (pacientes.isEmpty()) { %>
            <p class="vazio">Nenhum paciente vinculado.
                <a href="${pageContext.request.contextPath}/vinculos.jsp" style="color:#2b6cb0">Vincular paciente</a>
            </p>
        <% } else { %>
            <div class="grade-pacientes">
            <% for (Pessoa p : pacientes) {
                int numAlertas = alertasPorPaciente.getOrDefault(p.getId_pessoa(), 0);
            %>
                <div class="card-paciente">
                    <div class="card-paciente-topo">
                        <strong class="nome-paciente"><%= p.getNome() %></strong>
                        <% if (numAlertas > 0) { %>
                            <span class="badge-alertas-paciente"><%= numAlertas %></span>
                        <% } %>
                    </div>
                    <p class="email-paciente"><%= p.getEmail() != null ? p.getEmail() : "" %></p>
                    <div class="acoes-paciente">
                        <a href="${pageContext.request.contextPath}/medicamentos.jsp?paciente=<%= p.getId_pessoa() %>"
                           class="btn-secundario">Medicamentos</a>
                    </div>
                </div>
            <% } %>
            </div>
        <% } %>
    </section>

<% } else { %>

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
                    <span class="mensagem-alerta">
                        <% if (a.getNomeMedicamento() != null) { %>
                            <strong><%= a.getNomeMedicamento() %></strong> —
                        <% } %>
                        <%= a.getMensagem() %>
                    </span>
                    <small class="data-alerta"><%= a.getData_geracao() %></small>
                </li>
            <% } %>
            </ul>
        <% } %>
    </section>

<% } %>

</main>

<script>
    (function () {
        <% if (!eCuidador) { %>
        var total = document.querySelectorAll('.alerta-item').length;
        if (total > 0) document.title = '(' + total + ') Dashboard – MediControl';
        <% } %>
    })();
</script>

</body>
</html>
