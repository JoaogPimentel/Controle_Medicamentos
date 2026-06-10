import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import { buscarPerfil, atualizarPerfil } from '../services/perfil'

const PAPEL_LABEL = {
    PACIENTE: 'Paciente',
    CUIDADOR: 'Cuidador',
    ADMIN: 'Administrador'
}

export default function PerfilPage() {
    const [perfil, setPerfil] = useState(null)
    const [nome, setNome] = useState('')
    const [editando, setEditando] = useState(false)
    const [salvando, setSalvando] = useState(false)
    const [feedback, setFeedback] = useState(null)
    const navigate = useNavigate()

    const usuario = JSON.parse(localStorage.getItem('usuario'))

    useEffect(() => {
        if (!usuario) { navigate('/login'); return }
        buscarPerfil(usuario.id_pessoa)
            .then(data => {
                setPerfil(data)
                setNome(data.nome)
            })
            .catch(() => {
                // fallback para os dados já armazenados localmente
                setPerfil(usuario)
                setNome(usuario.nome)
            })
    }, [])

    function mostrarFeedback(msg, tipo) {
        setFeedback({ msg, tipo })
        setTimeout(() => setFeedback(null), 5000)
    }

    async function handleSalvar(e) {
        e.preventDefault()
        if (!nome.trim()) return
        setSalvando(true)
        try {
            const atualizado = await atualizarPerfil(usuario.id_pessoa, { nome: nome.trim() })
            const usuarioAtualizado = { ...usuario, nome: atualizado.nome }
            localStorage.setItem('usuario', JSON.stringify(usuarioAtualizado))
            setPerfil(atualizado)
            setEditando(false)
            mostrarFeedback('Perfil atualizado com sucesso!', 'sucesso')
        } catch (err) {
            mostrarFeedback(err.message, 'erro')
        } finally {
            setSalvando(false)
        }
    }

    if (!usuario || !perfil) return null

    return (
        <div>
            <Cabecalho usuario={usuario} />
            <NavPrincipal papel={usuario.papel} />

            <main className="conteudo">
                {feedback && (
                    <div className={feedback.tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro'}>
                        {feedback.msg}
                    </div>
                )}

                <div className="card">
                    <div className="card-header">
                        <h2>Meu Perfil</h2>
                        {!editando && (
                            <button className="btn-secundario" onClick={() => setEditando(true)}>
                                Editar nome
                            </button>
                        )}
                    </div>

                    <div className="campos-grade">
                        <div className="campo">
                            <label>E-mail</label>
                            <p style={{ padding: '.5rem 0', color: '#4a5568' }}>{perfil.email}</p>
                        </div>
                        <div className="campo">
                            <label>Perfil de acesso</label>
                            <p style={{ padding: '.5rem 0', color: '#4a5568' }}>
                                {PAPEL_LABEL[perfil.papel] ?? perfil.papel}
                            </p>
                        </div>
                    </div>

                    <hr className="separador" />

                    {editando ? (
                        <form onSubmit={handleSalvar} noValidate>
                            <div className="campos-grade" style={{ alignItems: 'flex-end' }}>
                                <div className="campo">
                                    <label htmlFor="nome">Nome completo</label>
                                    <input
                                        type="text"
                                        id="nome"
                                        value={nome}
                                        onChange={e => setNome(e.target.value)}
                                        required
                                    />
                                </div>
                            </div>
                            <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                                <button
                                    type="submit"
                                    className="btn-primario"
                                    style={{ width: 'auto' }}
                                    disabled={salvando}
                                >
                                    {salvando ? 'Salvando…' : 'Salvar'}
                                </button>
                                <button
                                    type="button"
                                    className="btn-secundario"
                                    style={{ width: 'auto' }}
                                    onClick={() => { setEditando(false); setNome(perfil.nome) }}
                                >
                                    Cancelar
                                </button>
                            </div>
                        </form>
                    ) : (
                        <div className="campo">
                            <label>Nome</label>
                            <p style={{ padding: '.5rem 0', fontSize: '1.1rem', fontWeight: '600', color: '#2d3748' }}>
                                {perfil.nome}
                            </p>
                        </div>
                    )}
                </div>
            </main>
        </div>
    )
}
