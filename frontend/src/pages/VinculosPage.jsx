import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import {
    buscarVinculosCuidador,
    buscarVinculosPaciente,
    buscarPacientePorEmail,
    criarVinculo,
    encerrarVinculo
} from '../services/vinculos'

function formatarData(str) {
    if (!str) return '—'
    const d = new Date(str)
    return d.toLocaleDateString('pt-BR')
}

export default function VinculosPage() {
    const [vinculos, setVinculos] = useState([])
    const [emailBusca, setEmailBusca] = useState('')
    const [pacienteEncontrado, setPacienteEncontrado] = useState(null)
    const [buscando, setBuscando] = useState(false)
    const [carregando, setCarregando] = useState(false)
    const [filtro, setFiltro] = useState('todos')
    const [feedback, setFeedback] = useState(null)
    const [mostrarForm, setMostrarForm] = useState(false)
    const navigate = useNavigate()

    const usuario = JSON.parse(localStorage.getItem('usuario'))
    const ehCuidador = usuario?.papel === 'CUIDADOR'
    const ehAdmin = usuario?.papel === 'ADMIN'

    useEffect(() => {
        if (!usuario) { navigate('/login'); return }
        carregar()
    }, [])

    function carregar() {
        setCarregando(true)
        const promise = ehCuidador || ehAdmin
            ? buscarVinculosCuidador(usuario.id_pessoa)
            : buscarVinculosPaciente(usuario.id_pessoa)

        promise
            .then(data => setVinculos(data))
            .catch(err => mostrarFeedback(err.message, 'erro'))
            .finally(() => setCarregando(false))
    }

    function mostrarFeedback(msg, tipo) {
        setFeedback({ msg, tipo })
        setTimeout(() => setFeedback(null), 5000)
    }

    async function handleBuscarPaciente(e) {
        e.preventDefault()
        if (!emailBusca.trim()) return
        setBuscando(true)
        setPacienteEncontrado(null)
        try {
            const paciente = await buscarPacientePorEmail(emailBusca.trim())
            setPacienteEncontrado(paciente)
        } catch (err) {
            mostrarFeedback(err.message, 'erro')
        } finally {
            setBuscando(false)
        }
    }

    async function handleCriarVinculo() {
        if (!pacienteEncontrado) return
        try {
            await criarVinculo(pacienteEncontrado.id_pessoa, usuario.id_pessoa)
            mostrarFeedback('Vínculo criado com sucesso!', 'sucesso')
            setPacienteEncontrado(null)
            setEmailBusca('')
            setMostrarForm(false)
            carregar()
        } catch (err) {
            mostrarFeedback(err.message, 'erro')
        }
    }

    async function handleEncerrar(idVinculo) {
        if (!confirm('Deseja encerrar este vínculo?')) return
        try {
            await encerrarVinculo(idVinculo)
            mostrarFeedback('Vínculo encerrado.', 'sucesso')
            carregar()
        } catch (err) {
            mostrarFeedback(err.message, 'erro')
        }
    }

    if (!usuario) return null

    const colunaPessoa = ehCuidador || ehAdmin ? 'Paciente' : 'Cuidador'
    const vinculosFiltrados = vinculos.filter(v => {
        if (filtro === 'ativo') return v.ativo
        if (filtro === 'encerrado') return !v.ativo
        return true
    })

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
                        <h2>Vínculos</h2>
                        {(ehCuidador || ehAdmin) && (
                            <button
                                className="btn-secundario"
                                onClick={() => {
                                    setMostrarForm(prev => !prev)
                                    setPacienteEncontrado(null)
                                    setEmailBusca('')
                                }}
                            >
                                {mostrarForm ? '− Cancelar' : '+ Vincular paciente'}
                            </button>
                        )}
                    </div>

                    {mostrarForm && (ehCuidador || ehAdmin) && (
                        <div>
                            <hr className="separador" />
                            <p style={{ marginBottom: '1rem', color: '#718096' }}>
                                Informe o e-mail do paciente que deseja vincular à sua conta de cuidador.
                            </p>
                            <form onSubmit={handleBuscarPaciente} noValidate>
                                <div className="campos-grade" style={{ alignItems: 'flex-end' }}>
                                    <div className="campo">
                                        <label htmlFor="email-paciente">E-mail do paciente</label>
                                        <input
                                            type="email"
                                            id="email-paciente"
                                            placeholder="paciente@exemplo.com"
                                            value={emailBusca}
                                            onChange={e => {
                                                setEmailBusca(e.target.value)
                                                setPacienteEncontrado(null)
                                            }}
                                        />
                                    </div>
                                    <div className="campo">
                                        <button
                                            type="submit"
                                            className="btn-primario"
                                            style={{ width: 'auto', padding: '.6rem 2rem' }}
                                            disabled={buscando}
                                        >
                                            {buscando ? 'Buscando…' : 'Buscar'}
                                        </button>
                                    </div>
                                </div>
                            </form>

                            {pacienteEncontrado && (
                                <div className="card" style={{ marginTop: '1rem', background: '#f0fff4', border: '1px solid #9ae6b4' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <div>
                                            <strong>{pacienteEncontrado.nome}</strong>
                                            <br />
                                            <small style={{ color: '#718096' }}>{pacienteEncontrado.email}</small>
                                        </div>
                                        <button className="btn-primario" onClick={handleCriarVinculo}>
                                            Confirmar vínculo
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    {vinculos.length > 0 && !carregando && (
                        <div style={{ display: 'flex', gap: '.5rem', margin: '1rem 0' }}>
                            {[
                                { valor: 'todos', label: 'Todos' },
                                { valor: 'ativo', label: 'Ativos' },
                                { valor: 'encerrado', label: 'Encerrados' }
                            ].map(({ valor, label }) => (
                                <button
                                    key={valor}
                                    onClick={() => setFiltro(valor)}
                                    className={filtro === valor ? 'btn-primario' : 'btn-secundario'}
                                    style={{ width: 'auto', padding: '.4rem 1.2rem', fontSize: '.85rem' }}
                                >
                                    {label}
                                </button>
                            ))}
                        </div>
                    )}

                    <div style={{ marginTop: '.5rem' }}>
                        {carregando ? (
                            <p className="vazio">Carregando vínculos…</p>
                        ) : vinculosFiltrados.length === 0 ? (
                            <p className="vazio">
                                {vinculos.length === 0
                                    ? (ehCuidador || ehAdmin
                                        ? 'Nenhum paciente vinculado. Use o botão acima para vincular.'
                                        : 'Nenhum cuidador vinculado à sua conta.')
                                    : `Nenhum vínculo com status "${filtro}".`}
                            </p>
                        ) : (
                            <div className="tabela-wrapper">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>{colunaPessoa}</th>
                                            <th>E-mail</th>
                                            <th>Vinculado desde</th>
                                            <th>Status</th>
                                            {(ehCuidador || ehAdmin) && <th>Ações</th>}
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {vinculosFiltrados.map(v => {
                                            const nome = ehCuidador || ehAdmin ? v.nome_paciente : v.nome_cuidador
                                            const email = ehCuidador || ehAdmin ? v.email_paciente : v.email_cuidador
                                            return (
                                                <tr key={v.id_vinculo}>
                                                    <td><strong>{nome || '—'}</strong></td>
                                                    <td>{email || '—'}</td>
                                                    <td>{formatarData(v.data_vinculo)}</td>
                                                    <td>
                                                        <span className={`tag-status ${v.ativo ? 'tag-ativo' : 'tag-inativo'}`}>
                                                            {v.ativo ? 'Ativo' : 'Encerrado'}
                                                        </span>
                                                    </td>
                                                    {(ehCuidador || ehAdmin) && (
                                                        <td className="acoes">
                                                            {v.ativo && (
                                                                <button
                                                                    className="btn-perigo"
                                                                    onClick={() => handleEncerrar(v.id_vinculo)}
                                                                >
                                                                    Encerrar
                                                                </button>
                                                            )}
                                                        </td>
                                                    )}
                                                </tr>
                                            )
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            </main>
        </div>
    )
}
