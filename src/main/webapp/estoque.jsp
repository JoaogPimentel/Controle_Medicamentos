<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.UsuarioSessao, model.RolePessoa" %>
<%
    UsuarioSessao usuario = (UsuarioSessao) session.getAttribute("usuario");
    String idMedicamento  = request.getParameter("medicamento");
    String pacienteCtxParam = request.getParameter("paciente");
    if (idMedicamento == null || idMedicamento.isBlank()) {
        response.sendRedirect(request.getContextPath() + "/medicamentos.jsp");
        return;
    }
    String medBackLink = request.getContextPath() + "/medicamentos.jsp" +
        (pacienteCtxParam != null ? "?paciente=" + pacienteCtxParam : "");
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Estoque – MediControl</title>
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
    &nbsp;/&nbsp; Estoque
</div>

<main class="conteudo">

    <div id="feedback" style="display:none"></div>

    <div class="card" id="card-info" style="display:none">
        <div class="info-bar">
            <div class="info-item"><strong id="info-nome">—</strong>Medicamento</div>
            <div class="info-item"><strong id="info-estoque">—</strong>Estoque atual</div>
            <div class="info-item"><strong id="info-minimo">—</strong>Estoque mínimo</div>
            <div class="info-item"><strong id="info-status">—</strong>Status</div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Movimentações de estoque</h2>
            <button class="btn-secundario" onclick="toggleForm('form-entrada', this)">+ Registrar entrada</button>
        </div>

        <div id="form-entrada" style="display:none">
            <hr class="separador">
            <div class="campos-grade">
                <div class="campo">
                    <label for="tipo">Tipo de entrada</label>
                    <select id="tipo">
                        <option value="ENTRADA_COMPRA">Compra</option>
                        <option value="ENTRADA_AJUSTE">Ajuste</option>
                    </select>
                </div>
                <div class="campo">
                    <label for="quantidade">Quantidade</label>
                    <input type="number" id="quantidade" step="0.1" placeholder="Ex: 30 ou -10">
                </div>
                <div class="campo">
                    <label for="observacao">Observação (opcional)</label>
                    <input type="text" id="observacao" placeholder="Ex: Caixa com 30 comprimidos">
                </div>
            </div>
            <button class="btn-primario" style="width:auto; padding:.6rem 2rem" onclick="registrarEntrada()">Registrar</button>
        </div>

        <div id="lista-estoque" style="margin-top:1rem">
            <p class="vazio">Carregando...</p>
        </div>
    </div>

</main>

<script>
var ID_MED    = '<%= idMedicamento %>';
var ID_USUARIO = <%= usuario.getIdPessoa() %>;

function toggleForm(id, btn) {
    var el = document.getElementById(id);
    var aberto = el.style.display !== 'none';
    el.style.display = aberto ? 'none' : 'block';
    btn.textContent = aberto ? '+ Registrar entrada' : '− Cancelar';
}

function mostrarFeedback(msg, tipo) {
    var el = document.getElementById('feedback');
    el.className = tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro';
    el.textContent = msg;
    el.style.display = 'block';
    setTimeout(function() { el.style.display = 'none'; }, 4000);
}

function tipoLabel(t) {
    return {ENTRADA_COMPRA:'Compra', ENTRADA_AJUSTE:'Ajuste',
            SAIDA_DOSE:'Dose tomada', SAIDA_AJUSTE:'Ajuste saída', SAIDA_DESCARTE:'Descarte'}[t] || t;
}

function statusLabel(s) {
    return {EM_USO:'Em uso', EM_ESTOQUE:'Em estoque', ARQUIVADO:'Arquivado', DESCARTADO:'Descartado'}[s] || s;
}

function formatarData(str) {
    if (!str) return '—';
    var d = new Date(str);
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', {hour:'2-digit', minute:'2-digit'});
}

function carregarInfo() {
    fetch('/api/medicamentos/' + ID_MED)
        .then(function(r) { return r.json(); })
        .then(function(m) {
            if (m.erro) return;
            document.getElementById('info-estoque').textContent = m.estoque_atual;
            document.getElementById('info-minimo').textContent  = m.estoque_minimo;
            document.getElementById('info-status').textContent  = statusLabel(m.status);

            fetch('/api/catalogo/' + m.id_catalogo)
                .then(function(r) { return r.json(); })
                .then(function(c) {
                    document.getElementById('info-nome').textContent = c.nome || '—';
                    document.getElementById('card-info').style.display = 'block';
                });
        });
}

function carregar() {
    fetch('/api/estoque?medicamento=' + ID_MED)
        .then(function(r) { return r.json(); })
        .then(function(lista) {
            var el = document.getElementById('lista-estoque');
            if (!Array.isArray(lista) || lista.length === 0) {
                el.innerHTML = '<p class="vazio">Nenhuma movimentação registrada.</p>';
                return;
            }

            var html = '<div class="tabela-wrapper"><table><thead><tr>' +
                '<th>Data</th><th>Tipo</th><th>Quantidade</th>' +
                '<th>Antes</th><th>Depois</th><th>Observação</th>' +
                '</tr></thead><tbody>';

            lista.forEach(function(mov) {
                var isEntrada = mov.tipo.startsWith('ENTRADA');
                var corQtd = isEntrada ? 'color:#276749' : 'color:#c53030';
                var sinalQtd = isEntrada ? '+' : '-';
                html += '<tr>' +
                    '<td>' + formatarData(mov.data_movimentacao) + '</td>' +
                    '<td>' + tipoLabel(mov.tipo) + '</td>' +
                    '<td style="font-weight:600;' + corQtd + '">' + sinalQtd + mov.quantidade + '</td>' +
                    '<td>' + mov.estoque_antes + '</td>' +
                    '<td>' + mov.estoque_depois + '</td>' +
                    '<td>' + (mov.observacao || '<span style="color:#a0aec0">—</span>') + '</td>' +
                '</tr>';
            });

            html += '</tbody></table></div>';
            el.innerHTML = html;
        })
        .catch(function() {
            document.getElementById('lista-estoque').innerHTML = '<p class="vazio">Erro ao carregar movimentações.</p>';
        });
}

function registrarEntrada() {
    var tipo      = document.getElementById('tipo').value;
    var quantidade = document.getElementById('quantidade').value;
    var obs       = document.getElementById('observacao').value.trim();

    if (!quantidade || parseFloat(quantidade) === 0) {
        alert('Informe uma quantidade válida (diferente de zero).');
        return;
    }

    var body = {
        id_medicamento: parseInt(ID_MED),
        id_responsavel: ID_USUARIO,
        tipo:           tipo,
        quantidade:     parseFloat(quantidade)
    };
    if (obs) body.observacao = obs;

    fetch('/api/estoque', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
        if (res.id_movimentacao) {
            mostrarFeedback('Entrada registrada com sucesso.', 'sucesso');
            document.getElementById('quantidade').value  = '';
            document.getElementById('observacao').value  = '';
            carregar();
            carregarInfo();
        } else {
            mostrarFeedback(res.erro || 'Erro ao registrar entrada.', 'erro');
        }
    });
}

carregarInfo();
carregar();
</script>

</body>
</html>
