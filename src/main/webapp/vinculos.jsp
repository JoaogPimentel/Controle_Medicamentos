<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.UsuarioSessao, model.RolePessoa" %>
<%
    UsuarioSessao usuario = (UsuarioSessao) session.getAttribute("usuario");
    if (usuario.getPapel() != RolePessoa.CUIDADOR) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vínculos – MediControl</title>
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
    <a href="${pageContext.request.contextPath}/catalogo.jsp" class="nav-link">Catálogo</a>
    <a href="${pageContext.request.contextPath}/vinculos.jsp" class="nav-link ativo">Vínculos</a>
</nav>

<main class="conteudo">

    <div id="feedback" style="display:none"></div>

    <div class="card">
        <div class="card-header">
            <h2>Meus vínculos com pacientes</h2>
            <button class="btn-secundario" onclick="toggleForm()">+ Novo vínculo</button>
        </div>

        <div id="form-vinculo" style="display:none">
            <hr class="separador">
            <p style="font-size:.875rem; color:#718096; margin-bottom:.75rem">
                Busque o paciente pelo e-mail cadastrado no sistema.
            </p>

            <div style="display:flex; gap:.5rem; align-items:flex-end; max-width:480px; margin-bottom:.75rem">
                <div class="campo" style="flex:1; margin-bottom:0">
                    <label for="email-paciente">E-mail do paciente</label>
                    <input type="email" id="email-paciente" placeholder="paciente@email.com">
                </div>
                <button class="btn-secundario" onclick="buscarPaciente()" style="white-space:nowrap; height:38px">Buscar</button>
            </div>

            <div id="resultado-busca" style="display:none; margin-bottom:.75rem">
                <div style="background:#f0fff4; border:1px solid #9ae6b4; border-radius:6px; padding:.6rem 1rem; display:flex; align-items:center; justify-content:space-between">
                    <span id="nome-paciente-encontrado" style="color:#276749; font-weight:600"></span>
                    <button class="btn-primario" style="width:auto; padding:.4rem 1.5rem" onclick="criarVinculo()">Vincular</button>
                </div>
            </div>
        </div>

        <div id="lista-vinculos" style="margin-top:1rem">
            <p class="vazio">Carregando...</p>
        </div>
    </div>

</main>

<script>
var CTX = '${pageContext.request.contextPath}';
var ID_USUARIO = <%= usuario.getIdPessoa() %>;
var pacienteEncontrado = null;

function escHtml(s) {
    if (s == null) return '';
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
var formAberto = false;

function toggleForm() {
    formAberto = !formAberto;
    document.getElementById('form-vinculo').style.display = formAberto ? 'block' : 'none';
    document.querySelector('[onclick="toggleForm()"]').textContent = formAberto ? '− Cancelar' : '+ Novo vínculo';
    if (!formAberto) {
        document.getElementById('email-paciente').value = '';
        document.getElementById('resultado-busca').style.display = 'none';
        pacienteEncontrado = null;
    }
}

function mostrarFeedback(msg, tipo) {
    var el = document.getElementById('feedback');
    el.className = tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro';
    el.textContent = msg;
    el.style.display = 'block';
    setTimeout(function() { el.style.display = 'none'; }, 4000);
}

function buscarPaciente() {
    var email = document.getElementById('email-paciente').value.trim();
    if (!email) { mostrarFeedback('Informe o e-mail do paciente.', 'erro'); return; }

    fetch('/api/vinculos/buscar-paciente?email=' + encodeURIComponent(email))
        .then(function(r) { return r.json().then(function(d) { return { status: r.status, data: d }; }); })
        .then(function(res) {
            if (res.status === 200) {
                pacienteEncontrado = res.data;
                document.getElementById('nome-paciente-encontrado').textContent =
                    res.data.nome + ' (ID ' + res.data.id_pessoa + ')';
                document.getElementById('resultado-busca').style.display = 'block';
            } else {
                pacienteEncontrado = null;
                document.getElementById('resultado-busca').style.display = 'none';
                mostrarFeedback(res.data.erro || 'Paciente não encontrado.', 'erro');
            }
        })
        .catch(function() { mostrarFeedback('Erro de conexão.', 'erro'); });
}

function carregar() {
    fetch('/api/vinculos?cuidador=' + ID_USUARIO)
        .then(function(r) { return r.json(); })
        .then(function(lista) {
            var el = document.getElementById('lista-vinculos');
            if (!Array.isArray(lista) || lista.length === 0) {
                el.innerHTML = '<p class="vazio">Nenhum vínculo ativo encontrado.</p>';
                return;
            }

            var html = '<div class="tabela-wrapper"><table><thead><tr>' +
                '<th>Paciente</th><th>Status</th><th>Ações</th>' +
                '</tr></thead><tbody>';

            lista.forEach(function(v) {
                var nomePaciente = escHtml(v.nome_paciente || ('Paciente #' + v.id_paciente));
                html += '<tr>' +
                    '<td><strong>' + nomePaciente + '</strong></td>' +
                    '<td><span class="tag-status ' + (v.ativo ? 'tag-ativo' : 'tag-inativo') + '">' +
                        (v.ativo ? 'Ativo' : 'Encerrado') +
                    '</span></td>' +
                    '<td class="acoes">' +
                        '<a href="' + CTX + '/medicamentos.jsp?paciente=' + v.id_paciente + '" class="btn-secundario">Medicamentos</a>' +
                        (v.ativo ? '<button class="btn-perigo" onclick="encerrar(' + v.id_vinculo + ')">Encerrar</button>' : '') +
                    '</td>' +
                '</tr>';
            });

            html += '</tbody></table></div>';
            el.innerHTML = html;
        })
        .catch(function() {
            document.getElementById('lista-vinculos').innerHTML = '<p class="vazio">Erro ao carregar vínculos.</p>';
        });
}

function criarVinculo() {
    if (!pacienteEncontrado) { mostrarFeedback('Busque um paciente primeiro.', 'erro'); return; }

    fetch('/api/vinculos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id_paciente: pacienteEncontrado.id_pessoa })
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
        if (res.id_vinculo) {
            mostrarFeedback('Vínculo criado com sucesso.', 'sucesso');
            toggleForm();
            carregar();
        } else {
            mostrarFeedback(res.erro || 'Erro ao criar vínculo.', 'erro');
        }
    });
}

function encerrar(id) {
    if (!confirm('Encerrar este vínculo?')) return;
    fetch('/api/vinculos/' + id, { method: 'DELETE' })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.mensagem) { mostrarFeedback(res.mensagem, 'sucesso'); carregar(); }
            else mostrarFeedback(res.erro || 'Erro ao encerrar vínculo.', 'erro');
        });
}

carregar();
</script>

</body>
</html>
