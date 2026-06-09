import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../services/auth'

export default function LoginPage() {
    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')
    const [lembrar, setLembrar] = useState(false)
    const [erro, setErro] = useState('')
    const navigate = useNavigate()

    useEffect(() => {
        const emailSalvo = localStorage.getItem('emailSalvo')
        if (emailSalvo) {
            setEmail(emailSalvo)
            setLembrar(true)
        }
    }, [])

    async function handleSubmit(e) {
        e.preventDefault()
        setErro('')

        const erros = []
        const reEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!reEmail.test(email)) erros.push('Informe um e-mail válido.')
        if (senha.length < 6) erros.push('A senha deve ter pelo menos 6 caracteres.')

        if (erros.length > 0) {
            setErro(erros.join(' '))
            return
        }

        try {
            await login(email, senha)
            if (lembrar) {
                localStorage.setItem('emailSalvo', email)
            } else {
                localStorage.removeItem('emailSalvo')
            }
            navigate('/dashboard')
        } catch (err) {
            setErro(err.message)
        }
    }

    return (
        <div className="pagina-login" style={{ minHeight: '100vh' }}>
            <main className="card-login">
                <h1>MediControl</h1>
                <p className="subtitulo">Controle de Medicamentos</p>

                {erro && <div className="alerta-erro">{erro}</div>}

                <form onSubmit={handleSubmit} noValidate>
                    <div className="campo">
                        <label htmlFor="email">E-mail</label>
                        <input
                            type="email"
                            id="email"
                            placeholder="seu@email.com"
                            autoComplete="email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="campo">
                        <label htmlFor="senha">Senha</label>
                        <input
                            type="password"
                            id="senha"
                            placeholder="••••••"
                            autoComplete="current-password"
                            value={senha}
                            onChange={e => setSenha(e.target.value)}
                            required
                        />
                    </div>

                    <label className="lembrar">
                        <input
                            type="checkbox"
                            checked={lembrar}
                            onChange={e => setLembrar(e.target.checked)}
                        />
                        Lembrar meu e-mail
                    </label>

                    <button type="submit" className="btn-primario">Entrar</button>
                </form>

                <p style={{ textAlign: 'center', marginTop: '1rem', fontSize: '.875rem', color: '#718096' }}>
                    Não tem conta? <Link to="/cadastro" style={{ color: '#2b6cb0' }}>Criar conta</Link>
                </p>
            </main>
        </div>
    )
}
