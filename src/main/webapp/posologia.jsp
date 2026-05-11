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
    <title>Posologia – MediControl</title>
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
    &nbsp;/&nbsp; Posologia
</div>

<main class="conteudo">

    <div id="feedback" style="display:none"></div>

    <div class="card" id="card-info">
        <h2 id="titulo-med">Posologias</h2>
        <div class="info-bar" id="info-med" style="display:none">
            <div class="info-item"><strong id="info-dosagem">—</strong>Dosagem</div>
            <div class="info-item"><strong id="info-estoque">—</strong>Estoque atual</div>
            <div class="info-item"><strong id="info-status">—</strong>Status</div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Posologias</h2>
            <button class="btn-secundario" onclick="toggleForm('form-posologia', this)">+ Iniciar tratamento</button>
        </div>

        <div id="form-posologia" style="display:none">
            <hr class="separador">
            <div class="campos-grade">
                <div class="campo">
                    <label for="horario">Horário da 1ª dose</label>
                    <input type="time" id="horario" value="08:00">
                </div>
                <div class="campo">
                    <label for="intervalo">Intervalo entre doses (h)</label>
                    <input type="number" id="intervalo" min="1" max="168" placeholder="Ex: 8">
                </div>
                <div class="campo">
                    <label for="qtd-dose">Quantidade por dose</label>
                    <input type="number" id="qtd-dose" min="0.1" step="0.1" placeholder="Ex: 1">
                </div>
                <div class="campo">
                    <label for="duracao">Duração (dias, opcional)</label>
                    <input type="number" id="duracao" min="1" placeholder="Deixe vazio para contínuo">
                </div>
                <div class="campo">
                    <label for="data-inicio">Data de início</label>
                    <input type="date" id="data-inicio">
                </div>
            </div>
            <button class="btn-primario" style="width:auto; padding:.6rem 2rem" onclick="iniciarTratamento()">Iniciar</button>
        </div>

        <div id="lista-posologias" style="margin-top:1rem">
            <p class="vazio">Carregando...</p>
        </div>
    </div>

</main>

<script>
var ID_MED     = '<%= idMedicamento %>';
var ID_USUARIO = <%= usuario.getIdPessoa() %>;
var ID_PACIENTE = '<%= pacienteCtxParam != null ? pacienteCtxParam : "" %>';

document.getElementById('data-inicio').valueAsDate = new Date();

function toggleForm(id, btn) {
    var el = document.getElementById(id);
    var aberto = el.style.display !== 'none';
    el.style.display = aberto ? 'none' : 'block';
    btn.textContent = aberto ? '+ Iniciar tratamento' : '− Cancelar';
}

function mostrarFeedback(msg, tipo) {
    var el = document.getElementById('feedback');
    el.className = tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro';
    el.textContent = msg;
    el.style.display = 'block';
    setTimeout(function() { el.style.display = 'none'; }, 4000);
}

function statusLabel(s) {
    return {EM_USO:'Em uso', EM_ESTOQUE:'Em estoque', ARQUIVADO:'Arquivado', DESCARTADO:'Descartado'}[s] || s;
}

function carregarInfo() {
    fetch('/api/medicamentos/' + ID_MED)
        .then(function(r) { return r.json(); })
        .then(function(m) {
            if (m.erro) return;
            document.getElementById('info-dosagem').textContent  = m.dosagem;
            document.getElementById('info-estoque').textContent  = m.estoque_atual;
            document.getElementById('info-status').textContent   = statusLabel(m.status);
            document.getElementById('info-med').style.display    = 'flex';

            fetch('/api/catalogo/' + m.id_catalogo)
                .then(function(r) { return r.json(); })
                .then(function(c) {
                    if (c.nome) document.getElementById('titulo-med').textContent = c.nome;
                });
        });
}

function carregar() {
    fetch('/api/posologias?medicamento=' + ID_MED)
        .then(function(r) { return r.json(); })
        .then(function(lista) {
            var el = document.getElementById('lista-posologias');
            if (!Array.isArray(lista) || lista.length === 0) {
                el.innerHTML = '<p class="vazio">Nenhuma posologia cadastrada.</p>';
                return;
            }

            var html = '<div class="tabela-wrapper"><table><thead><tr>' +
                '<th>1ª Dose</th><th>Intervalo</th><th>Qtd/dose</th>' +
                '<th>Duração</th><th>Início</th><th>Status</th><th>Ações</th>' +
                '</tr></thead><tbody>';

            lista.forEach(function(p) {
                var status = p.ativo
                    ? '<span class="tag-status tag-ativo">Ativo</span>'
                    : '<span class="tag-status tag-inativo">Inativo</span>';
                var duracao = p.duracao_dias ? p.duracao_dias + ' dias' : 'Contínuo';
                html += '<tr>' +
                    '<td>' + p.horario_primeira_dose + '</td>' +
                    '<td>A cada ' + p.intervalo_horas + 'h</td>' +
                    '<td>' + p.quantidade_por_dose + '</td>' +
                    '<td>' + duracao + '</td>' +
                    '<td>' + p.data_inicio.substring(0, 10) + '</td>' +
                    '<td>' + status + '</td>' +
                    '<td class="acoes">' +
                        '<a href="/historico.jsp?posologia=' + p.id_posologia + '&medicamento=' + ID_MED + (ID_PACIENTE ? '&paciente=' + ID_PACIENTE : '') + '" class="btn-secundario">Histórico</a>' +
                        (p.ativo
                            ? '<button class="btn-perigo" onclick="desativar(' + p.id_posologia + ')">Desativar</button>'
                            : '<button class="btn-secundario" onclick="reativar(' + p.id_posologia + ')">Reativar</button>') +
                    '</td>' +
                '</tr>';
            });

            html += '</tbody></table></div>';
            el.innerHTML = html;
        })
        .catch(function() {
            document.getElementById('lista-posologias').innerHTML = '<p class="vazio">Erro ao carregar posologias.</p>';
        });
}

function iniciarTratamento() {
    var horario   = document.getElementById('horario').value;
    var intervalo = document.getElementById('intervalo').value;
    var qtdDose   = document.getElementById('qtd-dose').value;
    var duracao   = document.getElementById('duracao').value;
    var inicio    = document.getElementById('data-inicio').value;

    if (!horario || !intervalo || !qtdDose) {
        alert('Preencha: horário, intervalo e quantidade por dose.');
        return;
    }

    var body = {
        id_medicamento:     parseInt(ID_MED),
        horario_primeira_dose: horario + ':00',
        intervalo_horas:    parseInt(intervalo),
        quantidade_por_dose: parseFloat(qtdDose),
        data_inicio:        inicio || new Date().toISOString().substring(0, 10)
    };
    if (duracao) body.duracao_dias = parseInt(duracao);

    fetch('/api/medicamentos/tratamento', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
        if (res.mensagem) {
            mostrarFeedback(res.mensagem, 'sucesso');
            document.getElementById('intervalo').value = '';
            document.getElementById('qtd-dose').value  = '';
            document.getElementById('duracao').value   = '';
            carregar();
            carregarInfo();
        } else {
            mostrarFeedback(res.erro || 'Erro ao iniciar tratamento.', 'erro');
        }
    });
}

function reativar(id) {
    if (!confirm('Reativar esta posologia?')) return;
    fetch('/api/posologias/' + id, { method: 'PUT' })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.mensagem) { mostrarFeedback(res.mensagem, 'sucesso'); carregar(); }
            else mostrarFeedback(res.erro || 'Erro ao reativar.', 'erro');
        });
}

function desativar(id) {
    if (!confirm('Desativar esta posologia?')) return;
    fetch('/api/posologias/' + id, { method: 'DELETE' })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.mensagem) { mostrarFeedback(res.mensagem, 'sucesso'); carregar(); }
            else mostrarFeedback(res.erro || 'Erro ao desativar.', 'erro');
        });
}

carregarInfo();
carregar();
</script>

</body>
</html>
