<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.UsuarioSessao, model.RolePessoa" %>
<%
    UsuarioSessao usuario = (UsuarioSessao) session.getAttribute("usuario");
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Catálogo – MediControl</title>
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
    <a href="${pageContext.request.contextPath}/catalogo.jsp" class="nav-link ativo">Catálogo</a>
    <% if (usuario.getPapel() == RolePessoa.CUIDADOR) { %>
    <a href="${pageContext.request.contextPath}/vinculos.jsp" class="nav-link">Vínculos</a>
    <% } %>
</nav>

<main class="conteudo">

    <div id="feedback" style="display:none"></div>

    <div class="card">
        <div class="card-header">
            <h2>Catálogo de Medicamentos</h2>
            <button class="btn-secundario" onclick="toggleForm('form-adicionar', this)">+ Adicionar</button>
        </div>

        <div id="form-adicionar" style="display:none">
            <hr class="separador">
            <div class="campos-grade">
                <div class="campo">
                    <label for="nome">Nome</label>
                    <input type="text" id="nome" placeholder="Ex: Paracetamol 500mg">
                </div>
                <div class="campo">
                    <label for="principio-ativo">Princípio ativo</label>
                    <input type="text" id="principio-ativo" placeholder="Ex: Paracetamol">
                </div>
                <div class="campo">
                    <label for="forma">Forma farmacêutica</label>
                    <select id="forma">
                        <option value="">Selecione...</option>
                        <option value="COMPRIMIDO">Comprimido</option>
                        <option value="CAPSULA">Cápsula</option>
                        <option value="LIQUIDO_ML">Líquido (mL)</option>
                        <option value="GOTAS">Gotas</option>
                        <option value="INJECAO">Injeção</option>
                        <option value="POMADA">Pomada</option>
                        <option value="SPRAY">Spray</option>
                        <option value="ADESIVO">Adesivo</option>
                        <option value="OUTRO">Outro</option>
                    </select>
                </div>
            </div>
            <button class="btn-primario" style="width:auto; padding:.6rem 2rem" onclick="adicionarCatalogo()">Salvar</button>
        </div>

        <hr class="separador">

        <div class="campo" style="max-width:300px; margin-bottom:1rem">
            <label for="busca">Buscar por nome</label>
            <input type="text" id="busca" placeholder="Digite para filtrar...">
        </div>

        <div id="lista-catalogo">
            <p class="vazio">Carregando...</p>
        </div>
    </div>

    <div id="modal-editar" style="display:none" class="card">
        <h2>Editar entrada do catálogo</h2>
        <input type="hidden" id="edit-id">
        <div class="campos-grade">
            <div class="campo">
                <label for="edit-nome">Nome</label>
                <input type="text" id="edit-nome">
            </div>
            <div class="campo">
                <label for="edit-principio">Princípio ativo</label>
                <input type="text" id="edit-principio">
            </div>
            <div class="campo">
                <label for="edit-forma">Forma farmacêutica</label>
                <select id="edit-forma">
                    <option value="COMPRIMIDO">Comprimido</option>
                    <option value="CAPSULA">Cápsula</option>
                    <option value="LIQUIDO_ML">Líquido (mL)</option>
                    <option value="GOTAS">Gotas</option>
                    <option value="INJECAO">Injeção</option>
                    <option value="POMADA">Pomada</option>
                    <option value="SPRAY">Spray</option>
                    <option value="ADESIVO">Adesivo</option>
                    <option value="OUTRO">Outro</option>
                </select>
            </div>
        </div>
        <div style="display:flex; gap:.5rem">
            <button class="btn-primario" style="width:auto; padding:.6rem 2rem" onclick="salvarEdicao()">Salvar</button>
            <button class="btn-secundario" onclick="fecharEdicao()">Cancelar</button>
            <button class="btn-perigo" style="width:auto; padding:.6rem 2rem; margin-left:auto" onclick="excluirCatalogo()">Excluir</button>
        </div>
    </div>

</main>

<script>
var catalogo = [];

function toggleForm(id, btn) {
    var el = document.getElementById(id);
    var aberto = el.style.display !== 'none';
    el.style.display = aberto ? 'none' : 'block';
    btn.textContent = aberto ? '+ Adicionar' : '− Cancelar';
}

function mostrarFeedback(msg, tipo) {
    var el = document.getElementById('feedback');
    el.className = tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro';
    el.textContent = msg;
    el.style.display = 'block';
    setTimeout(function() { el.style.display = 'none'; }, 4000);
}

function formaLabel(f) {
    return {COMPRIMIDO:'Comprimido', CAPSULA:'Cápsula', LIQUIDO_ML:'Líquido (mL)',
            GOTAS:'Gotas', INJECAO:'Injeção', POMADA:'Pomada',
            SPRAY:'Spray', ADESIVO:'Adesivo', OUTRO:'Outro'}[f] || f;
}

function renderizarLista(lista) {
    var el = document.getElementById('lista-catalogo');
    if (lista.length === 0) {
        el.innerHTML = '<p class="vazio">Nenhum item encontrado.</p>';
        return;
    }
    var html = '<div class="tabela-wrapper"><table><thead><tr>' +
        '<th>Nome</th><th>Princípio ativo</th><th>Forma farmacêutica</th><th>Cadastro</th><th>Ações</th>' +
        '</tr></thead><tbody>';
    lista.forEach(function(c) {
        html += '<tr>' +
            '<td><strong>' + c.nome + '</strong></td>' +
            '<td>' + c.principio_ativo + '</td>' +
            '<td>' + formaLabel(c.forma_farmaceutica) + '</td>' +
            '<td>' + (c.data_cadastro ? c.data_cadastro.substring(0,10) : '—') + '</td>' +
            '<td class="acoes">' +
                '<button class="btn-secundario" onclick="abrirEdicao(' + c.id_catalogo + ')">Editar</button>' +
            '</td>' +
        '</tr>';
    });
    html += '</tbody></table></div>';
    el.innerHTML = html;
}

function carregar() {
    fetch('/api/catalogo')
        .then(function(r) { return r.json(); })
        .then(function(lista) {
            catalogo = lista;
            renderizarLista(lista);
        })
        .catch(function() {
            document.getElementById('lista-catalogo').innerHTML = '<p class="vazio">Erro ao carregar catálogo.</p>';
        });
}

document.getElementById('busca').addEventListener('input', function() {
    var q = this.value.toLowerCase();
    var filtrados = catalogo.filter(function(c) {
        return c.nome.toLowerCase().includes(q) || c.principio_ativo.toLowerCase().includes(q);
    });
    renderizarLista(filtrados);
});

function adicionarCatalogo() {
    var nome     = document.getElementById('nome').value.trim();
    var princAti = document.getElementById('principio-ativo').value.trim();
    var forma    = document.getElementById('forma').value;

    if (!nome || !princAti || !forma) { alert('Preencha todos os campos.'); return; }

    fetch('/api/catalogo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome: nome, principio_ativo: princAti, forma_farmaceutica: forma })
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
        if (res.id_catalogo) {
            mostrarFeedback('Medicamento adicionado ao catálogo.', 'sucesso');
            document.getElementById('nome').value = '';
            document.getElementById('principio-ativo').value = '';
            document.getElementById('forma').value = '';
            carregar();
        } else {
            mostrarFeedback(res.erro || 'Erro ao adicionar.', 'erro');
        }
    });
}

function abrirEdicao(id) {
    var c = catalogo.find(function(x) { return x.id_catalogo === id; });
    if (!c) return;
    document.getElementById('edit-id').value      = c.id_catalogo;
    document.getElementById('edit-nome').value    = c.nome;
    document.getElementById('edit-principio').value = c.principio_ativo;
    document.getElementById('edit-forma').value   = c.forma_farmaceutica;
    document.getElementById('modal-editar').style.display = 'block';
    document.getElementById('modal-editar').scrollIntoView({ behavior: 'smooth' });
}

function fecharEdicao() {
    document.getElementById('modal-editar').style.display = 'none';
}

function excluirCatalogo() {
    var id = document.getElementById('edit-id').value;
    var nome = document.getElementById('edit-nome').value;
    if (!confirm('Excluir "' + nome + '" do catálogo? Esta ação não pode ser desfeita.')) return;
    fetch('/api/catalogo/' + id, { method: 'DELETE' })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.ok) {
                mostrarFeedback('Medicamento excluído do catálogo.', 'sucesso');
                fecharEdicao();
                carregar();
            } else {
                mostrarFeedback(res.erro || 'Erro ao excluir.', 'erro');
            }
        })
        .catch(function() {
            mostrarFeedback('Erro ao excluir. Recompile o servidor.', 'erro');
        });
}

function salvarEdicao() {
    var id      = document.getElementById('edit-id').value;
    var nome    = document.getElementById('edit-nome').value.trim();
    var princAti = document.getElementById('edit-principio').value.trim();
    var forma   = document.getElementById('edit-forma').value;

    if (!nome || !princAti || !forma) { alert('Preencha todos os campos.'); return; }

    fetch('/api/catalogo/' + id, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome: nome, principio_ativo: princAti, forma_farmaceutica: forma })
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
        if (res.id_catalogo) {
            mostrarFeedback('Catálogo atualizado.', 'sucesso');
            fecharEdicao();
            carregar();
        } else {
            mostrarFeedback(res.erro || 'Erro ao atualizar.', 'erro');
        }
    });
}

carregar();
</script>

</body>
</html>
