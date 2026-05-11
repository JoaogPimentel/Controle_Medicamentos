<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro – MediControl</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="pagina-login">

<main class="card-login">
    <h1>MediControl</h1>
    <p class="subtitulo">Criar conta</p>

    <div id="alerta-erro" class="alerta-erro" style="display:none"></div>
    <div id="alerta-sucesso" class="mensagem-sucesso" style="display:none"></div>

    <div class="campo">
        <label for="role">Tipo de conta</label>
        <select id="role" onchange="alternarCamposCuidador()">
            <option value="PACIENTE">Paciente</option>
            <option value="CUIDADOR">Cuidador</option>
        </select>
    </div>

    <div class="campo">
        <label for="nome">Nome completo</label>
        <input type="text" id="nome" placeholder="Seu nome" autocomplete="name">
    </div>

    <div class="campo">
        <label for="email">E-mail</label>
        <input type="email" id="email" placeholder="seu@email.com" autocomplete="email">
    </div>

    <div class="campo">
        <label for="senha">Senha</label>
        <input type="password" id="senha" placeholder="Mínimo 6 caracteres" autocomplete="new-password">
    </div>

    <div class="campo">
        <label for="data-nascimento">Data de nascimento</label>
        <input type="date" id="data-nascimento">
    </div>

    <div class="campo">
        <label for="telefone">Telefone (opcional)</label>
        <input type="tel" id="telefone" placeholder="(11) 99999-9999">
    </div>

    <div id="campos-cuidador" style="display:none">
        <label class="lembrar" style="margin-bottom:.75rem">
            <input type="checkbox" id="profissional-saude" onchange="alternarRegistroProfissional()">
            Sou profissional de saúde (médico, enfermeiro, etc.)
        </label>

        <div id="campo-registro" class="campo" style="display:none">
            <label for="registro-profissional">Registro profissional <span style="color:#e53e3e">*</span></label>
            <input type="text" id="registro-profissional" placeholder="Ex: CRM 12345 / COREN 98765">
        </div>
    </div>

    <button class="btn-primario" onclick="cadastrar()">Criar conta</button>

    <p style="text-align:center; margin-top:1rem; font-size:.875rem; color:#718096">
        Já tem conta? <a href="${pageContext.request.contextPath}/login" style="color:#2b6cb0">Entrar</a>
    </p>
</main>

<script>
function alternarCamposCuidador() {
    var role = document.getElementById('role').value;
    var mostrar = role === 'CUIDADOR';
    document.getElementById('campos-cuidador').style.display = mostrar ? 'block' : 'none';
    if (!mostrar) {
        document.getElementById('profissional-saude').checked = false;
        document.getElementById('campo-registro').style.display = 'none';
        document.getElementById('registro-profissional').value = '';
    }
}

function alternarRegistroProfissional() {
    var isProfissional = document.getElementById('profissional-saude').checked;
    document.getElementById('campo-registro').style.display = isProfissional ? 'block' : 'none';
    if (!isProfissional) document.getElementById('registro-profissional').value = '';
}

function cadastrar() {
    var role     = document.getElementById('role').value;
    var nome     = document.getElementById('nome').value.trim();
    var email    = document.getElementById('email').value.trim();
    var senha    = document.getElementById('senha').value;
    var dataNasc = document.getElementById('data-nascimento').value;
    var telefone = document.getElementById('telefone').value.trim();

    var erros = [];
    if (!nome)                                                  erros.push('Informe seu nome.');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))            erros.push('Informe um e-mail válido.');
    if (senha.length < 6)                                       erros.push('A senha deve ter pelo menos 6 caracteres.');
    if (!dataNasc)                                              erros.push('Informe sua data de nascimento.');

    if (role === 'CUIDADOR') {
        var isProfissional = document.getElementById('profissional-saude').checked;
        var reg = document.getElementById('registro-profissional').value.trim();
        if (isProfissional && !reg) {
            erros.push('Registro profissional é obrigatório para profissionais de saúde.');
        }
    }

    if (erros.length > 0) {
        mostrarErro(erros.join(' '));
        return;
    }

    var body = { role: role, nome: nome, email: email, senha: senha, data_nascimento: dataNasc };
    if (telefone) body.telefone = telefone;

    if (role === 'CUIDADOR') {
        var isProfissional = document.getElementById('profissional-saude').checked;
        body.profissional_saude = isProfissional.toString();
        var reg = document.getElementById('registro-profissional').value.trim();
        if (reg) body.registro_profissional = reg;
    }

    fetch('${pageContext.request.contextPath}/api/auth/cadastrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(function(r) { return r.json().then(function(d) { return { status: r.status, data: d }; }); })
    .then(function(res) {
        if (res.status === 201) {
            document.getElementById('alerta-erro').style.display = 'none';
            var ok = document.getElementById('alerta-sucesso');
            ok.textContent = 'Conta criada com sucesso! Redirecionando...';
            ok.style.display = 'block';
            setTimeout(function() {
                window.location.href = '${pageContext.request.contextPath}/login';
            }, 2000);
        } else {
            mostrarErro(res.data.erro || 'Erro ao criar conta.');
        }
    })
    .catch(function(err) {
        console.error('Fetch erro:', err);
        mostrarErro('Erro de conexão: ' + (err && err.message ? err.message : 'sem detalhes'));
    });
}

function mostrarErro(msg) {
    var el = document.getElementById('alerta-erro');
    el.textContent = msg;
    el.style.display = 'block';
}
</script>

</body>
</html>
