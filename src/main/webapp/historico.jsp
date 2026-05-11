<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.UsuarioSessao, model.RolePessoa" %>
<%
    UsuarioSessao usuario   = (UsuarioSessao) session.getAttribute("usuario");
    String idPosologia      = request.getParameter("posologia");
    String idMedicamentoRef = request.getParameter("medicamento");
    String pacienteCtxParam = request.getParameter("paciente");
    if (idPosologia == null || idPosologia.isBlank()) {
        response.sendRedirect(request.getContextPath() + "/medicamentos.jsp");
        return;
    }
    String medBackLink = request.getContextPath() + "/medicamentos.jsp" +
        (pacienteCtxParam != null ? "?paciente=" + pacienteCtxParam : "");
    String posBackLink = idMedicamentoRef != null
        ? request.getContextPath() + "/posologia.jsp?medicamento=" + idMedicamentoRef +
          (pacienteCtxParam != null ? "&paciente=" + pacienteCtxParam : "")
        : null;
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Histórico – MediControl</title>
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
    <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a>
    <% if (usuario.getPapel() != RolePessoa.CUIDADOR) { %>
    <a href="${pageContext.request.contextPath}/medicamentos.jsp" class="nav-link">Medicamentos</a>
    <% } %>
    <a href="${pageContext.request.contextPath}/catalogo.jsp" class="nav-link">Catálogo</a>
    <% if (usuario.getPapel() == RolePessoa.CUIDADOR) { %>
    <a href="${pageContext.request.contextPath}/vinculos.jsp" class="nav-link">Vínculos</a>
    <% } %>
</nav>

<div class="breadcrumb">
    <a href="<%= medBackLink %>">← Medicamentos</a>
    <% if (posBackLink != null) { %>
    &nbsp;/&nbsp; <a href="<%= posBackLink %>">Posologia</a>
    <% } %>
    &nbsp;/&nbsp; Histórico
</div>

<main class="conteudo">

    <div id="feedback" style="display:none"></div>

    <div class="card">
        <div class="card-header">
            <h2>Histórico de doses</h2>
            <button class="btn-secundario" onclick="toggleForm('form-dose', this)">+ Registrar dose</button>
        </div>

        <div id="form-dose" style="display:none">
            <hr class="separador">
            <div class="campos-grade" style="max-width:400px">
                <div class="campo">
                    <label for="observacao">Observação (opcional)</label>
                    <input type="text" id="observacao" placeholder="Ex: Tomado com alimento">
                </div>
            </div>
            <button class="btn-primario" style="width:auto; padding:.6rem 2rem" onclick="registrarDose()">Registrar</button>
        </div>

        <div id="lista-historico" style="margin-top:1rem">
            <p class="vazio">Carregando...</p>
        </div>
    </div>

</main>

<script>
var ID_POSOLOGIA = '<%= idPosologia %>';
var ID_USUARIO   = <%= usuario.getIdPessoa() %>;

function escHtml(s) {
    if (s == null) return '';
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function toggleForm(id, btn) {
    var el = document.getElementById(id);
    var aberto = el.style.display !== 'none';
    el.style.display = aberto ? 'none' : 'block';
    btn.textContent = aberto ? '+ Registrar dose' : '− Cancelar';
}

function mostrarFeedback(msg, tipo) {
    var el = document.getElementById('feedback');
    el.className = tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro';
    el.textContent = msg;
    el.style.display = 'block';
    setTimeout(function() { el.style.display = 'none'; }, 4000);
}

function statusLabel(s) {
    return {TOMADA:'Tomada', ATRASADA:'Atrasada', PREVISTA:'Prevista', PULADA:'Pulada'}[s] || s;
}

function formatarData(str) {
    if (!str) return '—';
    var d = new Date(str);
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', {hour:'2-digit', minute:'2-digit'});
}

function carregar() {
    fetch('/api/historico?posologia=' + ID_POSOLOGIA)
        .then(function(r) { return r.json(); })
        .then(function(lista) {
            var el = document.getElementById('lista-historico');
            if (!Array.isArray(lista) || lista.length === 0) {
                el.innerHTML = '<p class="vazio">Nenhum registro de dose encontrado.</p>';
                return;
            }

            var html = '<div class="tabela-wrapper"><table><thead><tr>' +
                '<th>Data / Hora</th><th>Status</th><th>Observação</th>' +
                '</tr></thead><tbody>';

            lista.forEach(function(h) {
                html += '<tr>' +
                    '<td>' + formatarData(h.data_hora) + '</td>' +
                    '<td><span class="tag-status tag-' + escHtml(h.status) + '">' + statusLabel(h.status) + '</span></td>' +
                    '<td>' + (h.observacao ? escHtml(h.observacao) : '<span style="color:#a0aec0">—</span>') + '</td>' +
                '</tr>';
            });

            html += '</tbody></table></div>';
            el.innerHTML = html;
        })
        .catch(function() {
            document.getElementById('lista-historico').innerHTML = '<p class="vazio">Erro ao carregar histórico.</p>';
        });
}

function registrarDose() {
    var obs = document.getElementById('observacao').value.trim();
    var body = { id_posologia: parseInt(ID_POSOLOGIA) };
    if (obs) body.observacao = obs;

    fetch('/api/medicamentos/dose', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
        if (res.mensagem) {
            mostrarFeedback(res.mensagem, 'sucesso');
            document.getElementById('observacao').value = '';
            carregar();
        } else {
            mostrarFeedback(res.erro || 'Erro ao registrar dose.', 'erro');
        }
    });
}

carregar();
</script>

</body>
</html>
