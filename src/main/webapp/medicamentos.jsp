<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.UsuarioSessao, model.RolePessoa" %>
<%
    UsuarioSessao usuario = (UsuarioSessao) session.getAttribute("usuario");
    String pacienteParam = request.getParameter("paciente");
    boolean contextoExternoPaciente = pacienteParam != null
        && usuario.getPapel() == RolePessoa.CUIDADOR
        && !pacienteParam.equals(String.valueOf(usuario.getIdPessoa()));
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Medicamentos – MediControl</title>
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
    <a href="${pageContext.request.contextPath}/medicamentos.jsp" class="nav-link ativo">Medicamentos</a>
    <% } %>
    <a href="${pageContext.request.contextPath}/catalogo.jsp" class="nav-link">Catálogo</a>
    <% if (usuario.getPapel() == RolePessoa.CUIDADOR) { %>
    <a href="${pageContext.request.contextPath}/vinculos.jsp" class="nav-link">Vínculos</a>
    <% } %>
</nav>

<% if (contextoExternoPaciente) { %>
<div class="banner-contexto">
    <a href="${pageContext.request.contextPath}/dashboard">← Voltar ao Dashboard</a>
    <span>Gerenciando medicamentos do paciente #<%= pacienteParam %></span>
</div>
<% } %>

<main class="conteudo">

    <div id="feedback" style="display:none"></div>

    <div class="card">
        <div class="card-header">
            <h2>Medicamentos</h2>
            <button class="btn-secundario" onclick="toggleForm('form-adicionar', this)">+ Adicionar</button>
        </div>

        <div id="form-adicionar" style="display:none">
            <hr class="separador">
            <div class="campos-grade">
                <div class="campo">
                    <label for="sel-catalogo">Medicamento</label>
                    <select id="sel-catalogo">
                        <option value="">Carregando catálogo...</option>
                    </select>
                </div>
                <div class="campo">
                    <label for="dosagem">Dosagem</label>
                    <input type="text" id="dosagem" placeholder="Ex: 500mg">
                </div>
                <div class="campo">
                    <label for="estoque-minimo">Estoque mínimo</label>
                    <input type="number" id="estoque-minimo" min="0" step="0.1" placeholder="0">
                </div>
                <div class="campo">
                    <label for="qtd-inicial">Quantidade inicial</label>
                    <input type="number" id="qtd-inicial" min="0" step="0.1" placeholder="0">
                </div>
                <div class="campo">
                    <label for="data-validade">Data de validade</label>
                    <input type="date" id="data-validade">
                </div>
            </div>
            <button class="btn-primario" style="width:auto; padding:.6rem 2rem" onclick="adicionarMedicamento()">Salvar</button>
        </div>

        <div id="lista-medicamentos" style="margin-top:1rem">
            <p class="vazio">Carregando...</p>
        </div>
    </div>

</main>

<script>
var CTX = '${pageContext.request.contextPath}';
var ID_USUARIO = <%= usuario.getIdPessoa() %>;
var PAPEL = '<%= usuario.getPapel() %>';

function escHtml(s) {
    if (s == null) return '';
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

var params = new URLSearchParams(window.location.search);
var idPaciente = params.get('paciente') || ID_USUARIO;
var pacienteCtx = (String(idPaciente) !== String(ID_USUARIO)) ? '&paciente=' + idPaciente : '';

var catalogo = [];

function toggleForm(id, btn) {
    var el = document.getElementById(id);
    var aberto = el.style.display !== 'none';
    el.style.display = aberto ? 'none' : 'block';
    btn.textContent = aberto ? '+ Adicionar' : '− Cancelar';
}

function statusLabel(s) {
    return {EM_USO:'Em uso', EM_ESTOQUE:'Em estoque', ARQUIVADO:'Arquivado', DESCARTADO:'Descartado'}[s] || s;
}

function mostrarFeedback(msg, tipo) {
    var el = document.getElementById('feedback');
    el.className = tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro';
    el.textContent = msg;
    el.style.display = 'block';
    setTimeout(function() { el.style.display = 'none'; }, 4000);
}

function carregarCatalogo() {
    return fetch('/api/catalogo')
        .then(function(r) { return r.json(); })
        .then(function(lista) {
            catalogo = lista;
            var sel = document.getElementById('sel-catalogo');
            sel.innerHTML = '<option value="">Selecione...</option>';
            lista.forEach(function(c) {
                var opt = document.createElement('option');
                opt.value = c.id_catalogo;
                opt.textContent = c.nome + ' (' + c.forma_farmaceutica + ')';
                sel.appendChild(opt);
            });
        });
}

function carregarMedicamentos() {
    fetch('/api/medicamentos?paciente=' + idPaciente)
        .then(function(r) { return r.json(); })
        .then(function(meds) {
            var catMap = {};
            catalogo.forEach(function(c) { catMap[c.id_catalogo] = c; });

            var el = document.getElementById('lista-medicamentos');
            if (!Array.isArray(meds) || meds.length === 0) {
                el.innerHTML = '<p class="vazio">Nenhum medicamento cadastrado.</p>';
                return;
            }

            var html = '<div class="tabela-wrapper"><table><thead><tr>' +
                '<th>Medicamento</th><th>Dosagem</th><th>Estoque / Mín.</th>' +
                '<th>Status</th><th>Validade</th><th>Ações</th>' +
                '</tr></thead><tbody>';

            meds.forEach(function(m) {
                var c = catMap[m.id_catalogo] || {};
                var validade = m.data_validade ? m.data_validade.substring(0, 10) : '—';
                var estoqueClass = m.estoque_atual <= m.estoque_minimo ? ' style="color:#e53e3e;font-weight:600"' : '';
                html += '<tr>' +
                    '<td><strong>' + escHtml(c.nome || '—') + '</strong><br>' +
                    '<small style="color:#a0aec0">' + escHtml(c.principio_ativo || '') + '</small></td>' +
                    '<td>' + escHtml(m.dosagem) + '</td>' +
                    '<td' + estoqueClass + '>' + m.estoque_atual + ' / ' + m.estoque_minimo + '</td>' +
                    '<td><span class="tag-status tag-' + escHtml(m.status) + '">' + statusLabel(m.status) + '</span></td>' +
                    '<td>' + validade + '</td>' +
                    '<td class="acoes">' +
                        '<a href="' + CTX + '/posologia.jsp?medicamento=' + m.id_medicamento + pacienteCtx + '" class="btn-secundario">Posologia</a>' +
                        '<a href="' + CTX + '/estoque.jsp?medicamento=' + m.id_medicamento + pacienteCtx + '" class="btn-secundario">Estoque</a>' +
                        (m.status !== 'ARQUIVADO'
                            ? '<button class="btn-perigo" onclick="arquivar(' + m.id_medicamento + ')">Arquivar</button>'
                            : '<button class="btn-secundario" onclick="desarquivar(' + m.id_medicamento + ')">Desarquivar</button>') +
                        '<button class="btn-perigo" onclick="excluir(' + m.id_medicamento + ')">Excluir</button>' +
                    '</td>' +
                '</tr>';
            });

            html += '</tbody></table></div>';
            el.innerHTML = html;
        })
        .catch(function() {
            document.getElementById('lista-medicamentos').innerHTML = '<p class="vazio">Erro ao carregar medicamentos.</p>';
        });
}

function excluir(id) {
    if (!confirm('Excluir permanentemente este medicamento? Todo o histórico e posologia serão apagados. Esta ação não pode ser desfeita.')) return;
    fetch('/api/medicamentos/' + id + '?force=true', { method: 'DELETE' })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.mensagem) { mostrarFeedback(res.mensagem, 'sucesso'); carregarMedicamentos(); }
            else mostrarFeedback(res.erro || 'Erro ao excluir.', 'erro');
        });
}

function desarquivar(id) {
    if (!confirm('Deseja desarquivar este medicamento?')) return;
    fetch('/api/medicamentos/' + id, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'EM_ESTOQUE' })
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
        if (res.id_medicamento) { mostrarFeedback('Medicamento desarquivado.', 'sucesso'); carregarMedicamentos(); }
        else mostrarFeedback(res.erro || 'Erro ao desarquivar.', 'erro');
    });
}

function arquivar(id) {
    if (!confirm('Deseja arquivar este medicamento?')) return;
    fetch('/api/medicamentos/' + id, { method: 'DELETE' })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.mensagem) { mostrarFeedback(res.mensagem, 'sucesso'); carregarMedicamentos(); }
            else mostrarFeedback(res.erro || 'Erro ao arquivar.', 'erro');
        });
}

function adicionarMedicamento() {
    var idCat    = document.getElementById('sel-catalogo').value;
    var dosagem  = document.getElementById('dosagem').value.trim();
    var estMin   = document.getElementById('estoque-minimo').value;
    var qtdIni   = document.getElementById('qtd-inicial').value;
    var validade = document.getElementById('data-validade').value;

    if (!idCat)   { mostrarFeedback('Selecione um medicamento do catálogo.', 'erro'); return; }
    if (!dosagem) { mostrarFeedback('Informe a dosagem.', 'erro'); return; }
    if (validade && validade <= new Date().toISOString().substring(0, 10)) {
        mostrarFeedback('A data de validade deve ser posterior à data de hoje.', 'erro');
        return;
    }

    var body = {
        id_paciente:       parseInt(idPaciente),
        id_catalogo:       parseInt(idCat),
        dosagem:           dosagem,
        estoque_minimo:    parseFloat(estMin) || 0,
        quantidade_inicial: parseFloat(qtdIni) || 0
    };
    if (validade) body.data_validade = validade;

    fetch('/api/medicamentos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
        if (res.id_medicamento) {
            mostrarFeedback('Medicamento adicionado com sucesso.', 'sucesso');
            document.getElementById('sel-catalogo').value = '';
            document.getElementById('dosagem').value = '';
            document.getElementById('estoque-minimo').value = '';
            document.getElementById('qtd-inicial').value = '';
            document.getElementById('data-validade').value = '';
            carregarMedicamentos();
        } else {
            mostrarFeedback(res.erro || 'Erro ao adicionar.', 'erro');
        }
    });
}

carregarCatalogo().then(carregarMedicamentos);
</script>

</body>
</html>
