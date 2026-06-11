import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { cadastrar } from '../services/auth'
import Campo from '../components/Campo'
import Botao from '../components/Botao'
import Feedback from '../components/Feedback'

export default function CadastroPage() {
    const [role, setRole] = useState('PACIENTE')
    const [nome, setNome] = useState('')
    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')
    const [dataNascimento, setDataNascimento] = useState('')
    const [telefone, setTelefone] = useState('')
    const [isProfissional, setIsProfissional] = useState(false)
    const [registroProfissional, setRegistroProfissional] = useState('')
    const [erro, setErro] = useState('')
    const [sucesso, setSucesso] = useState('')
    const navigate = useNavigate()

    function alternarRole(novoRole) {
        setRole(novoRole)
        if (novoRole !== 'CUIDADOR') {
            setIsProfissional(false)
            setRegistroProfissional('')
        }
    }

    async function handleSubmit(e) {
        e.preventDefault()
        setErro('')
        setSucesso('')

        const erros = []
        if (!nome) erros.push('Informe seu nome.')
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) erros.push('Informe um e-mail válido.')
        if (senha.length < 6) erros.push('A senha deve ter pelo menos 6 caracteres.')
        if (!dataNascimento) erros.push('Informe sua data de nascimento.')
        if (role === 'CUIDADOR' && isProfissional && !registroProfissional) {
            erros.push('Registro profissional é obrigatório para profissionais de saúde.')
        }

        if (erros.length > 0) {
            setErro(erros.join(' '))
            return
        }

        const body = { role, nome, email, senha, data_nascimento: dataNascimento }
        if (telefone) body.telefone = telefone
        if (role === 'CUIDADOR') {
            body.profissional_saude = isProfissional.toString()
            if (registroProfissional) body.registro_profissional = registroProfissional
        }

        try {
            await cadastrar(body)
            setSucesso('Conta criada com sucesso! Redirecionando...')
            setTimeout(() => navigate('/login'), 2000)
        } catch (err) {
            setErro(err.message)
        }
    }

    return (
        <div className="pagina-login" style={{ minHeight: '100vh' }}>
            <main className="card-login">
                <h1>MediControl</h1>
                <p className="subtitulo">Criar conta</p>

                <Feedback tipo="erro">{erro}</Feedback>
                <Feedback tipo="sucesso">{sucesso}</Feedback>

                <form onSubmit={handleSubmit} noValidate>
                    <Campo as="select" label="Tipo de conta" id="role"
                        value={role} onChange={e => alternarRole(e.target.value)}>
                        <option value="PACIENTE">Paciente</option>
                        <option value="CUIDADOR">Cuidador</option>
                    </Campo>

                    <Campo label="Nome completo" type="text" id="nome" placeholder="Seu nome"
                        autoComplete="name" value={nome} onChange={e => setNome(e.target.value)} />

                    <Campo label="E-mail" type="email" id="email" placeholder="seu@email.com"
                        autoComplete="email" value={email} onChange={e => setEmail(e.target.value)} />

                    <Campo label="Senha" type="password" id="senha" placeholder="Mínimo 6 caracteres"
                        autoComplete="new-password" value={senha} onChange={e => setSenha(e.target.value)} />

                    <Campo label="Data de nascimento" type="date" id="dataNascimento"
                        value={dataNascimento} onChange={e => setDataNascimento(e.target.value)} />

                    <Campo label="Telefone (opcional)" type="tel" id="telefone" placeholder="(11) 99999-9999"
                        value={telefone} onChange={e => setTelefone(e.target.value)} />

                    {role === 'CUIDADOR' && (
                        <div id="campos-cuidador">
                            <label className="lembrar" style={{ marginBottom: '.75rem' }}>
                                <input type="checkbox" checked={isProfissional}
                                    onChange={e => {
                                        setIsProfissional(e.target.checked)
                                        if (!e.target.checked) setRegistroProfissional('')
                                    }} />
                                Sou profissional de saúde (médico, enfermeiro, etc.)
                            </label>

                            {isProfissional && (
                                <Campo label={<>Registro profissional <span style={{ color: '#e53e3e' }}>*</span></>}
                                    type="text" id="registroProfissional"
                                    placeholder="Ex: CRM 12345 / COREN 98765"
                                    value={registroProfissional}
                                    onChange={e => setRegistroProfissional(e.target.value)} />
                            )}
                        </div>
                    )}

                    <Botao type="submit">Criar conta</Botao>
                </form>

                <p style={{ textAlign: 'center', marginTop: '1rem', fontSize: '.875rem', color: '#718096' }}>
                    Já tem conta? <Link to="/login" style={{ color: '#2b6cb0' }}>Entrar</Link>
                </p>
            </main>
        </div>
    )
}
